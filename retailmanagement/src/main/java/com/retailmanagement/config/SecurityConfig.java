package com.retailmanagement.config;

import com.retailmanagement.security.CustomUserDetailsService;
import com.retailmanagement.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * ✅ Fix cho lỗi của bạn:
     * - DaoAuthenticationProvider bắt buộc truyền UserDetailsService trong constructor
     * - Không dùng setUserDetailsService() (vì version bạn đang dùng không có)
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider provider) {
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(provider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/api/products/**",
                                "/api/categories/**",
                                "/uploads/**"
                        ).permitAll()

                        // ví dụ rule role (tuỳ bạn đang dùng ROLE_... hay không)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/sales/**").hasAnyRole("SALES", "ADMIN")
                        .requestMatchers("/api/inventory/**").hasAnyRole("INVENTORY", "ADMIN")
                        .requestMatchers("/api/customer/**").hasAnyRole("CUSTOMER", "ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
