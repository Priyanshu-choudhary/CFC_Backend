package com.example.WebSecurityExample.ConfigSecurity;

import com.example.WebSecurityExample.Service.UserDetailServicesImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ConfigSecurity {

    @Autowired
    private UserDetailServicesImp userDetailServicesImp;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf().disable() // Disable CSRF protection
                .authorizeHttpRequests(authorizeRequests -> {
                    // Allow access to static resources
                    authorizeRequests.requestMatchers("/images/**", "/css/**", "/js/**", "/webjars/**").permitAll();

                    // Restrict access to other endpoints
                    authorizeRequests.requestMatchers("/users/**").authenticated();
                    authorizeRequests.anyRequest().permitAll();
                })
                .formLogin(formLogin -> formLogin.permitAll())
                .httpBasic() // Enable Basic Auth
                .and()
                .build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, PasswordEncoder passwordEncoder) throws Exception {
        auth.userDetailsService(userDetailServicesImp).passwordEncoder(passwordEncoder);
    }
}
