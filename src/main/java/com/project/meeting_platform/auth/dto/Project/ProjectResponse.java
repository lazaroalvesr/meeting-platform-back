package com.project.meeting_platform.auth.dto.Project;

import com.project.meeting_platform.Enum.Project.ContractStatus;
import com.project.meeting_platform.Enum.Project.ProjectStatus;
import com.project.meeting_platform.Enum.Project.ProjectType;
import com.project.meeting_platform.Model.Project;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID clientId,
        String clientName,
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
        LocalDate deliveryDate,
        Instant createdAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getClient().getId(),
                project.getClient().getName(),
                project.getName(),
                project.getProjectType(),
                project.getStatus(),
                project.getScope(),
                project.getTotalValue(),
                project.getContractStatus(),
                project.getContractUrl(),
                project.isMaintenanceActive(),
                project.getMaintenanceMonthlyValue(),
                project.getMaintenanceStartDate(),
                project.getStartDate(),
                project.getDeliveryDate(),
                project.getCreatedAt()
        );
    }
}
