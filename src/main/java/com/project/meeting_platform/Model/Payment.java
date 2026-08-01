package com.project.meeting_platform.Model;

import com.project.meeting_platform.Enum.Payment.PaymentStatus;
import com.project.meeting_platform.Enum.Payment.PaymentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;

    @Column(nullable = false, length = 160)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 30)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "reference_month")
    private LocalDate referenceMonth;

    @Column(name = "asaas_payment_id", length = 64, unique = true)
    private String asaasPaymentId;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Payment(
            Project project,
            String description,
            PaymentType paymentType,
            PaymentStatus status,
            BigDecimal amount,
            LocalDate dueDate,
            Instant paidAt
    ) {
        this(project, description, paymentType, status, amount, dueDate, null, paidAt);
    }

    public Payment(
            Project project,
            String description,
            PaymentType paymentType,
            PaymentStatus status,
            BigDecimal amount,
            LocalDate dueDate,
            LocalDate referenceMonth,
            Instant paidAt
    ) {
        this.project = project;
        this.description = description;
        this.paymentType = paymentType;
        this.status = status;
        this.amount = amount;
        this.dueDate = dueDate;
        this.referenceMonth = referenceMonth;
        this.paidAt = paidAt;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }

    public void markAsPaid() {
        this.status = PaymentStatus.PAID;
        this.paidAt = Instant.now();
    }

    public void reschedule(LocalDate dueDate) {
        if (this.status == PaymentStatus.PENDING || this.status == PaymentStatus.OVERDUE) {
            this.dueDate = dueDate;
        }
    }

    public void syncFromAsaas(
            String asaasPaymentId,
            PaymentStatus status,
            BigDecimal amount,
            LocalDate dueDate,
            LocalDate referenceMonth,
            Instant paidAt
    ) {
        this.asaasPaymentId = asaasPaymentId;
        this.status = status;
        this.amount = amount;
        this.dueDate = dueDate;
        this.referenceMonth = referenceMonth;
        this.paidAt = paidAt;
    }
}
