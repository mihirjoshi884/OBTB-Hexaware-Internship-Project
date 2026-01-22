package org.hexaware.oauthservice.configuration.customizers;

import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.Map;

public class PublicClientRefreshTokenAuthentication extends OAuth2ClientAuthenticationToken {
    public PublicClientRefreshTokenAuthentication(String clientId) {
        super(clientId, ClientAuthenticationMethod.NONE, null, null);
    }

    public PublicClientRefreshTokenAuthentication(RegisteredClient registeredClient) {
        super(registeredClient, ClientAuthenticationMethod.NONE, null);
    }
}
