package covy.covyuser.user.service;

import covy.covyuser.security.JwtTokenProvider;
import covy.covyuser.user.dto.UserDto;
import covy.covyuser.user.dto.response.TokenResponseDto;
import covy.covyuser.user.entitiy.RefreshToken;
import covy.covyuser.user.repository.RefreshTokenRedisRepository;
import covy.covyuser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {

  private final JwtTokenProvider tokenProvider;
  private final RefreshTokenRedisRepository redisRepository;
  private final UserRepository userRepository;

  @Transactional
  public TokenResponseDto reissue(String oldAccessToken, String oldRefreshToken) {
    // 1. 만료된 AccessToken에서 유저 식별
    String userId = tokenProvider.getUserIdFromToken(oldAccessToken);
    if (userId == null) {
      throw new RuntimeException("유효하지 않은 Access Token입니다.");
    }

    // 2. Redis에서 해당 유저의 RefreshToken 객체 조회 (findById 사용)
    RefreshToken savedToken = redisRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("Refresh Token not found in Redis for user: {}", userId);
          return new RuntimeException("이미 만료되었거나 존재하지 않는 세션입니다.");
        });

    // 3. 클라이언트가 보낸 Refresh Token에서 JTI 추출
    String currentRequestJti = tokenProvider.getJtiFromToken(oldRefreshToken);

    // 4. 보안 검증 (객체에 저장된 JTI와 요청받은 JTI 비교)
    if (currentRequestJti == null || !savedToken.getJti().equals(currentRequestJti)) {
      // 🚨 탈취 혹은 중복 사용 감지!
      log.warn("Security Alert: Invalid JTI match for user: {}. Expected: {}, Received: {}",
          userId, savedToken.getJti(), currentRequestJti);

      // 보안 위협 시 해당 유저의 토큰 정보 즉시 삭제 (deleteById 사용)
      redisRepository.deleteById(userId);
      throw new RuntimeException("보안 위협이 감지되었습니다. 다시 로그인해주세요.");
    }

    // 5. 새로운 유저 정보 조회 (DB)
    UserDto userDto = userRepository.findByUserId(userId)
        .map(UserDto::from)
        .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

    // 6. 신규 토큰 세트 발급 및 Redis 갱신
    // JwtTokenProvider.createTokenResponse 내부에서 redisRepository.save()가 호출되도록 구현됨
    TokenResponseDto newTokens = tokenProvider.createTokenResponse(userDto);

    log.info("Successfully reissued tokens for user: {}", userId);
    return newTokens;
  }
}