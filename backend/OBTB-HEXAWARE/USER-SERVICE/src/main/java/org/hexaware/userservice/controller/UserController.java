package org.hexaware.userservice.controller;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.hexaware.userservice.dtos.*;
import org.hexaware.userservice.exceptions.UserNotFoundException;
import org.hexaware.userservice.services.UserService;
import org.hexaware.userservice.services.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.management.relation.RoleNotFoundException;
import java.util.UUID;

@RestController
@RequestMapping("/user-api/v1")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreationRequest user) throws RoleNotFoundException {

        UserSummary saved = userService.addUser(user);
        return ResponseEntity.ok().body(saved);
    }
    // /user-api/v1/update-user/{username}
    @PutMapping(value = "/update-user/{username}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUserProfile(@Valid
                                        @PathVariable String username,
                                        @RequestPart(value = "profileImage", required = false) MultipartFile file,
                                        @RequestPart(value = "updatedFields") String updateFieldsJson) throws JsonProcessingException, FileUploadException {
        ObjectMapper objectMapper = new ObjectMapper();
        UpdateUserRequest updateRequest = objectMapper.readValue(updateFieldsJson, UpdateUserRequest.class);

        ResponseDto<UserDashboardSummary> response =  userService.updateUser(username,file, updateRequest);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUser(@Valid @PathVariable UUID userId) throws RoleNotFoundException {
        return ResponseEntity.ok().body(userService.getUser(userId));
    }

    @GetMapping("/user-role/{roleMappingId}")
    public ResponseEntity<String> getUserRole(@PathVariable UUID roleMappingId) throws RoleNotFoundException {
        return ResponseEntity.ok(userService.getUserRole(roleMappingId));
    }

    @GetMapping("/fetch-user-email/{username}")
    public ResponseEntity<ResponseDto> getUserEmail(@PathVariable String username) throws UserNotFoundException {
        String email = userService.getEmail(username);

        ResponseDto response = new ResponseDto(
                email,HttpStatus.SC_OK,
                "Successfully fetched user email"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-info/email/{userId}")
    public ResponseEntity<?> getUserEmail(@PathVariable UUID userId) throws UserNotFoundException {
        return ResponseEntity.ok(userService.getEmail(userId));
    }

    @GetMapping("/dashboard/{username}")
    public ResponseEntity<?> getDashboardSummary(@PathVariable String username)
            throws RoleNotFoundException {

        // Call the service method we refined
        ResponseDto<UserDashboardSummary> response = userService.getUserDashboardSummary(username);

        // Return the response with the status code defined in the DTO
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    //http://localhost:8082/user-api/v1/add-funds/{username}
    //http://localhost:9090/user/user-api/v1/add-funds/{username}
    @PutMapping("/add-funds/{username}")
    public ResponseEntity<?> addFundsToWallet(@PathVariable String username,
                                                                         @RequestBody Double amount){
        ResponseDto<FundsSummaryDto> results = userService.addFunds(username,amount);
        return ResponseEntity.status(results.getStatus()).body(results); // Return 'results' instead of 'results.getBody()'
    }


}
