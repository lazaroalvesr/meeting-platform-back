package com.project.meeting_platform.calendar;

import com.project.meeting_platform.Repository.User.UserRepository;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;

@Service
public class CalendarService {
    private final CalendarNoteRepository notes;
    private final UserRepository users;
    public CalendarService(CalendarNoteRepository notes, UserRepository users) {
        this.notes = notes; this.users = users;
    }
    public record NoteRequest(@NotNull LocalDate date, @NotBlank @Size(max = 5000) String content) {}
    public record NoteResponse(UUID id, LocalDate date, String content) {
        static NoteResponse of(CalendarNote note) { return new NoteResponse(note.getId(), note.getNoteDate(), note.getContent()); }
    }
    @Transactional(readOnly = true)
    public List<NoteResponse> list(String email, LocalDate from, LocalDate to) {
        if (to.isBefore(from) || from.plusYears(1).isBefore(to)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escolha um intervalo de até um ano.");
        return notes.findByOwner_EmailAndNoteDateBetweenOrderByNoteDateAsc(email, from, to).stream().map(NoteResponse::of).toList();
    }
    @Transactional
    public NoteResponse save(String email, UUID id, NoteRequest request) {
        CalendarNote note = id == null ? new CalendarNote(users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED))) : owned(email, id);
        note.update(request.date(), request.content().trim());
        return NoteResponse.of(notes.save(note));
    }
    @Transactional
    public void delete(String email, UUID id) { notes.delete(owned(email, id)); }
    private CalendarNote owned(String email, UUID id) {
        return notes.findByIdAndOwner_Email(id, email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anotação não encontrada."));
    }
}
