package covy.covymarket.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import covy.covymarket.user.dto.UserDto;
import covy.covymarket.user.service.UserService;
import covy.covymarket.user.vo.RequestLogin;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import java.io.IOException;
import java.util.ArrayList;
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
  private final JwtTokenProvider jwtTokenProvider;
  private final Environment env;

  // 액세스 토큰 만료시간: 15분
  private final long ACCESS_TOKEN_VALIDITY_MS = 15 * 60 * 1000;
  // 리프레시 토큰 만료시간: 7일
  private final long REFRESH_TOKEN_VALIDITY_MS = 7 * 24 * 60 * 60 * 1000;

  public AuthenticationFilter(AuthenticationManager authenticationManager,
      UserService userService, Environment env, JwtTokenProvider jwtTokenProvider) {
    super.setAuthenticationManager(authenticationManager);
    this.userService = userService;
    this.env = env;
    this.jwtTokenProvider = jwtTokenProvider;
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

    String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

    // 1️⃣ Access Token JSON Body
    Map<String, String> body = new HashMap<>();
    body.put("accessToken", accessToken);

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    new ObjectMapper().writeValue(response.getWriter(), body);

    // 2️⃣ Refresh Token HttpOnly Cookie
    Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setSecure(true); // HTTPS 환경이면 true
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge((int)(7 * 24 * 60 * 60));
    response.addCookie(refreshCookie);
  }
}
