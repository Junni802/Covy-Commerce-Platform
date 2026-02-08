package covy.covyuser.user.dto;

import covy.covyuser.user.entitiy.UserEntity;
import covy.covyuser.user.vo.ResponseOrder;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User DTO - 사용자 정보를 표현 - 주문 정보까지 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

  private String email;             // 사용자 이메일
  private String pwd;               // 원본 비밀번호
  private String name;              // 사용자 이름
  private String userId;            // 사용자 고유 ID
  private Date createAt;            // 생성 일자
  private String encryptedPwd;      // 암호화된 비밀번호
  private List<ResponseOrder> orders; // 주문 내역 리스트

  public static UserDto from(UserEntity entity) {
    return UserDto.builder()
        .email(entity.getEmail())
        .name(entity.getName())
        .userId(entity.getUserId())
        .encryptedPwd(entity.getEncryptedPwd())
        .build();
  }

}
