package covy.covyuser.user.service;

import covy.covyuser.user.dto.UserDto;
import covy.covyuser.user.entitiy.UserEntity;
import covy.covyuser.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * UserService 구현체
 * - 사용자 CRUD 및 Spring Security 인증 처리
 * - 주문 마이크로서비스 호출은 CircuitBreaker로 보호
 */
@Service
public class UserServiceImpl implements UserService {

  private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final Environment env;
  private final CircuitBreakerFactory circuitBreakerFactory;

  private final ModelMapper modelMapper;

  @Autowired
  public UserServiceImpl(UserRepository userRepository,
      BCryptPasswordEncoder passwordEncoder,
      Environment env,
      CircuitBreakerFactory circuitBreakerFactory) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.env = env;
    this.circuitBreakerFactory = circuitBreakerFactory;
    this.modelMapper = new ModelMapper();
    this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserEntity userEntity = userRepository.findByEmail(username);
    if (userEntity == null) {
      throw new UsernameNotFoundException(username);
    }

    return User.builder()
        .username(userEntity.getEmail())
        .password(userEntity.getEncryptedPwd())
        .authorities(new ArrayList<>())
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .disabled(false)
        .build();
  }

  @Override
  public UserDto createUser(UserDto userDto) {
    userDto.setUserId(UUID.randomUUID().toString());
    UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);
    userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));
    userRepository.save(userEntity);
    return modelMapper.map(userEntity, UserDto.class);
  }

  @Override
  public UserDto getUserByUserId(String userId) {
    UserEntity userEntity = userRepository.findByUserId(userId);
    if (userEntity == null) {
      throw new UsernameNotFoundException("User not found");
    }

    UserDto userDto = modelMapper.map(userEntity, UserDto.class);

    // CircuitBreaker 예시 (주문 마이크로서비스 호출)
    log.info("Before calling orders microservice");
    CircuitBreaker circuitBreaker = circuitBreakerFactory.create("orderServiceCircuitBreaker");
    // List<ResponseOrder> orderList = circuitBreaker.run(() -> orderServiceClient.getOrders(userId),
    //         throwable -> new ArrayList<>());
    log.info("After calling orders microservice");

    // userDto.setOrders(orderList);

    return userDto;
  }

  @Override
  public Iterable<UserEntity> getUserByAll() {
    return userRepository.findAll();
  }

  @Override
  public UserDto getUserDetsByEmail(String userName) {
    UserEntity userEntity = userRepository.findByEmail(userName);
    if (userEntity == null) {
      throw new UsernameNotFoundException("User not found by email: " + userName);
    }
    return modelMapper.map(userEntity, UserDto.class);
  }
}
