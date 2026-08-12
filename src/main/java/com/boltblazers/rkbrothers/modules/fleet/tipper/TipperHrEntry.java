package com.boltblazers.rkbrothers.modules.fleet.tipper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tipper_hr_entries")
public class TipperHrEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id")
    private TipperDailyLog log;

    @Column(name = "opening_hrs", precision = 8, scale = 1)
    private BigDecimal openingHrs;

    @Column(name = "closing_hrs", precision = 8, scale = 1)
    private BigDecimal closingHrs;

    @Column(name = "total_hrs", precision = 8, scale = 1)
    private BigDecimal totalHrs;
}
