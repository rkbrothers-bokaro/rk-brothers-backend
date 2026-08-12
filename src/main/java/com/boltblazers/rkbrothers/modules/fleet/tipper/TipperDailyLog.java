package com.boltblazers.rkbrothers.modules.fleet.tipper;

import com.boltblazers.rkbrothers.core.auth.User;
import com.boltblazers.rkbrothers.core.common.BaseEntity;
import com.boltblazers.rkbrothers.core.masters.operator.Operator;
import com.boltblazers.rkbrothers.core.masters.vehicle.Vehicle;
import com.boltblazers.rkbrothers.core.masters.workorder.WorkOrder;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tipper_daily_log")
public class TipperDailyLog extends BaseEntity {

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Operator driver;

    @Column(name = "opening_hrs", precision = 8, scale = 1)
    private BigDecimal openingHrs;

    @Column(name = "closing_hrs", precision = 8, scale = 1)
    private BigDecimal closingHrs;

    @Column(name = "total_hrs", precision = 8, scale = 1)
    private BigDecimal totalHrs;

    @Column(name = "opening_km", precision = 10, scale = 1)
    private BigDecimal openingKm;

    @Column(name = "closing_km", precision = 10, scale = 1)
    private BigDecimal closingKm;

    @Column(name = "total_km", precision = 10, scale = 1)
    private BigDecimal totalKm;

    @Column(name = "diesel_ltr", precision = 8, scale = 2)
    private BigDecimal dieselLtr;

    @Column(name = "diesel_hrs", precision = 8, scale = 1)
    private BigDecimal dieselHrs;

    @Column(name = "diesel_km", precision = 10, scale = 1)
    private BigDecimal dieselKm;

    @Column(name = "run_km", precision = 10, scale = 1)
    private BigDecimal runKm;

    @Column(name = "avg_km_per_ltr", precision = 6, scale = 2)
    private BigDecimal avgKmPerLtr;

    @Column(name = "trip_count")
    @Builder.Default
    private Integer tripCount = 0;

    @Column(name = "work_description", length = 2000)
    private String workDescription;

    @Column(name = "receiving_slip_url", length = 500)
    private String receivingSlipUrl;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "submitted";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    private User submittedBy;

    @OneToMany(mappedBy = "log", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TipperTripEntry> tripEntries = new ArrayList<>();

    @OneToMany(mappedBy = "log", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TipperHrEntry> hrEntries = new ArrayList<>();
}
