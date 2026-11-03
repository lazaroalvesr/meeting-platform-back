package com.project.meeting_platform.calendar;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.util.*;

@RestController @RequestMapping("/api/calendar")
public class CalendarController {
    private final CalendarService service;
    public CalendarController(CalendarService service) { this.service = service; }
    @GetMapping public List<CalendarService.NoteResponse> list(Authentication auth, @RequestParam LocalDate from, @RequestParam LocalDate to) { return service.list(auth.getName(), from, to); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CalendarService.NoteResponse create(Authentication auth, @Valid @RequestBody CalendarService.NoteRequest request) { return service.save(auth.getName(), null, request); }
    @PutMapping("/{id}") public CalendarService.NoteResponse update(Authentication auth, @PathVariable UUID id, @Valid @RequestBody CalendarService.NoteRequest request) { return service.save(auth.getName(), id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable UUID id) { service.delete(auth.getName(), id); }
}
