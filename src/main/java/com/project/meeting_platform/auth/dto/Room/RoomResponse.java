package com.project.meeting_platform.auth.dto.Room;

import com.project.meeting_platform.Enum.Room.RoomStatus;
import com.project.meeting_platform.Model.Room;

import java.time.Instant;
import java.util.UUID;

public record RoomResponse(
        UUID id,
        String slug,
        String title,
        String projectUrl,
        RoomStatus status,
        boolean scrollLocked,
        boolean presentationActive,
        Instant createdAt
) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getSlug(),
                room.getTitle(),
                room.getProjectUrl(),
                room.getStatus(),
                room.isScrollLocked(),
                room.isPresentationActive(),
                room.getCreatedAt()
        );
    }

}
