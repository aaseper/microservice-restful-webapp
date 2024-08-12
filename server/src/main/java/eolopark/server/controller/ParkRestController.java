package eolopark.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eolopark.server.controller.exception.ResourceNotFoundException;
import eolopark.server.model.dto.*;
import eolopark.server.model.internal.*;
import eolopark.server.rabbit.ServerRabbitMQSender;
import eolopark.server.service.DataInitService;
import eolopark.server.service.ParkService;
import eolopark.server.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RestController
@RequestMapping ("/api/parks")
public class ParkRestController {

    /* Attributes */
    private final ParkService parkService;
    private final ReportService reportService;
    private final ServerRabbitMQSender serverRabbitMQSender;
    private final DataInitService dataInitService;
    private long processedParkId;
    @Value ("${controller.verbosity}")
    private int verbosity;

    /* Constructor */
    ParkRestController (ParkService parkService, ReportService reportService,
                        ServerRabbitMQSender serverRabbitMQSender, DataInitService dataInitService
    ) {
        this.parkService = parkService;
        this.reportService = reportService;
        this.serverRabbitMQSender = serverRabbitMQSender;
        this.dataInitService = dataInitService;
    }

    /* Methods */
    private long getProcessedParkId () {
        long cache = parkService.getMaxEoloparkId();
        if (!Objects.equals(cache, processedParkId)) {
            processedParkId = cache;
            return processedParkId;
        }
        else return ++processedParkId;
    }

    @Operation (summary = "Get all parks")
    @ApiResponses (value = {
            @ApiResponse (
                    responseCode = "200",
                    description = "Return the list of paginated parks",
                    content = {@Content (
                            mediaType = "application/json",
                            schema = @Schema (implementation = EoloparkIdAndNameAndCityResponse.class)
                    )}
            ),
            @ApiResponse (
                    responseCode = "401",
                    description = "Full authentication is required to access this resource",
                    content = @Content
            )
    })
    @GetMapping ("/")
    public ResponseEntity<List<EoloparkIdAndNameAndCityResponse>> getAllParks (@Parameter (description="Optional keyword for filtering parks") @RequestParam (required = false) Optional<String> keyword,
                                                                               @Parameter (description="Optional search type") @RequestParam (required = false) Optional<String> searchType,
                                                                               @Parameter (description="Page information for pagination") Pageable page,
                                                                               @Parameter (description="HTTP Servlet request containing user information, including role details") HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String keywordString = keyword.orElseGet(() -> "");
        String searchTypeString = searchType.orElseGet(() -> "ALL");
        if (searchTypeString.equals("ALL"))
            return ResponseEntity.ok(parkService.getParkByKeyword(keywordString, page).stream().map(
                    (Eolopark e) -> new EoloparkIdAndNameAndCityResponse(e.getId(), e.getName(), e.getCity())).toList());
        else
            return ResponseEntity.ok(parkService.getParkByUserIdAndKeyword(keywordString, principal.getName(), page).stream().map(
                    (Eolopark e) -> new EoloparkIdAndNameAndCityResponse(e.getId(), e.getName(), e.getCity())).toList());
    }

    @Operation (summary = "Get park by ID")
    @ApiResponses (value = {
            @ApiResponse (
                    responseCode = "200",
                    description = "Return the park with the ID given",
                    content = {@Content (
                            mediaType = "application/json",
                            schema = @Schema (implementation = EoloparkResponse.class)
                    )}
            ),
            @ApiResponse (
                    responseCode = "401",
                    description = "Full authentication is required to access this resource",
                    content = @Content
            ),
            @ApiResponse (
                    responseCode = "403",
                    description = "You are not the owner of this park",
                    content = @Content
            ),
            @ApiResponse (
                    responseCode = "404",
                    description = "The requested resource could not be found",
                    content = @Content
            )
    })
    @GetMapping ("/{id}")
    public ResponseEntity<EoloparkResponse> getPark (@Parameter (description="ID of the park to retrieve")@PathVariable Long id,
                                                     @Parameter (description="HTTP Servlet request containing user information, including role details")HttpServletRequest request) {
        Eolopark e = parkService.getParkById(id);
        if (e.getUser().getName().equals(request.getUserPrincipal().getName()) || request.isUserInRole("admin"))
            return ResponseEntity.ok(new EoloparkResponse(e.getId(), e.getName(), e.getCity(), e.getLatitude(),
                    e.getLongitude(), e.getArea(), e.getTerrainType(),
                    new SubstationResponse(e.getSubstation().getModel(), e.getSubstation().getPower(),
                            e.getSubstation().getVoltage()),
                    e.getAerogenerators().stream().map((Aerogenerator a) -> new AerogeneratorResponse(a.getId(),
                            a.getAerogeneratorId(), a.getAerogeneratorLatitude(), a.getAerogeneratorLongitude(),
                            a.getBladeLength(), a.getHeight(), a.getAerogeneratorPower())).collect(Collectors.toList())));
        else throw new InsufficientAuthenticationException("You are not the owner of this park");
    }

    @Operation (summary = "Create new park manually")
    @ApiResponses (value = {
            @ApiResponse (
                    responseCode = "201",
                    description = "New park manually created",
                    content = @Content
            ),
            @ApiResponse (
                    responseCode = "401",
                    description = "Full authentication is required to access this resource",
                    content = @Content
            ),
            @ApiResponse (
                    responseCode = "429",
                    description = "You have reached the maximum allowed number of resources (5). Please try again later",
                    content = @Content
            ),
            @ApiResponse (
                    responseCode = "500",
                    description = "You have tried to create a park with an already existing name",
                    content = @Content
            )
    })
    @PostMapping ("/")
    public ResponseEntity<Object> newPark (@Parameter (description="Request body containing details for creating a new park")@RequestBody @Validated EoloparkRequest eoloparkRequest,
                                           @Parameter (description="HTTP Servlet request containing user information, including role details")HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        List<Aerogenerator> aerogenerators = new ArrayList<>();
        if (!eoloparkRequest.aerogenerator().aerogeneratorId().equals("0"))
            aerogenerators.add(new Aerogenerator(eoloparkRequest.aerogenerator().aerogeneratorId(),
                    eoloparkRequest.aerogenerator().aerogeneratorLatitude(),
                    eoloparkRequest.aerogenerator().aerogeneratorLongitude(),
                    eoloparkRequest.aerogenerator().bladeLength(), eoloparkRequest.aerogenerator().height(),
                    eoloparkRequest.aerogenerator().aerogeneratorPower()));
        parkService.savePark(new Eolopark(eoloparkRequest.name(), eoloparkRequest.city(), eoloparkRequest.latitude(),
                eoloparkRequest.longitude(), eoloparkRequest.area(), eoloparkRequest.terrainType(),
                new Substation(eoloparkRequest.substation().model(), eoloparkRequest.substation().power(),
                        eoloparkRequest.substation().voltage())), aerogenerators, principal.getName());
        URI location = URI.create("/api/parks/" + parkService.getParkByName(eoloparkRequest.name()).getId());
        return ResponseEntity.created(location).build();
    }

    @Operation (summary = "Create new automated park with city and area")
    @ApiResponses (value = {
            @ApiResponse (
                    responseCode = "201",
                    description = "New automated park created",
                    content = @Content (
                            mediaType = "application/json")
            ),
            @ApiResponse (
                    responseCode = "401",
                    description = "Full authentication is required to access this resource",
                    content = @Content
            ),
            @ApiResponse (
                    responseCode = "429",
                    description = "You have reached the maximum allowed number of resources (5). Please try again later",
                    content = @Content
            )
    })
    @PostMapping ("/automated")
    public ResponseEntity<Object> newAutomatedPark (@Parameter (description="Request body containing details for creating a new automated park")@RequestBody @Validated AutomatedRequest automatedRequest,
                                                    @Parameter (description="HTTP Servlet request containing user information, including role details")HttpServletRequest request) throws InterruptedException, ExecutionException, TimeoutException, JsonProcessingException {
        parkService.reviewMaxParks(request.getUserPrincipal().getName());
        if (dataInitService.getCityByCapital(automatedRequest.city()) == null)
            throw new ResourceNotFoundException("City not found");
        Long id = reportService.saveReport(Objects.requireNonNull(Objects.requireNonNull(request.getUserPrincipal().getName())));
        serverRabbitMQSender.sendCreationProgressMessage(new EoloparkRequestMessage(id, automatedRequest.city(), automatedRequest.area()));
        Long cache = getProcessedParkId();
        URI location = URI.create("/api/parks/" + ++cache);
        return ResponseEntity.created(location).header("content-type", "application/json").body("{ \"reportId\": " + id + " }");
    }

    @Operation (summary = "Create new random park")
    @ApiResponses (value = {
            @ApiResponse (
                    responseCode = "201",
                    description = "New random park created",
                    content = @Content (
                            mediaType = "application/json")
            ),
            @ApiResponse (
                    responseCode = "401",
                    description = "Full authentication is required to access this resource",
                    content = @Content
            ),
            @ApiResponse (
                    responseCode = "429",
                    description = "You have reached the maximum allowed number of resources (5). Please try again later",
                    content = @Content
            )
    })
    @PostMapping ("/automated/random")
    public ResponseEntity<Object> newRandomPark (@Parameter (description="HTTP Servlet request containing user information, including role details") HttpServletRequest request)
            throws InterruptedException, TimeoutException, ExecutionException {
        parkService.reviewMaxParks(request.getUserPrincipal().getName());
        Long id = reportService.saveReport(Objects.requireNonNull(Objects.requireNonNull(request.getUserPrincipal().getName())));
        serverRabbitMQSender.sendCreationProgressMessage(new EoloparkRequestMessage(id,
                dataInitService.getCityName((int) (dataInitService.nextRandom() * dataInitService.getCitySize())),
                (int) (dataInitService.nextRandom() * 10) + 1));
        Long cache = getProcessedParkId();
        URI location = URI.create("/api/parks/" + ++cache);
        return ResponseEntity.created(location).header("content-type", "application/json").body("{ \"reportId\": " + id + " }");
    }

    @Operation (summary = "Update park by ID")
    @ApiResponses (value = {
            @ApiResponse (
                    responseCode = "200",
                    description = "Park modified",
                    content = @Content
            ),
            @ApiResponse (
                    responseCode = "401",
                    description = "Full authentication is required to access this resource",
                    content = @Content
            ),
            @ApiResponse (
                    responseCode = "403",
                    description = "You are not the owner of this park",
                    content = @Content
            ),
            @ApiResponse (
                    responseCode = "404",
                    description = "The requested resource could not be found",
                    content = @Content
            )
    })
    @PutMapping ("/{id}")
    public ResponseEntity<Object> putPark (@Parameter (description="ID of the park to update")@PathVariable Long id,
                                           @Parameter (description="Request body containing details for updating the park")@RequestBody @Validated EoloparkRequest eoloparkRequest,
                                           @Parameter (description="Flag indicating whether to keep existing data")@RequestParam (required = true) boolean keep,
                                           @Parameter (description="HTTP Servlet request containing user information, including role details")HttpServletRequest request) {
        Eolopark eolopark = parkService.getParkById(id);
        if (eolopark.getUser().getName().equals(request.getUserPrincipal().getName()) || request.isUserInRole("admin")) {
            Eolopark e = new Eolopark(eoloparkRequest.name(), eoloparkRequest.city(), eoloparkRequest.latitude(),
                    eoloparkRequest.longitude(), eoloparkRequest.area(), eoloparkRequest.terrainType(),
                    new Substation(eoloparkRequest.substation().model(), eoloparkRequest.substation().power(),
                            eoloparkRequest.substation().voltage()));
            parkService.replaceParkById(id, e, e.getSubstation(), keep);
            URI location = URI.create("/api/parks/" + parkService.getParkById(id).getId());
            return ResponseEntity.ok().header("Location", location.toString()).build();
        }
        else throw new InsufficientAuthenticationException("You are not the owner of this park");
    }

    @Operation (summary = "Delete park by ID")
    @ApiResponses (value = {
            @ApiResponse (
                    responseCode = "200",
                    description = "Park deleted",
                    content = {@Content (
                            mediaType = "application/json",
                            schema = @Schema (implementation = EoloparkIdAndNameAndCityResponse.class)
                    )}
            ),
            @ApiResponse (
                    responseCode = "401",
                    description = "Full authentication is required to access this resource",
                    content = @Content
            ),
            @ApiResponse (
                    responseCode = "403",
                    description = "You are not the owner of this park",
                    content = @Content
            ),
            @ApiResponse (
                    responseCode = "404",
                    description = "The requested resource could not be found",
                    content = @Content
            )
    })
    @DeleteMapping ("/{id}")
    public ResponseEntity<EoloparkIdAndNameAndCityResponse> deletePark (@Parameter (description="ID of the park to delete")@PathVariable Long id,
                                                                        @Parameter (description="HTTP Servlet request containing user information, including role details")HttpServletRequest request) {
        Eolopark eolopark = parkService.getParkById(id);
        if (eolopark.getUser().getName().equals(request.getUserPrincipal().getName()) || request.isUserInRole("admin")) {
            parkService.deleteParkById(id);
            return ResponseEntity.ok(new EoloparkIdAndNameAndCityResponse(eolopark.getId(), eolopark.getName(),
                    eolopark.getCity()));
        }
        else throw new InsufficientAuthenticationException("You are not the owner of this park");
    }

    @Operation (summary = "Get report by ID")
    @ApiResponses (value = {
            @ApiResponse (
                    responseCode = "200",
                    description = "Return the report with the ID given",
                    content = {@Content (
                            mediaType = "application/json",
                            schema = @Schema (implementation = ReportResponse.class)
                    )}
            ),
            @ApiResponse (
                    responseCode = "401",
                    description = "Full authentication is required to access this resource",
                    content = @Content
            ),
            @ApiResponse (
                    responseCode = "404",
                    description = "The requested resource could not be found",
                    content = @Content
            )
    })
    @GetMapping ("/report/{id}")
    public ResponseEntity<ReportResponse> getReport (@Parameter (description="ID of the report to retrieve") @PathVariable Long id) {
        Report report = reportService.getReportById(id);
        return ResponseEntity.ok(new ReportResponse(report.getUsername(), report.getProgress(), report.isCompleted()));
    }
}