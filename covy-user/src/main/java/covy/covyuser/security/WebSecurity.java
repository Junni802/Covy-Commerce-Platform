package covy.covyuser.security;

import covy.covyuser.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
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

  private static final String[] WHITE_LIST = { "/", "/actuator/**" };

  private final UserService userService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final Environment env;
  private final JwtTokenProvider jwtTokenProvider;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .antMatchers(WHITE_LIST).permitAll()
            .antMatchers(String.valueOf(new IpAddressMatcher("127.0.0.1"))).permitAll()
            .antMatchers("/users/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(authenticationFilter(authManager), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationManager authManager(HttpSecurity http) throws Exception {
    return http.getSharedObject(AuthenticationManagerBuilder.class)
        .userDetailsService(userService)
        .passwordEncoder(bCryptPasswordEncoder)
        .and()
        .build();
  }

  @Bean
  public AuthenticationFilter authenticationFilter(AuthenticationManager authManager) {
    return new AuthenticationFilter(authManager, userService, env, jwtTokenProvider);
  }
}
