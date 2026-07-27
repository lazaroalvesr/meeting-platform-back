package com.project.meeting_platform.Model;

import com.project.meeting_platform.Enum.Room.RoomStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false, updatable = false)
    private User host;

    @Column(nullable = false, unique = true, length = 16, updatable = false)
    private String slug;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(name = "project_url", length = 2048)
    private String projectUrl;

    @Column(name = "scroll_locked", nullable = false)
    private boolean scrollLocked = true;

    @Column(name = "presentation_active", nullable = false)
    private boolean presentationActive = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    public Room(User host, String slug, String title, String projectUrl) {
        this.host = host;
        this.slug = slug;
        this.title = title;
        this.projectUrl = projectUrl;
        this.status = RoomStatus.WAITING;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }

    public void activate() {
        this.status = RoomStatus.ACTIVE;
    }

    public void close() {
        this.status = RoomStatus.CLOSED;
        this.closedAt = Instant.now();
    }

    public void setScrollLocked(boolean scrollLocked) {
        this.scrollLocked = scrollLocked;
    }

    public void setPresentationActive(boolean presentationActive) {
        this.presentationActive = presentationActive;
    }
}
