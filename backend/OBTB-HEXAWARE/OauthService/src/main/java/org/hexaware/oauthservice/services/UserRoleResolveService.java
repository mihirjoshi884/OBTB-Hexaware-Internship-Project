package org.hexaware.oauthservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hexaware.oauthservice.repositories.AuthIdentityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class UserRoleResolveService {

    private final AuthIdentityRepository authIdentityRepository;
    @Value("${oauth-client-clientId}")
    private String clientId;

    @Value("${oauth-client-clientSecret}")
    private String clientSecret;

    @Value("${userservice.base-uri}")
    private String userServiceBaseUrl;

    @Value("${authservice.base-uri}")
    private String authBaseUrl;

    @Autowired
    private  WebClient webClient;

    public UserRoleResolveService(AuthIdentityRepository authIdentityRepository) {
        this.authIdentityRepository = authIdentityRepository;
    }

    public List<String> resolveRoles(UUID roleMappingId) {

        // 1️⃣ Step-1: Acquire OAuth Token
        String tokenResponse = webClient.post()
                .uri(authBaseUrl + "/oauth2/token")
                .header("Authorization", basicAuthHeader())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=client_credentials&scope=openid profile")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        String accessToken = extractToken(tokenResponse);


        // 2️⃣ Step-2: Call user-role API — workaround applied here
        // Force IntelliJ to use the correct WebClient RequestHeadersSpec type
        WebClient.RequestHeadersSpec<?> requestSpec = (WebClient.RequestHeadersSpec<?>) webClient.get()
                .uri(userServiceBaseUrl + "/user-api/v1/user-role/" + roleMappingId)
                .header("Authorization", "Bearer " + accessToken);

        String roleResponse = requestSpec
                .retrieve()
                .bodyToMono(String.class)
                .block();


        return parseRoles(roleResponse);
    }

    private String basicAuthHeader() {
        String raw = clientId + ":" + clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private String extractToken(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(json).get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse access_token", e);
        }
    }


    private List<String> parseRoles(String roleResponse) {
        if (roleResponse == null || roleResponse.isBlank()) {
            return List.of();
        }

        // ✅ Case 1: Plain string (USER)
        if (!roleResponse.trim().startsWith("[")) {
            return List.of(roleResponse.trim());
        }

        // ✅ Case 2: JSON array (["USER", "ADMIN"])
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(roleResponse);
            List<String> roles = new ArrayList<>();
            node.forEach(r -> roles.add(r.asText()));
            return roles;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse roles JSON", e);
        }
    }

}
