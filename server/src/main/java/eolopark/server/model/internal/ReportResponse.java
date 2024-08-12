package eolopark.server.model.internal;

import jakarta.validation.constraints.NotNull;

public record ReportResponse (@NotNull String username, @NotNull int progress, @NotNull boolean completed) {
}
