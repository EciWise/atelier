package com.eciwise.study.flashcard.dto;

import java.util.List;

public record UsageSummaryResponse(
        long totalUsed,
        List<UsageResponse> history
) {
}
