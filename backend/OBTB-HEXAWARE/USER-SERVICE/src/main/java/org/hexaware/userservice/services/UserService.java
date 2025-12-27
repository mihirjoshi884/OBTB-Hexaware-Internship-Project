package org.hexaware.userservice.services;


import org.hexaware.userservice.dtos.UserCreationRequest;
import org.hexaware.userservice.dtos.UserSummary;
import org.hexaware.userservice.entities.Role;
import org.hexaware.userservice.entities.RoleMapping;
import org.hexaware.userservice.entities.User;
import org.hexaware.userservice.enums.Roles;
import org.hexaware.userservice.exceptions.UserNotFoundException;
import org.hexaware.userservice.mappers.UserMapper;
import org.hexaware.userservice.repositories.RoleMappingRepository;
import org.hexaware.userservice.repositories.RoleRepository;
import org.hexaware.userservice.repositories.UserRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    RoleMappingRepository roleMappingRepository;
    @Autowired
    private UserMapper mapper;

    public UserSummary addUser(UserCreationRequest user) throws RoleNotFoundException {

        String roleName = user.getRoleName();
        Roles roleEnum = Roles.valueOf(roleName.toUpperCase());
        User toBeSaved = mapper.toUser(user);

        Role roleEntity = roleRepository.findByRole(roleEnum)
                .orElseThrow(() -> new RoleNotFoundException("Role not found in DB: " + roleEnum));
        User savedUser =  userRepository.save(toBeSaved);

        UUID roleId = roleEntity.getRoleid();

        RoleMapping roleMappingEntity = new RoleMapping();
        roleMappingEntity.setUserId(savedUser.getUserId());
        roleMappingEntity.setRoleId(roleId);
        var savedRoleMapping = roleMappingRepository.save(roleMappingEntity);
        var userSummary = mapper.toUserSummary(savedUser);
        userSummary.setRoleMappingId(savedRoleMapping.getRoleMappingId());
        return userSummary;
    }

    public String getUserRole(UUID roleMappingId) throws RoleNotFoundException {
        var mapping = roleMappingRepository.findById(roleMappingId);
        if(mapping.isEmpty()) {
            throw new RoleNotFoundException("Role not found in DB: " + roleMappingId);
        }
        else{
            var role = roleRepository.findById(mapping.get().getRoleId());
            if(role == null) {
                throw new RoleNotFoundException("Role not found in DB: " + roleMappingId);
            }
            return role.get().getRole().toString();
        }
    }
    public UserSummary getUser(UUID userId) throws RoleNotFoundException, UserNotFoundException {

        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        UserSummary fetchedUser = mapper.toUserSummary(userRepository.findByUserId(userId).get());

        // Fetch role mapping: repository returns Optional<RoleMapping>
        RoleMapping mapping = roleMappingRepository.findByUserId(userId)
                .orElseThrow(() -> new RoleNotFoundException("Role mapping not found for userId: " + userId));

        fetchedUser.setRoleMappingId(mapping.getRoleMappingId());
        return fetchedUser;
    }

    public String getEmail(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        return mapper.toUserSummary(userRepository.findByUserId(userId).get()).getEmail();
    }
}
