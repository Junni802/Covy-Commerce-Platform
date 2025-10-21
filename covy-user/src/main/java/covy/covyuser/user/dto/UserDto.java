package covy.covyuser.user.dto;

import covy.covyuser.user.vo.ResponseOrder;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

  private String email;

  private String pwd;

  private String name;

  private String userId;

  private Date createAt;

  private String encryptedPwd;

  private List<ResponseOrder> orders;

}