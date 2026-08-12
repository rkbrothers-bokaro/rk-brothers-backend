package com.boltblazers.rkbrothers.modules.fleet.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, Long> {

    List<VehicleDocument> findByVehicleId(Long vehicleId);

    List<VehicleDocument> findByExpiryDateBetween(LocalDate start, LocalDate end);

    List<VehicleDocument> findByExpiryDateBefore(LocalDate date);

    List<VehicleDocument> findAllByOrderByExpiryDateAsc();
}
