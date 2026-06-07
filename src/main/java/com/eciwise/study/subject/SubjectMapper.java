package com.eciwise.study.subject;

import com.eciwise.study.subject.dto.SubjectResponse;
import org.springframework.stereotype.Component;

@Component
public class SubjectMapper {

    public SubjectResponse toResponse(Subject subject) {
        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                subject.getCreatedAt(),
                subject.getUpdatedAt()
        );
    }
}
