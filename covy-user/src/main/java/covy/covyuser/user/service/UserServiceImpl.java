package covy.covyuser.user.service;

import covy.covyuser.user.dto.UserDto;
import covy.covyuser.user.entitiy.UserEntity;
import covy.covyuser.user.repository.UserRepository;
import java.util.ArrayList;
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
import org.springframework.web.client.RestTemplate;

@Service
public class UserServiceImpl implements UserService {

  private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
  UserRepository userRepository;
  BCryptPasswordEncoder passwordEncoder;
  Environment env;
  RestTemplate restTemplate;
//  OrderServiceClient orderServiceClient;
  CircuitBreakerFactory circuitBreakerFactory;

  @Autowired
  public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
      Environment env, CircuitBreakerFactory circuitBreakerFactory) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.env = env;
//    this.orderServiceClient = orderServiceClient;
    this.circuitBreakerFactory = circuitBreakerFactory;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserEntity userEntity = userRepository.findByEmail(username);

    if (userEntity == null) {
      throw new UsernameNotFoundException(username);
    }

    return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(),
        true, true, true, true,
        new ArrayList<>());
  }

  @Override
  public UserDto createUser(UserDto userDto) {
    userDto.setUserId(UUID.randomUUID().toString());

    ModelMapper mapper = new ModelMapper();
    mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    UserEntity userEntity = mapper.map(userDto, UserEntity.class);
    userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));

    userRepository.save(userEntity);

    UserDto map = mapper.map(userEntity, UserDto.class);

    return map;
  }

  @Override
  public UserDto getUserByUserId(String userId) {
    UserEntity userEntity = userRepository.findByUserId(userId);

    if (userEntity == null) {
      throw new UsernameNotFoundException("User not found");
    }

    ModelMapper mapper = new ModelMapper();
    mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    UserDto userDto = mapper.map(userEntity, UserDto.class);
    /* Using a resttemplate */
    /* String orderUrl = String.format(env.getProperty("order_service.url"), userId);
    ResponseEntity<List<ResponseOrder>> orderListResponse =
        restTemplate.exchange(orderUrl, HttpMethod.GET, null,
            new ParameterizedTypeReference<List<ResponseOrder>>() {
            });
    List<ResponseOrder> orderList = orderListResponse.getBody();*/

    /* Using a feign client */
    /* feign exception handling */
    /*
    List<ResponseOrder> orderList = null;
    try{
      orderList = orderServiceClient.getOrders(userId);
    } catch (FeignException e) {
      log.error(e.getMessage());
    }*/

    /* ErrorDecoder */
//    List<ResponseOrder> orderList = orderServiceClient.getOrders(userId);
    log.info("Before call orders microservice");
    CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
//    List<ResponseOrder> orderList = circuitBreaker.run(() -> orderServiceClient.getOrders(userId),
//        throwable -> new ArrayList<>());
    log.info("After call orders microservice");


//    userDto.setOrders(orderList);

    return userDto;
  }

  @Override
  public Iterable<UserEntity> getUserByAll() {
    return userRepository.findAll();
  }

  @Override
  public UserDto getUserDetsByEmail(String userName) {
    UserEntity userEntity = userRepository.findByEmail(userName);

    UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
    return userDto;
  }
}