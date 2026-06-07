package com.eciwise.study.quiz;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Respuesta individual dentro de una sesion. La unicidad (session, question) evita
 * responder dos veces la misma pregunta y, por tanto, doble conteo en estadisticas.
 */
@Entity
@Table(name = "quiz_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private QuizSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;

    @Column(name = "given_answer", columnDefinition = "text")
    private String givenAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "time_taken_ms")
    private Integer timeTakenMs;

    @Column(name = "points_awarded", nullable = false)
    private int pointsAwarded;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt;
}
