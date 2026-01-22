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
import org.hexaware.userservice.services.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.management.relation.RoleNotFoundException;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleMappingRepository roleMappingRepository;
    @Mock
    private UserMapper mapper;

    // Shared Data Fields
    private UserCreationRequest userCreationRequest;
    private User mockUser;
    private UserSummary mockUserSummary;
    private RoleMapping mockRoleMapping;
    private Role mockRole;
    private UUID userId;
    private UUID roleId;
    private UUID mappingId;

    @BeforeEach
    public void setUp() {
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        mappingId = UUID.randomUUID();

        userCreationRequest = new UserCreationRequest();
        userCreationRequest.setUsername("johndoe");
        userCreationRequest.setEmail("john@email.com");
        userCreationRequest.setRoleName("CUSTOMER");
        userCreationRequest.setFirstName("John");
        userCreationRequest.setLastName("Doe");

        mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setUsername(userCreationRequest.getUsername());
        mockUser.setEmail(userCreationRequest.getEmail());

        mockRole = new Role();
        mockRole.setRoleid(roleId);
        mockRole.setRole(Roles.CUSTOMER);

        mockRoleMapping = new RoleMapping();
        mockRoleMapping.setRoleMappingId(mappingId);
        mockRoleMapping.setUserId(userId);
        mockRoleMapping.setRoleId(roleId);

        mockUserSummary = new UserSummary();
        mockUserSummary.setUserId(userId);
        mockUserSummary.setUsername(userCreationRequest.getUsername());
        mockUserSummary.setEmail(userCreationRequest.getEmail());
    }

    // --- SUCCESS TESTS ---

    @Test
    @DisplayName("Add User: Success path")
    public void testAddUser_Success() throws RoleNotFoundException {
        when(mapper.toUser(any(UserCreationRequest.class))).thenReturn(mockUser);
        when(roleRepository.findByRole(Roles.CUSTOMER)).thenReturn(Optional.of(mockRole));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(roleMappingRepository.save(any(RoleMapping.class))).thenReturn(mockRoleMapping);
        when(mapper.toUserSummary(any(User.class))).thenReturn(mockUserSummary);

        UserSummary result = userServiceImpl.addUser(userCreationRequest);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(mappingId, result.getRoleMappingId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Get User Role: Success path")
    public void testGetUserRole_Success() throws RoleNotFoundException {
        when(roleMappingRepository.findById(mappingId)).thenReturn(Optional.of(mockRoleMapping));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(mockRole));

        String role = userServiceImpl.getUserRole(mappingId);

        Assertions.assertEquals("CUSTOMER", role);
    }

    @Test
    @DisplayName("Get User Summary: Success path")
    public void testGetUser_Success() throws RoleNotFoundException, UserNotFoundException {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(mockUser));
        when(mapper.toUserSummary(mockUser)).thenReturn(mockUserSummary);
        when(roleMappingRepository.findByUserId(userId)).thenReturn(Optional.of(mockRoleMapping));

        UserSummary result = userServiceImpl.getUser(userId);

        Assertions.assertEquals(userId, result.getUserId());
        Assertions.assertEquals(mappingId, result.getRoleMappingId());
    }

    @Test
    @DisplayName("Get Email: Success path")
    public void testGetEmail_Success() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(mockUser));
        when(mapper.toUserSummary(mockUser)).thenReturn(mockUserSummary);

        String email = userServiceImpl.getEmail(userId);

        Assertions.assertEquals("john@email.com", email);
    }

    // --- NEGATIVE & VALIDATION TESTS ---

    @Test
    @DisplayName("Add User: Should throw RoleNotFoundException when role is missing in DB")
    public void testAddUser_RoleNotFound() {
        when(roleRepository.findByRole(any())).thenReturn(Optional.empty());

        Assertions.assertThrows(RoleNotFoundException.class, () -> {
            userServiceImpl.addUser(userCreationRequest);
        });
    }

    @Test
    @DisplayName("Get User: Should throw IllegalArgumentException for null ID")
    public void testGetUser_NullId() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userServiceImpl.getUser(null);
        });
    }

    @Test
    @DisplayName("Get User: Should throw RoleNotFoundException if RoleMapping is missing")
    public void testGetUser_MappingMissing() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(mockUser));
        when(mapper.toUserSummary(mockUser)).thenReturn(mockUserSummary);
        when(roleMappingRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Assertions.assertThrows(RoleNotFoundException.class, () -> {
            userServiceImpl.getUser(userId);
        });
    }

//    @Test
//    @DisplayName("Get Email: Should throw IllegalArgumentException for null ID")
//    public void testGetEmail_NullId() {
//        Assertions.assertThrows(IllegalArgumentException.class, () -> {
//            userService.getEmail(null);
//        });
//    }
}