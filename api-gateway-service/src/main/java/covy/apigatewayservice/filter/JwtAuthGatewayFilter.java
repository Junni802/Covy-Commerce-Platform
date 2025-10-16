package covy.apigatewayservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthGatewayFilter extends AbstractGatewayFilterFactory<JwtAuthGatewayFilter.Config> {

  private final Environment env;

  public static class Config {}

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();

      // 1️⃣ JWT 추출
      String jwt = extractJwt(request);
      if (jwt == null) return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);

      // 2️⃣ JWT 검증
      Claims claims = parseJwt(jwt);
      if (claims == null) return onError(exchange, "Jwt token is not valid", HttpStatus.UNAUTHORIZED);

      // 3️⃣ 사용자 정보 헤더 추가
      ServerHttpRequest modifiedRequest = addUserHeaders(request, claims);

      return chain.filter(exchange.mutate().request(modifiedRequest).build());
    };
  }

  private String extractJwt(ServerHttpRequest request) {
    if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) return null;
    return request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION).replace("Bearer ", "").trim();
  }

  private Claims parseJwt(String jwt) {
    try {
      return Jwts.parser()
          .setSigningKey(env.getProperty("jwt.secret"))
          .parseClaimsJws(jwt)
          .getBody();
    } catch (Exception e) {
      log.error("JWT 검증 실패: {}", e.getMessage());
      return null;
    }
  }

  private ServerHttpRequest addUserHeaders(ServerHttpRequest request, Claims claims) {
    return request.mutate()
        .header("X-User-Id", claims.get("userId", String.class))
        .header("X-User-Email", claims.get("email", String.class))
        .build();
  }

  private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
    exchange.getResponse().setStatusCode(status);
    log.error(err);
    return exchange.getResponse().setComplete();
  }
}