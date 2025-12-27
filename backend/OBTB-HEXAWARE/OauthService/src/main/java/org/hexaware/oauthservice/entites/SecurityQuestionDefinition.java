package org.hexaware.oauthservice.entites;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity @Getter @Setter @AllArgsConstructor @NoArgsConstructor
@Table(name = "security_question_definition")
public class SecurityQuestionDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID questionId;


    @Column(name = "question_text", nullable = false, unique = true, length = 255)
    private String questionText;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public SecurityQuestionDefinition(String Question) {
        questionText = Question;
        isActive = true;
    }
}
