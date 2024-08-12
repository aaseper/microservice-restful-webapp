package eolopark.server.model.dto;

import jakarta.validation.constraints.NotNull;

public record UserRequest(@NotNull String username, @NotNull String password) {}
