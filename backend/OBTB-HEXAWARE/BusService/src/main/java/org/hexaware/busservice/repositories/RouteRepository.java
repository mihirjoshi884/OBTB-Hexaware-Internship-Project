package org.hexaware.busservice.repositories;

import org.hexaware.busservice.entities.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RouteRepository extends JpaRepository<Route, UUID> {

    List<Route> findAllByCompanyCompanyId(UUID companyId);

    @Query("""
    SELECT DISTINCT r FROM Route r
    LEFT JOIN FETCH r.stops s_all
    WHERE (
       (r.origin ILIKE :origin AND r.destination ILIKE :destination)
       OR (r.origin ILIKE :origin AND EXISTS(
           SELECT 1 FROM RouteStop s WHERE s.route = r AND s.stopName ILIKE :destination
       ))
       OR (r.destination ILIKE :destination AND EXISTS(
           SELECT 1 FROM RouteStop s WHERE s.route = r AND s.stopName ILIKE :origin
       ))
       OR EXISTS (
           SELECT 1 FROM RouteStop s1, RouteStop s2
           WHERE s1.route = r AND s2.route = r 
           AND s1.stopName ILIKE :origin 
           AND s2.stopName ILIKE :destination 
           AND s1.stopOrder < s2.stopOrder
       )
    )
""")
    List<Route> findRoutesByOriginAndDestination(
            @Param("origin") String origin,
            @Param("destination") String destination
    );

}