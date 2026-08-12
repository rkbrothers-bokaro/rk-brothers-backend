package com.boltblazers.rkbrothers.core.masters.vehicle;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Page<Vehicle> findAllByIsActiveTrue(Pageable pageable);

    Page<Vehicle> findByVehicleNoContainingIgnoreCaseAndIsActiveTrue(String search, Pageable pageable);

    boolean existsByVehicleNo(String vehicleNo);
}
