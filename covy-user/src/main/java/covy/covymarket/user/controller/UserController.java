package covy.covymarket.user.controller;

import covy.covymarket.user.dto.UserDto;
import covy.covymarket.user.entitiy.UserEntity;
import covy.covymarket.user.service.UserService;
import covy.covymarket.user.vo.RequestUser;
import covy.covymarket.user.vo.ResponseUser;
import io.micrometer.core.annotation.Timed;
import java.util.ArrayList;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UserController {

  private UserService userService;
  private Environment env;
  @Value("${greeting.message}")
  private String greeting;

  public UserController(UserService userService, Environment env) {
    this.userService = userService;
    this.env = env;
  }

  @GetMapping("/health_check")
  @Timed(value = "users.status", longTask = true)
  public String status() {
    return String.format("It's Working in User Service"
        + ", port(local.server.port)=" + env.getProperty("local.server.port")
        + ", port(server.port)=" + env.getProperty("server.port")
        + ", token secret=" + env.getProperty("jwt.secret")
        + ", token expiration time=" + env.getProperty("jwt.token_expiration_time"));
  }

  @GetMapping("/welcome")
  @Timed(value = "users.welcome", longTask = true)
  public String welcome() {
    return greeting;
  }

  @PostMapping("/users")
  public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {
    ModelMapper mapper = new ModelMapper();
    mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    UserDto userDto = mapper.map(user, UserDto.class);
    userService.createUser(userDto);

    ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);

    return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
  }

  @GetMapping("/user-service")
  public void userTestingService(String testCode) {

  }

  @GetMapping("/users")
  public ResponseEntity<List<ResponseUser>> getUsersAll() {
    Iterable<UserEntity> userList = userService.getUserByAll();

    List<ResponseUser> resut = new ArrayList<>();
    userList.forEach(v -> {
      resut.add(new ModelMapper().map(v, ResponseUser.class));
    });

    return ResponseEntity.status(HttpStatus.OK).body(resut);
  }

  @GetMapping("/users/{userId}")
  public ResponseEntity<ResponseUser> getUsers(@PathVariable("userId") String userId) {
    UserDto userDto = userService.getUserByUserId(userId);

    ResponseUser map = new ModelMapper().map(userDto, ResponseUser.class);

    return ResponseEntity.status(HttpStatus.OK).body(map);
  }

}