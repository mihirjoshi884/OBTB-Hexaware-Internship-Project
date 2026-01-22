package org.hexaware.oauthservice.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
@Entity @Table(name = "SecurityQuestions")
public class SecurityQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID secQuesId;

    @Column(name = "UserId", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "QuesAns-1", nullable = false, unique = false)
    private String questionAndAnswer1;

    @Column(name = "QuesAns-2", nullable = false, unique = false)
    private String questionAndAnswer2;
}
