package com.project.meeting_platform.auth.Service.Payment;

import com.project.meeting_platform.Enum.Payment.PaymentStatus;
import com.project.meeting_platform.Enum.Payment.PaymentType;
import com.project.meeting_platform.Model.Payment;
import com.project.meeting_platform.Model.Project;
import com.project.meeting_platform.Repository.Payment.PaymentRepository;
import com.project.meeting_platform.Repository.Project.ProjectRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class MaintenanceBillingService {

    private final ProjectRepository projectRepository;
    private final PaymentRepository paymentRepository;

    public MaintenanceBillingService(ProjectRepository projectRepository, PaymentRepository paymentRepository) {
        this.projectRepository = projectRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void createUpcomingPayments() {
        LocalDate today = LocalDate.now();
        projectRepository.findByMaintenanceActiveTrue()
                .forEach(project -> createEligiblePayments(project, today));
    }

    @Transactional
    public void createEligiblePayments(Project project) {
        createEligiblePayments(project, LocalDate.now());
    }

    @Scheduled(cron = "${app.maintenance-billing.cron:0 5 0 * * *}", zone = "${app.time-zone:America/Sao_Paulo}")
    @Transactional
    public void scheduleUpcomingPayments() {
        createUpcomingPayments();
    }

    private void createEligiblePayments(Project project, LocalDate today) {
        if (!project.isMaintenanceActive()
                || project.getMaintenanceMonthlyValue() == null
                || project.getMaintenanceMonthlyValue().signum() <= 0
                || project.getMaintenanceStartDate() == null
        ) {
            return;
        }

        YearMonth firstBillingMonth = YearMonth.from(project.getMaintenanceStartDate());
        for (YearMonth billingMonth : List.of(YearMonth.from(today), YearMonth.from(today).plusMonths(1))) {
            if (billingMonth.isBefore(firstBillingMonth)) {
                continue;
            }

            LocalDate dueDate = dueDateFor(project, billingMonth);
            if (!today.isBefore(dueDate.minusDays(5))) {
                createPaymentForMonth(project, billingMonth);
            }
        }
    }

    private void createPaymentForMonth(Project project, YearMonth billingMonth) {
        if (!project.isMaintenanceActive()
                || project.getMaintenanceMonthlyValue() == null
                || project.getMaintenanceMonthlyValue().signum() <= 0
                || project.getMaintenanceStartDate() == null) {
            return;
        }

        LocalDate referenceMonth = billingMonth.atDay(1);
        if (paymentRepository.existsByProject_IdAndPaymentTypeAndReferenceMonth(
                project.getId(), PaymentType.MONTHLY_MAINTENANCE, referenceMonth
        )) {
            return;
        }

        LocalDate dueDate = dueDateFor(project, billingMonth);
        paymentRepository.save(new Payment(
                project,
                "Manutenção mensal - " + project.getName(),
                PaymentType.MONTHLY_MAINTENANCE,
                PaymentStatus.PENDING,
                project.getMaintenanceMonthlyValue(),
                dueDate,
                referenceMonth,
                null
        ));
    }

    private LocalDate dueDateFor(Project project, YearMonth billingMonth) {
        int dueDay = Math.min(project.getMaintenanceStartDate().getDayOfMonth(), billingMonth.lengthOfMonth());
        return billingMonth.atDay(dueDay);
    }
}
