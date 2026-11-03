package com.project.meeting_platform.calendar;

import com.project.meeting_platform.Model.User;
import com.project.meeting_platform.Repository.User.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalendarServiceTest {
    private CalendarNoteRepository notes;
    private UserRepository users;
    private CalendarService service;
    private final User owner = new User("Ana", "ana@example.com", "hash");
    @BeforeEach void setup() {
        notes = mock(CalendarNoteRepository.class); users = mock(UserRepository.class);
        service = new CalendarService(notes, users);
    }
    @Test void cannotModifyAnotherOwnersNote() {
        UUID id = UUID.randomUUID();
        when(notes.findByIdAndOwner_Email(id, owner.getEmail())).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.delete(owner.getEmail(), id)).getStatusCode().value());
        assertThrows(ResponseStatusException.class, () -> service.save(owner.getEmail(), id, new CalendarService.NoteRequest(LocalDate.now(), "Anotação")));
        verify(notes, never()).delete(any());
        verify(notes, never()).save(any());
    }
    @Test void savesAnnotationWithoutEmailConfiguration() {
        when(users.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(notes.save(any())).thenAnswer(call -> call.getArgument(0));
        LocalDate date = LocalDate.of(2026, 11, 3);
        var result = service.save(owner.getEmail(), null, new CalendarService.NoteRequest(date, " Ideia para o projeto "));
        assertEquals("Ideia para o projeto", result.content());
        assertEquals(date, result.date());
    }
    @Test void rejectsInvalidDateRange() {
        LocalDate date = LocalDate.of(2026, 11, 3);
        assertThrows(ResponseStatusException.class, () -> service.list(owner.getEmail(), date, date.minusDays(1)));
        assertThrows(ResponseStatusException.class, () -> service.list(owner.getEmail(), date, date.plusYears(2)));
        verifyNoInteractions(notes);
    }
}
