package com.project.meeting_platform.auth.dto.Prospecting;

import com.project.meeting_platform.Model.ProspectedLead;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProspectedLeadResponse(
        UUID id,
        String name,
        String category,
        String city,
        String state,
        String publicAddress,
        String website,
        String phone,
        String sourceUrl,
        Integer priorityScore,
        String openingMessage,
        Instant prospectedAt,
        int followUpCount,
        LocalDate nextFollowUpDate,
        Instant lastFollowUpAt
) {
    public static ProspectedLeadResponse from(ProspectedLead lead) {
        return new ProspectedLeadResponse(
                lead.getId(), lead.getName(), lead.getCategory(), lead.getCity(), lead.getState(),
                lead.getPublicAddress(), lead.getWebsite(), lead.getPhone(), lead.getSourceUrl(),
                lead.getPriorityScore(), lead.getOpeningMessage(), lead.getProspectedAt(), lead.getFollowUpCount(),
                lead.getNextFollowUpDate(), lead.getLastFollowUpAt()
        );
    }
}
