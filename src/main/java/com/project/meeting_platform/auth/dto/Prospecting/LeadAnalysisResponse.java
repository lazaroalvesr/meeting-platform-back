package com.project.meeting_platform.auth.dto.Prospecting;

import java.util.List;

public record LeadAnalysisResponse(
        String status,
        int score,
        String opportunity,
        List<String> strengths,
        List<String> cautions,
        String suggestedApproach,
        String observationUsed,
        String openingChosen,
        String message,
        String followUp3Days,
        String confidence
) {
}
