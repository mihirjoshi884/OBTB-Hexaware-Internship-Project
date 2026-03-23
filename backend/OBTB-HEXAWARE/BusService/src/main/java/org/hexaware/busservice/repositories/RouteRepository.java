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
        WHERE(
           (LOWER(TRIM(r.origin)) = LOWER(TRIM(:origin)) AND LOWER(TRIM(r.destination)) = LOWER(TRIM(:destination)))
           OR (LOWER(TRIM(r.origin)) = LOWER(TRIM(:origin)) AND EXISTS(
               SELECT 1 FROM RouteStop s WHERE s.route = r AND LOWER(TRIM(s.stopName)) = LOWER(TRIM(:destination)) AND s.stopOrder > 0
           ))
           OR (LOWER(TRIM(r.destination)) = LOWER(TRIM(:destination)) AND EXISTS(
               SELECT 1 FROM RouteStop s WHERE s.route = r AND LOWER(TRIM(s.stopName)) = LOWER(TRIM(:origin))
           ))
           OR EXISTS (
               SELECT 1 FROM RouteStop s1, RouteStop s2
               WHERE (s1.route = r AND s2.route = r) AND (
                   LOWER(TRIM(s1.stopName)) = LOWER(TRIM(:origin))
                   AND LOWER(TRIM(s2.stopName)) = LOWER(TRIM(:destination))
                   AND s1.stopOrder < s2.stopOrder
               )
           )
        )
    """)
    List<Route> findRoutesByOriginAndDestination(
            @Param("origin") String origin,
            @Param("destination") String destination
    );

}