package covy.covyuser.user.service;

import covy.covyuser.security.JwtTokenProvider;
import covy.covyuser.user.dto.UserDto;
import covy.covyuser.user.dto.response.TokenResponseDto;
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
    // 1. 만료된 AccessToken에서 유저 식별 (ExpiredJwtException 대응 완료)
    String userId = tokenProvider.getUserIdFromToken(oldAccessToken);
    if (userId == null) {
      throw new RuntimeException("유효하지 않은 Access Token입니다.");
    }

    // 2. Redis에서 해당 유저에게 발급했던 유효한 JTI 가져오기
    // (참고: 기존에 저장된 값이 '토큰'이든 'JTI'든 상관없이 여기서 대조합니다)
    String savedJti = redisRepository.getRefreshToken(userId);

    // 3. 클라이언트가 보낸 Refresh Token에서 JTI 추출
    String currentRequestJti = tokenProvider.getJtiFromToken(oldRefreshToken);

    // 4. 보안 검증 (Rotation 핵심 로직)
    if (savedJti == null || !savedJti.equals(currentRequestJti)) {
      // 🚨 탈취 혹은 중복 사용 감지! (이미 사용된 RT이거나 위조됨)
      log.warn("Security Alert: Invalid Refresh Token request for user: {}", userId);
      redisRepository.deleteRefreshToken(userId); // 해당 유저의 모든 RT 무효화
      throw new RuntimeException("보안 위협이 감지되었습니다. 다시 로그인해주세요.");
    }

    // 5. 새로운 유저 정보 조회 (DB)
    UserDto userDto = userRepository.findByUserId(userId)
        .map(UserDto::from)
        .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

    // 6. [핵심] 신규 토큰 세트 발급 및 Redis 갱신
    // JwtTokenProvider 내부에서 새 JTI 생성 및 Redis 저장을 한 번에 처리합니다.
    TokenResponseDto newTokens = tokenProvider.createTokenResponse(userDto);

    log.info("Successfully reissued tokens for user: {}", userId);
    return newTokens;
  }
}