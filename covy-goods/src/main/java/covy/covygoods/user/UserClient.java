package covy.covygoods.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// UserClient.java
@FeignClient(name = "covy-user", url = "http://127.0.0.1:8000/covy-user")
public interface UserClient {
  @GetMapping("/users/{id}")
  UserResponse getUser(@PathVariable("id") Long id);
}