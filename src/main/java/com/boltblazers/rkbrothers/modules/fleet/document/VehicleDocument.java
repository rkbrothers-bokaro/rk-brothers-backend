package com.boltblazers.rkbrothers.modules.fleet.document;

import com.boltblazers.rkbrothers.core.common.BaseEntity;
import com.boltblazers.rkbrothers.core.masters.vehicle.Vehicle;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vehicle_documents")
public class VehicleDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(name = "document_type", length = 50)
    private String documentType;

    @Column(name = "document_no", length = 100)
    private String documentNo;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "ai_parsed_data", length = 4000)
    private String aiParsedData;

    @Column(name = "reminder_sent_at")
    private LocalDateTime reminderSentAt;
}
