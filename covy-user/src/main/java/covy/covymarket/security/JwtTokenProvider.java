package covy.covymarket.security;

import covy.covymarket.user.dto.UserDto;
import covy.covymarket.user.repository.RefreshTokenRedisRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  private final Environment env;
  private final RefreshTokenRedisRepository redisRepository;

  private final long ACCESS_TOKEN_VALIDITY_MS = 15 * 60 * 1000; // 15분
  private final long REFRESH_TOKEN_VALIDITY_MS = 7 * 24 * 60 * 60 * 1000; // 7일

  public String generateAccessToken(UserDto user) {
    return Jwts.builder()
        .setSubject(user.getUserId())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_MS))
        .signWith(SignatureAlgorithm.HS384, env.getProperty("jwt.secret"))
        .compact();
  }

  public String generateRefreshToken(UserDto user) {
    String refreshToken = Jwts.builder()
        .setSubject(user.getUserId())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY_MS))
        .signWith(SignatureAlgorithm.HS384, env.getProperty("jwt.secret"))
        .compact();

    // Redis에 저장
    redisRepository.saveRefreshToken(user.getUserId(), refreshToken, REFRESH_TOKEN_VALIDITY_MS);

    return refreshToken;
  }

  public boolean validateRefreshToken(String userId, String refreshToken) {
    String savedToken = redisRepository.getRefreshToken(userId);
    return refreshToken.equals(savedToken);
  }

  public void revokeRefreshToken(String userId) {
    redisRepository.deleteRefreshToken(userId);
  }
}
