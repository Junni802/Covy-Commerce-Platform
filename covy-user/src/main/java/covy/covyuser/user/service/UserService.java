package covy.covyuser.user.service;

import covy.covyuser.user.dto.UserDto;
import covy.covyuser.user.entitiy.UserEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 사용자 관련 서비스 인터페이스
 * - UserDetailsService 상속: Spring Security 인증 연동
 */
public interface UserService extends UserDetailsService {

  /**
   * 새로운 사용자 생성
   * @param userDto 사용자 정보 DTO
   * @return 생성된 사용자 DTO
   */
  UserDto createUser(UserDto userDto);

  /**
   * 사용자 ID로 조회
   * @param userId 사용자 고유 ID
   * @return 사용자 DTO
   */
  UserDto getUserByUserId(String userId);

  /**
   * 모든 사용자 조회
   * @return UserEntity Iterable
   */
  Iterable<UserEntity> getUserByAll();

  /**
   * 이메일로 사용자 조회
   * @param userName 사용자 이메일
   * @return 사용자 DTO
   */
  UserDto getUserDetsByEmail(String userName);

}
