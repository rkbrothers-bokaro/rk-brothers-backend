package com.boltblazers.rkbrothers.modules.fleet.tipper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TipperDailyLogRepository extends JpaRepository<TipperDailyLog, Long> {

    Optional<TipperDailyLog> findByVehicleIdAndDate(Long vehicleId, LocalDate date);

    Page<TipperDailyLog> findBySubmittedById(Long userId, Pageable pageable);

    List<TipperDailyLog> findByVehicleIdAndDateBetween(Long vehicleId, LocalDate startDate, LocalDate endDate);

    List<TipperDailyLog> findByWorkOrderIdAndDateBetween(Long workOrderId, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT COALESCE(SUM(t.dieselLtr), 0) FROM TipperDailyLog t
            WHERE t.vehicle.id = :vehicleId AND t.date < :beforeDate
            """)
    BigDecimal sumDieselLtrBefore(@Param("vehicleId") Long vehicleId, @Param("beforeDate") LocalDate beforeDate);

    @Query("SELECT t.workOrder.id, t.date FROM TipperDailyLog t WHERE t.workOrder IS NOT NULL")
    List<Object[]> findWorkOrderDatesForBilling();

    @Query("""
            SELECT t FROM TipperDailyLog t
            WHERE (CAST(:vehicleId AS Long) IS NULL OR t.vehicle.id = :vehicleId)
            AND (CAST(:workOrderId AS Long) IS NULL OR t.workOrder.id = :workOrderId)
            AND (CAST(:driverId AS Long) IS NULL OR t.driver.id = :driverId)
            AND (CAST(:startDate AS LocalDate) IS NULL OR t.date >= :startDate)
            AND (CAST(:endDate AS LocalDate) IS NULL OR t.date <= :endDate)
            AND (CAST(:submittedById AS Long) IS NULL OR t.submittedBy.id = :submittedById)
            """)
    Page<TipperDailyLog> findAllWithFilters(@Param("vehicleId") Long vehicleId,
                                             @Param("workOrderId") Long workOrderId,
                                             @Param("driverId") Long driverId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate,
                                             @Param("submittedById") Long submittedById,
                                             Pageable pageable);
}
