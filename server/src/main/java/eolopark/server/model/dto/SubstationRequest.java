package eolopark.server.model.dto;

import jakarta.validation.constraints.NotNull;

public record SubstationRequest(@NotNull String model, @NotNull double power, @NotNull double voltage) {}
