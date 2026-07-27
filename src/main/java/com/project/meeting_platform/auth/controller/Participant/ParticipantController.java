package com.project.meeting_platform.auth.controller.Participant;


import com.project.meeting_platform.auth.Service.Participant.ParticipantService;
import com.project.meeting_platform.auth.dto.Participant.JoinRoomRequest;
import com.project.meeting_platform.auth.dto.Participant.JoinRoomResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms/{slug}/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    public ParticipantController(
            ParticipantService participantService
    ) {
        this.participantService = participantService;
    }

    @PostMapping
    public ResponseEntity<JoinRoomResponse> join(
            @PathVariable String slug,
            @Valid @RequestBody JoinRoomRequest request
    ) {
        JoinRoomResponse response = participantService.join(
                slug,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/host")
    public ResponseEntity<JoinRoomResponse> joinAsHost(
            @PathVariable String slug,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                participantService.joinAsHost(
                        slug,
                        authentication.getName()
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<JoinRoomResponse>> listActive(
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(participantService.listActive(slug));
    }

    @PostMapping("/{participantId}/leave")
    public ResponseEntity<Void> leave(
            @PathVariable String slug,
            @PathVariable java.util.UUID participantId
    ) {
        participantService.leave(slug, participantId);
        return ResponseEntity.noContent().build();
    }
}
