package covy.covyuser.user.repository;

import covy.covyuser.user.entitiy.UserEntity;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserEntity, Long> {

  UserEntity findByUserId(String userId);

  UserEntity findByEmail(String username);
}
