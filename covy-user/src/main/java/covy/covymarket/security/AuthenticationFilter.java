package covy.covymarket.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import covy.covymarket.user.dto.UserDto;
import covy.covymarket.user.service.UserService;
import covy.covymarket.user.vo.RequestLogin;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
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

    String email = ((org.springframework.security.core.userdetails.User) authResult.getPrincipal()).getUsername();
    UserDto userDetails = userService.getUserDetsByEmail(email);

    // Key 생성 (env.getProperty("jwt.secret")이 Base64일 경우 decode 사용)
    byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(env.getProperty("jwt.secret"));
    Key key = Keys.hmacShaKeyFor(keyBytes);

    // Access Token (짧은 만료)
    String accessToken = Jwts.builder()
        .setSubject(userDetails.getUserId())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_MS))
        .signWith(key, io.jsonwebtoken.SignatureAlgorithm.HS384)
        .compact();

    // Refresh Token (긴 만료)
    String refreshToken = Jwts.builder()
        .setSubject(userDetails.getUserId())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY_MS))
        .signWith(key, io.jsonwebtoken.SignatureAlgorithm.HS384)
        .compact();

    // 1) Refresh Token을 HttpOnly + Secure 쿠키로 설정 (SameSite 포함)
    // maxAge는 초 단위
    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(true)                // HTTPS 환경에서만 전송
        .path("/")                   // 필요에 따라 도메인/경로 조정
        .maxAge(REFRESH_TOKEN_VALIDITY_MS / 1000) // 초 단위
        .sameSite("Strict")          // or "Lax" / "None" (None이면 secure 필요)
        .build();

    response.setHeader("Set-Cookie", refreshCookie.toString());

    // 2) Access Token을 JSON 바디로 응답 (선호) + optional: Authorization 헤더로도 추가
    Map<String, String> tokens = new HashMap<>();
    tokens.put("accessToken", accessToken);
    // tokens.put("refreshToken", refreshToken); // (보안상 쿠키로 보냈으니 바디에 담지 않는 게 안전)

    // Optional: Authorization 헤더에도 access token 추가 (클라이언트 편의)
    response.setHeader("access", accessToken);
    response.addHeader("userId", userDetails.getUserId());

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    new ObjectMapper().writeValue(response.getWriter(), tokens);
  }

//  @Override
//  protected void successfulAuthentication(HttpServletRequest request,
//      HttpServletResponse response,
//      FilterChain chain,
//      Authentication authResult) throws IOException {
//
//    String email = ((User) authResult.getPrincipal()).getUsername();
//    UserDto userDetails = userService.getUserDetsByEmail(email);
//
//    // 1️⃣ Access Token (짧은 만료)
//    String accessToken = Jwts.builder()
//        .setSubject(userDetails.getUserId())
////        .claim("role", userDetails.getRole()) // 필요 시 권한 포함
//        .setIssuedAt(new Date())
//        .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_MS))
//        .signWith(SignatureAlgorithm.HS384, env.getProperty("jwt.secret"))
//        .compact();
//
//    // 2️⃣ Refresh Token (긴 만료)
//    String refreshToken = Jwts.builder()
//        .setSubject(userDetails.getUserId())
//        .setIssuedAt(new Date())
//        .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY_MS))
//        .signWith(SignatureAlgorithm.HS384, env.getProperty("jwt.secret"))
//        .compact();
//
//    // 3️⃣ 응답 JSON에 토큰 전달 (헤더 대신)
//    Map<String, String> tokens = new HashMap<>();
//    tokens.put("accessToken", accessToken);
//    tokens.put("refreshToken", refreshToken);
//
//    response.setContentType("application/json");
//    response.setCharacterEncoding("UTF-8");
//    new ObjectMapper().writeValue(response.getWriter(), tokens);
//  }
}
