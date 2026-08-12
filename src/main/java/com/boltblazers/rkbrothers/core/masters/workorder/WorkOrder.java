package com.boltblazers.rkbrothers.core.masters.workorder;

import com.boltblazers.rkbrothers.core.common.BaseEntity;
import com.boltblazers.rkbrothers.core.masters.party.Party;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "work_orders")
public class WorkOrder extends BaseEntity {

    @Column(name = "wo_number", nullable = false, unique = true, length = 50)
    private String woNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    private Party party;

    @Column(length = 1000)
    private String description;

    @Column(name = "site_location", length = 255)
    private String siteLocation;

    @Column(name = "billing_basis", length = 20)
    private String billingBasis;

    @Column(precision = 10, scale = 2)
    private BigDecimal rate;

    @Column(length = 20)
    private String unit;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
