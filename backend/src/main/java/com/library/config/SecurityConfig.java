package com.library.config;

import com.library.security.JwtAuthenticationFilter;
import com.library.security.JwtAuthEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                         JwtAuthEntryPoint jwtAuthEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .exceptionHandling().authenticationEntryPoint(jwtAuthEntryPoint).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests()
                .antMatchers("/api/auth/register", "/api/auth/login").permitAll()
                .antMatchers("/api/auth/me", "/api/auth/username", "/api/auth/password", "/api/auth/avatar", "/api/auth/bio").authenticated()
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/borrow/statistics").permitAll()
                .antMatchers(HttpMethod.GET, "/api/borrow/users/*/profile").permitAll()
                // 图书增删改：admin或super_admin
                .antMatchers(HttpMethod.POST, "/api/books").hasAnyRole("admin", "super_admin")
                .antMatchers(HttpMethod.PUT, "/api/books/**").hasAnyRole("admin", "super_admin")
                .antMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyRole("admin", "super_admin")
                .antMatchers("/api/borrow/**").authenticated()
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
