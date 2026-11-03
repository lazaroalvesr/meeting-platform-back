package com.project.meeting_platform.calendar;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.time.*;
import java.util.*;

public interface CalendarNoteRepository extends JpaRepository<CalendarNote, UUID> {
    List<CalendarNote> findByOwner_EmailAndNoteDateBetweenOrderByNoteDateAsc(String email, LocalDate from, LocalDate to);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CalendarNote> findByIdAndOwner_Email(UUID id, String email);
}
