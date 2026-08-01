package com.project.meeting_platform.Repository.Participant;

import com.project.meeting_platform.Model.Participant;
import com.project.meeting_platform.Enum.Participant.ParticipantRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import java.util.UUID;

public interface ParticipantRepository extends JpaRepository<Participant, UUID> {
    List<Participant> findByRoom_IdAndLeftAtIsNull(UUID roomId);

    Optional<Participant> findByIdAndRoom_SlugAndLeftAtIsNull(
            UUID participantId,
            String roomSlug
    );

    Optional<Participant> findFirstByRoom_IdAndRoleAndLeftAtIsNull(
            UUID roomId,
            ParticipantRole role
    );

    List<Participant> findByLeftAtIsNullAndLastSeenAtBefore(Instant cutoff);

    boolean existsByRoom_IdAndLeftAtIsNull(UUID roomId);
}
