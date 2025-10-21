package covy.covycart.dto;

import covy.covycart.domain.CartItem;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartResponse {
  private String userId;
  private List<CartItem> items;
}
