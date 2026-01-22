package org.hexaware.oauthservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// FIX: Change to PUBLIC so it's accessible in AuthService
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class SecurityAnswerDTO {
    private String question;
    private String answer;
}
