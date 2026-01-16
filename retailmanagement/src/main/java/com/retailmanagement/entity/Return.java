package com.retailmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "returns")
public class Return {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Size(max = 500)
    @Nationalized
    @Column(name = "reason", length = 500)
    private String reason;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "refund_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal refundAmount;

    @Size(max = 50)
    @Nationalized
    @Column(name = "refund_method", length = 50)
    private String refundMethod;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @ColumnDefault("'PENDING'")
    @Column(name = "refund_status", nullable = false, length = 20)
    private String refundStatus;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @ColumnDefault("'PENDING'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Nationalized
    @Lob
    @Column(name = "note")
    private String note;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

}