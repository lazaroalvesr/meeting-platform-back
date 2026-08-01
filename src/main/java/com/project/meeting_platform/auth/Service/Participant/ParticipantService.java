package com.project.meeting_platform.auth.Service.Participant;

import com.project.meeting_platform.Enum.Participant.ParticipantRole;
import com.project.meeting_platform.Enum.Room.RoomStatus;
import com.project.meeting_platform.Model.Participant;
import com.project.meeting_platform.Model.Room;
import com.project.meeting_platform.Repository.Participant.ParticipantRepository;
import com.project.meeting_platform.Repository.Room.RoomRepository;
import com.project.meeting_platform.auth.dto.Participant.JoinRoomRequest;
import com.project.meeting_platform.auth.dto.Participant.JoinRoomResponse;
import com.project.meeting_platform.realtime.ParticipantPresenceEvent;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.Instant;
import java.util.UUID;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final RoomRepository roomRepository;

    public ParticipantService(
            ParticipantRepository participantRepository,
            RoomRepository roomRepository
    ) {
        this.participantRepository = participantRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public JoinRoomResponse join(
            String roomSlug,
            JoinRoomRequest request
    ) {
        Room room = roomRepository.findBySlug(roomSlug)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Sala não encontrada."
                ));

        if (room.getStatus() == RoomStatus.CLOSED) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Esta sala foi encerrada."
            );
        }

        Participant participant = new Participant(
                room,
                request.displayName().trim(),
                ParticipantRole.GUEST
        );

        participantRepository.save(participant);

        return JoinRoomResponse.from(participant);
    }

    @Transactional(readOnly = true)
    public ParticipantPresenceEvent createJoinedEvent(
            String roomSlug,
            UUID participantId
    ) {
        Participant participant = participantRepository
                .findByIdAndRoom_SlugAndLeftAtIsNull(
                        participantId,
                        roomSlug
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Participante não encontrado na sala."
                ));

        return new ParticipantPresenceEvent(
                "PARTICIPANT_JOINED",
                participant.getId(),
                participant.getDisplayName(),
                participant.getRole()
        );
    }

    @Transactional(readOnly = true)
    public List<JoinRoomResponse> listActive(String roomSlug) {
        Room room = roomRepository.findBySlug(roomSlug)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Sala não encontrada."
                ));

        return participantRepository.findByRoom_IdAndLeftAtIsNull(room.getId())
                .stream()
                .map(JoinRoomResponse::from)
                .toList();
    }

    @Transactional
    public void leave(String roomSlug, UUID participantId) {
        participantRepository.findByIdAndRoom_SlugAndLeftAtIsNull(
                        participantId,
                        roomSlug
                )
                .ifPresent(Participant::leave);
    }

    @Transactional
    public void heartbeat(String roomSlug, UUID participantId) {
        participantRepository.findByIdAndRoom_SlugAndLeftAtIsNull(
                        participantId,
                        roomSlug
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.GONE,
                        "Participante não está mais ativo nesta sala."
                ))
                .touch();
    }

    @Transactional
    public void expireInactiveParticipantsAndCloseEmptyRooms(long timeoutSeconds) {
        Instant cutoff = Instant.now().minusSeconds(timeoutSeconds);

        participantRepository.findByLeftAtIsNullAndLastSeenAtBefore(cutoff)
                .forEach(Participant::leave);

        roomRepository.findByStatus(RoomStatus.ACTIVE)
                .stream()
                .filter(room -> !participantRepository.existsByRoom_IdAndLeftAtIsNull(room.getId()))
                .forEach(Room::close);
    }

    @Transactional(readOnly = true)
    public void requireActiveParticipant(
            String roomSlug,
            UUID participantId
    ) {
        participantRepository.findByIdAndRoom_SlugAndLeftAtIsNull(
                        participantId,
                        roomSlug
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Participante sem acesso a esta sala."
                ));
    }

    @Transactional
    public JoinRoomResponse joinAsHost(
            String roomSlug,
            String authenticatedEmail
    ) {
        Room room = roomRepository.findBySlug(roomSlug)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Sala não encontrada."
                ));

        if (!room.getHost().getEmail().equalsIgnoreCase(authenticatedEmail)) {
            throw new AccessDeniedException(
                    "Você não é o apresentador desta sala."
            );
        }

        return participantRepository
                .findFirstByRoom_IdAndRoleAndLeftAtIsNull(
                        room.getId(),
                        ParticipantRole.HOST
                )
                .map(JoinRoomResponse::from)
                .orElseGet(() -> {
                    Participant participant = new Participant(
                            room,
                            room.getHost().getName(),
                            ParticipantRole.HOST
                    );

                    participantRepository.save(participant);

                    return JoinRoomResponse.from(participant);
                });
    }
}
