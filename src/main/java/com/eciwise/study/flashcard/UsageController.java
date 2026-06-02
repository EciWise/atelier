package com.eciwise.study.flashcard;

import com.eciwise.study.flashcard.dto.UsageResponse;
import com.eciwise.study.flashcard.dto.UsageSummaryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @PostMapping("/flashcards/{id}/use")
    @ResponseStatus(HttpStatus.CREATED)
    public UsageResponse use(@PathVariable Long id) {
        return usageService.recordUsage(id);
    }

    @GetMapping("/usage/me")
    public UsageSummaryResponse mySummary() {
        return usageService.mySummary();
    }
}
