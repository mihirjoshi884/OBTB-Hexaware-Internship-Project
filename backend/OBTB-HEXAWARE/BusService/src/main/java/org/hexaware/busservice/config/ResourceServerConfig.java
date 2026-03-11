package org.hexaware.busservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerConfig {

    @Bean
    public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/bus-api/public/v1/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // FIX: Combined rule for the private API
                        // This allows BOTH the Human Operator (via Gateway) AND the Booking Service (via M2M)
                        .requestMatchers("/bus-api/private/v1/**").hasAnyAuthority("ROLE_BUS_OPERATOR", "SCOPE_internal")

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // 1. Convert Scopes (Check both "scope" and "scp" claims just in case)
            JwtGrantedAuthoritiesConverter scpConverter = new JwtGrantedAuthoritiesConverter();
            scpConverter.setAuthorityPrefix("SCOPE_");

            // Try the default "scope" claim
            authorities.addAll(scpConverter.convert(jwt));

            // Try the "scp" claim if "scope" was empty
            if (authorities.isEmpty()) {
                scpConverter.setAuthoritiesClaimName("scp");
                authorities.addAll(scpConverter.convert(jwt));
            }

            // 2. Convert Roles (For human users coming via Gateway)
            JwtGrantedAuthoritiesConverter roleConverter = new JwtGrantedAuthoritiesConverter();
            roleConverter.setAuthoritiesClaimName("roles");
            roleConverter.setAuthorityPrefix("");
            authorities.addAll(roleConverter.convert(jwt));

            // DEBUG: Keep this line until you see "SCOPE_internal" in your logs!
            System.out.println("Final Authorities for request: " + authorities);

            return authorities;
        });

        return converter;
    }
}
