package com.example.riskmanagementsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(
                                org.springframework.security.web.csrf
                                        .CookieCsrfTokenRepository
                                        .withHttpOnlyFalse())
                        .ignoringRequestMatchers("/analytics/ai-analysis"))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/", "/auth/**", "/static/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/dashboard","/org/**","/profile/**","/risks/**","/notifications/**", "/assignments/**", "/dashboard/**").authenticated()
                        .anyRequest().permitAll()
                )

                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/auth/post-login", true)
                        .failureUrl("/auth/login?error=true")
                        .permitAll()

                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }
}
