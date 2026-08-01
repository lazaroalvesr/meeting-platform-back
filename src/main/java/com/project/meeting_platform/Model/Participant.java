package com.project.meeting_platform.Model;
import com.project.meeting_platform.Enum.Participant.ParticipantRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "participants")
@Getter
@NoArgsConstructor
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, updatable = false)
    private Room room;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipantRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    public Participant(
            Room room,
            String displayName,
            ParticipantRole role
    ) {
        this.room = room;
        this.displayName = displayName;
        this.role = role;
    }

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        this.joinedAt = now;
        this.lastSeenAt = now;
    }

    public void leave() {
        this.leftAt = Instant.now();
    }

    public void touch() {
        this.lastSeenAt = Instant.now();
    }
}
