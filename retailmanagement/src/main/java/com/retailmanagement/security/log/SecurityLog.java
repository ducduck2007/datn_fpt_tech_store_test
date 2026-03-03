package com.retailmanagement.security.log;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "security_log")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SecurityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @Nationalized
    @Column(name = "username", length = 100)
    private String username;

    @Size(max = 100)
    @Nationalized
    @Column(name = "action_type", length = 100)
    private String actionType;

    @Size(max = 100)
    @Nationalized
    @Column(name = "target_entity", length = 100)
    private String targetEntity;

    @Size(max = 100)
    @Nationalized
    @Column(name = "target_id", length = 100)
    private String targetId;

    @Size(max = 50)
    @Nationalized
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Size(max = 20)
    @Nationalized
    @Column(name = "severity", length = 20)
    private String severity;

    @Size(max = 50)
    @Nationalized
    @Column(name = "status", length = 50)
    private String status;

    @Nationalized
    @Lob
    @Column(name = "description")
    private String description;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private Instant createdAt;

}