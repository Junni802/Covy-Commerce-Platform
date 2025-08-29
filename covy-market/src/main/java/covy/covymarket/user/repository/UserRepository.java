package covy.covymarket.user.repository;

import covy.covymarket.user.entitiy.UserEntity;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserEntity, Long> {

  UserEntity findByUserId(String userId);

  UserEntity findByEmail(String username);
}
