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

@Service
public class MaintenanceBillingService {

    private final ProjectRepository projectRepository;
    private final PaymentRepository paymentRepository;

    public MaintenanceBillingService(ProjectRepository projectRepository, PaymentRepository paymentRepository) {
        this.projectRepository = projectRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void createCurrentMonthPayments() {
        LocalDate today = LocalDate.now();
        projectRepository.findByMaintenanceActiveTrue()
                .forEach(project -> createCurrentMonthPayment(project, today));
    }

    @Transactional
    public void createInitialOrCurrentMonthPayment(Project project) {
        LocalDate today = LocalDate.now();
        if (project.getMaintenanceStartDate() != null && project.getMaintenanceStartDate().isAfter(today)) {
            createPaymentForMonth(project, YearMonth.from(project.getMaintenanceStartDate()));
            return;
        }
        createCurrentMonthPayment(project, today);
    }

    @Scheduled(cron = "${app.maintenance-billing.cron:0 5 0 * * *}", zone = "${app.time-zone:America/Sao_Paulo}")
    @Transactional
    public void scheduleCurrentMonthPayments() {
        createCurrentMonthPayments();
    }

    private void createCurrentMonthPayment(Project project, LocalDate today) {
        if (!project.isMaintenanceActive()
                || project.getAsaasSubscriptionId() != null
                || project.getMaintenanceMonthlyValue() == null
                || project.getMaintenanceMonthlyValue().signum() <= 0
                || project.getMaintenanceStartDate() == null
                || project.getMaintenanceStartDate().isAfter(today)) {
            return;
        }

        createPaymentForMonth(project, YearMonth.from(today));
    }

    private void createPaymentForMonth(Project project, YearMonth billingMonth) {
        if (!project.isMaintenanceActive()
                || project.getAsaasSubscriptionId() != null
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

        int dueDay = Math.min(project.getMaintenanceStartDate().getDayOfMonth(), billingMonth.lengthOfMonth());
        LocalDate dueDate = billingMonth.atDay(dueDay);
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
}
