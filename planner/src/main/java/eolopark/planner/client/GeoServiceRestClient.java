package eolopark.planner.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eolopark.planner.controller.exception.ResourceNotFoundException;
import eolopark.planner.model.GeoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Component
public class GeoServiceRestClient {

    /* Attributes */
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String protocol = "http://";
    private final String endpoint = "/api/geoservice/?cityName=";
    @Value("${rest.client.geoService.address}")
    private String host;
    @Value("${controller.verbosity}")
    private int verbosity;

    /* Constructors */
    public GeoServiceRestClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /* Methods */
    @Async
    public CompletableFuture<GeoResponse> requestGeoData(String cityName) throws JsonProcessingException, ResourceNotFoundException {
        String url = protocol + host + endpoint + cityName;
        String jsonResponse = restTemplate.getForObject(url, String.class);
        GeoResponse geoResponse = objectMapper.readValue(jsonResponse, GeoResponse.class);
        return CompletableFuture.completedFuture(geoResponse);
    }
}
