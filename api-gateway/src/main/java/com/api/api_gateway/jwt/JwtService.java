//package com.api.api_gateway.jwt;
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.security.Key;
//
//@Service
//public class JwtService {
//
//    @Value("${jwt.secret}")
//    private String secret;
//
//    public void validateToken(String token) {
//        extractAllClaims(token); // nếu lỗi -> sẽ ném exception
//    }
//
//    public Claims extractAllClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(getSignKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    private Key getSignKey() {
//        return Keys.hmacShaKeyFor(secret.getBytes());
//    }
//}
package com.api.api_gateway.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Objects;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    public void validateToken(String token, ServerHttpRequest request) {
        Claims claims = extractAllClaims(token);
        System.out.println("🔑 Token nhận được: " + token);


        String tokenIp = claims.get("ip", String.class);
        String tokenUa = claims.get("ua", String.class);
        String requestIp = request.getRemoteAddress().getAddress().getHostAddress();
        if ("0:0:0:0:0:0:0:1".equals(requestIp)) {
            requestIp = "127.0.0.1";
        }


        String requestUa = request.getHeaders().getFirst("User-Agent");
        System.out.println("🔑  " + tokenIp );
        System.out.println("🔑  " + tokenUa );
        System.out.println("🔑  " + requestIp );
        System.out.println("🔑  " + requestUa );
        if (!tokenIp.equals(requestIp) || !tokenUa.equals(requestUa)) {
            throw new RuntimeException("IP or User-Agent mismatch!");
        }

        if (claims.getExpiration().before(new Date())) {
            throw new RuntimeException("Token expired");
        }
    }


    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
