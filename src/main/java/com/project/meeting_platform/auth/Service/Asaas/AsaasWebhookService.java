package com.project.meeting_platform.auth.Service.Asaas;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.meeting_platform.Enum.Payment.PaymentStatus;
import com.project.meeting_platform.Enum.Payment.PaymentType;
import com.project.meeting_platform.Model.Payment;
import com.project.meeting_platform.Model.Project;
import com.project.meeting_platform.Repository.Payment.PaymentRepository;
import com.project.meeting_platform.Repository.Project.ProjectRepository;
import com.project.meeting_platform.config.AsaasProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class AsaasWebhookService {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    private final AsaasProperties properties;
    private final ProjectRepository projectRepository;
    private final PaymentRepository paymentRepository;

    public AsaasWebhookService(
            AsaasProperties properties,
            ProjectRepository projectRepository,
            PaymentRepository paymentRepository
    ) {
        this.properties = properties;
        this.projectRepository = projectRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void process(String receivedToken, JsonNode payload) {
        validateToken(receivedToken);

        JsonNode asaasPayment = payload.path("payment");
        String paymentId = text(asaasPayment, "id");
        String subscriptionId = text(asaasPayment, "subscription");
        if (!StringUtils.hasText(paymentId) || !StringUtils.hasText(subscriptionId)) {
            return;
        }

        Optional<Project> project = projectRepository.findByAsaasSubscriptionId(subscriptionId);
        if (project.isEmpty()) {
            return;
        }

        LocalDate dueDate = parseDate(text(asaasPayment, "dueDate"));
        BigDecimal amount = asaasPayment.path("value").decimalValue();
        if (dueDate == null || amount == null) {
            return;
        }

        PaymentStatus status = mapStatus(text(asaasPayment, "status"));
        Instant paidAt = paidAt(asaasPayment, status);
        LocalDate referenceMonth = dueDate.withDayOfMonth(1);

        Payment payment = paymentRepository.findByAsaasPaymentId(paymentId)
                .or(() -> paymentRepository.findByProject_IdAndPaymentTypeAndReferenceMonth(
                        project.get().getId(), PaymentType.MONTHLY_MAINTENANCE, referenceMonth
                ))
                .orElseGet(() -> new Payment(
                        project.get(),
                        "Manutencao mensal - " + project.get().getName(),
                        PaymentType.MONTHLY_MAINTENANCE,
                        status,
                        amount,
                        dueDate,
                        referenceMonth,
                        paidAt
                ));

        payment.syncFromAsaas(paymentId, status, amount, dueDate, referenceMonth, paidAt);
        paymentRepository.save(payment);
    }

    private void validateToken(String receivedToken) {
        if (!properties.hasWebhookToken() || receivedToken == null
                || !MessageDigest.isEqual(
                properties.webhookToken().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                receivedToken.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        )) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Webhook nao autorizado.");
        }
    }

    private PaymentStatus mapStatus(String asaasStatus) {
        return switch (asaasStatus) {
            case "RECEIVED", "CONFIRMED", "RECEIVED_IN_CASH" -> PaymentStatus.PAID;
            case "OVERDUE" -> PaymentStatus.OVERDUE;
            case "DELETED", "REFUNDED", "REFUND_REQUESTED", "CHARGEBACK_REQUESTED", "CHARGEBACK_DISPUTE" -> PaymentStatus.CANCELLED;
            default -> PaymentStatus.PENDING;
        };
    }

    private Instant paidAt(JsonNode payment, PaymentStatus status) {
        if (status != PaymentStatus.PAID) {
            return null;
        }
        LocalDate paymentDate = parseDate(text(payment, "clientPaymentDate"));
        if (paymentDate == null) paymentDate = parseDate(text(payment, "paymentDate"));
        if (paymentDate == null) paymentDate = LocalDate.now(SAO_PAULO);
        return paymentDate.atStartOfDay(SAO_PAULO).toInstant();
    }

    private LocalDate parseDate(String value) {
        try {
            return StringUtils.hasText(value) ? LocalDate.parse(value) : null;
        } catch (java.time.format.DateTimeParseException exception) {
            return null;
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isTextual() ? value.asText() : null;
    }
}
