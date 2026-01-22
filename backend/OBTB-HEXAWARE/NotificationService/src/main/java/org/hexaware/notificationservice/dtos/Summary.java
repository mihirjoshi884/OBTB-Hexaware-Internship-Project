package org.hexaware.notificationservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Summary {


    private UUID userId;
    private UUID roleMappingId;
    private String username;
    private String email;

}