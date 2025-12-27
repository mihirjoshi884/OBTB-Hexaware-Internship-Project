package org.hexaware.userservice.repositories;

import org.hexaware.userservice.entities.Role;
import org.hexaware.userservice.enums.Roles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
public class RoleRepositoryTest {


    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TestEntityManager testEntityManager;

    private Role savedRole;

    @BeforeEach
    public void setupRole(){
        Role role = new Role();
        role.setRole(Roles.CUSTOMER);
        savedRole = testEntityManager.persistFlushFind(role);
    }

    @Test
    public void testFindByRole(){
        var role = roleRepository.findByRole(Roles.CUSTOMER);
        Assertions.assertEquals(Roles.CUSTOMER, role.get().getRole());
    }
}
