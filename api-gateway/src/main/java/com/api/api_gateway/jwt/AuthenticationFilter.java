//package com.api.api_gateway.jwt;
//
//import io.jsonwebtoken.Claims;
//import lombok.RequiredArgsConstructor;
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//
//@Component
//public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
//
//    private final JwtService jwtService;
//
//    public AuthenticationFilter(JwtService jwtService) {
//        super(Config.class); // bắt buộc phải có dòng này để Spring Cloud hiểu Config class
//        this.jwtService = jwtService;
//    }
//
//    @Override
//    public GatewayFilter apply(Config config) {
//        return (exchange, chain) -> {
//            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                return exchange.getResponse().setComplete();
//            }
//
//            String token = authHeader.substring(7);
//            try {
//                jwtService.validateToken(token);
//                Claims claims = jwtService.extractAllClaims(token);
//
//                String userEmail = claims.getSubject();
//
//                var mutatedRequest = exchange.getRequest().mutate()
//                        .header("X-User-Email", userEmail)
//                        .build();
//
//                return chain.filter(exchange.mutate().request(mutatedRequest).build());
//            } catch (Exception e) {
//                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                return exchange.getResponse().setComplete();
//            }
//        };
//    }
//
//    public static class Config {
//        // có thể thêm config sau này nếu cần
//    }
//}
package com.api.api_gateway.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtService jwtService;

    public AuthenticationFilter(JwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = extractToken(exchange);

            if (token == null || token.isEmpty()) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                System.out.println("🔑 Token nhận được: " + token);

                return exchange.getResponse().setComplete();
            }

            try {
                // ✅ Gọi validate và extract thông tin
                jwtService.validateToken(token, exchange.getRequest());
                Claims claims = jwtService.extractAllClaims(token);

                String userEmail = claims.getSubject();
                System.out.println("✅ X-User-Email attached: " + userEmail);

                var mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Email", userEmail)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    // ✅ Extract token từ Header hoặc Cookie
    private String extractToken(ServerWebExchange exchange) {
        // Ưu tiên lấy từ Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Nếu không có thì lấy từ cookie
        String cookieHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.COOKIE);
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                cookie = cookie.trim();
                if (cookie.startsWith("access_token=")) {
                    return cookie.substring("access_token=".length());
                }
            }
        }

        return null;
    }

    public static class Config {
        // dùng cho config sau này nếu cần
    }
}
