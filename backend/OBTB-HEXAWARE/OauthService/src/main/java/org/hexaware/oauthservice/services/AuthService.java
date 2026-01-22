package org.hexaware.oauthservice.services;

import jakarta.transaction.Transactional;
import org.hexaware.oauthservice.dtos.*;
import org.hexaware.oauthservice.entites.AuthIdentity;
import org.hexaware.oauthservice.entites.RecoveryBundle;
import org.hexaware.oauthservice.entites.SecurityQuestion;
import org.hexaware.oauthservice.exceptions.ExpiredTokenException;
import org.hexaware.oauthservice.exceptions.InvalidTokenException;
import org.hexaware.oauthservice.exceptions.SecurityQuestionsMismatchException;
import org.hexaware.oauthservice.exceptions.UserDoesNotExistException;
import org.hexaware.oauthservice.repositories.AuthIdentityRepository;
import org.hexaware.oauthservice.repositories.SecurityQuestionRepository;
import org.hexaware.oauthservice.repositories.UserLockOutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private KafkaAdmin kafkaAdmin;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthIdentityRepository authIdentityRepository;
    @Autowired
    private SecurityQuestionRepository securityQuestionRepository;
    @Autowired
    private UserLockOutRepository userLockOutRepository;
    @Autowired
    private JwtEncoder jwtEncoder;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private AccountService accountService;

    private Logger logger = LoggerFactory.getLogger(AuthService.class);

    public UserStatusResponse activateUser(UUID userId){
        Optional<AuthIdentity> auth = authIdentityRepository.findByUserId(userId);
        if(auth.isPresent()) {
            AuthIdentity identity = auth.get();
            identity.set_Active(true);
            var savedAuth = authIdentityRepository.save(identity);
            return new UserStatusResponse(
                    savedAuth.getUserId(),
                    savedAuth.getUsername(),
                    savedAuth.is_Active(),
                    savedAuth.is_Verified(),
                    "user with userId:\t"+identity.getUserId()+
                    "\t and username:\t"+identity.getUsername()+
                    "is:\t"+identity.is_Active()+"(activated)");
        }
        else throw new RuntimeException("something went wrong");
    }

    public UserStatusResponse verifyUser(UUID userId) {
        Optional<AuthIdentity> auth = authIdentityRepository.findByUserId(userId);
        if(auth.isPresent()) {
            AuthIdentity identity = auth.get();
            identity.set_Verified(true);
            var savedAuth= authIdentityRepository.save(identity);
            return new UserStatusResponse(
                    savedAuth.getUserId(),
                    savedAuth.getUsername(),
                    savedAuth.is_Active(),
                    savedAuth.is_Verified(),
                    "user with userId:\t"+identity.getUserId()+"\t and username:\t"+identity.getUsername()+
                            "is:\t"+identity.is_Verified()+"(verified)");
        }
        else throw new RuntimeException("something went wrong");
    }

    public boolean isVerified(UUID userId) {
        Optional<AuthIdentity> auth = authIdentityRepository.findByUserId(userId);
        if(auth.isPresent()) {
            AuthIdentity identity = auth.get();
            return identity.is_Verified();
        }else throw new RuntimeException("user not found with userId:\t"+userId);
    }

    public boolean isActive(UUID userId) {
        Optional<AuthIdentity> auth = authIdentityRepository.findByUserId(userId);
        if(auth.isPresent()) {
            AuthIdentity identity = auth.get();
            return identity.is_Active();
        }else throw new RuntimeException("user not found with userId:\t"+userId);
    }

    public String generateRecoveryToken(String username){
        Optional<AuthIdentity> auth = authIdentityRepository.findByUsername(username);
        if(!auth.isPresent()) {
            throw new RuntimeException("user with username:\t"+username+"user does not exist");
        }
        AuthIdentity identity = auth.get();
        Instant now = Instant.now();
        long expiry = 900L; // 15 minutes
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("http://localhost:8081")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(username)
                .claim("purpose","AccountRecovery")
                .claim("username", username)
                .claim("v", identity.getHashPassword())
                .build();


        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

    }

    private boolean validateOneTimeToken(Jwt jwt, AuthIdentity identity) {
        // 1. Verify the 'purpose' claim matches your generation logic ("AccountRecovery")
        String purpose = jwt.getClaim("purpose");
        if (!"AccountRecovery".equals(purpose)) {
            throw new InvalidTokenException("This token was not issued for account recovery.");
        } else {
            // 2. Extract the hash version from the token
            String tokenHashVersion = jwt.getClaim("v");

            // 3. Compare with the current hash in the database
            // If the user already changed their password, identity.getHashPassword()
            // will be different from the one embedded in this JWT.
            if (tokenHashVersion == null || !tokenHashVersion.equals(identity.getHashPassword())) {
                throw new ExpiredTokenException("This recovery link is no longer valid or has already been used.");
            }
        }
        return true;
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, Jwt jwt, String email) {
        AuthIdentity userIdentity = authIdentityRepository.findByUsername(jwt.getSubject())
                .orElseThrow(() -> new UserDoesNotExistException("User " + jwt.getSubject() + " not found"));

        // Check if this is a recovery flow by looking at the token claims
        // Check for the "purpose" claim you set in generateRecoveryToken
        boolean isRecovery = "AccountRecovery".equals(jwt.getClaim("purpose"));

        if (isRecovery) {
            // Only validate as a one-time token if it's meant for recovery
            validateOneTimeToken(jwt, userIdentity);
        }

        SecurityQuestion storedEntity = securityQuestionRepository.findByUserId(userIdentity.getUserId())
                .orElseThrow(() -> new RuntimeException("Security questions not found"));

        // MFA check is required for both flows
        verifySecurityQuestions(request.getSecurityVerification(), storedEntity);

        // 2. Logic for Point #2: Bypass current password for Recovery
        if (!isRecovery) {
            // Standard Change Password: MUST provide and match old password
            if (request.getCurrentPassword() == null ||
                    !passwordEncoder.matches(request.getCurrentPassword(), userIdentity.getHashPassword())) {
                throw new BadCredentialsException("Current password verification failed.");
            }
        }

        // Update password
        userIdentity.setHashPassword(passwordEncoder.encode(request.getNewPassword()));
        userIdentity.setPasswordUpdatedAt(Instant.now());
        authIdentityRepository.save(userIdentity);

        sendPasswordChangeNotification("producer_oauth.urgent.password_change", userIdentity, email);
    }

    // FIX: Parameter type changed to List<SecurityAnswerDTO> to resolve the IDE error
    private void verifySecurityQuestions(List<SecurityAnswerDTO> providedAnswers, SecurityQuestion storedEntity) {
        Set<String> storedHashes = Set.of(
                storedEntity.getQuestionAndAnswer1(),
                storedEntity.getQuestionAndAnswer2()
        );

        long matchCount = providedAnswers.stream()
                .map(item -> {
                    try {
                        // MUST match Registration exactly: trim + lowercase
                        String q = item.getQuestion().trim().toLowerCase();
                        String a = item.getAnswer().trim().toLowerCase();

                        return registrationService.createHash(q + a);
                    } catch (Exception e) {
                        throw new RuntimeException("Hashing failed", e);
                    }
                })
                .filter(storedHashes::contains)
                .count();

        if (matchCount < 2) {
            throw new SecurityQuestionsMismatchException("Security answers do not match.");
        }
    }
    private void sendPasswordChangeNotification(String topic, AuthIdentity identity, String email) {
        try {
            Summary summary = new Summary();
            summary.setUsername(identity.getUsername());
            summary.setEmail(email); // Already available here!
            summary.setUserId(identity.getUserId());

            Map<String, Object> payload = new HashMap<>();
            payload.put("user-information", summary);


            // Create the event with the full payload
            NotificationEvent<Map<String, Object>> notificationEvent = new NotificationEvent<>();
            notificationEvent.setData(payload);
            notificationEvent.setRecipientIdentifier(summary.getUsername());
            notificationEvent.setTimestamp(java.time.Instant.now().toEpochMilli());

            String message = objectMapper.writeValueAsString(notificationEvent);
            kafkaTemplate.send(topic, message);
            logger.info("Password change notification sent to " + email);
        } catch(Exception e) {
            logger.error("Error sending password change notification to " + email, e);
        }
    }
}
