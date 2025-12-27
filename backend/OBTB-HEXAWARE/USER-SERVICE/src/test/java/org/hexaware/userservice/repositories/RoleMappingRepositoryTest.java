package org.hexaware.userservice.repositories;

import org.hexaware.userservice.entities.RoleMapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.UUID;

@DataJpaTest
public class RoleMappingRepositoryTest {

    @Autowired
    private RoleMappingRepository roleMappingRepository;
    @Autowired
    private TestEntityManager testEntityManager;

    private RoleMapping savedRoleMapping;

    @BeforeEach
    public void setupRoleMapping(){
        RoleMapping roleMapping = new RoleMapping();
        roleMapping.setRoleId(UUID.randomUUID());
        roleMapping.setUserId(UUID.randomUUID());
        savedRoleMapping = testEntityManager.persistAndFlush(roleMapping);
    }

    @Test
    @DisplayName("find role mapping")
    public void testFindRoleMappingIdByUserId(){
        UUID id = roleMappingRepository
                .findByUserId(savedRoleMapping.getUserId())
                .get().getRoleMappingId();
        Assertions.assertEquals(savedRoleMapping.getRoleMappingId(),id);
    }

    @Test
    @DisplayName("find role mapping id by RoleId")
    public void testFindRoleMappingByRoleId(){
        UUID id = roleMappingRepository
                .findByRoleId(savedRoleMapping.getRoleId())
                .get().getRoleMappingId();
        Assertions.assertEquals(savedRoleMapping.getRoleMappingId(),id);
    }

}
