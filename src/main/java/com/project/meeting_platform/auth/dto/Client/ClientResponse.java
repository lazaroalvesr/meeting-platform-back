package com.project.meeting_platform.auth.dto.Client;

import com.project.meeting_platform.Model.Client;

import java.time.Instant;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String name,
        String companyName,
        String email,
        String phone,
        String document,
        String notes,
        Instant createdAt
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getCompanyName(),
                client.getEmail(),
                client.getPhone(),
                client.getDocument(),
                client.getNotes(),
                client.getCreatedAt()
        );
    }
}
