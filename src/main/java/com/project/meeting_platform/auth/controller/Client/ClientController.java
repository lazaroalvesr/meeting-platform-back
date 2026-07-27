package com.project.meeting_platform.auth.controller.Client;

import com.project.meeting_platform.auth.Service.Client.ClientService;
import com.project.meeting_platform.auth.dto.Client.ClientResponse;
import com.project.meeting_platform.auth.dto.Client.CreateClientRequest;
import com.project.meeting_platform.auth.dto.Client.UpdateClientRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(
            @Valid @RequestBody CreateClientRequest request,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clientService.create(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> list(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                clientService.list(authentication.getName())
        );
    }

    @PatchMapping("/{clientId}")
    public ResponseEntity<ClientResponse> update(
            @PathVariable UUID clientId,
            @Valid @RequestBody UpdateClientRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(clientService.update(authentication.getName(), clientId, request));
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID clientId,
            Authentication authentication
    ) {
        clientService.delete(authentication.getName(), clientId);
        return ResponseEntity.noContent().build();
    }
}
