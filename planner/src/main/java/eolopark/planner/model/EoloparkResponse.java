package eolopark.planner.model;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EoloparkResponse(@NotNull long id,
                               @NotNull String name,
                               @NotNull String city,
                               @NotNull double latitude,
                               @NotNull double longitude,
                               @NotNull int area,
                               @NotNull String terrainType,
                               @NotNull SubstationResponse substation,
                               @NotNull List<AerogeneratorResponse> aerogenerators) {
}
