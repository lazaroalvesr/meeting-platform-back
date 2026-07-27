package com.project.meeting_platform.auth.controller.Project;

import com.project.meeting_platform.auth.Service.Project.ProjectService;
import com.project.meeting_platform.auth.dto.Project.CreateProjectRequest;
import com.project.meeting_platform.auth.dto.Project.ProjectResponse;
import com.project.meeting_platform.auth.dto.Project.UpdateProjectRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.create(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(projectService.list(authentication.getName()));
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(projectService.update(authentication.getName(), projectId, request));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID projectId,
            Authentication authentication
    ) {
        projectService.delete(authentication.getName(), projectId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{projectId}/contract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjectResponse> uploadContract(
            @PathVariable UUID projectId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            Authentication authentication
    ) {
        return ResponseEntity.ok(projectService.uploadContract(authentication.getName(), projectId, file));
    }

    @GetMapping("/{projectId}/contract")
    public ResponseEntity<Resource> downloadContract(
            @PathVariable UUID projectId,
            Authentication authentication
    ) {
        Resource contract = projectService.loadContract(authentication.getName(), projectId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=contrato-" + projectId + ".pdf")
                .body(contract);
    }
}
