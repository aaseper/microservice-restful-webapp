package eolopark.server.model.dto;

import jakarta.validation.constraints.NotNull;

public record AerogeneratorRequest(@NotNull String aerogeneratorId, @NotNull double aerogeneratorLatitude,
                                   @NotNull double aerogeneratorLongitude, @NotNull int bladeLength,
                                   @NotNull int height, @NotNull int aerogeneratorPower) {}