package com.gedtutor.config;

import com.gedtutor.security.CustomUserDetailsService;
import com.gedtutor.security.LoginRateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final LoginRateLimitFilter loginRateLimitFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          LoginRateLimitFilter loginRateLimitFilter) {
        this.userDetailsService = userDetailsService;
        this.loginRateLimitFilter = loginRateLimitFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Prevents Spring Boot from auto-registering LoginRateLimitFilter as a plain
     * servlet filter (which would cause double-invocation). The filter is registered
     * explicitly inside the Spring Security chain via addFilterBefore().
     */
    @Bean
    public FilterRegistrationBean<LoginRateLimitFilter> rateLimitFilterRegistration() {
        FilterRegistrationBean<LoginRateLimitFilter> registration = new FilterRegistrationBean<>(loginRateLimitFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Use a cookie-based CSRF token so JS fetch() can read it without Thymeleaf
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // Skip CSRF only for the sidebar practice widget — narrowed from /api/**
                        // to /api/practice/** so any future API routes stay CSRF-protected by default.
                        .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/api/practice/**"))
                )
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher("/"),
                                AntPathRequestMatcher.antMatcher("/register"),
                                AntPathRequestMatcher.antMatcher("/login"),
                                AntPathRequestMatcher.antMatcher("/videos"),
                                AntPathRequestMatcher.antMatcher("/videos/**"),
                                AntPathRequestMatcher.antMatcher("/api/practice/**"),
                                AntPathRequestMatcher.antMatcher("/css/**"),
                                AntPathRequestMatcher.antMatcher("/js/**"),
                                AntPathRequestMatcher.antMatcher("/images/**"),
                                AntPathRequestMatcher.antMatcher("/error"),
                                // Actuator: health + info are safe to expose publicly
                                AntPathRequestMatcher.antMatcher("/actuator/health"),
                                AntPathRequestMatcher.antMatcher("/actuator/info")
                        ).permitAll()
                        // H2 console and all other actuator endpoints — ADMIN only
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher("/h2/**"),
                                AntPathRequestMatcher.antMatcher("/actuator/**")
                        ).hasRole("ADMIN")
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/admin/**")).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/videos", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                // Security response headers
                .headers(h -> h
                        .frameOptions(f -> f.sameOrigin())
                        .contentTypeOptions(c -> {})           // X-Content-Type-Options: nosniff
                        .referrerPolicy(r -> r.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
                                        .ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        // Content-Security-Policy: tightened for a Thymeleaf + KaTeX app.
                        // 'unsafe-inline' for style is required by KaTeX; remove if you switch to
                        // a nonce-based approach later.
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                "script-src 'self' https://cdn.jsdelivr.net; " +
                                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                                "font-src 'self' https://cdn.jsdelivr.net; " +
                                "img-src 'self' data:; " +
                                "frame-ancestors 'self'; " +
                                "object-src 'none';"
                        ))
                )
                // Rate-limit brute-force login AND registration attempts before Spring Security processes them
                .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
