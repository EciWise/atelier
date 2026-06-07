package com.eciwise.study.quiz;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Decide cuantas y cuales preguntas debe hacer el estudiante en el modo Parcial.
 *
 * Cantidad: a partir de la "intensidad" combinando urgencia (dias para el parcial),
 * brecha de preparacion y ambicion (meta de nota), escalada entre MIN_Q y MAX_Q.
 *
 * Seleccion: prioriza las que el estudiante ha fallado antes, luego las que nunca ha
 * visto, y por ultimo el resto; con barajado dentro de cada grupo.
 */
@Component
public class ParcialSelector {

    static final int MIN_Q = 5;
    static final int MAX_Q = 30;

    public int desiredCount(int daysUntilExam, int preparedness, BigDecimal targetGrade, int available) {
        double urgency = 1.0 / (1.0 + daysUntilExam / 7.0);
        double gap = (5 - preparedness) / 4.0;
        double ambition = targetGrade.doubleValue() / 5.0;
        double intensity = (urgency + gap + ambition) / 3.0;

        int count = (int) Math.round(MIN_Q + intensity * (MAX_Q - MIN_Q));
        count = Math.max(MIN_Q, count);
        return Math.min(count, available);
    }

    public List<Question> select(List<Question> pool, int count,
                                 Set<Long> incorrectIds, Set<Long> answeredIds) {
        List<Question> failed = new ArrayList<>();
        List<Question> unseen = new ArrayList<>();
        List<Question> rest = new ArrayList<>();
        for (Question q : pool) {
            if (incorrectIds.contains(q.getId())) {
                failed.add(q);
            } else if (!answeredIds.contains(q.getId())) {
                unseen.add(q);
            } else {
                rest.add(q);
            }
        }
        Collections.shuffle(failed);
        Collections.shuffle(unseen);
        Collections.shuffle(rest);

        List<Question> ordered = new ArrayList<>(pool.size());
        ordered.addAll(failed);
        ordered.addAll(unseen);
        ordered.addAll(rest);
        return ordered.subList(0, Math.min(count, ordered.size()));
    }
}
