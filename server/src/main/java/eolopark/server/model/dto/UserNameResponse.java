package eolopark.server.model.dto;

import jakarta.validation.constraints.NotNull;

public record UserNameResponse(@NotNull String Name) {}