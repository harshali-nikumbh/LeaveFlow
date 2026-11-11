
package com.settribe.leavemanagement;

import com.settribe.leavemanagement.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;

@Configuration
public class SecurityConfig {

        @Value("${app.cookie.secure:false}")
        private boolean secureCookie;

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();

                csrfRepository.setCookieCustomizer(cookie -> cookie.path("/")
                                .sameSite(secureCookie ? "None" : "Lax")
                                .secure(secureCookie));

                http
                                .cors(cors -> {
                                })
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(csrfRepository)
                                                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
                                .formLogin(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                                                .requestMatchers(HttpMethod.POST,
                                                                "/api/auth/register",
                                                                "/api/auth/login",
                                                                "/api/auth/forgot-password",
                                                                "/api/auth/reset-password")
                                                .permitAll()
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(List.of(
                                "http://localhost:5173",
                                "https://leaveflow-2gzs.onrender.com"));

                configuration.setAllowedMethods(
                                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

                configuration.setAllowedHeaders(
                                List.of("Content-Type", "X-XSRF-TOKEN"));

                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration("/**", configuration);

                return source;
        }

        private static final class SpaCsrfTokenRequestHandler
                        implements CsrfTokenRequestHandler {

                private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();

                private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

                @Override
                public void handle(
                                HttpServletRequest request,
                                HttpServletResponse response,
                                Supplier<CsrfToken> csrfToken) {

                        xor.handle(request, response, csrfToken);
                        csrfToken.get();
                }

                @Override
                public String resolveCsrfTokenValue(
                                HttpServletRequest request,
                                CsrfToken csrfToken) {

                        String headerValue = request.getHeader(csrfToken.getHeaderName());

                        return StringUtils.hasText(headerValue)
                                        ? plain.resolveCsrfTokenValue(request, csrfToken)
                                        : xor.resolveCsrfTokenValue(request, csrfToken);
                }

        }
}
