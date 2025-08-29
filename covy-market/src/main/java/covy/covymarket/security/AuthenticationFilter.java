package covy.covymarket.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import covy.covymarket.user.dto.UserDto;
import covy.covymarket.user.service.UserService;
import covy.covymarket.user.vo.RequestLogin;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  @Value("${jwt.token-validity-in-seconds}")
  long tokenValidityInSeconds;
  private UserService userService;
  private Environment env;

  public AuthenticationFilter(AuthenticationManager authenticationManager, UserService userService,
      Environment env) {
    super.setAuthenticationManager(authenticationManager);
    this.userService = userService;
    this.env = env;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException {
    try {
      RequestLogin creds = new ObjectMapper().readValue(request.getInputStream(),
          RequestLogin.class);

      return getAuthenticationManager().authenticate(
          new UsernamePasswordAuthenticationToken(
              creds.getEmail(),
              creds.getPassword(),
              new ArrayList<>()
          )
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authResult) throws IOException,
      ServletException {

    String userName = ((User) authResult.getPrincipal()).getUsername();
    UserDto userDetails = userService.getUserDetsByEmail(userName);

    String token = Jwts.builder()
        .setSubject(userDetails.getUserId())
        .setExpiration(
            new Date(System.currentTimeMillis() + 86400000))
        .signWith(SignatureAlgorithm.HS384,
            env.getProperty("jwt.secret"))
        .compact();

    response.addHeader("token", token);
    response.addHeader("userId", userDetails.getUserId());
  }

}
