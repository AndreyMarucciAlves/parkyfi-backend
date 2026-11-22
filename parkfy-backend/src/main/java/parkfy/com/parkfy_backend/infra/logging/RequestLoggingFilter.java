package parkfy.com.parkfy_backend.infra.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registra no console as requisições HTTP recebidas, com rota, parâmetros e corpo.
 * O log é feito após o processamento porque o corpo só fica disponível depois de lido pelo controller.
 * @Author: João Szczypior
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_BODY_SIZE = 10_000;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());

        return HttpMethod.OPTIONS.matches(request.getMethod())
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, MAX_BODY_SIZE);

        try {
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            log.info("Requisição recebida: {} {} | parâmetros: {} | corpo: {} | status: {}",
                    wrappedRequest.getMethod(),
                    sanitize(wrappedRequest.getRequestURI()),
                    formatParameters(wrappedRequest.getParameterMap()),
                    formatBody(wrappedRequest),
                    response.getStatus());
        }
    }

    private String formatParameters(Map<String, String[]> parameters) {
        if (parameters.isEmpty()) {
            return "nenhum";
        }

        return parameters.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
                .map(this::sanitize)
                .collect(Collectors.joining(", "));
    }

    private String formatBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();

        if (content.length == 0) {
            return "vazio";
        }

        return sanitize(new String(content, StandardCharsets.UTF_8));
    }

    // Remove quebras de linha para manter cada requisição em uma linha e evitar falsificação de logs
    private String sanitize(String value) {
        return value.replaceAll("[\\r\\n]+", " ").trim();
    }
}
