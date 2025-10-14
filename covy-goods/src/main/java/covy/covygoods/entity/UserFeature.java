package covy.covygoods.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "user_feature")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserFeature {

  @Id
  private String userId;

  @ElementCollection
  @CollectionTable(name = "user_recent_clicks", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "product_id")
  private List<String> recentClickedProducts;

  private int cartCount;

  private double clickRateFashion;

  private int purchaseCount;
}
