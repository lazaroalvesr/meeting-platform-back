package com.project.meeting_platform.auth.Service.Prospecting;

import com.project.meeting_platform.Model.ProspectedLead;
import com.project.meeting_platform.Model.User;
import com.project.meeting_platform.Repository.Prospecting.ProspectedLeadRepository;
import com.project.meeting_platform.Repository.User.UserRepository;
import com.project.meeting_platform.auth.dto.Prospecting.ProspectedLeadResponse;
import com.project.meeting_platform.auth.dto.Prospecting.SaveProspectedLeadRequest;
import com.project.meeting_platform.auth.dto.Prospecting.FollowUpMessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProspectedLeadService {

    private final ProspectedLeadRepository prospectedLeadRepository;
    private final UserRepository userRepository;
    private final LeadAnalysisService leadAnalysisService;

    public ProspectedLeadService(
            ProspectedLeadRepository prospectedLeadRepository,
            UserRepository userRepository,
            LeadAnalysisService leadAnalysisService
    ) {
        this.prospectedLeadRepository = prospectedLeadRepository;
        this.userRepository = userRepository;
        this.leadAnalysisService = leadAnalysisService;
    }

    @Transactional(readOnly = true)
    public List<ProspectedLeadResponse> list(String authenticatedEmail) {
        return prospectedLeadRepository.findByOwner_EmailOrderByProspectedAtDesc(authenticatedEmail)
                .stream()
                .map(ProspectedLeadResponse::from)
                .toList();
    }

    @Transactional
    public ProspectedLeadResponse save(String authenticatedEmail, SaveProspectedLeadRequest request) {
        String deduplicationKey = deduplicationKey(request);
        return prospectedLeadRepository
                .findByOwner_EmailAndDeduplicationKey(authenticatedEmail, deduplicationKey)
                .map(ProspectedLeadResponse::from)
                .orElseGet(() -> create(authenticatedEmail, request, deduplicationKey));
    }

    @Transactional
    public ProspectedLeadResponse registerFollowUp(String authenticatedEmail, UUID leadId) {
        ProspectedLead lead = prospectedLeadRepository.findByIdAndOwner_Email(leadId, authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead prospectado não encontrado."));

        lead.registerFollowUp();
        return ProspectedLeadResponse.from(lead);
    }

    @Transactional(readOnly = true)
    public FollowUpMessageResponse suggestFollowUp(String authenticatedEmail, UUID leadId) {
        ProspectedLead lead = prospectedLeadRepository.findByIdAndOwner_Email(leadId, authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead prospectado não encontrado."));
        return new FollowUpMessageResponse(leadAnalysisService.generateFollowUpMessage(lead));
    }

    private ProspectedLeadResponse create(
            String authenticatedEmail,
            SaveProspectedLeadRequest request,
            String deduplicationKey
    ) {
        User owner = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário autenticado não encontrado."));

        ProspectedLead lead = new ProspectedLead(
                owner,
                normalize(request.providerPlaceId()),
                deduplicationKey,
                request.name().trim(),
                normalize(request.category()),
                request.city().trim(),
                request.state().trim().toUpperCase(Locale.ROOT),
                normalize(request.publicAddress()),
                normalize(request.website()),
                normalize(request.phone()),
                normalize(request.sourceUrl()),
                request.priorityScore(),
                normalize(request.openingMessage())
        );

        return ProspectedLeadResponse.from(prospectedLeadRepository.save(lead));
    }

    private String deduplicationKey(SaveProspectedLeadRequest request) {
        if (request.providerPlaceId() != null && !request.providerPlaceId().isBlank()) {
            return "place:" + request.providerPlaceId().trim();
        }
        return (request.name() + "|" + request.city() + "|" + request.state() + "|" + value(request.phone()))
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
