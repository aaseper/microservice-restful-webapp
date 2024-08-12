package eolopark.server.model.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserUpdate(@NotNull String Name, List<String> roles) {}
