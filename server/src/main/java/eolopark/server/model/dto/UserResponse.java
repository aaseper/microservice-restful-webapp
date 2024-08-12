package eolopark.server.model.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserResponse(@NotNull Long id, @NotNull String name, @NotNull int parkMax, @NotNull List<String> roles,
                           List<EoloparkResponse> eoloparks) {}
