package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;

/**
 * One generator per {@link MathProblemKind}. Implementations are
 * Spring-managed beans collected by {@code MathProblemService} into a
 * registry keyed by kind. Add a new kind by writing a new {@code @Service}
 * implementing this interface — Spring picks it up automatically.
 */
public interface MathProblemGenerator {
    /** The kind this generator handles. */
    MathProblemKind kind();

    /**
     * Build a fresh problem instance from the template's parameters JSON
     * (or sensible defaults if it's null).
     */
    GeneratedMathProblem generate(MathProblemTemplate template);
}
