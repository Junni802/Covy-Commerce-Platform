package covy.covymarket.user.service;

import covy.covymarket.user.dto.UserDto;
import covy.covymarket.user.entitiy.UserEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

  UserDto createUser(UserDto userDto);

  UserDto getUserByUserId(String userId);

  Iterable<UserEntity> getUserByAll();

  UserDto getUserDetsByEmail(String userName);

}