package com.project.meeting_platform.auth.controller.Prospecting;

import com.project.meeting_platform.auth.Service.Prospecting.CompanyProspectingService;
import com.project.meeting_platform.auth.Service.Prospecting.GooglePlacesCityAutocompleteService;
import com.project.meeting_platform.auth.Service.Prospecting.LeadAnalysisService;
import com.project.meeting_platform.auth.Service.Prospecting.ProspectedLeadService;
import com.project.meeting_platform.auth.dto.Prospecting.AnalyzeLeadRequest;
import com.project.meeting_platform.auth.dto.Prospecting.CitySuggestion;
import com.project.meeting_platform.auth.dto.Prospecting.CompanySearchRequest;
import com.project.meeting_platform.auth.dto.Prospecting.CompanySearchResult;
import com.project.meeting_platform.auth.dto.Prospecting.LeadAnalysisResponse;
import com.project.meeting_platform.auth.dto.Prospecting.ProspectedLeadResponse;
import com.project.meeting_platform.auth.dto.Prospecting.SaveProspectedLeadRequest;
import com.project.meeting_platform.auth.dto.Prospecting.FollowUpMessageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/prospecting")
public class ProspectingController {

    private final CompanyProspectingService companyProspectingService;
    private final GooglePlacesCityAutocompleteService cityAutocompleteService;
    private final LeadAnalysisService leadAnalysisService;
    private final ProspectedLeadService prospectedLeadService;

    public ProspectingController(
            CompanyProspectingService companyProspectingService,
            GooglePlacesCityAutocompleteService cityAutocompleteService,
            LeadAnalysisService leadAnalysisService,
            ProspectedLeadService prospectedLeadService
    ) {
        this.companyProspectingService = companyProspectingService;
        this.cityAutocompleteService = cityAutocompleteService;
        this.leadAnalysisService = leadAnalysisService;
        this.prospectedLeadService = prospectedLeadService;
    }

    @GetMapping("/cities/autocomplete")
    public ResponseEntity<List<CitySuggestion>> suggestCities(
            @RequestParam String input,
            @RequestParam(required = false) String state
    ) {
        if (input == null || input.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(cityAutocompleteService.suggest(input, state));
    }

    @PostMapping("/companies/search")
    public ResponseEntity<List<CompanySearchResult>> searchCompanies(
            @Valid @RequestBody CompanySearchRequest request
    ) {
        return ResponseEntity.ok(companyProspectingService.searchCompanies(request));
    }

    @PostMapping("/leads/analyze")
    public ResponseEntity<LeadAnalysisResponse> analyzeLead(
            @Valid @RequestBody AnalyzeLeadRequest request
    ) {
        return ResponseEntity.ok(leadAnalysisService.analyze(request));
    }

    @GetMapping("/leads/prospected")
    public ResponseEntity<List<ProspectedLeadResponse>> listProspectedLeads(Authentication authentication) {
        return ResponseEntity.ok(prospectedLeadService.list(authentication.getName()));
    }

    @PostMapping("/leads/prospected")
    public ResponseEntity<ProspectedLeadResponse> saveProspectedLead(
            @Valid @RequestBody SaveProspectedLeadRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prospectedLeadService.save(authentication.getName(), request));
    }

    @PostMapping("/leads/prospected/{leadId}/follow-ups")
    public ResponseEntity<ProspectedLeadResponse> registerFollowUp(
            @PathVariable UUID leadId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(prospectedLeadService.registerFollowUp(authentication.getName(), leadId));
    }

    @PostMapping("/leads/prospected/{leadId}/follow-up-message")
    public ResponseEntity<FollowUpMessageResponse> suggestFollowUp(
            @PathVariable UUID leadId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(prospectedLeadService.suggestFollowUp(authentication.getName(), leadId));
    }
}
