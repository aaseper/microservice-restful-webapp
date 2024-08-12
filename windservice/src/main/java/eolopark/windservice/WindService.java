package eolopark.windservice;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import eolopark.grpc.WindServiceGrpc;
import eolopark.grpc.WindServiceRequest;
import eolopark.grpc.WindServiceResponse;

import java.util.Map;

@GrpcService
public class WindService extends WindServiceGrpc.WindServiceImplBase {

    private final Map<String, Double> meanWindMap = Map.<String, Double>ofEntries(
            Map.entry("A Coruña", 7.2),
            Map.entry("Albacete", 6.9),
            Map.entry("Alicante", 7.5),
            Map.entry("Almería", 8.0),
            Map.entry("Ávila", 6.7),
            Map.entry("Badajoz", 7.0),
            Map.entry("Barcelona", 7.4),
            Map.entry("Bilbao", 7.7),
            Map.entry("Burgos", 7.6),
            Map.entry("Cáceres", 7.2),
            Map.entry("Cádiz", 8.1),
            Map.entry("Castellón de la Plana", 7.1),
            Map.entry("Ceuta", 7.4),
            Map.entry("Ciudad Real", 6.6),
            Map.entry("Córdoba", 7.1),
            Map.entry("Cuenca", 6.8),
            Map.entry("Girona", 7.5),
            Map.entry("Granada", 7.5),
            Map.entry("Guadalajara", 6.9),
            Map.entry("Huelva", 7.9),
            Map.entry("Huesca", 8.1),
            Map.entry("Jaén", 7.3),
            Map.entry("Las Palmas de Gran Canaria", 8.2),
            Map.entry("León", 7.4),
            Map.entry("Lleida", 7.5),
            Map.entry("Logroño", 7.2),
            Map.entry("Lugo", 7.3),
            Map.entry("Madrid", 7.3),
            Map.entry("Málaga", 7.8),
            Map.entry("Melilla", 7.3),
            Map.entry("Murcia", 7.6),
            Map.entry("Ourense", 6.5),
            Map.entry("Oviedo", 7.5),
            Map.entry("Palencia", 6.8),
            Map.entry("Palma", 7.9),
            Map.entry("Pamplona", 7.4),
            Map.entry("Pontevedra", 6.6),
            Map.entry("Salamanca", 7.2),
            Map.entry("San Sebastián", 7.6),
            Map.entry("Santa Cruz de Tenerife", 8.2),
            Map.entry("Santander", 7.4),
            Map.entry("Segovia", 6.9),
            Map.entry("Sevilla", 7.4),
            Map.entry("Soria", 6.7),
            Map.entry("Tarragona", 7.2),
            Map.entry("Teruel", 7.0),
            Map.entry("Toledo", 7.1),
            Map.entry("Valencia", 7.3),
            Map.entry("Valladolid", 7.0),
            Map.entry("Vitoria", 7.2),
            Map.entry("Zamora", 6.8),
            Map.entry("Zaragoza", 8.1));

    @Override
    public void requestWind(WindServiceRequest request, StreamObserver<WindServiceResponse> responseObserver) throws
            ClassCastException, NullPointerException {
        double meanWind = meanWindMap.getOrDefault(request.getCityName(), -1.0);
        WindServiceResponse response = WindServiceResponse.newBuilder().setSpeed(meanWind).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}