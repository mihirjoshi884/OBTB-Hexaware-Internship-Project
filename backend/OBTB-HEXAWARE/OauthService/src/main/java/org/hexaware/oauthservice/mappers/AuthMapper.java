package org.hexaware.oauthservice.mappers;


import org.hexaware.oauthservice.dtos.AuthUserCreationRequest;
import org.hexaware.oauthservice.dtos.Summary;
import org.hexaware.oauthservice.dtos.UserCreationRequest;
import org.hexaware.oauthservice.dtos.UserSummary;
import org.hexaware.oauthservice.entites.AuthIdentity;
import org.springframework.stereotype.Component;


@Component
public class AuthMapper {

    /**
     * Maps AuthUserCreationRequest (Initial Registration) to AuthIdentity Entity
     */
    public AuthIdentity toAuthIdentity(AuthUserCreationRequest request) {
        if (request == null) return null;

        AuthIdentity auth = new AuthIdentity();
        auth.setUsername(request.getUsername());
        // Note: hashPassword, userId, and roleMappingId are typically
        // set in the Service after hashing and calling User-Service
        return auth;
    }

    /**
     * Maps UserSummary (from User-Service response) to AuthIdentity
     */
    public AuthIdentity toAuthIdentity(UserSummary summary) {
        if (summary == null) return null;

        AuthIdentity auth = new AuthIdentity();
        auth.setUserId(summary.getUserId());
        auth.setRoleMappingId(summary.getRoleMappingId());
        auth.setUsername(summary.getUsername());
        auth.set_Active(true);
        auth.set_Verified(false); // Default state for new users
        return auth;
    }

    /**
     * Extracts common User info from the specific Auth request
     * to send to the User-Service
     */
    public UserCreationRequest toUserCreationRequest(AuthUserCreationRequest request) {
        if (request == null) return null;

        UserCreationRequest userReq = new UserCreationRequest();
        userReq.setFirstName(request.getFirstName());
        userReq.setLastName(request.getLastName());
        userReq.setUsername(request.getUsername());
        userReq.setEmail(request.getEmail());
        userReq.setContact(request.getContact());
        userReq.setRoleName(request.getRoleName());
        return userReq;
    }

    /**
     * Maps the internal AuthIdentity entity to a lightweight Summary DTO
     */
    public Summary toSummary(AuthIdentity auth) {
        if (auth == null) return null;

        Summary summary = new Summary();
        summary.setUserId(auth.getUserId());
        summary.setRoleMappingId(auth.getRoleMappingId());
        summary.setUsername(auth.getUsername());
        return summary;
    }

    /**
     * Maps UserSummary to a basic Summary
     */
    public Summary toSummary(UserSummary userSummary) {
        if (userSummary == null) return null;

        Summary summary = new Summary();
        summary.setUserId(userSummary.getUserId());
        summary.setRoleMappingId(userSummary.getRoleMappingId());
        summary.setUsername(userSummary.getUsername());
        return summary;
    }
}
