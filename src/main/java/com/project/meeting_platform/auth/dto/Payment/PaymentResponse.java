package com.project.meeting_platform.auth.dto.Payment;

import com.project.meeting_platform.Enum.Payment.PaymentStatus;
import com.project.meeting_platform.Enum.Payment.PaymentType;
import com.project.meeting_platform.Model.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID projectId,
        String projectName,
        String clientName,
        String description,
        PaymentType paymentType,
        PaymentStatus status,
        BigDecimal amount,
        LocalDate dueDate,
        Instant paidAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getProject().getId(),
                payment.getProject().getName(),
                payment.getProject().getClient().getName(),
                payment.getDescription(),
                payment.getPaymentType(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getDueDate(),
                payment.getPaidAt()
        );
    }
}
