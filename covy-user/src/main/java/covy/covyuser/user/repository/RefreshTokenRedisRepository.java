package covy.covyuser.user.repository;

import covy.covyuser.user.entitiy.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRedisRepository extends CrudRepository<RefreshToken, String> {
  // 기본적으로 save(), findById(), deleteById()를 제공합니다.
  // findById(userId)를 호출하면 Redis에서 데이터를 가져옵니다.
}