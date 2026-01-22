package org.hexaware.oauthservice.services;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.hexaware.oauthservice.entites.PrincipleUser;
import org.hexaware.oauthservice.repositories.AuthIdentityRepository;
import org.hexaware.oauthservice.repositories.UserLockOutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.Instant;


@Service
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    UserLockOutRepository userLockOutRepository;
    @Autowired
    AuthIdentityRepository authIdentityRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        var userId = ((PrincipleUser) authentication.getPrincipal()).getUserId();


        // Use findByUserId instead of findById
        userLockOutRepository.findByUserId(userId).ifPresent(lockout -> {
            if (lockout.getLoginCounter() > 0) {
                lockout.setAttempt1(null);
                lockout.setAttempt2(null);
                lockout.setAttempt3(null);
                lockout.setLoginCounter(0);
                lockout.setLocked(false);
                userLockOutRepository.save(lockout);
            }
        });

        authIdentityRepository.findByUserId(userId).ifPresent(auth -> {
            // Assuming you add 'lastLoginSuccess' to AuthIdentity entity
            auth.setLastLoginSuccess(java.time.Instant.now());
            authIdentityRepository.save(auth);
        });

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\": \"SUCCESS\"}");
        response.getWriter().flush();
    }
}
