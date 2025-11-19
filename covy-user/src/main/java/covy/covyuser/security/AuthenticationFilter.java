package covy.covyuser.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import covy.covyuser.user.dto.UserDto;
import covy.covyuser.user.service.UserService;
import covy.covyuser.user.vo.RequestLogin;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Authentication Filter for handling login requests.
 * <p>
 * - Generates Access Token in JSON response body
 * - Sets Refresh Token in HttpOnly, Secure Cookie
 */
@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final int ACCESS_TOKEN_VALIDITY_MS = 15 * 60 * 1000; // 15분
  private static final int REFRESH_TOKEN_VALIDITY_SEC = 7 * 24 * 60 * 60; // 7일

  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;
  private final Environment env;

  public AuthenticationFilter(AuthenticationManager authenticationManager,
      UserService userService,
      Environment env,
      JwtTokenProvider jwtTokenProvider) {
    super.setAuthenticationManager(authenticationManager);
    this.userService = userService;
    this.env = env;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) {
    try {
      RequestLogin creds = OBJECT_MAPPER.readValue(request.getInputStream(), RequestLogin.class);
      return getAuthenticationManager().authenticate(
          new UsernamePasswordAuthenticationToken(
              creds.getEmail(),
              creds.getPassword(),
              Collections.emptyList()
          )
      );
    } catch (IOException e) {
      log.error("Authentication attempt failed", e);
      throw new RuntimeException("Failed to parse authentication request", e);
    }
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authResult) throws IOException {

    String email = ((User) authResult.getPrincipal()).getUsername();
    UserDto userDetails = userService.getUserDetsByEmail(email);

    // Generate Tokens
    String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

    // 1️⃣ Access Token: JSON Response Body
    Map<String, String> body = new HashMap<>();
    body.put("accessToken", accessToken);

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    OBJECT_MAPPER.writeValue(response.getWriter(), body);

    // 2️⃣ Refresh Token: HttpOnly, Secure Cookie
    Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setSecure(true); // HTTPS 환경에서 true
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge(REFRESH_TOKEN_VALIDITY_SEC);
    response.addCookie(refreshCookie);
  }
}
