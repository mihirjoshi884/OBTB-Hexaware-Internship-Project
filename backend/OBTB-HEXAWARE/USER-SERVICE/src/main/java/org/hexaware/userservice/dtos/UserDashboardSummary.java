package org.hexaware.userservice.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDate;

public record UserDashboardSummary (
        String username,
        String firstName,
        String lastName,
        String roleName,
        String gender,

        @JsonProperty("dateOfBirth") // Ensures the JSON key is dateOfBirth
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate dateofBirth,

        Double walletBalance,
        String contact,
        String email,
        String profilePictureUrl,

        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant createdAt,

        @JsonProperty("passwordLastUpdated")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant passwordLastUpdated,

        @JsonProperty("lastLogin") // Standardize to lastLogin
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant lastlogin
){}
