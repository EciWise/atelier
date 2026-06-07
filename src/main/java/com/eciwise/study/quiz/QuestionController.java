package com.eciwise.study.quiz;

import com.eciwise.study.quiz.dto.QuestionRequest;
import com.eciwise.study.quiz.dto.QuestionResponse;
import com.eciwise.study.quiz.dto.QuestionStatsResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gestion del banco de preguntas (admin/tutor). Estos endpoints exponen la respuesta
 * correcta; los estudiantes solo ven preguntas a traves del motor de quiz (QuizController).
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public List<QuestionResponse> listBySubject(@RequestParam Long subjectId,
                                                @RequestParam(required = false) Integer corte) {
        return questionService.listBySubject(subjectId, corte);
    }

    @GetMapping("/{id}")
    public QuestionResponse findById(@PathVariable Long id) {
        return questionService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse create(@Valid @RequestBody QuestionRequest request) {
        return questionService.create(request);
    }

    @PutMapping("/{id}")
    public QuestionResponse update(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        return questionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        questionService.delete(id);
    }

    @GetMapping("/{id}/stats")
    public QuestionStatsResponse stats(@PathVariable Long id) {
        return questionService.stats(id);
    }
}
