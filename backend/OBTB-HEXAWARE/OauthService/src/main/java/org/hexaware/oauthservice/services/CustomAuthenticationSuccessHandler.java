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
import org.springframework.stereotype.Service;
import java.io.IOException;



@Service
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    AuthIdentityRepository authIdentityRepository;
    @Autowired
    UserLockOutRepository userLockOutRepository;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        var userId = ((PrincipleUser) authentication.getPrincipal()).getUserId();
        var userLockOut = userLockOutRepository.findById(userId).orElse(null);
        if(userLockOut == null){
            throw new ServletException("User not found");
        }else {
            if(userLockOut.getLoginCounter() == 0){
                return ;
            } else {
                userLockOut.setAttempt1(null);
                userLockOut.setAttempt2(null);
                userLockOut.setAttempt3(null);
                userLockOut.setLoginCounter(0);
                userLockOutRepository.save(userLockOut);
            }
        }

    }
}
