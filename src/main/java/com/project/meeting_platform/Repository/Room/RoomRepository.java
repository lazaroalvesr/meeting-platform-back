package com.project.meeting_platform.Repository.Room;

import com.project.meeting_platform.Model.Room;
import com.project.meeting_platform.Enum.Room.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    Optional<Room> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Room> findByHost_EmailOrderByCreatedAtDesc(String hostEmail);

    List<Room> findByStatus(RoomStatus status);
}
