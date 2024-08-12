package eolopark.planner.model;

import jakarta.validation.constraints.NotNull;

public record EoloparkRequestMessage(@NotNull Long id,
                                     @NotNull String city,
                                     @NotNull int size) {
}
