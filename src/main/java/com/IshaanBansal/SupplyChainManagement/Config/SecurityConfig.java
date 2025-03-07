package com.IshaanBansal.SupplyChainManagement.Config;


import com.IshaanBansal.SupplyChainManagement.Util.JwtAuthenticationFilter;
import com.IshaanBansal.SupplyChainManagement.Util.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;




import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    //Make Changes for Security Concerns to allow limited access to apis
    private final JwtUtil jwtUtil;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf->csrf.disable())
                .authorizeHttpRequests(auth->
                        auth.requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                                .requestMatchers("/auth/**").permitAll()
                                .requestMatchers("/orders/**").permitAll()
                                .requestMatchers("/Order/**").permitAll()
                                .requestMatchers("/payments/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/Users/**").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/users/getUserByEmail").permitAll()

                                // Admin Access (authentication and role required)
                                .requestMatchers("/users/**").hasRole("ADMIN")

                                .anyRequest().authenticated()); // All other endpoints require authentication

        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}


