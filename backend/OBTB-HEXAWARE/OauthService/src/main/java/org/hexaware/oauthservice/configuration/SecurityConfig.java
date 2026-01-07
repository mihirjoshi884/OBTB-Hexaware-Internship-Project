package org.hexaware.oauthservice.configuration;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import jakarta.servlet.http.HttpServletResponse;
import org.hexaware.oauthservice.entites.PrincipleUser;
import org.hexaware.oauthservice.services.CustomAuthenticationFailureHandler;
import org.hexaware.oauthservice.services.CustomAuthenticationSuccessHandler;
import org.hexaware.oauthservice.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;
import java.security.KeyPair;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Value("${userservice.base-uri}")
    private String userServiceUri;
    @Value("${angular.base-uri}")
    private String angularBaseUri;
    @Value("${authservice.base-uri}")
    private String authServiceBaseUri;
    @Autowired
    @Lazy
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private CustomAuthenticationFailureHandler authenticationFailureHandler;
    @Autowired
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;



    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // Order 0
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer configurer = new OAuth2AuthorizationServerConfigurer();

        http
                // 1. Updated Matcher: Ensure it captures the user API correctly
                .securityMatcher(request ->
                        configurer.getEndpointsMatcher().matches(request) ||
                                request.getServletPath().startsWith("/.well-known/") ||
                                request.getServletPath().startsWith("/oauth2/") ||
                                request.getServletPath().startsWith("/auth-api/v1/user")
                )
                .with(configurer, (authorizationServer) -> {
                    authorizationServer
                            .authorizationEndpoint(endpoint -> endpoint.consentPage("/oauth2/consent"))
                            .oidc(Customizer.withDefaults());
                })
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2. CRITICAL: Disable CSRF globally for this chain to allow PATCH without tokens
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 3. Specific Method Permits FIRST
                        .requestMatchers(HttpMethod.PATCH, "/auth-api/v1/user/verify/**", "/auth-api/v1/user/activate/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth-api/v1/user/is-active/**").permitAll()
                        // 4. General Permits
                        .requestMatchers("/.well-known/**", "/oauth2/jwks", "/auth-api/v1/user/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );

        return http.build();
    }

    /**
     * FINAL FIX: Bypasses the entire Security Filter Chain for these public endpoints.
     * This prevents any 302 redirects from happening for verification and registration.
     */
    @Bean
    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/auth-api/v1/user/verify/**",
                "/auth-api/v1/user/activate/**",
                "/auth-api/v1/user/is-active/**",
                "/auth-api/v1/register"
        );
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("frame-ancestors 'self' "+angularBaseUri))
                )
                // FIX 1: Enable CORS for the login and verification endpoints
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // FIX 2: Disable CSRF so Angular can POST to /login without a token
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/login", "/auth-api/v1/user/**",
                                "/auth-api/v1/register").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout.logoutUrl("/logout").permitAll())
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler)
                        .permitAll()
                );


        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        var angularClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("obtb-client-001")
                .clientName("obtb-angular-client-001")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .redirectUri(angularBaseUri+"/login/callback")
                .redirectUri(angularBaseUri+"/silent-refresh.html")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(true)
                        .build())
                .build();


        var authClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("obtb-client-002")
                .clientName("obtb-auth-client-002")
                .clientSecret("{noop}obtb-auth-clientSecret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .redirectUri(authServiceBaseUri+"/auth")
                .build();

        var notificationClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("obtb-client-003")
                .clientName("obtb-notification-client-003")
                .clientSecret("{noop}obtb-notification-client-003")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .build();

        var apiGatewayClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("obtb-api-gateway")
                .clientName("OBTB API Gateway")
                // Use {noop} for plain text in development
                .clientSecret("{noop}obtb-api-gateway-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                // Gateway usually uses basic auth to talk to Auth Service
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                // CRITICAL: This must match the resolved {baseUrl} pattern
                .redirectUri("http://localhost:9090/login/oauth2/code/api-gateway")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository( angularClient,authClient,notificationClient, apiGatewayClient);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }




    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();

        // Use the variable from your application properties
        configuration.setAllowedOrigins(List.of(angularBaseUri, "null"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));

        // FIX: Allow all headers to ensure preflight doesn't fail on "Origin", "Accept", etc.
        configuration.setAllowedHeaders(List.of("*"));

        // FIX: Expose headers if your frontend needs to read things like Authorization from the response
        configuration.setExposedHeaders(List.of("Authorization"));

        configuration.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    public AuthenticationProvider daoAuthenticationProvider() {
        // Pass the customUserDetailsService directly into the constructor
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);

        // Set the password encoder
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey(); // Generates a secure RSA key pair
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString()) // Unique ID for the key
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet); // Provides the key for signing
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource); // For verifying JWTs
    }


    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            if (context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)||
                    "id_token".equals(context.getTokenType().getValue())) {
                Authentication principal = context.getPrincipal();
                context.getClaims().claims((claims) -> {
                    // For users logging in via Authorization Code flow (our Angular app)
                    if (context.getAuthorizationGrantType().equals(AuthorizationGrantType.AUTHORIZATION_CODE)) {
                        Set<String> roles = AuthorityUtils.authorityListToSet(context.getPrincipal().getAuthorities())
                                .stream()
                                .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
                        claims.put("authority", roles);// Add user's authorities/roles to the token

                        Object userPrincipal = principal.getPrincipal();
                        if(userPrincipal instanceof PrincipleUser customUser) {
                            claims.put("username", customUser.getUsername());
                            claims.put("userId",customUser.getUserId().toString());
                            claims.put("roleMappingId", customUser.getRoleMappingId().toString());
                        }
                    }
                });
            }
        };
    }
    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

}