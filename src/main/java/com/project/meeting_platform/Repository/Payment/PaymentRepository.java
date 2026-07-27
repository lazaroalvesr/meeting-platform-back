package com.project.meeting_platform.Repository.Payment;

import com.project.meeting_platform.Model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByProject_Owner_EmailOrderByDueDateAsc(String ownerEmail);

    List<Payment> findByProject_Id(UUID projectId);

    void deleteByProject_Id(UUID projectId);

}
