package com.boltblazers.rkbrothers.modules.fleet.diesel;

import com.boltblazers.rkbrothers.core.auth.User;
import com.boltblazers.rkbrothers.core.masters.vehicle.Vehicle;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "diesel_receipts")
public class DieselReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal litres;

    @Column(name = "received_from", length = 200)
    private String receivedFrom;

    @Column(name = "invoice_no", length = 100)
    private String invoiceNo;

    @Column(name = "slip_url", length = 500)
    private String slipUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entered_by")
    private User enteredBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
