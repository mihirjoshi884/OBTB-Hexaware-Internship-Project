package org.hexaware.oauthservice.configuration.customizers;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

public class PublicClientRefreshTokenAuthenticationConverter implements AuthenticationConverter {
    @Override
    public @Nullable Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if(!grantType.equals(OAuth2ParameterNames.REFRESH_TOKEN)) {
            return null;
        }

        String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
        if(!StringUtils.hasText(clientId)) {
            return null;
        }

        return new PublicClientRefreshTokenAuthentication(clientId);
    }
}
