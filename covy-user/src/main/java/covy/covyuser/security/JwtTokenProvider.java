package covy.covyuser.security;

import covy.covyuser.user.dto.UserDto;
import covy.covyuser.user.dto.response.TokenResponseDto;
import covy.covyuser.user.repository.RefreshTokenRedisRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {

  private final Environment env;
  private final RefreshTokenRedisRepository redisRepository;
  private final long ACCESS_TOKEN_VALIDITY_MS = 15 * 60 * 1000; // 15분
  private final long REFRESH_TOKEN_VALIDITY_MS = 24 * 60 * 60 * 1000; // 1일
  private Key signingKey;

  @PostConstruct
  protected void init() {
    // String 키를 미리 Key 객체로 변환하여 성능과 보안 강화
    byte[] keyBytes = Decoders.BASE64.decode(env.getProperty("jwt.secret"));
    this.signingKey = Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * Access & Refresh Token 통합 생성 (Rotation 전략)
   */
  public TokenResponseDto createTokenResponse(UserDto user) {
    long now = System.currentTimeMillis();
    String jti = UUID.randomUUID().toString(); // Refresh Token 식별자

    // 1. Access Token 생성
    String accessToken = Jwts.builder()
        .setSubject(user.getUserId())
        .claim("email", user.getEmail())
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(now + ACCESS_TOKEN_VALIDITY_MS))
        .signWith(signingKey, SignatureAlgorithm.HS384)
        .compact();

    // 2. Refresh Token 생성 (JTI 포함)
    String refreshToken = Jwts.builder()
        .setSubject(user.getUserId())
        .setId(jti) // Redis 대조용 고유 ID
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(now + REFRESH_TOKEN_VALIDITY_MS))
        .signWith(signingKey, SignatureAlgorithm.HS384)
        .compact();

    // 3. Redis 저장 (토큰 전체가 아닌 JTI만 저장하여 메모리 최적화)
    redisRepository.saveRefreshToken(user.getUserId(), jti, REFRESH_TOKEN_VALIDITY_MS);

    return TokenResponseDto.builder()
        .grantType("Bearer")
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(ACCESS_TOKEN_VALIDITY_MS)
        .build();
  }

  /**
   * 토큰 검증 및 Claims 추출 (만료 시 ExpiredJwtException에서 추출)
   */
  public Claims parseClaims(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(signingKey)
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (ExpiredJwtException e) {
      log.info("만료된 토큰이지만 재발급을 위해 Claims를 추출합니다.");
      return e.getClaims();
    } catch (SecurityException | MalformedJwtException | UnsupportedJwtException |
             IllegalArgumentException e) {
      log.error("유효하지 않은 토큰입니다: {}", e.getMessage());
      return null;
    }
  }

  /**
   * 토큰에서 JTI(UUID) 추출
   */
  public String getJtiFromToken(String token) {
    Claims claims = parseClaims(token);
    return claims != null ? claims.getId() : null;
  }

  /**
   * 토큰에서 UserId 추출
   */
  public String getUserIdFromToken(String token) {
    Claims claims = parseClaims(token);
    return claims != null ? claims.getSubject() : null;
  }

  public void revokeRefreshToken(String userId) {
    redisRepository.deleteRefreshToken(userId);
  }
}