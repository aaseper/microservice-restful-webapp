package eolopark.server.model.internal;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EoloparkAndAerogenerators(@NotNull Eolopark eolopark, @NotNull List<Aerogenerator> aerogenerators) {}