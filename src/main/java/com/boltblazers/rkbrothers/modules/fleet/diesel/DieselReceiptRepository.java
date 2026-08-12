package com.boltblazers.rkbrothers.modules.fleet.diesel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DieselReceiptRepository extends JpaRepository<DieselReceipt, Long> {

    List<DieselReceipt> findByVehicleIdAndDateBetween(Long vehicleId, LocalDate startDate, LocalDate endDate);

    List<DieselReceipt> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT COALESCE(SUM(r.litres), 0) FROM DieselReceipt r
            WHERE r.vehicle.id = :vehicleId AND r.date < :beforeDate
            """)
    BigDecimal sumLitresBefore(@Param("vehicleId") Long vehicleId, @Param("beforeDate") LocalDate beforeDate);
}
