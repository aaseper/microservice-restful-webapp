package eolopark.planner.client;

import eolopark.planner.controller.exception.ResourceNotFoundException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import eolopark.grpc.WindServiceGrpc.WindServiceBlockingStub;
import eolopark.grpc.WindServiceRequest;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Controller
public class WindServiceGrpcClient {

    @GrpcClient("windServer")
    private WindServiceBlockingStub client;

    @Async
    public CompletableFuture<Double> requestWind(String cityName) throws ResourceNotFoundException {

        WindServiceRequest request = WindServiceRequest.newBuilder().setCityName(cityName).build();
        double windSpeed = client.requestWind(request).getSpeed();
        if (Objects.equals(windSpeed, -1.0)) throw new ResourceNotFoundException("City not found.");
        return CompletableFuture.completedFuture(windSpeed);
    }
}