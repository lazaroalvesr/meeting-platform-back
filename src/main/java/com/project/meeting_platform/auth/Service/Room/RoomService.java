package com.project.meeting_platform.auth.Service.Room;

import com.project.meeting_platform.Model.Room;
import com.project.meeting_platform.Model.User;
import com.project.meeting_platform.Repository.Room.RoomRepository;
import com.project.meeting_platform.Repository.User.UserRepository;
import com.project.meeting_platform.auth.dto.Room.CreateRoomRequest;
import com.project.meeting_platform.auth.dto.Room.PublicRoomResponse;
import com.project.meeting_platform.auth.dto.Room.RoomResponse;
import com.project.meeting_platform.auth.dto.Room.UpdatePresentationSettingsRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String SLUG_CHARACTERS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int SLUG_LENGTH = 10;
    private static final int MAX_SLUG_GENERATION_ATTEMPTS = 10;

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public RoomService(
            RoomRepository roomRepository,
            UserRepository userRepository
    ) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RoomResponse create(
            String authenticatedEmail,
            CreateRoomRequest request
    ) {
        User host = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário autenticado não encontrado."
                ));

        String slug = generateUniqueSlug();

        Room room = new Room(
                host,
                slug,
                request.title().trim(),
                request.projectUrl().trim()
        );

        roomRepository.save(room);

        return RoomResponse.from(room);
    }

    private String generateUniqueSlug() {
        for (int attempt = 0;
             attempt < MAX_SLUG_GENERATION_ATTEMPTS;
             attempt++) {

            String slug = generateSlug();

            if (!roomRepository.existsBySlug(slug)) {
                return slug;
            }
        }

        throw new IllegalStateException(
                "Não foi possível gerar um link único para a sala."
        );
    }

    private String generateSlug() {
        StringBuilder slug = new StringBuilder(SLUG_LENGTH);

        for (int index = 0; index < SLUG_LENGTH; index++) {
            int randomIndex = SECURE_RANDOM.nextInt(
                    SLUG_CHARACTERS.length()
            );

            slug.append(SLUG_CHARACTERS.charAt(randomIndex));
        }

        return slug.toString();
    }

    @Transactional(readOnly = true)
    public Optional<PublicRoomResponse> findPublicBySlug(String slug) {
        return roomRepository.findBySlug(slug)
                .map(PublicRoomResponse::from);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> listForHost(String authenticatedEmail) {
        return roomRepository
                .findByHost_EmailOrderByCreatedAtDesc(authenticatedEmail)
                .stream()
                .map(RoomResponse::from)
                .toList();
    }

    @Transactional
    public RoomResponse start(String authenticatedEmail, String slug) {
        Room room = roomRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Sala não encontrada."
                ));

        if (!room.getHost().getEmail().equals(authenticatedEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não pode iniciar esta sala."
            );
        }

        room.activate();
        return RoomResponse.from(room);
    }

    @Transactional
    public RoomResponse updatePresentationSettings(
            String authenticatedEmail,
            String slug,
            UpdatePresentationSettingsRequest request
    ) {
        Room room = findOwnedRoom(authenticatedEmail, slug);
        room.setScrollLocked(request.scrollLocked());
        room.setPresentationActive(request.presentationActive());
        return RoomResponse.from(room);
    }

    @Transactional
    public RoomResponse close(String authenticatedEmail, String slug) {
        Room room = findOwnedRoom(authenticatedEmail, slug);
        room.close();
        return RoomResponse.from(room);
    }

    @Transactional
    public void delete(String authenticatedEmail, String slug) {
        roomRepository.delete(findOwnedRoom(authenticatedEmail, slug));
    }

    private Room findOwnedRoom(String authenticatedEmail, String slug) {
        Room room = roomRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Sala não encontrada."
                ));

        if (!room.getHost().getEmail().equals(authenticatedEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não pode alterar esta sala."
            );
        }

        return room;
    }
}
