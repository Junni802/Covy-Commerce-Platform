package covy.covyuser.user.service;

import covy.covyuser.user.dto.UserDto;
import covy.covyuser.user.entitiy.UserEntity;
import covy.covyuser.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private BCryptPasswordEncoder passwordEncoder;

  @Mock
  private CircuitBreakerFactory circuitBreakerFactory;

  @InjectMocks
  private UserServiceImpl userService;

  private static final String EMAIL = "test@example.com";
  private static final String RAW_PWD = "password";
  private static final String ENCODED_PWD = "encodedPwd";

  @Test
  @DisplayName("createUser: 새로운 사용자 생성 시 UserDto 반환, 비밀번호 암호화 저장")
  void createUser_success() {
    // stub: password 암호화 + repository save
    when(passwordEncoder.encode(RAW_PWD)).thenReturn(ENCODED_PWD);

    UserDto userDto = new UserDto();
    userDto.setEmail(EMAIL);
    userDto.setPwd(RAW_PWD);

    UserEntity savedEntity = new UserEntity();
    savedEntity.setEmail(EMAIL);
    savedEntity.setEncryptedPwd(ENCODED_PWD);
    when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

    UserDto result = userService.createUser(userDto);

    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo(EMAIL);
    verify(userRepository, times(1)).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("loadUserByUsername: 존재하는 이메일 조회 시 UserDetails 반환")
  void loadUserByUsername_found() {
    UserEntity entity = new UserEntity();
    entity.setEmail(EMAIL);
    entity.setEncryptedPwd(ENCODED_PWD);
    when(userRepository.findByEmail(EMAIL)).thenReturn(entity);

    UserDetails userDetails = userService.loadUserByUsername(EMAIL);

    assertThat(userDetails.getUsername()).isEqualTo(EMAIL);
    assertThat(userDetails.getPassword()).isEqualTo(ENCODED_PWD);
  }

  @Test
  @DisplayName("loadUserByUsername: 존재하지 않는 이메일 조회 시 예외 발생")
  void loadUserByUsername_notFound() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(null);

    assertThatThrownBy(() -> userService.loadUserByUsername(EMAIL))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessage(EMAIL);
  }

  @Test
  @DisplayName("getUserByUserId: 존재하는 userId 조회 시 UserDto 반환")
  void getUserByUserId_success() {
    String userId = UUID.randomUUID().toString();
    UserEntity entity = new UserEntity();
    entity.setUserId(userId);
    entity.setEmail(EMAIL);
    entity.setEncryptedPwd(ENCODED_PWD);
    when(userRepository.findByUserId(userId)).thenReturn(entity);

    // stub circuitBreakerFactory if service uses it internally
    when(circuitBreakerFactory.create(anyString())).thenReturn(mock(org.springframework.cloud.client.circuitbreaker.CircuitBreaker.class));

    UserDto dto = userService.getUserByUserId(userId);

    assertThat(dto).isNotNull();
    assertThat(dto.getEmail()).isEqualTo(EMAIL);
    verify(userRepository, times(1)).findByUserId(userId);
  }

  @Test
  @DisplayName("getUserByUserId: 존재하지 않는 userId 조회 시 UsernameNotFoundException")
  void getUserByUserId_notFound() {
    String userId = UUID.randomUUID().toString();
    when(userRepository.findByUserId(userId)).thenReturn(null);

    assertThatThrownBy(() -> userService.getUserByUserId(userId))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("User not found");
  }

  @Test
  @DisplayName("getUserDetsByEmail: 존재하는 email 조회 시 UserDto 반환")
  void getUserDetsByEmail_success() {
    UserEntity entity = new UserEntity();
    entity.setEmail(EMAIL);
    entity.setEncryptedPwd(ENCODED_PWD);
    when(userRepository.findByEmail(EMAIL)).thenReturn(entity);

    UserDto dto = userService.getUserDetsByEmail(EMAIL);

    assertThat(dto).isNotNull();
    assertThat(dto.getEmail()).isEqualTo(EMAIL);
    verify(userRepository, times(1)).findByEmail(EMAIL);
  }

  @Test
  @DisplayName("getUserDetsByEmail: 존재하지 않는 email 조회 시 UsernameNotFoundException")
  void getUserDetsByEmail_notFound() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(null);

    assertThatThrownBy(() -> userService.getUserDetsByEmail(EMAIL))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("User not found by email");
  }
}
