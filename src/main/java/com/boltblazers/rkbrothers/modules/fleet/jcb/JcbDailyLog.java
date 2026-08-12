package com.boltblazers.rkbrothers.modules.fleet.jcb;

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
@Table(name = "jcb_daily_log")
public class JcbDailyLog extends BaseEntity {

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private Operator operator;

    @Column(name = "opening_hrs", precision = 8, scale = 1)
    private BigDecimal openingHrs;

    @Column(name = "closing_hrs", precision = 8, scale = 1)
    private BigDecimal closingHrs;

    @Column(name = "total_hrs", precision = 8, scale = 1)
    private BigDecimal totalHrs;

    @Column(name = "diesel_ltr", precision = 8, scale = 2)
    private BigDecimal dieselLtr;

    @Column(name = "diesel_mtr", precision = 8, scale = 1)
    private BigDecimal dieselMtr;

    @Column(name = "running_ltr", precision = 8, scale = 2)
    private BigDecimal runningLtr;

    @Column(name = "avg_ltr_per_hr", precision = 6, scale = 2)
    private BigDecimal avgLtrPerHr;

    @Column(name = "material_type", length = 50)
    private String materialType;

    @Column(name = "material_qty", precision = 10, scale = 2)
    private BigDecimal materialQty;

    @Column(name = "material_unit", length = 20)
    private String materialUnit;

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
    private List<JcbDailyLogShift> shifts = new ArrayList<>();
}
