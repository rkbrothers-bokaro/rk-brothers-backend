package com.boltblazers.rkbrothers.core.masters.party;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyRepository extends JpaRepository<Party, Long> {

    Page<Party> findAllByIsActiveTrue(Pageable pageable);

    Page<Party> findByNameContainingIgnoreCaseAndIsActiveTrue(String search, Pageable pageable);
}
