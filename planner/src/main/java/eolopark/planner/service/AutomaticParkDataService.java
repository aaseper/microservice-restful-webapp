package eolopark.planner.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import eolopark.planner.client.GeoServiceRestClient;
import eolopark.planner.client.WindServiceGrpcClient;
import eolopark.planner.controller.LogController;
import eolopark.planner.model.*;
import eolopark.planner.rabbit.PlannerRabbitMQSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;

@Service
public class AutomaticParkDataService {
    private static final double KM_TO_DEGREES = 0.5 / 111;
    private static final double POWER_MARGIN = 1.20;
    private static final int SLEEP_TIME = 1000;
    /* Attributes */
    private final LogController logController;
    private final WindServiceGrpcClient windServiceGrpcClient;
    private final RandomParkNameService randomParkNameService;
    private final PlannerRabbitMQSender rabbitMQSender;
    private final GeoServiceRestClient geoServiceRestClient;
    // Aerogenerator values for blade length, height and power
    // depending on the mean wind of the city
    private final Map<String, int[]> aerogeneratorValues =
            Map.of("LOW", new int[]{10, 25, 1500},
                    "MEDIUM", new int[]{20, 50, 2500},
                    "HIGH", new int[]{40, 100, 3500});
    @Value("${controller.verbosity}")
    private int verbosity;

    /* Constructor */
    public AutomaticParkDataService(LogController logController, WindServiceGrpcClient windServiceGrpcClient, RandomParkNameService randomParkNameService,
                                    PlannerRabbitMQSender rabbitMQSender, GeoServiceRestClient geoServiceRestClient) {
        this.logController = logController;
        this.windServiceGrpcClient = windServiceGrpcClient;
        this.randomParkNameService = randomParkNameService;
        this.rabbitMQSender = rabbitMQSender;
        this.geoServiceRestClient = geoServiceRestClient;
    }

    /* Methods */
    /*
     * Returns the substation model based on the total power of the park
     */
    private String getSubstationModel(double power) {
        if (power <= 10000) {
            return "A-10000";
        } else if (power <= 50000) {
            int multiple = (int) (power / 10000) + 1;
            return "B-" + (multiple * 10000);
        } else {
            int multiple = (int) (power / 100000) + 1;
            return "C-" + (multiple * 100000);
        }
    }

    /*
     * Returns the mean wind of the city based on the wind speed
     */
    private String getMeanWind(double windSpeed) {
        if (windSpeed > 7.63) return "HIGH";
        else if (windSpeed > 7.06) return "MEDIUM";
        else return "LOW";
    }

    private String getTerrainType(double altitude) {
        if (altitude > 700) return "MOUNTAIN";
        else if (altitude > 450) return "HILL";
        else if (altitude > 200) return "JUNGLE";
        else if (altitude > 100) return "DESERT";
        else return "PLAIN";
    }

    /*
     * Creates an eolopark response with the given id, city and area
     */
    @Async
    public void automateEoloparkResponse(Long id, String city, int area) throws IllegalArgumentException, JsonProcessingException, ExecutionException, InterruptedException {

        /* Request altitude, latitude and longitude of the city from the GeoService */
        CompletableFuture<GeoResponse> geoResponse = geoServiceRestClient.requestGeoData(city);

        /* Request wind speed of the city from the WindService */
        CompletableFuture<Double> windSpeed = windServiceGrpcClient.requestWind(city);

        /* Wait for the responses of the first service, and notify the progress at 25% */
        CompletableFuture.anyOf(geoResponse, windSpeed);
        try {
            sleep(SLEEP_TIME); // Simulate the delay of the second service response
            rabbitMQSender.sendCreationProgressMessage(new CreationProgressMessage(id, 25, false, null));
        } catch (InterruptedException e) {
            logController.error("Error while simulating the delay of the first service response");
            throw new RuntimeException(e);
        }

        /* Wait for the responses of the second service, and notify the progress at 50% */
        CompletableFuture.allOf(geoResponse, windSpeed);
        try {
            sleep(SLEEP_TIME); // Simulate the delay of the second service response
            rabbitMQSender.sendCreationProgressMessage(new CreationProgressMessage(id, 50, false, null));
        } catch (InterruptedException e) {
            logController.error("Error while simulating the delay of the second service response");
            throw new RuntimeException(e);
        }

        // Get the mean wind of the city
        String meanWind = getMeanWind(windSpeed.get());

        // Get the altitude, latitude and longitude of the city
        double altitude = geoResponse.get().altitude();
        double latitude = geoResponse.get().latETRS89();
        double longitude = geoResponse.get().lonETRS89();

        if (verbosity > 0)
            logController.info("City " + city + " has altitude " + altitude + " m, latitude " + latitude + " m, and longitude " + longitude + " m" + "\nCity " + city + " has a mean speed of " + meanWind + " m/s");

        // Set eolopark name and terrain type
        String name = randomParkNameService.getRandomParkName(); // Get a random park name
        String terrainType = getTerrainType(altitude); // Get the terrain type of the city

        /* Aerogenerator responses creation */
        // Get the aerogenerator values for blade length, height and power based on the mean wind
        int[] values = aerogeneratorValues.get(meanWind);
        List<AerogeneratorResponse> aerogeneratorResponses = new ArrayList<>();
        for (int i = 0; i < area; i++) {
            aerogeneratorResponses.add(
                    new AerogeneratorResponse(id,
                            name.replaceAll("\\s+", "").concat(String.valueOf(i)),
                            latitude - KM_TO_DEGREES,
                            longitude + KM_TO_DEGREES + (i * 2 * KM_TO_DEGREES),
                            values[0], values[1], values[2]));
        }

        try {
            sleep(SLEEP_TIME); // Simulate the delay of determining the aerogenerator responses
        } catch (InterruptedException e) {
            logController.error("Error while simulating the delay of the aerogenerator creation");
            throw new RuntimeException(e);
        }

        if (verbosity > 0) logController.info("Aerogenerator data for " + city + " created");

        // Send creation progress message to the queue to notify the progress at 75%
        rabbitMQSender.sendCreationProgressMessage(new CreationProgressMessage(id, 75, false, null));

        /* Substation and eolopark response creation */
        // Create the substation response
        double totalPower = values[2] * area * POWER_MARGIN;
        SubstationResponse substationResponse =
                new SubstationResponse(getSubstationModel(totalPower), totalPower, 220);
        // Create the eolopark response
        EoloparkResponse eoloparkResponse = new EoloparkResponse(id, name, city, latitude,
                longitude, area, terrainType, substationResponse, aerogeneratorResponses);

        try {
            sleep(SLEEP_TIME); // Simulate the delay of determining the eolopark response
        } catch (InterruptedException e) {
            logController.error("Error while simulating the delay of the eolopark creation");
            throw new RuntimeException(e);
        }

        if (verbosity > 0) logController.info("Eolopark data for " + city + " created");

        // Send creation end message to the queue to notify the end of the eolopark data creation
        rabbitMQSender.sendCreationProgressMessage(new CreationProgressMessage(id, 100, true, eoloparkResponse));
    }
}