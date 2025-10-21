package covy.apigatewayservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
public class JwtAuthGatewayFilter extends AbstractGatewayFilterFactory<JwtAuthGatewayFilter.Config> {

  private final Environment env;

  // Config 클래스 정의
  public static class Config {}

  // 생성자에서 Config.class 명시
  public JwtAuthGatewayFilter(Environment env) {
    super(Config.class);
    this.env = env;
  }

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

      return Jwts.parserBuilder()
          .setSigningKey(env.getProperty("jwt.secret"))
          .build()
          .parseClaimsJws(jwt)
          .getBody();

    } catch (io.jsonwebtoken.ExpiredJwtException e) {
      log.error("JWT expired: {}", e.getMessage());
      return null;
    } catch (io.jsonwebtoken.JwtException e) {
      log.error("JWT invalid: {}", e.getMessage());
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
