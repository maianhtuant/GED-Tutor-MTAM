package com.gedtutor.service;

import com.gedtutor.dto.AnswerShape;
import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.model.AnswerMode;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import com.gedtutor.repository.MathProblemTemplateRepository;
import com.gedtutor.service.math.MathProblemGenerator;
import com.gedtutor.service.math.MultipleChoiceSupport;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Façade that routes a {@link MathProblemTemplate} to the right per-kind
 * {@link MathProblemGenerator}. Spring auto-collects every generator bean
 * into the constructor list; we index them by kind at startup so
 * generation is an O(1) map lookup.
 */
@Service
public class MathProblemService {

    /** Standard GED-style option count for auto-generated multiple-choice problems. */
    private static final int MULTIPLE_CHOICE_OPTION_COUNT = 4;

    private final MathProblemTemplateRepository repository;
    private final Map<MathProblemKind, MathProblemGenerator> generators;

    public MathProblemService(MathProblemTemplateRepository repository,
                              List<MathProblemGenerator> generatorBeans) {
        this.repository = repository;
        Map<MathProblemKind, MathProblemGenerator> idx = new EnumMap<>(MathProblemKind.class);
        for (MathProblemGenerator g : generatorBeans) {
            MathProblemGenerator prev = idx.put(g.kind(), g);
            if (prev != null) {
                throw new IllegalStateException("Two generators registered for " + g.kind()
                        + ": " + prev.getClass() + " vs " + g.getClass());
            }
        }
        this.generators = idx;
    }

    /** Generate a fresh instance of the given template. */
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        MathProblemGenerator g = generators.get(template.getKind());
        if (g == null) {
            throw new IllegalStateException("No generator registered for " + template.getKind());
        }
        return applyAnswerMode(template, g.generate(template));
    }

    /**
     * When the template is set to MULTIPLE_CHOICE, attach auto-generated
     * answer options — but only for problems that resolve to a single plain
     * numeric answer. Problems needing 2+ values (quadratic roots, (x, y)
     * pairs) or a special non-numeric answer ("no real solution") stay
     * fill-in-the-blank even if the template asked for multiple-choice,
     * since matching several values to one choice isn't meaningful.
     */
    private GeneratedMathProblem applyAnswerMode(MathProblemTemplate template, GeneratedMathProblem problem) {
        if (template.getAnswerMode() != AnswerMode.MULTIPLE_CHOICE) return problem;
        if (problem.hasSpecialAnswer()
                || problem.shape() != AnswerShape.SCALAR
                || problem.expectedAnswers().size() != 1) {
            return problem;
        }
        List<String> choices = MultipleChoiceSupport.buildChoices(
                problem.expectedAnswers().get(0), problem.decimalPlaces(), problem.roundAnswer(),
                MULTIPLE_CHOICE_OPTION_COUNT);
        return problem.withChoices(choices);
    }

    /**
     * Pick a random active template (any kind). If no templates exist
     * yet, fall back to randomly picking among ALL registered generators
     * — that way a fresh install with no DB rows still surfaces every
     * problem kind on the practice page.
     */
    public GeneratedMathProblem generateRandomActive() {
        List<MathProblemTemplate> active = repository.findByActiveTrueOrderByIdAsc();
        if (!active.isEmpty()) {
            MathProblemTemplate t = active.get(ThreadLocalRandom.current().nextInt(active.size()));
            return generate(t);
        }
        // No DB rows — pick a random kind from the registered generators.
        MathProblemKind[] kinds = generators.keySet().toArray(new MathProblemKind[0]);
        MathProblemKind chosen = kinds[ThreadLocalRandom.current().nextInt(kinds.length)];
        return generate(defaultTemplateFor(chosen));
    }

    public Optional<MathProblemTemplate> findById(Long id) {
        return repository.findById(id);
    }

    /** Empty template of a given kind — generator uses its own defaults for parametersJson. */
    private static MathProblemTemplate defaultTemplateFor(MathProblemKind kind) {
        MathProblemTemplate t = new MathProblemTemplate();
        t.setKind(kind);
        t.setLabel(kind.name());
        return t;
    }
}
