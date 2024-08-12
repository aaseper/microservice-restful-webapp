package eolopark.server.controller;

import eolopark.server.model.dto.AerogeneratorRequest;
import eolopark.server.model.dto.AerogeneratorResponse;
import eolopark.server.model.internal.Aerogenerator;
import eolopark.server.model.internal.Eolopark;
import eolopark.server.service.AerogeneratorService;
import eolopark.server.service.ParkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Controller
@RequestMapping ("/api/parks")
public class AerogeneratorRestController {

    /* Attributes */
    private final AerogeneratorService aerogeneratorService;
    private final ParkService parkService;

    /* Constructor */
    public AerogeneratorRestController (AerogeneratorService aerogeneratorService, ParkService parkService) {
        this.aerogeneratorService = aerogeneratorService;
        this.parkService = parkService;
    }

    /* Methods */
    @Operation (summary = "Create a new aerogenerator manually")
    @ApiResponses (value = {@ApiResponse (responseCode = "201", description = "New aerogenerator manually created",
            content = @Content), @ApiResponse (responseCode = "401", description = "Full authentication is required " +
            "to access this resource", content = @Content), @ApiResponse (responseCode = "403", description = "You " +
            "are not the owner of that park, so you can't create a new aerogenerator in it", content = @Content),
            @ApiResponse (responseCode = "400", description = "You have tried to create a park with an already " +
                    "existing id", content = @Content), @ApiResponse (responseCode = "404", description = "The " +
            "requested resource could not be found", content = @Content)

    })
    @PostMapping ("/{eoloparkId}/aerogenerators")
    public ResponseEntity<AerogeneratorResponse> newAerogenerator (@Parameter (description = "ID of the Eolopark " +
            "where the aerogenerator will be added") @PathVariable Long eoloparkId, @Parameter (description =
            "Request body containing details for creating a new aerogenerator") @RequestBody @Validated AerogeneratorRequest aerogeneratorRequest, @Parameter (description = "HTTP Servlet request containing user information, including role details") HttpServletRequest request) {
        Eolopark eolopark = parkService.getParkById(eoloparkId);
        if (eolopark.getUser().getName().equals(request.getUserPrincipal().getName()) || request.isUserInRole("admin")) {
            aerogeneratorService.saveAerogenerator(new Aerogenerator(aerogeneratorRequest.aerogeneratorId(),
                    aerogeneratorRequest.aerogeneratorLatitude(), aerogeneratorRequest.aerogeneratorLongitude(),
                    aerogeneratorRequest.bladeLength(), aerogeneratorRequest.height(),
                    aerogeneratorRequest.aerogeneratorPower()), eolopark);
            URI location =
                    URI.create("/api/parks/" + eoloparkId + "/aerogenerators/" + aerogeneratorService.getAerogeneratorByName(aerogeneratorRequest.aerogeneratorId()).getId());
            return ResponseEntity.created(location).build();
        }
        else
            throw new InsufficientAuthenticationException("User not authorized to create aerogenerator in park " + eoloparkId);
    }

    @Operation (summary = "Update aerogenerator by ID")
    @ApiResponses (value = {@ApiResponse (responseCode = "201", description = "Aerogenerator modified", content =
    @Content), @ApiResponse (responseCode = "401", description = "Full authentication is required to access this " +
            "resource", content = @Content), @ApiResponse (responseCode = "403", description = "You are not the owner" +
            " of that park, so you can't update its aerogenerators", content = @Content), @ApiResponse (responseCode
            = "404", description = "The requested resource could not be found", content = @Content)})
    @PutMapping ("/{eoloparkId}/aerogenerators/{aerogeneratorId}")
    public ResponseEntity<Object> putAerogenerator (@Parameter (description = "ID of the Eolopark containing the " +
            "aerogenerator to update") @PathVariable Long eoloparkId, @Parameter (description = "ID of the " +
            "aerogenerator to update") @PathVariable Long aerogeneratorId, @Parameter (description = "Request body " +
            "containing details for updating the aerogenerator") @RequestBody @Validated AerogeneratorRequest aerogeneratorRequest, @Parameter (description = "HTTP Servlet request containing user information, including role details") HttpServletRequest request) {
        Eolopark eolopark = parkService.getParkById(eoloparkId);
        if (eolopark.getUser().getName().equals(request.getUserPrincipal().getName()) || request.isUserInRole("admin")) {
            aerogeneratorService.replaceAerogeneratorById(new Aerogenerator(aerogeneratorRequest.aerogeneratorId(),
                    aerogeneratorRequest.aerogeneratorLatitude(), aerogeneratorRequest.aerogeneratorLongitude(),
                    aerogeneratorRequest.bladeLength(), aerogeneratorRequest.height(),
                    aerogeneratorRequest.aerogeneratorPower()), aerogeneratorId, eoloparkId);
            URI location =
                    URI.create("/api/parks/" + eoloparkId + "/aerogenerators/" + aerogeneratorService.getAerogeneratorById(aerogeneratorId).getId());
            return ResponseEntity.created(location).build();
        }
        else
            throw new InsufficientAuthenticationException("User not authorized to put aerogenerator " + aerogeneratorId + " in park " + eoloparkId);
    }

    @Operation (summary = "Delete aerogenerator by ID")
    @ApiResponses (value = {@ApiResponse (responseCode = "200", description = "Park deleted", content =
            {@Content (mediaType = "application/json", schema =
            @Schema (implementation = AerogeneratorResponse.class))}), @ApiResponse (responseCode = "401",
            description = "Full authentication is required to access this resource", content = @Content),
            @ApiResponse (responseCode = "403", description = "You are not the owner of that aerogenerator, so you " +
                    "can't delete it", content = @Content), @ApiResponse (responseCode = "404", description = "The " +
            "requested resource could not be found", content = @Content)})
    @DeleteMapping ("/{eoloparkId}/aerogenerators/{aerogeneratorId}")
    public ResponseEntity<AerogeneratorResponse> deleteAerogenerator (@Parameter (description = "Eolopark ID") @PathVariable Long eoloparkId, @Parameter (description = "Aerogenerator ID") @PathVariable Long aerogeneratorId, @Parameter (description = "HTTP Servlet request containing user information, including role details") HttpServletRequest request) {
        Eolopark eolopark = parkService.getParkById(eoloparkId);
        if (eolopark.getUser().getName().equals(request.getUserPrincipal().getName()) || request.isUserInRole("admin")) {
            Aerogenerator aerogenerator = aerogeneratorService.getAerogeneratorById(aerogeneratorId);
            aerogeneratorService.deleteAerogeneratorById(aerogeneratorId);
            return ResponseEntity.ok(new AerogeneratorResponse(aerogenerator.getId(),
                    aerogenerator.getAerogeneratorId(), aerogenerator.getAerogeneratorLatitude(),
                    aerogenerator.getAerogeneratorLongitude(), aerogenerator.getBladeLength(),
                    aerogenerator.getHeight(), aerogenerator.getAerogeneratorPower()));
        }
        else
            throw new InsufficientAuthenticationException("User not authorized to delete aerogenerator " + aerogeneratorId + " in park " + eoloparkId);
    }
}
