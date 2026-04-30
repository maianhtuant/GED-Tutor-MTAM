package com.gedtutor.service;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import com.gedtutor.repository.MathProblemTemplateRepository;
import com.gedtutor.service.math.MathProblemGenerator;
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
        return g.generate(template);
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
