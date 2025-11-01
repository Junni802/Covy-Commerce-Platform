package covy.covycart.dto;

import covy.covycart.config.log.ActionType;
import lombok.Data;

@Data
public class CartRequest {
  private String goodsCd;
  private ActionType actionType;
}