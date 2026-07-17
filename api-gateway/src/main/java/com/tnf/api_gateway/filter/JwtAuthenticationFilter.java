package com.tnf.api_gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

/**
 * Validates the HS256 JWT on every request except the public paths, and forwards the caller's
 * identity to downstream services as trusted headers (X-Customer-Id, X-Auth-Email).
 *
 * Downstream services trust these headers, so we strip any client-supplied values first
 * (anti-spoofing) and set them only from a verified token.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    public static final String CUSTOMER_ID_HEADER = "X-Customer-Id";
    public static final String AUTH_EMAIL_HEADER = "X-Auth-Email";

    // Paths reachable without a token.
    private static final List<String> PUBLIC_PATTERNS = List.of(
            "/auth/**",
            "/actuator/**",
            "/fallback/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/**/v3/api-docs");

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final SecretKey key;

    public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return unauthorized(exchange, "Invalid or expired token");
        }

        String customerId = claims.getSubject();
        String email = claims.get("email", String.class);

        // Strip any client-supplied identity headers, then inject the verified ones.
        ServerHttpRequest mutated = request.mutate()
                .headers(headers -> {
                    headers.remove(CUSTOMER_ID_HEADER);
                    headers.remove(AUTH_EMAIL_HEADER);
                    headers.add(CUSTOMER_ID_HEADER, customerId);
                    if (email != null) {
                        headers.add(AUTH_EMAIL_HEADER, email);
                    }
                })
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATTERNS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // Run before routing so unauthenticated requests never reach a downstream service.
        return -1;
    }
}
