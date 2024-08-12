package eolopark.geoservice.controller;

import eolopark.geoservice.model.GeoData;
import eolopark.geoservice.model.GeoServiceResponse;
import eolopark.geoservice.service.GeoDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geoservice")
public class GeoServiceRestController {

    private final GeoDataService geoDataService;

    public GeoServiceRestController(GeoDataService geoDataService) {
        this.geoDataService = geoDataService;
    }

    @Operation(summary = "Get city's latitude, longitude and altitude data")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Return the city data",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GeoServiceResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "City not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "City name not provided (is required)",
                    content = @Content
            )
    })
    @GetMapping("/")
    public ResponseEntity<GeoServiceResponse> getCityAttributes(@Parameter(description = "City whose data is wanted to be shown") @RequestParam(required = true) String cityName) {

        GeoData geoData = geoDataService.getGeoDataByCityName(cityName);
        return ResponseEntity.ok(new GeoServiceResponse(geoData.latETRS89(), geoData.lonETRS89(), geoData.altitude()));
    }
}
