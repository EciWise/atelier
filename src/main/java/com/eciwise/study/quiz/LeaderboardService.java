package com.eciwise.study.quiz;

import com.eciwise.study.quiz.dto.LeaderboardEntryResponse;
import com.eciwise.study.quiz.dto.LeaderboardResponse;
import com.eciwise.study.quiz.dto.LeaderboardScore;
import com.eciwise.study.quiz.dto.PagedResponse;
import com.eciwise.study.user.AppUser;
import com.eciwise.study.user.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Ranking publico de Supervivencia: tabla general paginada (mejor puntaje por usuario)
 * mas el puesto del usuario consultor. El puesto se calcula contando cuantos usuarios
 * tienen un mejor puntaje, de modo que es consistente ante empates.
 */
@Service
public class LeaderboardService {

    private final QuizSessionRepository sessionRepository;
    private final CurrentUserService currentUserService;

    public LeaderboardService(QuizSessionRepository sessionRepository,
                              CurrentUserService currentUserService) {
        this.sessionRepository = sessionRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public LeaderboardResponse survivalLeaderboard(Pageable pageable) {
        AppUser user = currentUserService.getOrCreate();

        Page<LeaderboardScore> page = sessionRepository.leaderboard(pageable);
        long offset = (long) page.getNumber() * page.getSize();
        List<LeaderboardEntryResponse> rows = new ArrayList<>();
        long index = 0;
        for (LeaderboardScore s : page.getContent()) {
            rows.add(new LeaderboardEntryResponse(
                    offset + (++index), s.userId(), fullName(s), s.bestScore()));
        }
        PagedResponse<LeaderboardEntryResponse> table = new PagedResponse<>(
                rows, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());

        Integer myBest = sessionRepository.findBestSurvivalScore(user.getId());
        Long myRank = myBest == null ? null : sessionRepository.countUsersWithBetterScore(myBest) + 1;

        return new LeaderboardResponse(table, myRank, myBest);
    }

    private String fullName(LeaderboardScore s) {
        String name = ((s.firstName() == null ? "" : s.firstName()) + " "
                + (s.lastName() == null ? "" : s.lastName())).trim();
        return name.isEmpty() ? "Anonimo" : name;
    }
}
