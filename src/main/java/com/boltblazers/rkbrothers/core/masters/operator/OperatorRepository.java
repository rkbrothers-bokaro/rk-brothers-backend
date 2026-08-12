package com.boltblazers.rkbrothers.core.masters.operator;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperatorRepository extends JpaRepository<Operator, Long> {

    Page<Operator> findAllByIsActiveTrue(Pageable pageable);

    Page<Operator> findByNameContainingIgnoreCaseAndIsActiveTrue(String search, Pageable pageable);

    boolean existsByLicenceNo(String licenceNo);
}
