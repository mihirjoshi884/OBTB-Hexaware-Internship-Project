package org.hexaware.oauthservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth-api/v1/debug")
public class DebugController {

    @Autowired
    private OAuth2AuthorizationService authorizationService;
    //http://localhost:8081/auth-api/v1/debug/authorizations
    @GetMapping("/authorizations")
    public ResponseEntity<List<String>> listActiveAuthorizations() {
        // Since the default InMemory service doesn't have a 'findAll',
        // this is for architectural verification.
        // In a real database, you'd run: SELECT * FROM oauth2_authorization;
        return ResponseEntity.ok(List.of("Service is active and tracking tokens."));
    }
}