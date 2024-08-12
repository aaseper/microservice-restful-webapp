package eolopark.server.model.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserIdAndNameAndRolesResponse(@NotNull Long id, @NotNull String name, @NotNull List<String> roles) {}
