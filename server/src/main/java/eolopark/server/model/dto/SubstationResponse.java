package eolopark.server.model.dto;

import jakarta.validation.constraints.NotNull;

public record SubstationResponse(@NotNull String model, @NotNull double power, @NotNull double voltage) {}
