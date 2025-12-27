package org.hexaware.oauthservice.services;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hexaware.oauthservice.entites.AuthIdentity;
import org.hexaware.oauthservice.entites.UserLockout;
import org.hexaware.oauthservice.repositories.AuthIdentityRepository;
import org.hexaware.oauthservice.repositories.UserLockOutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Transactional
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Autowired
    private UserLockOutRepository userLockOutRepository;

    @Autowired
    private AuthIdentityRepository authIdentityRepository;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        String username = request.getParameter("username");
        String message = "Invalid username or password";
        int status = HttpServletResponse.SC_UNAUTHORIZED;

        if (username == null || username.isBlank()) {
            send(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Username or password missing");
            return;
        }

        Optional<AuthIdentity> identityOptional =
                authIdentityRepository.findByUsername(username);

        // ✅ USER NOT FOUND
        if (identityOptional.isEmpty()) {
            send(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Either username or password is missing");
            return;
        }

        AuthIdentity identity = identityOptional.get();

        // ✅ ACCOUNT NOT VERIFIED
        if (!identity.is_Verified()) {
            send(response, HttpServletResponse.SC_FORBIDDEN,
                    "Please verify your account before logging in");
            return;
        }

        // ✅ ACCOUNT NOT ACTIVE
        if (!identity.is_Active()) {
            send(response, HttpServletResponse.SC_FORBIDDEN,
                    "Your account is not activated");
            return;
        }

        // ✅ ACCOUNT ACTIVE + VERIFIED → apply lockout logic
        userLockOutRepository.findByUserId(identity.getUserId())
                .ifPresent(lockout -> {

                    int count = lockout.getLoginCounter();
                    lockout.setLoginCounter(count + 1);
                    LocalDateTime now = LocalDateTime.now();

                    if (count == 0) {
                        lockout.setAttempt1(now);
                    } else if (count == 1) {
                        lockout.setAttempt2(now);
                    } else if (count == 2) {
                        lockout.setAttempt3(now);
                        lockout.setLocked(true);
                    }

                    userLockOutRepository.save(lockout);
                });

        send(response, HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid username or password");
    }

    private void send(HttpServletResponse response, int status, String message)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter()
                .write("{\"error\": \"" + message + "\"}");
    }
}
