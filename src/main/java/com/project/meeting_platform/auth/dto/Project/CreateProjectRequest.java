package com.project.meeting_platform.auth.dto.Project;

import com.project.meeting_platform.Enum.Project.ContractStatus;
import com.project.meeting_platform.Enum.Project.ProjectStatus;
import com.project.meeting_platform.Enum.Project.ProjectType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateProjectRequest(
        @NotNull UUID clientId,
        @NotBlank @Size(max = 160) String name,
        @NotNull ProjectType projectType,
        @NotNull ProjectStatus status,
        @Size(max = 20_000) String scope,
        @DecimalMin(value = "0.00") BigDecimal totalValue,
        @NotNull ContractStatus contractStatus,
        @Size(max = 2048) String contractUrl,
        boolean maintenanceActive,
        @DecimalMin(value = "0.00") BigDecimal maintenanceMonthlyValue,
        LocalDate maintenanceStartDate,
        @Min(1) @Max(2) Integer installmentCount,
        @Min(2) @Max(12) Integer firstInstallmentCardInstallmentCount,
        @Min(2) @Max(12) Integer secondInstallmentCardInstallmentCount,
        @NotNull LocalDate startDate,
        @NotNull LocalDate deliveryDate
) {
}
