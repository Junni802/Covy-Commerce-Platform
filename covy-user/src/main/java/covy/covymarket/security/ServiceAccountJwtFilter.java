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

    String header = request.getHeader("Authorization");

    if (header == null || !header.startsWith("Bearer ")) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String token = header.replace("Bearer ", "");

    try {
      Jwts.parserBuilder()
          .setSigningKey(Keys.hmacShaKeyFor(serviceSecret.getBytes(StandardCharsets.UTF_8)))
          .build()
          .parseClaimsJws(token);

      filterChain.doFilter(request, response);
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }
}
