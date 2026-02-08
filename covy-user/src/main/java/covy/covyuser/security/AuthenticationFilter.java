package covy.covyuser.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import covy.covyuser.user.dto.UserDto;
import covy.covyuser.user.dto.response.TokenResponseDto;
import covy.covyuser.user.service.UserService;
import covy.covyuser.user.vo.RequestLogin;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Authentication Filter for handling login requests.
 * <p>
 * - Generates Access Token in JSON response body - Sets Refresh Token in HttpOnly, Secure Cookie
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

    // 1. 토큰 생성
    TokenResponseDto tokenResponse = jwtTokenProvider.createTokenResponse(userDetails);

    // 2. Access Token 쿠키 생성 (15분)
    ResponseCookie accessCookie = ResponseCookie.from("accessToken", tokenResponse.getAccessToken())
        .path("/")
        .httpOnly(true)
        .secure(false) // 로컬 테스트 시 false, 배포 시 true
        .maxAge(15 * 60)
        .sameSite("Lax")
        .build();

    // 3. Refresh Token 쿠키 생성 (7일)
    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken",
            tokenResponse.getRefreshToken())
        .path("/")
        .httpOnly(true)
        .secure(false) // 로컬 테스트 시 false, 배포 시 true
        .maxAge(REFRESH_TOKEN_VALIDITY_SEC)
        .sameSite("Lax")
        .build();

    // 4. 응답 헤더에 쿠키 추가 (순서 중요: 바디를 쓰기 전에 헤더를 먼저 설정)
    response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, accessCookie.toString());
    response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, refreshCookie.toString());

    // 5. 바디 응답 (쿠키로만 관리한다면 바디는 비워두거나 메시지만 전달)
    Map<String, String> body = new HashMap<>();
    body.put("message", "Login Success");

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    OBJECT_MAPPER.writeValue(response.getWriter(), body);
  }
}
