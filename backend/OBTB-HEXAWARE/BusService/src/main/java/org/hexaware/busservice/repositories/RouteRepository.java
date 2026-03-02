package org.hexaware.busservice.repositories;

import org.hexaware.busservice.entities.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RouteRepository extends JpaRepository<Route, UUID> {

    List<Route> findAllByCompanyCompanyId(UUID companyId);

}