package org.hexaware.busservice.repositories;

import org.hexaware.busservice.entities.BusTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BusTemplateRepository extends JpaRepository<BusTemplate, UUID> {
}
