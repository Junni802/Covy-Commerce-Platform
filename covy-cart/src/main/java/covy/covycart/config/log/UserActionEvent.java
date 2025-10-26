package covy.covycart.config.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserActionEvent {

  private String userId;

  private String goodsCd;

  private ActionType actionType;

  private Long timestamp;

}
