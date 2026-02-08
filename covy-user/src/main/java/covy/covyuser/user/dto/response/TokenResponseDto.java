package covy.covyuser.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponseDto {

  private String grantType;      // 보통 "Bearer"로 고정
  private String accessToken;
  private String refreshToken;
  private Long accessTokenExpiresIn;  // 클라이언트가 만료를 미리 알 수 있게 함 (초 단위)

  // 선택 사항: Redis 대조용으로 썼던 jti (로그용이나 디버깅용으로 포함 가능)
  // private String refreshTokenJti;
}