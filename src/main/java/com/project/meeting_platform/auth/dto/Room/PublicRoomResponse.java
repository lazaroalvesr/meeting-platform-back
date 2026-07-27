package com.project.meeting_platform.auth.dto.Room;

import com.project.meeting_platform.Enum.Room.RoomStatus;
import com.project.meeting_platform.Model.Room;

public record PublicRoomResponse(
        String slug,
        String title,
        String projectUrl,
        RoomStatus status,
        boolean scrollLocked,
        boolean presentationActive
) {
    public static PublicRoomResponse from(Room room) {
        return new PublicRoomResponse(
                room.getSlug(),
                room.getTitle(),
                room.getProjectUrl(),
                room.getStatus(),
                room.isScrollLocked(),
                room.isPresentationActive()
        );
    }
}
