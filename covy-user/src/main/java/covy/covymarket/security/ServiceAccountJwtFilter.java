package covy.covymarket.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ServiceAccountJwtFilter extends OncePerRequestFilter {

  private final String serviceSecret;

  public ServiceAccountJwtFilter(Environment env) {
    this.serviceSecret = env.getProperty("service.jwt.secret");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String path = request.getRequestURI();

    // ✅ 예외 경로(회원가입, 로그인 등)는 바로 통과시킴
    if (isExcludedPath(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    String header = request.getHeader("Authorization");

    // ✅ JWT 토큰이 없는 경우 401
    if (header == null || !header.startsWith("Bearer ")) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String token = header.replace("Bearer ", "");

    try {
      // ✅ JWT 유효성 검증
      Jwts.parserBuilder()
          .setSigningKey(Keys.hmacShaKeyFor(serviceSecret.getBytes(StandardCharsets.UTF_8)))
          .build()
          .parseClaimsJws(token);

      filterChain.doFilter(request, response);

    } catch (Exception e) {
      // ✅ JWT 파싱 실패 시 401 반환
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  private boolean isExcludedPath(String path) {
    return path.startsWith("/users")  // 회원가입
        || path.startsWith("/login")  // 로그인
        || path.startsWith("/actuator")         // 헬스체크
        || path.startsWith("/swagger");          // 문
  }
  }
