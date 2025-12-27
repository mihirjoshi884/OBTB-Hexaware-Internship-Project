package org.hexaware.userservice.config;



import org.hexaware.userservice.entities.Role;
import org.hexaware.userservice.enums.Roles;
import org.hexaware.userservice.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoleSeeder {

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            for (Roles r : Roles.values()) {
                // use orElseGet with a supplier
                roleRepository.findByRole(r)
                        .orElseGet(() -> roleRepository.save(new Role(r)));
            }
        };
    }
}
