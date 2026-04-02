package org.hexaware.bookingservice.repositories;

import org.hexaware.bookingservice.entites.PrimaryPassangerDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PrimaryPassengerDetailRepository extends JpaRepository<PrimaryPassangerDetail, UUID> {
    PrimaryPassangerDetail findByEmail(String email);
    PrimaryPassangerDetail findByPhone(String phone);
}
