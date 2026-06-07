package com.eciwise.study.quiz;

import com.eciwise.study.quiz.dto.OptionResponse;
import com.eciwise.study.quiz.dto.QuestionCollectionResponse;
import com.eciwise.study.quiz.dto.QuestionResponse;
import com.eciwise.study.quiz.dto.QuizOptionResponse;
import com.eciwise.study.quiz.dto.QuizQuestionResponse;
import com.eciwise.study.quiz.dto.SessionSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class QuizMapper {

    /** Vista completa (admin/tutor) con la respuesta correcta. */
    public QuestionResponse toResponse(Question q) {
        return new QuestionResponse(
                q.getId(),
                q.getSubject().getId(),
                q.getSubject().getName(),
                q.getCorte(),
                q.getType(),
                q.getStatement(),
                q.getExplanation(),
                q.getCorrectAnswer(),
                q.isAvailableForSurvival(),
                q.getTimeLimitSeconds(),
                q.getOptions().stream()
                        .map(o -> new OptionResponse(o.getId(), o.getText(), o.isCorrect(), o.getPosition()))
                        .toList(),
                q.getCreatedAt(),
                q.getUpdatedAt()
        );
    }

    /** Vista jugable: sin revelar cual opcion o texto es correcto. */
    public QuizQuestionResponse toQuizQuestion(Question q) {
        return new QuizQuestionResponse(
                q.getId(),
                q.getType(),
                q.getStatement(),
                q.getTimeLimitSeconds(),
                q.getType() == QuestionType.CLOSED
                        ? q.getOptions().stream()
                        .map(o -> new QuizOptionResponse(o.getId(), o.getText(), o.getPosition()))
                        .toList()
                        : java.util.List.of()
        );
    }

    public QuestionCollectionResponse toResponse(QuestionCollection c) {
        return new QuestionCollectionResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getSubject() == null ? null : c.getSubject().getId(),
                c.getSubject() == null ? null : c.getSubject().getName(),
                c.getItems() == null ? 0 : c.getItems().size(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    public SessionSummaryResponse toSummary(QuizSession s) {
        int answered = s.getCorrectCount() + s.getIncorrectCount();
        double accuracy = answered == 0 ? 0.0 : (s.getCorrectCount() * 100.0) / answered;
        return new SessionSummaryResponse(
                s.getId(),
                s.getMode(),
                s.getStatus(),
                s.getSubject() == null ? null : s.getSubject().getId(),
                s.getCorte(),
                s.getCollection() == null ? null : s.getCollection().getId(),
                s.getTotalQuestions(),
                s.getCorrectCount(),
                s.getIncorrectCount(),
                s.getScore(),
                Math.round(accuracy * 100.0) / 100.0,
                s.getLivesRemaining(),
                s.getStartedAt(),
                s.getFinishedAt()
        );
    }
}
