package com.project.meeting_platform.Model;

import com.project.meeting_platform.Enum.Project.ContractStatus;
import com.project.meeting_platform.Enum.Project.ProjectStatus;
import com.project.meeting_platform.Enum.Project.ProjectType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false, updatable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private User owner;

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", nullable = false, length = 30)
    private ProjectType projectType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectStatus status;

    @Column(columnDefinition = "TEXT")
    private String scope;

    @Column(name = "total_value", precision = 12, scale = 2)
    private BigDecimal totalValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status", nullable = false, length = 30)
    private ContractStatus contractStatus;

    @Column(name = "contract_url", length = 2048)
    private String contractUrl;

    @Column(name = "maintenance_active", nullable = false)
    private boolean maintenanceActive;

    @Column(name = "maintenance_monthly_value", precision = 12, scale = 2)
    private BigDecimal maintenanceMonthlyValue;

    @Column(name = "maintenance_start_date")
    private LocalDate maintenanceStartDate;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Project(
            Client client,
            User owner,
            String name,
            ProjectType projectType,
            ProjectStatus status,
            String scope,
            BigDecimal totalValue,
            ContractStatus contractStatus,
            String contractUrl,
            boolean maintenanceActive,
            BigDecimal maintenanceMonthlyValue,
            LocalDate maintenanceStartDate,
            LocalDate startDate,
            LocalDate deliveryDate
    ) {
        this.client = client;
        this.owner = owner;
        this.name = name;
        this.projectType = projectType;
        this.status = status;
        this.scope = scope;
        this.totalValue = totalValue;
        this.contractStatus = contractStatus;
        this.contractUrl = contractUrl;
        this.maintenanceActive = maintenanceActive;
        this.maintenanceMonthlyValue = maintenanceActive ? maintenanceMonthlyValue : null;
        this.maintenanceStartDate = maintenanceActive ? maintenanceStartDate : null;
        this.startDate = startDate;
        this.deliveryDate = deliveryDate;
    }

    public void update(
            String name,
            ProjectType projectType,
            ProjectStatus status,
            String scope,
            BigDecimal totalValue,
            ContractStatus contractStatus,
            String contractUrl,
            boolean maintenanceActive,
            BigDecimal maintenanceMonthlyValue,
            LocalDate maintenanceStartDate,
            LocalDate startDate,
            LocalDate deliveryDate
    ) {
        this.name = name;
        this.projectType = projectType;
        this.status = status;
        this.scope = scope;
        this.totalValue = totalValue;
        this.contractStatus = contractStatus;
        this.contractUrl = contractUrl;
        this.maintenanceActive = maintenanceActive;
        this.maintenanceMonthlyValue = maintenanceActive ? maintenanceMonthlyValue : null;
        this.maintenanceStartDate = maintenanceActive ? maintenanceStartDate : null;
        this.startDate = startDate == null ? this.startDate : startDate;
        this.deliveryDate = deliveryDate;
    }

    public void attachContract(String contractUrl) {
        this.contractUrl = contractUrl;
    }

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
