package covy.covymarket.security;

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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

/**
 * <클래스 설명>
 *
 * @author : junni802
 * @date : 2025-02-20
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity {

  private static final String[] WHITE_LIST = {
      "/users/**",
      "/",
      "/**",
      "/actuator/**"
  };
  private final UserService userService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final ObjectPostProcessor<Object> objectPostProcessor;
  private final Environment env;

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .headers((headers) ->
            headers.frameOptions((frameOptions) -> frameOptions.sameOrigin())
        )
        .csrf((csrf) -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
            .disable())
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/actuator/**").permitAll()  // ← 이걸 꼭 추가
            .requestMatchers(WHITE_LIST).permitAll()
            .requestMatchers(new IpAddressMatcher("127.0.0.1")).permitAll()
            .requestMatchers("/user-service/users/**").permitAll()
            .anyRequest().denyAll()
        )
        .addFilter(getAuthentication());

    return http.build();
  }

  public AuthenticationManager authenticationManager(AuthenticationManagerBuilder auth)
      throws Exception {
    auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
    return auth.build();
  }


  private AuthenticationFilter getAuthentication() throws Exception {
    AuthenticationManagerBuilder builder = new AuthenticationManagerBuilder(objectPostProcessor);
    AuthenticationFilter authenticationFilter = new AuthenticationFilter(
        authenticationManager(builder), userService, env);
    return authenticationFilter;
  }


}
