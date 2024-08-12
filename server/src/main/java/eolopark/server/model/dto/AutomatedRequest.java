package eolopark.server.model.dto;

import jakarta.validation.constraints.NotNull;

public record AutomatedRequest(@NotNull String city, @NotNull int area) {}
