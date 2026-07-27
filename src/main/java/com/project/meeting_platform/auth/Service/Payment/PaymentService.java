package com.project.meeting_platform.auth.Service.Payment;

import com.project.meeting_platform.Enum.Payment.PaymentStatus;
import com.project.meeting_platform.Model.Payment;
import com.project.meeting_platform.Model.Project;
import com.project.meeting_platform.Repository.Payment.PaymentRepository;
import com.project.meeting_platform.Repository.Project.ProjectRepository;
import com.project.meeting_platform.auth.dto.Payment.CreatePaymentRequest;
import com.project.meeting_platform.auth.dto.Payment.PaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProjectRepository projectRepository;

    public PaymentService(PaymentRepository paymentRepository, ProjectRepository projectRepository) {
        this.paymentRepository = paymentRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public PaymentResponse create(String authenticatedEmail, CreatePaymentRequest request) {
        Project project = projectRepository.findById(request.projectId())
                .filter(foundProject -> foundProject.getOwner().getEmail().equals(authenticatedEmail))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado."));

        Instant paidAt = request.status() == PaymentStatus.PAID ? Instant.now() : null;
        Payment payment = new Payment(
                project,
                request.description().trim(),
                request.paymentType(),
                request.status(),
                request.amount(),
                request.dueDate(),
                paidAt
        );

        paymentRepository.save(payment);
        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> list(String authenticatedEmail) {
        return paymentRepository.findByProject_Owner_EmailOrderByDueDateAsc(authenticatedEmail)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Transactional
    public PaymentResponse markAsPaid(String authenticatedEmail, java.util.UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .filter(foundPayment -> foundPayment.getProject().getOwner().getEmail().equals(authenticatedEmail))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cobrança não encontrada."));

        payment.markAsPaid();
        return PaymentResponse.from(payment);
    }

    @Transactional
    public void delete(String authenticatedEmail, java.util.UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .filter(foundPayment -> foundPayment.getProject().getOwner().getEmail().equals(authenticatedEmail))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cobrança não encontrada."));

        paymentRepository.delete(payment);
    }
}
