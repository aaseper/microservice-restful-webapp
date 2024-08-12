package eolopark.server.model.internal;

import eolopark.server.model.dto.EoloparkResponse;
import jakarta.validation.constraints.NotNull;

public record CreationProgressMessage(@NotNull Long id, @NotNull int progress, @NotNull boolean completed,
                                      EoloparkResponse eoloparkResponse) {}
