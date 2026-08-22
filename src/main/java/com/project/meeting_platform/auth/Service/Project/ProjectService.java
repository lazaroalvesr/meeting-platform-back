package com.project.meeting_platform.auth.Service.Project;

import com.project.meeting_platform.Model.Client;
import com.project.meeting_platform.Model.Payment;
import com.project.meeting_platform.Model.Project;
import com.project.meeting_platform.Model.User;
import com.project.meeting_platform.Enum.Payment.PaymentStatus;
import com.project.meeting_platform.Enum.Payment.PaymentType;
import com.project.meeting_platform.Repository.Client.ClientRepository;
import com.project.meeting_platform.Repository.Payment.PaymentRepository;
import com.project.meeting_platform.Repository.Project.ProjectRepository;
import com.project.meeting_platform.Repository.User.UserRepository;
import com.project.meeting_platform.auth.dto.Project.CreateProjectRequest;
import com.project.meeting_platform.auth.dto.Project.ProjectResponse;
import com.project.meeting_platform.auth.dto.Project.UpdateProjectRequest;
import com.project.meeting_platform.auth.Service.Payment.MaintenanceBillingService;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class ProjectService {

    private static final long MAX_CONTRACT_SIZE_BYTES = 10L * 1024 * 1024;

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final MaintenanceBillingService maintenanceBillingService;

    public ProjectService(
            ProjectRepository projectRepository,
            ClientRepository clientRepository,
            UserRepository userRepository,
            PaymentRepository paymentRepository,
            MaintenanceBillingService maintenanceBillingService
    ) {
        this.projectRepository = projectRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.maintenanceBillingService = maintenanceBillingService;
    }

    @Transactional
    public ProjectResponse create(String authenticatedEmail, CreateProjectRequest request) {
        validateMaintenance(request.maintenanceActive(), request.maintenanceMonthlyValue(), request.maintenanceStartDate());
        User owner = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário autenticado não encontrado."));

        Client client = clientRepository.findById(request.clientId())
                .filter(foundClient -> foundClient.getOwner().getId().equals(owner.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado."));

        Project project = new Project(
                client,
                owner,
                request.name().trim(),
                request.projectType(),
                request.status(),
                normalize(request.scope()),
                request.totalValue(),
                request.contractStatus(),
                normalize(request.contractUrl()),
                request.maintenanceActive(),
                request.maintenanceActive() ? request.maintenanceMonthlyValue() : null,
                resolveMaintenanceStartDate(request.maintenanceActive(), request.maintenanceStartDate(), request.deliveryDate(), request.startDate()),
                request.startDate(),
                request.deliveryDate()
        );

        projectRepository.save(project);
        createProjectPayments(project, request.installmentCount(), request.firstInstallmentCardInstallmentCount(), request.secondInstallmentCardInstallmentCount());
        syncMaintenanceBilling(project);
        return ProjectResponse.from(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(String authenticatedEmail) {
        return projectRepository.findByOwner_EmailOrderByCreatedAtDesc(authenticatedEmail)
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional
    public ProjectResponse update(String authenticatedEmail, java.util.UUID projectId, UpdateProjectRequest request) {
        validateMaintenance(request.maintenanceActive(), request.maintenanceMonthlyValue(), request.maintenanceStartDate());
        Project project = projectRepository.findById(projectId)
                .filter(foundProject -> foundProject.getOwner().getEmail().equals(authenticatedEmail))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado."));

        project.update(
                request.name().trim(),
                request.projectType(),
                request.status(),
                normalize(request.scope()),
                request.totalValue(),
                request.contractStatus(),
                normalize(request.contractUrl()),
                request.maintenanceActive(),
                request.maintenanceMonthlyValue(),
                resolveMaintenanceStartDate(request.maintenanceActive(), request.maintenanceStartDate(), request.deliveryDate(), request.startDate()),
                request.startDate(),
                request.deliveryDate()
        );
        rescheduleFinalPayment(project);
        syncMaintenanceBilling(project);

        return ProjectResponse.from(project);
    }

    @Transactional
    public void delete(String authenticatedEmail, java.util.UUID projectId) {
        Project project = findOwnedProject(authenticatedEmail, projectId);
        paymentRepository.deleteByProject_Id(project.getId());
        projectRepository.delete(project);

        try {
            Files.deleteIfExists(Path.of("uploads", "contracts", project.getId() + ".pdf").toAbsolutePath().normalize());
        } catch (IOException ignored) {
            // A exclusão do registro não deve falhar apenas porque o arquivo local já não existe.
        }
    }

    @Transactional
    public ProjectResponse uploadContract(
            String authenticatedEmail,
            java.util.UUID projectId,
            MultipartFile file
    ) {
        Project project = findOwnedProject(authenticatedEmail, projectId);
        validatePdf(file);

        try {
            Path contractsDirectory = Path.of("uploads", "contracts").toAbsolutePath().normalize();
            Files.createDirectories(contractsDirectory);
            Path target = contractsDirectory.resolve(project.getId() + ".pdf");
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            project.attachContract("/api/projects/" + project.getId() + "/contract");
            return ProjectResponse.from(project);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível salvar o contrato em PDF.");
        }
    }

    @Transactional(readOnly = true)
    public Resource loadContract(String authenticatedEmail, java.util.UUID projectId) {
        Project project = findOwnedProject(authenticatedEmail, projectId);
        if (project.getContractUrl() == null || !project.getContractUrl().endsWith("/contract")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum contrato em PDF foi anexado a este projeto.");
        }

        Path contractPath = Path.of("uploads", "contracts", project.getId() + ".pdf").toAbsolutePath().normalize();
        if (!Files.isRegularFile(contractPath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo do contrato não encontrado.");
        }
        return new FileSystemResource(contractPath);
    }

    private Project findOwnedProject(String authenticatedEmail, java.util.UUID projectId) {
        return projectRepository.findById(projectId)
                .filter(foundProject -> foundProject.getOwner().getEmail().equals(authenticatedEmail))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado."));
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um arquivo PDF.");
        }
        if (file.getSize() > MAX_CONTRACT_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "O contrato deve ter no máximo 10 MB.");
        }
        String fileName = file.getOriginalFilename();
        boolean hasPdfExtension = fileName != null && fileName.toLowerCase().endsWith(".pdf");
        boolean hasPdfType = "application/pdf".equalsIgnoreCase(file.getContentType());
        if (!hasPdfExtension || !hasPdfType) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie somente arquivos em PDF.");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LocalDate resolveMaintenanceStartDate(
            boolean maintenanceActive,
            LocalDate requestedMaintenanceStartDate,
            LocalDate deliveryDate,
            LocalDate startDate
    ) {
        if (!maintenanceActive) {
            return null;
        }
        if (requestedMaintenanceStartDate != null) {
            return requestedMaintenanceStartDate;
        }
        return deliveryDate != null ? deliveryDate : startDate;
    }

    private void validateMaintenance(
            boolean maintenanceActive,
            BigDecimal maintenanceMonthlyValue,
            LocalDate maintenanceStartDate
    ) {
        if (!maintenanceActive) {
            return;
        }
        if (maintenanceMonthlyValue == null || maintenanceMonthlyValue.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o valor mensal da manutenção.");
        }
        if (maintenanceStartDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o primeiro vencimento da manutenção.");
        }
    }

    private void syncMaintenanceBilling(Project project) {
        if (project.isMaintenanceActive()) {
            maintenanceBillingService.createEligiblePayments(project);
        }
    }

    private void createProjectPayments(
            Project project,
            Integer requestedInstallments,
            Integer firstInstallmentCardInstallmentCount,
            Integer secondInstallmentCardInstallmentCount
    ) {
        if (project.getTotalValue() == null || project.getTotalValue().signum() <= 0) {
            return;
        }

        int installments = requestedInstallments == null ? 1 : requestedInstallments;
        LocalDate dueDate = project.getDeliveryDate();

        if (installments == 2) {
            BigDecimal entryAmount = project.getTotalValue()
                    .multiply(new BigDecimal("0.50"))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal finalAmount = project.getTotalValue().subtract(entryAmount);

            Payment entryPayment = new Payment(
                    project,
                    "Entrada 50% - " + project.getName(),
                    PaymentType.PROJECT,
                    PaymentStatus.PENDING,
                    entryAmount,
                    dueDate,
                    null
            );
            entryPayment.setCardInstallmentCount(firstInstallmentCardInstallmentCount);
            paymentRepository.save(entryPayment);

            Payment finalPayment = new Payment(
                    project,
                    "Saldo final 50% - " + project.getName(),
                    PaymentType.PROJECT,
                    PaymentStatus.PENDING,
                    finalAmount,
                    dueDate,
                    null
            );
            finalPayment.setCardInstallmentCount(secondInstallmentCardInstallmentCount);
            paymentRepository.save(finalPayment);
            return;
        }

        paymentRepository.save(new Payment(
                project,
                "Pagamento do projeto - " + project.getName(),
                PaymentType.PROJECT,
                PaymentStatus.PENDING,
                project.getTotalValue(),
                dueDate,
                null
        ));
    }

    private void rescheduleFinalPayment(Project project) {
        if (project.getDeliveryDate() == null) {
            return;
        }

        paymentRepository.findByProject_Id(project.getId())
                .stream()
                .filter(payment -> payment.getDescription().startsWith("Saldo final 50% - "))
                .forEach(payment -> payment.reschedule(project.getDeliveryDate()));
    }
}
