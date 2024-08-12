package eolopark.server.model.dto;

import jakarta.validation.constraints.NotNull;

public record EoloparkRequest(@NotNull String name, @NotNull String city, @NotNull double latitude,
                              @NotNull double longitude, @NotNull int area, @NotNull String terrainType,
                              @NotNull SubstationRequest substation, @NotNull AerogeneratorRequest aerogenerator) {}
