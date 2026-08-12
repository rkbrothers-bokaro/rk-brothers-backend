package com.boltblazers.rkbrothers.modules.fleet.jcb;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JcbDailyLogRepository extends JpaRepository<JcbDailyLog, Long> {

    Optional<JcbDailyLog> findByVehicleIdAndDate(Long vehicleId, LocalDate date);

    Page<JcbDailyLog> findBySubmittedById(Long userId, Pageable pageable);

    List<JcbDailyLog> findByVehicleIdAndDateBetween(Long vehicleId, LocalDate startDate, LocalDate endDate);

    List<JcbDailyLog> findByWorkOrderIdAndDateBetween(Long workOrderId, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT COALESCE(SUM(j.dieselLtr), 0) FROM JcbDailyLog j
            WHERE j.vehicle.id = :vehicleId AND j.date < :beforeDate
            """)
    BigDecimal sumDieselLtrBefore(@Param("vehicleId") Long vehicleId, @Param("beforeDate") LocalDate beforeDate);

    @Query("SELECT j.workOrder.id, j.date FROM JcbDailyLog j WHERE j.workOrder IS NOT NULL")
    List<Object[]> findWorkOrderDatesForBilling();

    @Query("""
            SELECT j FROM JcbDailyLog j
            WHERE (:vehicleId IS NULL OR j.vehicle.id = :vehicleId)
            AND (:workOrderId IS NULL OR j.workOrder.id = :workOrderId)
            AND (:operatorId IS NULL OR j.operator.id = :operatorId)
            AND (:startDate IS NULL OR j.date >= :startDate)
            AND (:endDate IS NULL OR j.date <= :endDate)
            AND (:submittedById IS NULL OR j.submittedBy.id = :submittedById)
            """)
    Page<JcbDailyLog> findAllWithFilters(@Param("vehicleId") Long vehicleId,
                                          @Param("workOrderId") Long workOrderId,
                                          @Param("operatorId") Long operatorId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("submittedById") Long submittedById,
                                          Pageable pageable);
}
