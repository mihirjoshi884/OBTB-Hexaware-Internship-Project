package org.hexaware.userservice.services.impl;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.hexaware.userservice.dtos.*;
import org.hexaware.userservice.entities.Role;
import org.hexaware.userservice.entities.RoleMapping;
import org.hexaware.userservice.entities.User;
import org.hexaware.userservice.entities.Wallet;
import org.hexaware.userservice.enums.Roles;
import org.hexaware.userservice.exceptions.RoleNotFoundException;
import org.hexaware.userservice.exceptions.UserNotFoundException;
import org.hexaware.userservice.mappers.UserMapper;
import org.hexaware.userservice.repositories.RoleMappingRepository;
import org.hexaware.userservice.repositories.RoleRepository;
import org.hexaware.userservice.repositories.UserRepository;
import org.hexaware.userservice.repositories.WalletRepository;
import org.hexaware.userservice.services.ImageUploadService;
import org.hexaware.userservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;


import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    RoleMappingRepository roleMappingRepository;
    @Autowired
    private UserMapper mapper;
    @Autowired
    private WebClient userservicedWebClient;
    @Value("${authservice.base-uri}")
    private String authServiceBaseUri;
    @Autowired
    private ImageUploadService imageUploadService;
    @Autowired
    private WalletRepository walletRepository;

    @Override
    @Transactional
    public UserSummary addUser(UserCreationRequest user) throws RoleNotFoundException {
        Roles roleEnum = Roles.valueOf(user.getRoleName().toUpperCase());
        User toBeSaved = mapper.toUser(user);

        // Set up Wallet properly before saving
        Wallet wallet = new Wallet();
        wallet.setBalance(0.0);
        toBeSaved.setWallet(wallet); // Bidirectional link

        Role roleEntity = roleRepository.findByRole(roleEnum)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleEnum));

        // Save once; CascadeType.ALL handles the wallet
        User savedUser = userRepository.save(toBeSaved);

        // Update wallet with the generated UserId
        savedUser.getWallet().setUserId(savedUser.getUserId());

        RoleMapping roleMappingEntity = new RoleMapping();
        roleMappingEntity.setUserId(savedUser.getUserId());
        roleMappingEntity.setRoleId(roleEntity.getRoleid());
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

    public String getEmail(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        return userRepository.findByUsername(username)
                .map(user -> mapper.toUserSummary(user).getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional
    public ResponseDto<FundsSummaryDto> addFunds(String username, Double amount) {
        // Fetch user and wallet through relationship
        var fetchedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = fetchedUser.getWallet();
        Double newBalance = wallet.getBalance() + amount;

        wallet.setBalance(newBalance);

        // Save user (cascades to wallet)
        userRepository.save(fetchedUser);

        FundsSummaryDto summaryDto = new FundsSummaryDto(username, newBalance);
        return new ResponseDto<>(summaryDto, 200, "Funds added successfully");
    }

    public ResponseDto<UserDashboardSummary> getUserDashboardSummary(String username) throws RoleNotFoundException {
        var securityInfo = getAuthSecurityResponse(username);
        var user = userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("User not found with username: " + username));
        var role = getUserRole(roleMappingRepository.findByUserId(user.getUserId()).get().getRoleMappingId());
        var response = new ResponseDto();
        var userDashBoard =  new UserDashboardSummary(
          user.getUsername(),
          user.getFirstName(),
          user.getLastName(),
          role,
          user.getGender(),
          user.getDateOfBirth(),
          user.getWallet().getBalance(),
          user.getContact(),
          user.getEmail(),
          user.getProfilePictureUrl(),
          user.getCreatedAt(),
          securityInfo != null ? securityInfo.passwordLastUpdated() : null,
          securityInfo!=null ? securityInfo.lastlogin() : null
        );
        response.setBody(userDashBoard);
        response.setStatus(200);
        response.setMessage("User dashboard summary");
        return response;
    }

    @Override
    @Transactional // Added for safety during profile updates
    public ResponseDto<UserDashboardSummary> updateUser(String username, MultipartFile file, UpdateUserRequest updateRequest) throws FileUploadException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setDateOfBirth(LocalDate.parse(updateRequest.getDateOfBirth()));
        user.setGender(updateRequest.getGender());
        user.setContact(updateRequest.getContact());

        if (file != null && !file.isEmpty()) {
            try {
                // Pass existing profilePictureId to allow the service to replace the old image
                Map result = imageUploadService.uploadImage(file, username, user.getProfilePictureId());
                user.setProfilePictureId((String) result.get("public_id"));
                user.setProfilePictureUrl((String) result.get("secure_url"));
            } catch (Exception ex) {
                throw new FileUploadException("Image upload failed: " + ex.getMessage());
            }
        }

        userRepository.save(user);
        return getUserDashboardSummary(username);
    }

    private AuthSecurityResponse getAuthSecurityResponse(String username) {
        return userservicedWebClient.get()
                .uri(authServiceBaseUri+"/auth-api/v1/private/security-info/{username}",username)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseDto<AuthSecurityResponse>>() {})
                .map(ResponseDto::getBody)
                .block();

    }
}
