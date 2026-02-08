package covy.covyuser.security;

import covy.covyuser.user.dto.UserDto;
import covy.covyuser.user.repository.RefreshTokenRedisRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {

  private final Environment env;
  private final RefreshTokenRedisRepository redisRepository;

  private final long ACCESS_TOKEN_VALIDITY_MS = 15 * 60 * 1000; // 15분
  private final long REFRESH_TOKEN_VALIDITY_MS = 24 * 60 * 60 * 1000; // 1일

  public String generateAccessToken(UserDto user) {
    return Jwts.builder()
        .claim("userId", user.getUserId())
        .claim("email", user.getEmail())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_MS))
        .signWith(SignatureAlgorithm.HS384, env.getProperty("jwt.secret"))
        .compact();
  }

  public String generateRefreshToken(UserDto user) {
    String refreshToken = Jwts.builder()
        .claim("userId", user.getUserId())
        .claim("email", user.getEmail())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY_MS))
        .signWith(SignatureAlgorithm.HS384, env.getProperty("jwt.secret"))
        .compact();

    // Redis에 저장
    redisRepository.saveRefreshToken(user.getUserId(), refreshToken, REFRESH_TOKEN_VALIDITY_MS);

    return refreshToken;
  }

  public Claims validateAndGetClaims(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(env.getProperty("jwt.secret"))
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (SecurityException | MalformedJwtException e) {
      log.error("잘못된 JWT 서명입니다.");
    } catch (ExpiredJwtException e) {
      log.error("만료된 JWT 토큰입니다.");
      // 💡 핵심: 재발급 처리를 위해 만료된 토큰의 Claims를 반환합니다.
      return e.getClaims();
    } catch (UnsupportedJwtException e) {
      log.error("지원되지 않는 JWT 토큰입니다.");
    } catch (IllegalArgumentException e) {
      log.error("JWT 토큰이 잘못되었습니다.");
    }
    return null;
  }

  public void revokeRefreshToken(String userId) {
    redisRepository.deleteRefreshToken(userId);
  }
}