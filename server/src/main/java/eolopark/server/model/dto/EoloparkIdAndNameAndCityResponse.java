package eolopark.server.model.dto;

import jakarta.validation.constraints.NotNull;

public record EoloparkIdAndNameAndCityResponse(@NotNull Long id, @NotNull String name, @NotNull String city) {}
