package com.project.meeting_platform.auth.Service.Prospecting;

import com.project.meeting_platform.auth.dto.Prospecting.CompanySearchRequest;
import com.project.meeting_platform.auth.dto.Prospecting.CompanySearchResult;

import java.util.List;

public interface SearchCompaniesProvider {

    List<CompanySearchResult> search(CompanySearchRequest request, int limit);
}
