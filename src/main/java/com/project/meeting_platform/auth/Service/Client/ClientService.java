package com.project.meeting_platform.auth.Service.Client;

import com.project.meeting_platform.Model.Client;
import com.project.meeting_platform.Model.User;
import com.project.meeting_platform.Repository.Client.ClientRepository;
import com.project.meeting_platform.Repository.Project.ProjectRepository;
import com.project.meeting_platform.Repository.User.UserRepository;
import com.project.meeting_platform.auth.dto.Client.ClientResponse;
import com.project.meeting_platform.auth.dto.Client.CreateClientRequest;
import com.project.meeting_platform.auth.dto.Client.UpdateClientRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public ClientService(
            ClientRepository clientRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository
    ) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ClientResponse create(
            String authenticatedEmail,
            CreateClientRequest request
    ) {
        User owner = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuário autenticado não encontrado."
                ));

        Client client = new Client(
                owner,
                request.name().trim(),
                normalize(request.companyName()),
                normalize(request.email()),
                normalize(request.phone()),
                normalize(request.document()),
                normalize(request.notes())
        );

        clientRepository.save(client);

        return ClientResponse.from(client);
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> list(String authenticatedEmail) {
        return clientRepository
                .findByOwner_EmailOrderByCreatedAtDesc(authenticatedEmail)
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

    @Transactional
    public void delete(String authenticatedEmail, UUID clientId) {
        Client client = clientRepository.findById(clientId)
                .filter(foundClient -> foundClient.getOwner().getEmail().equals(authenticatedEmail))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado."));

        clientRepository.delete(client);
    }

    @Transactional
    public ClientResponse update(String authenticatedEmail, UUID clientId, UpdateClientRequest request) {
        Client client = clientRepository.findById(clientId)
                .filter(foundClient -> foundClient.getOwner().getEmail().equals(authenticatedEmail))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado."));

        client.update(
                request.name().trim(),
                normalize(request.companyName()),
                normalize(request.email()),
                normalize(request.phone()),
                normalize(request.document()),
                normalize(request.notes())
        );

        return ClientResponse.from(client);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
