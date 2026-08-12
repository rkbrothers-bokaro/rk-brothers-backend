package com.boltblazers.rkbrothers.core.masters.vehicle;

import com.boltblazers.rkbrothers.core.common.BaseEntity;
import com.boltblazers.rkbrothers.core.masters.operator.Operator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vehicles")
public class Vehicle extends BaseEntity {

    @Column(name = "vehicle_no", nullable = false, unique = true, length = 30)
    private String vehicleNo;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(name = "billing_basis", length = 20)
    private String billingBasis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_operator_id")
    private Operator assignedOperator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VehicleStatus status;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
