package com.mansa.infrastructure.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Propagates the X-Correlation-Id header through the entire request chain.
 * Ensures full traceability:
 *   Frontend -> Gateway -> Card Service -> Kafka -> Transaction Service
 */
@Slf4j
@Component
@Order(1)
public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC    = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
                
        
        HttpServletRequest  httpRequest  = (HttpServletRequest)  request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        System.out.println("REQUEST RECEIVED -> " + httpRequest.getMethod() + " " + httpRequest.getRequestURI());
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank())
            correlationId = UUID.randomUUID().toString();

        MDC.put(CORRELATION_ID_MDC, correlationId);
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
               System.out.println("FILTER -> " +
                httpRequest.getMethod() +
                " " +
                httpRequest.getRequestURI());
                
            chain.doFilter(request, response);

            System.out.println(
            "FILTER RESPONSE STATUS -> "
            + httpResponse.getStatus()
        );

        } finally {
            MDC.remove(CORRELATION_ID_MDC);
        }
    }
}
