package org.hexaware.oauthservice.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class UserSummary {
    private UUID userId;
    private UUID roleMappingId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
}
