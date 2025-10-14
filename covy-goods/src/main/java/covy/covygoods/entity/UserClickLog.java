package covy.covygoods.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_click_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserClickLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String userId;

  private String productId;

  private String category;

  private LocalDateTime clickAt;
}
