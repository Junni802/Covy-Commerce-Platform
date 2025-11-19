package covy.covyuser.user.repository;

import covy.covyuser.user.entitiy.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * User Entity CRUD Repository
 * - 사용자 조회 및 저장 기능 제공
 */
@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {

  /**
   * 사용자 ID로 조회
   *
   * @param userId 사용자 고유 ID
   * @return UserEntity
   */
  UserEntity findByUserId(String userId);

  /**
   * 이메일로 조회
   *
   * @param username 사용자 이메일
   * @return UserEntity
   */
  UserEntity findByEmail(String username);
}
