package com.gedtutor.repository;

import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import com.gedtutor.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MathProblemTemplateRepository extends JpaRepository<MathProblemTemplate, Long> {
    List<MathProblemTemplate> findBySubjectAndActiveTrueOrderByIdAsc(Subject subject);
    List<MathProblemTemplate> findByKindAndActiveTrueOrderByIdAsc(MathProblemKind kind);
    List<MathProblemTemplate> findByActiveTrueOrderByIdAsc();
}
