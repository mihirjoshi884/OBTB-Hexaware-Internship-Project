package org.hexaware.oauthservice.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter @Setter @NoArgsConstructor
public class AuthUserCreationRequest {

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String username;
    @Email @NotBlank
    private String email;
    @NotBlank
    private String contact;
    @NotBlank
    private String roleName;
    @NotBlank @Size(min = 8)
    private String password;
    @NotBlank
    private List<SecurityAnswerDTO> SecQA;

}
