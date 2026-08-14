package com.project.meeting_platform.Repository.Prospecting;

import com.project.meeting_platform.Model.ProspectedLead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProspectedLeadRepository extends JpaRepository<ProspectedLead, UUID> {

    List<ProspectedLead> findByOwner_EmailOrderByProspectedAtDesc(String ownerEmail);

    Optional<ProspectedLead> findByOwner_EmailAndDeduplicationKey(String ownerEmail, String deduplicationKey);

    Optional<ProspectedLead> findByIdAndOwner_Email(UUID id, String ownerEmail);
}
