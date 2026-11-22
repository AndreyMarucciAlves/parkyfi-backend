package parkfy.com.parkfy_backend.infra.GlobalExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import parkfy.com.parkfy_backend.infra.exception.BadRequestException;
import parkfy.com.parkfy_backend.infra.exception.ResourceAlreadyExistsException;
import parkfy.com.parkfy_backend.infra.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException exception) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = "Dados da requisição inválidos.";
        }

        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(BadRequestException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        log.warn("Violação de integridade em {} {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getMostSpecificCause().getMessage());

        return buildResponse(HttpStatus.CONFLICT, getIntegrityViolationMessage(request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {
        log.error("Erro inesperado", exception);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno no servidor."
        );
    }

    private String getIntegrityViolationMessage(HttpServletRequest request) {
        if (!HttpMethod.DELETE.matches(request.getMethod())) {
            return "Não foi possível salvar, pois os dados informados conflitam com registros existentes.";
        }

        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (path.startsWith("/veiculo/")) {
            return "Não é possível remover este veículo, pois ele possui registros de entrada e saída.";
        }

        if (path.startsWith("/tipo-veiculo/")) {
            return "Não é possível remover este tipo de veículo, pois existem veículos vinculados a ele.";
        }

        return "Não é possível remover este registro, pois ele está vinculado a outros dados.";
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message) {
        ApiErrorResponse response = new ApiErrorResponse(
                message,
                status.value(),
                Instant.now()
        );

        return ResponseEntity.status(status).body(response);
    }
}
