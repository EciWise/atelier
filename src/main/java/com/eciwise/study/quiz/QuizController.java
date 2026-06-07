package com.eciwise.study.quiz;

import com.eciwise.study.quiz.dto.AnswerRequest;
import com.eciwise.study.quiz.dto.AnswerResultResponse;
import com.eciwise.study.quiz.dto.LeaderboardResponse;
import com.eciwise.study.quiz.dto.PagedResponse;
import com.eciwise.study.quiz.dto.SessionResponse;
import com.eciwise.study.quiz.dto.SessionSummaryResponse;
import com.eciwise.study.quiz.dto.StartSessionRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Motor de quiz (Parcial / Repaso / Supervivencia), historial del usuario y
 * ranking publico de Supervivencia.
 */
@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private static final int MAX_PAGE_SIZE = 100;

    private final QuizService quizService;
    private final LeaderboardService leaderboardService;

    public QuizController(QuizService quizService, LeaderboardService leaderboardService) {
        this.quizService = quizService;
        this.leaderboardService = leaderboardService;
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse start(@Valid @RequestBody StartSessionRequest request) {
        return quizService.start(request);
    }

    @PostMapping("/sessions/{id}/answers")
    public AnswerResultResponse answer(@PathVariable Long id, @Valid @RequestBody AnswerRequest request) {
        return quizService.answer(id, request);
    }

    @PostMapping("/sessions/{id}/finish")
    public SessionSummaryResponse finish(@PathVariable Long id) {
        return quizService.finish(id);
    }

    @GetMapping("/sessions/{id}")
    public SessionSummaryResponse getSession(@PathVariable Long id) {
        return quizService.getSession(id);
    }

    @GetMapping("/history")
    public PagedResponse<SessionSummaryResponse> history(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return quizService.history(PageRequest.of(page, clampSize(size)));
    }

    @GetMapping("/survival/leaderboard")
    public LeaderboardResponse leaderboard(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return leaderboardService.survivalLeaderboard(PageRequest.of(page, clampSize(size)));
    }

    private int clampSize(int size) {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }
}
