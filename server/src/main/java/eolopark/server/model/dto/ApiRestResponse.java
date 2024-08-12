package eolopark.server.model.dto;

import java.time.LocalDateTime;

public record ApiRestResponse(String path, String message, int statusCode, LocalDateTime localDateTime) {}