package org.hexaware.userservice.controller;



import jakarta.validation.Valid;
import org.hexaware.userservice.dtos.UserCreationRequest;
import org.hexaware.userservice.dtos.UserSummary;
import org.hexaware.userservice.exceptions.UserNotFoundException;
import org.hexaware.userservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUser(@Valid @PathVariable UUID userId) throws RoleNotFoundException {
        return ResponseEntity.ok().body(userService.getUser(userId));
    }

    @GetMapping("/user-role/{roleMappingId}")
    public ResponseEntity<String> getUserRole(@PathVariable UUID roleMappingId) throws RoleNotFoundException {
        return ResponseEntity.ok(userService.getUserRole(roleMappingId));
    }
    @GetMapping("/hello-world")
    public ResponseEntity<?> getHelloWorld(){
        return ResponseEntity.ok().body("Hello World");
    }
    @GetMapping("/user-info/email/{userId}")
    public ResponseEntity<?> getUserEmail(@PathVariable UUID userId) throws UserNotFoundException {
        return ResponseEntity.ok(userService.getEmail(userId));
    }

}
