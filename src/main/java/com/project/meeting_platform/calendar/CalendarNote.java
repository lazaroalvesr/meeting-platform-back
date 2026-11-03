package com.project.meeting_platform.calendar;

import com.project.meeting_platform.Model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.*;
import java.util.UUID;

@Entity @Table(name = "calendar_notes") @Getter @NoArgsConstructor
public class CalendarNote {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false) private User owner;
    @Column(name = "note_date", nullable = false) private LocalDate noteDate;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    public CalendarNote(User owner) { this.owner = owner; }
    public void update(LocalDate date, String content) { noteDate = date; this.content = content; }
}
