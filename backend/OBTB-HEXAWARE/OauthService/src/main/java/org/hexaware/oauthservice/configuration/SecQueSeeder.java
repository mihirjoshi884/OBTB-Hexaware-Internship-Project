package org.hexaware.oauthservice.configuration;

import org.hexaware.oauthservice.entites.SecurityQuestionDefinition;
import org.hexaware.oauthservice.repositories.SecQuesDefRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SecQueSeeder {

    private final List<String> securityQuestions = new ArrayList<>(List.of(
            "What is the first phone number you memorized as a child?",
            "What is the last name of your favorite high school teacher?"
    ));

    @Bean
    public CommandLineRunner initSecQue(SecQuesDefRepository repository){
        return args -> {

            // 2. Check if the questions already exist to prevent duplicates
            // We use count() to efficiently check if the table is empty.
            if (repository.count() == 0) {

                System.out.println("Seeding Security Questions...");

                // 3. Map each String to a SecurityQuestionDefinition entity
                List<SecurityQuestionDefinition> definitions = securityQuestions.stream()
                        .map(SecurityQuestionDefinition::new) // Uses the constructor public SecurityQuestionDefinition(String questionText)
                        .collect(Collectors.toList());

                // 4. Save all entities to the database in one batch operation
                repository.saveAll(definitions);

                System.out.println("Successfully seeded " + definitions.size() + " security questions.");
            } else {
                System.out.println("Security questions already exist. Skipping seeding.");
            }
        };
    }
}
