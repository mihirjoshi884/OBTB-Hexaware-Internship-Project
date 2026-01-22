package org.hexaware.userservice.mappers;

import org.hexaware.userservice.dtos.UpdateUserRequest;
import org.hexaware.userservice.dtos.UserCreationRequest;
import org.hexaware.userservice.dtos.UserSummary;
import org.hexaware.userservice.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /**
     * Maps UserCreationRequest DTO to User Entity
     */
    public User toUser(UserCreationRequest request) {
        if (request == null) return null;

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setContact(request.getContact());

        return user;
    }

    /**
     * Maps User Entity to UserSummary DTO
     */
    public UserSummary toUserSummary(User user) {
        if (user == null) return null;

        UserSummary summary = new UserSummary();
        summary.setUserId(user.getUserId());
        summary.setFirstName(user.getFirstName());
        summary.setLastName(user.getLastName());
        summary.setUsername(user.getUsername());
        summary.setEmail(user.getEmail());


        return summary;
    }

    /**
     * Updates an existing User Entity from an UpdateUserRequest
     */
    public void updateUserFromRequest(UpdateUserRequest request, User user) {
        if (request == null || user == null) return;

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setContact(request.getContact());
        user.setUsername(request.getUsername());
    }
}
