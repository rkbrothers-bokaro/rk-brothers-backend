package com.boltblazers.rkbrothers.core.masters.workorder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    Page<WorkOrder> findAllByIsActiveTrue(Pageable pageable);

    Page<WorkOrder> findByWoNumberContainingIgnoreCaseAndIsActiveTrue(String search, Pageable pageable);

    List<WorkOrder> findAllByPartyId(Long partyId);

    boolean existsByWoNumber(String woNumber);
}
