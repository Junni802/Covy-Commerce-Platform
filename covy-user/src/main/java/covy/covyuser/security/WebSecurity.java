package covy.covyuser.security;

import covy.covyuser.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity {

  private static final String[] WHITE_LIST = {
      "/", "/actuator/**"
  };

  private final UserService userService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final ObjectPostProcessor<Object> objectPostProcessor;
  private final Environment env;
  private final JwtTokenProvider jwtTokenProvider;

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(WHITE_LIST).permitAll()
            .requestMatchers(new IpAddressMatcher("127.0.0.1")).permitAll()
            .requestMatchers("/users/**").permitAll()
            .anyRequest().authenticated()
        )
        // 사용자 로그인 필터
        .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        // 서비스 계정 JWT 검증 필터
//        .addFilterBefore(serviceAccountJwtFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  public AuthenticationManager authenticationManager(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
    return auth.build();
  }

  @Bean
  public AuthenticationFilter authenticationFilter() throws Exception {
    return new AuthenticationFilter(authenticationManager(new AuthenticationManagerBuilder(objectPostProcessor)),
        userService, env, jwtTokenProvider);
  }

//  @Bean
//  public ServiceAccountJwtFilter serviceAccountJwtFilter() {
//    return new ServiceAccountJwtFilter(env);
//  }
}
