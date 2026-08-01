package com.project.meeting_platform.Repository.Payment;

import com.project.meeting_platform.Model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDate;
import java.util.UUID;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByProject_Owner_EmailOrderByDueDateAsc(String ownerEmail);

    List<Payment> findByProject_Id(UUID projectId);

    boolean existsByProject_IdAndPaymentTypeAndReferenceMonth(
            UUID projectId,
            com.project.meeting_platform.Enum.Payment.PaymentType paymentType,
            LocalDate referenceMonth
    );

    Optional<Payment> findByAsaasPaymentId(String asaasPaymentId);

    Optional<Payment> findByProject_IdAndPaymentTypeAndReferenceMonth(
            UUID projectId,
            com.project.meeting_platform.Enum.Payment.PaymentType paymentType,
            LocalDate referenceMonth
    );

    void deleteByProject_Id(UUID projectId);

}
