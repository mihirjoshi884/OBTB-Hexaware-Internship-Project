package org.hexaware.oauthservice.controller;

import org.hexaware.oauthservice.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth-api/v1/private")
public class protectedApiControler {

    @Autowired
    private AccountService accountService;

    @GetMapping("/security-info/{username}")
    public ResponseEntity<?> getUserSecurityInfo(@PathVariable String username){
        return ResponseEntity.ok(accountService.getSecurityInfo(username));
    }
}
