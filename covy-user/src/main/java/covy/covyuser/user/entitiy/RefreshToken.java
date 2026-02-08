package covy.covyuser.user.entitiy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "refreshToken") // Redis의 Key prefix가 됩니다.
public class RefreshToken {

  @Id // userId를 Key로 사용 (ex: refreshToken:userId)
  private String userId;

  @Indexed // 토큰으로 조회해야 할 경우가 있다면 인덱스를 겁니다.
  private String jti;

  @TimeToLive // 초(second) 단위로 설정됩니다.
  private Long expiration;
}