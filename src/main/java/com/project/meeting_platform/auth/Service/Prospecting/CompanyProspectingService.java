package com.project.meeting_platform.auth.Service.Prospecting;

import com.project.meeting_platform.auth.dto.Prospecting.CompanySearchRequest;
import com.project.meeting_platform.auth.dto.Prospecting.CompanySearchResult;
import com.project.meeting_platform.config.ProspectingProperties;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyProspectingService {

    private final SearchCompaniesProvider searchCompaniesProvider;
    private final ProspectingProperties properties;

    public CompanyProspectingService(
            SearchCompaniesProvider searchCompaniesProvider,
            ProspectingProperties properties
    ) {
        this.searchCompaniesProvider = searchCompaniesProvider;
        this.properties = properties;
    }

    public List<CompanySearchResult> searchCompanies(CompanySearchRequest request) {
        int configuredMaximum = properties.maxCompanies() > 0 ? properties.maxCompanies() : 20;
        int limit = Math.min(request.quantity(), configuredMaximum);
        return searchCompaniesProvider.search(request, limit);
    }
}
