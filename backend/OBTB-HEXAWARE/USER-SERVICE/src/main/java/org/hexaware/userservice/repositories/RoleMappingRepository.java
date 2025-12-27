package org.hexaware.userservice.repositories;



import org.hexaware.userservice.entities.RoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;
import java.util.UUID;

@RequestMapping
public interface RoleMappingRepository extends JpaRepository<RoleMapping, UUID> {
    Optional<RoleMapping> findByRoleId(UUID roleId);
    Optional<RoleMapping> findByUserId(UUID userId);
    Optional<RoleMapping> findByUserIdAndRoleId(UUID userId, UUID roleId);
}
