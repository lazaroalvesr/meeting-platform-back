package com.project.meeting_platform.Repository.Project;

import com.project.meeting_platform.Model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByOwner_EmailOrderByCreatedAtDesc(String ownerEmail);

    List<Project> findByMaintenanceActiveTrue();

    List<Project> findByClient_Id(UUID clientId);

}
