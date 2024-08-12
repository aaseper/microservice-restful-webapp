package eolopark.planner.model;

import jakarta.validation.constraints.NotNull;

public record CreationProgressMessage(@NotNull Long id,
                                      @NotNull int progress,
                                      @NotNull boolean completed,
                                      EoloparkResponse eoloparkResponse) {
}
