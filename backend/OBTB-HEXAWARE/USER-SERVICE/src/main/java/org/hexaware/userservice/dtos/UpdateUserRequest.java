package org.hexaware.userservice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class UpdateUserRequest {
    // Identity Fields (Read-only in UI, but sent for validation)
    @NotBlank
    private String username;
    @NotBlank @Email
    private String email;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;

    // Editable Fields
    @NotBlank
    private String contact;
    @NotBlank
    private String gender;
    @NotBlank
    private String dateOfBirth; // Matches HTML5 date picker "yyyy-MM-dd"
}