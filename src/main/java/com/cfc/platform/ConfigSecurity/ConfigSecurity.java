package com.cfc.platform.ConfigSecurity;

import com.cfc.platform.Service.UserDetailServicesImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class ConfigSecurity {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailServicesImp userDetailServicesImp;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ── Public endpoints - no token needed ──────────────────
                        .requestMatchers("/Public/**").permitAll()
                        .requestMatchers("/images/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/Public/Create-User").permitAll()
                        .requestMatchers("/auth/login").permitAll()

                        // ── Contest - read-only and room-lookup are public ───────
                        // Arena actions (start, end, join, submit) require login.
                        .requestMatchers(HttpMethod.GET,  "/Contest").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/Contest/public").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/Contest/status/**").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/Contest/id/**").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/Contest/join/**").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/Contest/*/leaderboard").permitAll()

                        // ── Judge - run/languages public; submit requires login ──
                        .requestMatchers(HttpMethod.POST, "/judge/run").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/judge/languages").permitAll()
                        .requestMatchers(HttpMethod.POST, "/judge/submit").authenticated()

                        // ── Posts - course/contest creation is admin-only ────────
                        .requestMatchers(HttpMethod.POST, "/Posts/Course/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/Posts/ProblemSet").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/Posts/id/**").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/Posts/filter").permitAll()

                        // ── Everything else requires a valid JWT ─────────────────
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JwtFilter(jwtUtil, userDetailServicesImp),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
