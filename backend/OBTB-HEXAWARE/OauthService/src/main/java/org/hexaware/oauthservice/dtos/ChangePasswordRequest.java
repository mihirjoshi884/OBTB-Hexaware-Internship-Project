// ChangePasswordRequest.java
package org.hexaware.oauthservice.dtos;

import lombok.*;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ChangePasswordRequest {
    private String email;
    private String currentPassword;
    private String newPassword;
    private List<SecurityAnswerDTO> securityVerification; // Matches Angular key
}

