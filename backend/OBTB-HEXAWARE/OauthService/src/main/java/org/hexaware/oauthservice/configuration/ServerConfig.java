package org.hexaware.oauthservice.configuration;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import jakarta.servlet.http.HttpServletResponse;
import org.hexaware.oauthservice.configuration.customizers.CustomOauth2RefreshTokenGenerator;
import org.hexaware.oauthservice.configuration.customizers.PublicClientRefreshProvider;
import org.hexaware.oauthservice.configuration.customizers.PublicClientRefreshTokenAuthenticationConverter;
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
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.PublicClientAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;

import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.oauth2.server.authorization.web.authentication.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.security.KeyPair;

@Configuration
@EnableWebSecurity
public class ServerConfig {


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
    public AuthorizationServerSettings  authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(authServiceBaseUri)
                .build();
    }
    @Bean
    public OAuth2AuthorizationService authorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }
    @Bean
    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/auth-api/v1/recover-account")
                .requestMatchers("/auth-api/v1/register")
                .requestMatchers("/auth-api/v1/user/verify/**")
                .requestMatchers("/auth-api/v1/user/activate/**")
                .requestMatchers("/auth-api/v1/user/is-active/**")
                .requestMatchers("/auth-api/v1/debug/authorizations");
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http, RegisteredClientRepository registeredClientRepository) throws Exception {
        OAuth2AuthorizationServerConfigurer configurer = new OAuth2AuthorizationServerConfigurer();

        // 1. Define exactly which URLs this chain handles
        http
                .securityMatcher(configurer.getEndpointsMatcher())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .with(configurer, (authorizationServer) -> {
                    authorizationServer
                            .clientAuthentication(authentication -> {
                                // Use your custom converter and provider
                                authentication.authenticationConverter(new PublicClientRefreshTokenAuthenticationConverter());
                                authentication.authenticationProvider(new PublicClientRefreshProvider(registeredClientRepository));
                            })
                            .tokenGenerator(tokenGenerator(jwkSource()))
                            .oidc(Customizer.withDefaults());
                })
                // 2. Remove ANY custom requestMatchers here.
                // The configurer handles /oauth2/* endpoints automatically.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                // 3. This tells Spring: "If you hit /oauth2/authorize and aren't logged in, go to /login"
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/login"),
                                new org.springframework.security.web.util.matcher.MediaTypeRequestMatcher(org.springframework.http.MediaType.TEXT_HTML)
                        )
                );
        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
        http
                // This chain ONLY looks at requests starting with /auth-api/v1/private/
                .securityMatcher("/auth-api/v1/private/**","/auth-api/v1/change-password")
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated() // All endpoints in this controller need a JWT
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())// Use JWT validation
                )
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(request ->
                                !request.getRequestURI().startsWith("/auth-api/v1/private/") &&
                                !request.getRequestURI().startsWith("/oauth2/") &&
                                !request.getRequestURI().contains(".well-known")
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // FIX 2: Disable CSRF so Angular can POST to /login without a token
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/auth-api/v1/**", "/login")
                        .disable()
                )

                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("frame-ancestors 'self' "+angularBaseUri))
                )
                // FIX 1: Enable CORS for the login and verification endpoints

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/login", "/auth-api/v1/user/**").permitAll()
                        .requestMatchers("/auth-api/v1/register", "/auth-api/v1/recover-account").permitAll()
                        .requestMatchers("/auth-api/v1/recover-account", "/auth-api/v1/register").permitAll()
                        .requestMatchers("/auth-api/v1/user/verify/**", "/auth-api/v1/user/activate/**").permitAll()
                        .requestMatchers("/auth-api/v1/user/is-active/**").permitAll()
                        .anyRequest().authenticated()
                )
                // ADD THIS BLOCK: This is the "Kill Switch" for the 302 Redirect
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                (request, response, authException) -> {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.setContentType("application/json");
                                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"API access denied\"}");
                                },
                                new MediaTypeRequestMatcher(MediaType.APPLICATION_JSON)
                        )
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
        var angularClient = RegisteredClient.withId("obtb-client-001")
                .clientId("obtb-client-001")
                .clientName("obtb-angular-client-001")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .redirectUri(angularBaseUri + "/login/callback")
                .redirectUri(angularBaseUri + "/silent-refresh.html")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("offline_access")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(true) // PKCE is mandatory for public clients
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofDays(1))
                        .refreshTokenTimeToLive(Duration.ofDays(30)) // Set your desired duration
                        .reuseRefreshTokens(false) // ENABLE ROTATION: Old token becomes invalid after use
                        .build())
                .build();

        var authClient = RegisteredClient.withId("obtb-client-002")
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

        var notificationClient = RegisteredClient.withId("obtb-client-003")
                .clientId("obtb-client-003")
                .clientName("obtb-notification-client-003")
                .clientSecret("{noop}obtb-notification-client-003")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .build();

        var userserviceClient = RegisteredClient.withId("obtb-client-004")
                .clientId("obtb-client-004")
                .clientName("obtb-userservice-client-004")
                .clientSecret("{noop}obtb-userservice-client-004")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .build();
        var transactionServiceClient = RegisteredClient.withId("obtb-client-005")
                .clientId("obtb-client-005")
                .clientName("obtb-transaction-service-client-005")
                .clientSecret("{noop}obtb-transaction-service-client-005")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .build();
        var busServiceClient = RegisteredClient.withId("obtb-client-006")
                .clientId("obtb-client-006")
                .clientName("obtb-BusService-client-006")
                .clientSecret("{noop}obtb-BusService-client-006")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .build();
        var apiGatewayClient = RegisteredClient.withId("obtb-api-gateway")
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

        return new InMemoryRegisteredClientRepository( angularClient,
                authClient,
                notificationClient,
                apiGatewayClient,
                userserviceClient,
                transactionServiceClient,
                busServiceClient);
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
    public NimbusJwtEncoder  jwtEncoder() {
        return new NimbusJwtEncoder(jwkSource());
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("obtb-auth-key-id") // Use a STATIC ID for development
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource); // For verifying JWTs
    }


    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType()) ||
                    "id_token".equals(context.getTokenType().getValue())) {

                Authentication principal = context.getPrincipal();
                context.getClaims().claims((claims) -> {
                    // 1. Get authorities for BOTH Auth Code and Client Credentials
                    Set<String> roles = AuthorityUtils.authorityListToSet(principal.getAuthorities());

                    // 2. IMPORTANT: Change "authority" to "roles" to match your ResourceServerConfig
                    claims.put("roles", roles);

                    // 3. User-specific details (only if it's a User Principal, not a Client)
                    if(principal.getPrincipal() instanceof PrincipleUser customUser) {
                        claims.put("username", customUser.getUsername());
                        claims.put("userId", customUser.getUserId().toString());
                        claims.put("roleMappingId", customUser.getRoleMappingId().toString());
                    }
                });
            }
        };
    }
    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(JWKSource<SecurityContext> jwkSource) {
        // 1. JWT Generator (Uses your customizer for Access/ID tokens)
        JwtGenerator jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource));
        jwtGenerator.setJwtCustomizer(jwtTokenCustomizer());

        // 2. Refresh Token Generator (Forces issuance for Angular)
        OAuth2TokenGenerator<OAuth2RefreshToken> refreshTokenGenerator = new CustomOauth2RefreshTokenGenerator();

        // 3. Combine them
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, refreshTokenGenerator);
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