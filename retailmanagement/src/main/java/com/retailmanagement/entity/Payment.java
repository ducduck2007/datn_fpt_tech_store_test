package com.retailmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "method", nullable = false, length = 50)
    private String method;

    @Size(max = 100)
    @Nationalized
    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @ColumnDefault("'PENDING'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "paid_at")
    private Instant paidAt;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}