package org.hexaware.userservice.repositories;


import org.hexaware.userservice.entities.Role;
import org.hexaware.userservice.enums.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByRole(Roles role);
}
