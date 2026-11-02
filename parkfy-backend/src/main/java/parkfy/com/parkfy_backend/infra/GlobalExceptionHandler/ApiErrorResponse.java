package parkfy.com.parkfy_backend.infra.GlobalExceptionHandler;

import java.time.Instant;

public record ApiErrorResponse(
        String message,
        int code,
        Instant timestamp
) {}
