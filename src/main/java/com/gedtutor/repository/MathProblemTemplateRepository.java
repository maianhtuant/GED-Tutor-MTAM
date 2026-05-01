package com.gedtutor.repository;

import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import com.gedtutor.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MathProblemTemplateRepository extends JpaRepository<MathProblemTemplate, Long> {
    List<MathProblemTemplate> findBySubjectAndActiveTrueOrderByIdAsc(Subject subject);

    /**
     * Eagerly fetches the subject association so callers can access
     * subject.getName() after the session closes (e.g. in the admin list view).
     */
    @Query("SELECT t FROM MathProblemTemplate t LEFT JOIN FETCH t.subject " +
           "WHERE t.kind = :kind AND t.active = true ORDER BY t.id ASC")
    List<MathProblemTemplate> findByKindAndActiveTrueOrderByIdAsc(@Param("kind") MathProblemKind kind);

    @Query("SELECT t FROM MathProblemTemplate t LEFT JOIN FETCH t.subject " +
           "WHERE t.active = true ORDER BY t.id ASC")
    List<MathProblemTemplate> findByActiveTrueOrderByIdAsc();
}
