package com.project.meeting_platform.Repository.Client;

import com.project.meeting_platform.Model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    List<Client> findByOwner_EmailOrderByCreatedAtDesc(String ownerEmail);
}
