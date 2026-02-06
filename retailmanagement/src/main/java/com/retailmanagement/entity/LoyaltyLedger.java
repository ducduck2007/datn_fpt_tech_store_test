package com.retailmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "loyalty_ledger")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @Column(name = "points_delta", nullable = false)
    private Integer pointsDelta;

    @Size(max = 200)
    @Nationalized
    @Column(name = "reason", length = 200)
    private String reason;

    @Size(max = 50)
    @Nationalized
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Size(max = 500)
    @Nationalized
    @Column(name = "note", length = 500)
    private String note;


    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "transaction_type", length = 50)
    private String transactionType; // EARN / DEDUCT / TIER_UPGRADE / TIER_DOWNGRADE / PENALTY

    @Column(name = "tier_before", length = 30)
    private String tierBefore;

    @Column(name = "tier_after", length = 30)
    private String tierAfter;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}