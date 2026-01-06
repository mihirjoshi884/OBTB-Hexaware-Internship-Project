package org.hexaware.apigateway.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .cors(Customizer.withDefaults()) // Matches the YAML globalcors
                .csrf(csrf -> csrf.disable())    // Disable for dev; use tokens for prod
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/**", "/login/**", "/oauth2/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(Customizer.withDefaults());

        return http.build();
    }
}
