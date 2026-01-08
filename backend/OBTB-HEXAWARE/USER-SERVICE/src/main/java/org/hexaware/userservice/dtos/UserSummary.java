package org.hexaware.userservice.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor
public class UserSummary {
    private UUID userId;
    private UUID roleMappingId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
}
