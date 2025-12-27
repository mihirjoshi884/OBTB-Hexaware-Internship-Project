package org.hexaware.oauthservice.services;

import org.hexaware.oauthservice.dtos.UserStatusResponse;
import org.hexaware.oauthservice.entites.AuthIdentity;
import org.hexaware.oauthservice.repositories.AuthIdentityRepository;
import org.hexaware.oauthservice.repositories.SecurityQuestionRepository;
import org.hexaware.oauthservice.repositories.UserLockOutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private AuthIdentityRepository authIdentityRepository;
    @Autowired
    private SecurityQuestionRepository securityQuestionRepository;
    @Autowired
    private UserLockOutRepository userLockOutRepository;

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

}
