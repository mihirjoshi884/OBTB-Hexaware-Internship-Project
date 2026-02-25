package org.hexaware.busservice.repositories;

import org.hexaware.busservice.entities.BusStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BusStaffRepository extends JpaRepository<BusStaff, UUID> { // Changed Integer to UUID
    List<BusStaff> findByBus_BusId(UUID busId);
    List<BusStaff> findByCompany_CompanyId(UUID companyId);
    BusStaff findByPhoneNumber(String phoneNumber);
    boolean existsByDriverLicenseNumber(String driverLicenseNumber);

    List<BusStaff> findAllByCompany_CompanyId(UUID companyId);
}
