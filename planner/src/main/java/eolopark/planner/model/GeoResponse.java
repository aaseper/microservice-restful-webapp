package eolopark.planner.model;

import jakarta.validation.constraints.NotNull;

public record GeoResponse(@NotNull double latETRS89, @NotNull double lonETRS89, @NotNull int altitude) {
}
