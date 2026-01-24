package org.hexaware.transactionservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class OauthClientConfig {
    @Value("${spring.security.oauth2.client.registration.client.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.client.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.OauthService.token-uri}")
    private String tokenUri;

    @Value("${oauth2.client.registration-key}")
    private String clientRegistrationKey;


    /**
     * FIX 1: Manually created ClientRegistrationRepository.
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(clientRegistrationKey)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tokenUri(tokenUri)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope(Arrays.asList("openid", "profile"))
                .build();

        return new SimpleClientRegistrationRepository(clientRegistration);
    }

    /**
     * FIX 2: Manually created OAuth2AuthorizedClientService.
     */
    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {
        return new SimpleAuthorizedClientService(clientRegistrationKey);
    }

    // The AuthorizedClientServiceOAuth2AuthorizedClientManager bean for M2M flow
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);

        manager.setAuthorizedClientProvider(authorizedClientProvider);

        // CRITICAL: Set the Principal name for M2M flow
        manager.setContextAttributesMapper(context ->
                Collections.singletonMap(
                        Principal.class.getName(),
                        new UsernamePasswordAuthenticationToken(
                                clientId,
                                null,
                                AuthorityUtils.NO_AUTHORITIES
                        )
                ));

        return manager;
    }

    // FINAL FIX: Reverted to the original filter class, enabled by the pom.xml change
    @Bean
    public WebClient TransactionServicedWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        oauth2.setDefaultClientRegistrationId(clientRegistrationKey);

        return WebClient.builder()
                .apply(oauth2.oauth2Configuration())
                .build();
    }

    // =========================================================================
    //  Custom Interface Implementations
    // =========================================================================

    /**
     * A basic in-memory implementation of ClientRegistrationRepository.
     */
    private static class SimpleClientRegistrationRepository implements ClientRegistrationRepository {
        private final Map<String, ClientRegistration> registrations;

        public SimpleClientRegistrationRepository(ClientRegistration clientRegistration) {
            this.registrations = Collections.singletonMap(clientRegistration.getRegistrationId(), clientRegistration);
        }

        @Override
        public ClientRegistration findByRegistrationId(String registrationId) {
            return registrations.get(registrationId);
        }
    }

    /**
     * A basic in-memory implementation of OAuth2AuthorizedClientService for M2M flow.
     */
    private static class SimpleAuthorizedClientService implements OAuth2AuthorizedClientService {
        private final Map<String, OAuth2AuthorizedClient> authorizedClients = new ConcurrentHashMap<>();
        private final String fixedClientRegistrationId;

        public SimpleAuthorizedClientService(String fixedClientRegistrationId) {
            this.fixedClientRegistrationId = fixedClientRegistrationId;
        }

        private String getKey(String clientRegistrationId, String principalName) {
            return clientRegistrationId + ":" + principalName;
        }

        @Override
        public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
            return (T) authorizedClients.get(getKey(clientRegistrationId, principalName));
        }

        @Override
        public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
            authorizedClients.put(getKey(fixedClientRegistrationId, principal.getName()), authorizedClient);
        }

        @Override
        public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
            authorizedClients.remove(getKey(clientRegistrationId, principalName));
        }
    }

}
