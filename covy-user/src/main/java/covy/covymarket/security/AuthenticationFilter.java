package covy.covymarket.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import covy.covymarket.user.dto.UserDto;
import covy.covymarket.user.service.UserService;
import covy.covymarket.user.vo.RequestLogin;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final UserService userService;
  private final Environment env;

  // 액세스 토큰 만료시간: 15분
  private final long ACCESS_TOKEN_VALIDITY_MS = 15 * 60 * 1000;
  // 리프레시 토큰 만료시간: 7일
  private final long REFRESH_TOKEN_VALIDITY_MS = 7 * 24 * 60 * 60 * 1000;

  public AuthenticationFilter(AuthenticationManager authenticationManager,
      UserService userService, Environment env) {
    super.setAuthenticationManager(authenticationManager);
    this.userService = userService;
    this.env = env;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) {
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
  protected void successfulAuthentication(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authResult) throws IOException {

    String email = ((User) authResult.getPrincipal()).getUsername();
    UserDto userDetails = userService.getUserDetsByEmail(email);

    // 1️⃣ Access Token (짧은 만료)
    String accessToken = Jwts.builder()
        .setSubject(userDetails.getUserId())
//        .claim("role", userDetails.getRole()) // 필요 시 권한 포함
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_MS))
        .signWith(SignatureAlgorithm.HS384, env.getProperty("jwt.secret"))
        .compact();

    // 2️⃣ Refresh Token (긴 만료)
    String refreshToken = Jwts.builder()
        .setSubject(userDetails.getUserId())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY_MS))
        .signWith(SignatureAlgorithm.HS384, env.getProperty("jwt.secret"))
        .compact();

    // 3️⃣ 응답 JSON에 토큰 전달 (헤더 대신)
    Map<String, String> tokens = new HashMap<>();
    tokens.put("accessToken", accessToken);
    tokens.put("refreshToken", refreshToken);

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    new ObjectMapper().writeValue(response.getWriter(), tokens);
  }
}
