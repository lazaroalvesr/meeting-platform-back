package com.project.meeting_platform.auth.controller.Room;

import com.project.meeting_platform.auth.Service.Room.RoomService;
import com.project.meeting_platform.auth.dto.Room.CreateRoomRequest;
import com.project.meeting_platform.auth.dto.Room.PublicRoomResponse;
import com.project.meeting_platform.auth.dto.Room.RoomResponse;
import com.project.meeting_platform.auth.dto.Room.UpdatePresentationSettingsRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<RoomResponse> create(
            @Valid @RequestBody CreateRoomRequest request,
            Authentication authentication
    ) {
        RoomResponse response = roomService.create(
                authentication.getName(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> list(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                roomService.listForHost(authentication.getName())
        );
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PublicRoomResponse> findPublicBySlug(
            @PathVariable String slug
    ) {
        return roomService.findPublicBySlug(slug)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> delete(
            @PathVariable String slug,
            Authentication authentication
    ) {
        roomService.delete(authentication.getName(), slug);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{slug}/start")
    public ResponseEntity<RoomResponse> start(
            @PathVariable String slug,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                roomService.start(authentication.getName(), slug)
        );
    }

    @PatchMapping("/{slug}/presentation-settings")
    public ResponseEntity<RoomResponse> updatePresentationSettings(
            @PathVariable String slug,
            @RequestBody UpdatePresentationSettingsRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(roomService.updatePresentationSettings(
                authentication.getName(),
                slug,
                request
        ));
    }

    @PatchMapping("/{slug}/close")
    public ResponseEntity<RoomResponse> close(
            @PathVariable String slug,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                roomService.close(authentication.getName(), slug)
        );
    }
}
