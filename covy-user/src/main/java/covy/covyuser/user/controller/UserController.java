package covy.covyuser.user.controller;

import covy.covyuser.user.dto.UserDto;
import covy.covyuser.user.entitiy.UserEntity;
import covy.covyuser.user.service.UserService;
import covy.covyuser.user.vo.RequestUser;
import covy.covyuser.user.vo.ResponseUser;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User 서비스 REST 컨트롤러
 * <p>
 * 유저 생성, 조회 등 API 제공
 */
@RestController
@RequestMapping("/")
public class UserController {

  private static final Logger log = LoggerFactory.getLogger(UserController.class);
  private final UserService userService;
  private final Environment env;
  private final ModelMapper modelMapper;

  public UserController(UserService userService, Environment env) {
    this.userService = userService;
    this.env = env;
    this.modelMapper = new ModelMapper();
    this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
  }

  /**
   * 신규 유저 생성
   */
  @PostMapping("/users")
  public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser requestUser) {
    log.info("Before call user microservice");
    UserDto userDto = modelMapper.map(requestUser, UserDto.class);
    userService.createUser(userDto);

    ResponseUser responseUser = modelMapper.map(userDto, ResponseUser.class);
    log.info("After call user microservice");
    return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
  }

  /**
   * 전체 유저 조회
   */
  @GetMapping("/users")
  public ResponseEntity<List<ResponseUser>> getUsersAll() {
    List<ResponseUser> users = StreamSupport.stream(userService.getUserByAll().spliterator(), false)
        .map(entity -> modelMapper.map(entity, ResponseUser.class))
        .collect(Collectors.toList());

    return ResponseEntity.ok(users);
  }

  /**
   * 특정 유저 조회
   */
  @GetMapping("/users/{userId}")
  public ResponseEntity<ResponseUser> getUser(@PathVariable("userId") String userId) {
    UserDto userDto = userService.getUserByUserId(userId);
    ResponseUser responseUser = modelMapper.map(userDto, ResponseUser.class);
    return ResponseEntity.ok(responseUser);
  }

  /**
   * 테스트용 엔드포인트 (미사용)
   */
  @GetMapping("/user-service")
  public void userTestingService(String testCode) {
    // TODO: 필요 시 구현
  }
}
