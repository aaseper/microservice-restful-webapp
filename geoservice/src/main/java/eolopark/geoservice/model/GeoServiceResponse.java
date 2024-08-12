package eolopark.geoservice.model;

import jakarta.validation.constraints.NotNull;

public record GeoServiceResponse(@NotNull double latETRS89, @NotNull double lonETRS89, @NotNull int altitude) {
}
