package com.project.meeting_platform.auth.dto.Payment;

import com.project.meeting_platform.Enum.Payment.PaymentStatus;
import com.project.meeting_platform.Enum.Payment.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePaymentRequest(
        @NotNull UUID projectId,
        @NotBlank @Size(max = 160) String description,
        @NotNull PaymentType paymentType,
        @NotNull PaymentStatus status,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull LocalDate dueDate
) {
}
