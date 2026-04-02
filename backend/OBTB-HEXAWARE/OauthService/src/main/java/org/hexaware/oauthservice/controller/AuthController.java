package org.hexaware.oauthservice.controller;

import org.hexaware.oauthservice.dtos.*;
import org.hexaware.oauthservice.entites.AuthIdentity;
import org.hexaware.oauthservice.entites.PrincipleUser;
import org.hexaware.oauthservice.services.AccountService;
import org.hexaware.oauthservice.services.AuthService;
import org.hexaware.oauthservice.services.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth-api/v1")
public class AuthController {

    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private AuthService authService;

    @Autowired
    private AccountService accountService;

//  http://localhost:8081/auth-api/v1/register
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthUserCreationRequest authUserCreationRequest) throws Exception {
        return  ResponseEntity.ok(registrationService.userRegistration(authUserCreationRequest));
    }
// http://localhost:8081/auth-api/v1/user/activate/{userId}
    @PatchMapping("/user/activate/{userId}")
    public ResponseEntity<?>  activateUser(@PathVariable UUID userId, HttpMethod httpMethod){
        var userResponse = authService.activateUser(userId);
        return ResponseEntity.ok(userResponse);
    }

    @PatchMapping("/user/verify/{userId}")
    public ResponseEntity<?> verifyUser(@PathVariable UUID userId){
        var userResponse = authService.verifyUser(userId);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/user/is-verified/{userId}")
    public ResponseEntity<?> isVerified(@PathVariable UUID userId){
        var userResponse = authService.isVerified(userId);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/user/is-active/{userId}")
    public ResponseEntity<?> isActive(@PathVariable UUID userId){
        var userResponse = authService.isActive(userId);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/user/get-current-user")
    public ResponseEntity<?> getPrincipal(
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt
    ) {
        // 🛡️ Safety check
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No valid token provided"));
        }

        // 1. Target the exact claim containing the UUID
        String userIdStr = jwt.getClaimAsString("userId");

        if (userIdStr == null || userIdStr.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "The 'userId' claim is missing from the token payload"));
        }

        // 2. Safely parse the UUID
        UUID userUuid;
        try {
            userUuid = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "The 'userId' claim is not a valid UUID format: " + userIdStr));
        }

        // 3. Fallback for username (your token uses the 'username' or 'sub' claim)
        String username = jwt.getClaimAsString("username");
        if (username == null || username.isBlank()) {
            username = jwt.getSubject(); // Backed by "aparnajoshi@1234"
        }

        // 4. Extract roles
        List<String> roles = jwt.getClaimAsStringList("roles");

        CurrentUserResponse response = new CurrentUserResponse(
                UUID.fromString(userIdStr),
                username,
                true, // enabled
                true, // account non-locked
                roles != null ? roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
                        : List.of() // Fallback to empty list if null
        );


        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt
    ) {
        // 1. Verify the request has the email
        // 2. Call the service with the extracted parameters
        authService.changePassword(request, jwt, request.getEmail());

        return ResponseEntity.ok(Map.of(
                "status", "Success",
                "message", "Your password has been updated successfully."
        ));
    }

    @PostMapping("/recover-account")
    public ResponseEntity<?> recoverAccount(@RequestBody RecoveryAccountRequestDto request){
        String username = request.getUsername();
        String email = request.getEmail();
        return ResponseEntity.ok(accountService.publishAccountRecovery(username,email));
    }

    //http://localhost:9090/auth/auth-api/v1/security-info/{username}

}
