package com.gedtutor.service;

import com.gedtutor.model.MathProblemTemplate;
import com.gedtutor.model.PracticeSet;
import com.gedtutor.model.PracticeSetItem;
import com.gedtutor.repository.MathProblemTemplateRepository;
import com.gedtutor.repository.PracticeAnswerRepository;
import com.gedtutor.repository.PracticeAttemptRepository;
import com.gedtutor.repository.PracticeSetItemRepository;
import com.gedtutor.repository.PracticeSetRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD façade for {@link PracticeSet} and its items. Run-time state
 * (the in-progress walk) lives in {@link PracticeRunService}; this
 * class only manages the persisted recipe.
 */
@Service
public class PracticeSetService {

    private final PracticeSetRepository setRepo;
    private final PracticeSetItemRepository itemRepo;
    private final MathProblemTemplateRepository templateRepo;
    private final PracticeAttemptRepository attemptRepo;
    private final PracticeAnswerRepository answerRepo;

    public PracticeSetService(PracticeSetRepository setRepo,
                              PracticeSetItemRepository itemRepo,
                              MathProblemTemplateRepository templateRepo,
                              PracticeAttemptRepository attemptRepo,
                              PracticeAnswerRepository answerRepo) {
        this.setRepo = setRepo;
        this.itemRepo = itemRepo;
        this.templateRepo = templateRepo;
        this.attemptRepo = attemptRepo;
        this.answerRepo = answerRepo;
    }

    public List<PracticeSet> listAll() {
        return setRepo.findAllByOrderByCreatedAtDesc();
    }

    public List<PracticeSet> listActive() {
        return setRepo.findByActiveTrueOrderByCreatedAtDesc();
    }

    public PracticeSet findById(Long id) {
        return setRepo.findById(id).orElseThrow(
                () -> new EntityNotFoundException("PracticeSet not found: " + id));
    }

    @Transactional
    public PracticeSet save(PracticeSet set) {
        return setRepo.save(set);
    }

    /**
     * Delete a practice set along with its dependent practice_attempts
     * (and their practice_answers) — otherwise the FKs block the delete
     * for any set a student has actually run (same pattern as
     * {@code HomeworkService.delete} clearing quiz_attempts first).
     * PracticeSetItems are cleaned up automatically via cascade/orphanRemoval.
     */
    @Transactional
    public void delete(Long id) {
        PracticeSet set = findById(id);
        answerRepo.deleteByPracticeSet(set);
        answerRepo.flush();
        attemptRepo.deleteByPracticeSet(set);
        attemptRepo.flush();
        setRepo.deleteById(id);
    }

    /**
     * One-shot helper: create a brand-new practice set whose items
     * randomly draw {@code totalQuestions} across every active template
     * (or every template whose id is in {@code templateIds} if non-null).
     *
     * <p>Question counts are distributed as evenly as possible — the
     * remainder is spread across the first {@code totalQuestions % n}
     * templates so totals always sum to {@code totalQuestions} exactly.
     *
     * <p>If there are no active templates this returns a saved empty set.
     */
    @Transactional
    public PracticeSet createQuickMix(String title, String description, int totalQuestions, java.util.List<Long> templateIds) {
        java.util.List<MathProblemTemplate> chosen;
        if (templateIds == null || templateIds.isEmpty()) {
            chosen = templateRepo.findByActiveTrueOrderByIdAsc();
        } else {
            chosen = new java.util.ArrayList<>();
            for (Long id : templateIds) {
                templateRepo.findById(id).ifPresent(chosen::add);
            }
        }

        PracticeSet set = new PracticeSet();
        set.setTitle(title == null || title.isBlank() ? "Math Quiz — " + totalQuestions + " questions" : title);
        set.setDescription(description);
        set.setActive(true);
        PracticeSet saved = setRepo.save(set);

        if (chosen.isEmpty() || totalQuestions <= 0) return saved;

        int n = chosen.size();
        int base = totalQuestions / n;
        int remainder = totalQuestions % n;
        // Shuffle so the +1 doesn't always land on the same first templates.
        java.util.Collections.shuffle(chosen);
        for (int i = 0; i < n; i++) {
            int count = base + (i < remainder ? 1 : 0);
            if (count <= 0) continue;
            PracticeSetItem item = new PracticeSetItem(saved, chosen.get(i), count, i);
            saved.getItems().add(item);
            itemRepo.save(item);
        }
        return saved;
    }

    @Transactional
    public PracticeSetItem addItem(Long setId, Long templateId, int questionCount) {
        PracticeSet set = findById(setId);
        MathProblemTemplate template = templateRepo.findById(templateId).orElseThrow(
                () -> new EntityNotFoundException("Template not found: " + templateId));
        int order = set.getItems().isEmpty()
                ? 0
                : set.getItems().get(set.getItems().size() - 1).getOrderIndex() + 1;
        PracticeSetItem item = new PracticeSetItem(set, template, Math.max(1, questionCount), order);
        set.getItems().add(item);
        itemRepo.save(item);
        return item;
    }

    @Transactional
    public void removeItem(Long itemId) {
        itemRepo.deleteById(itemId);
    }

    /**
     * Move an item up or down by swapping order indices with its neighbor.
     * No-op at the ends.
     */
    @Transactional
    public void moveItem(Long itemId, int delta) {
        PracticeSetItem item = itemRepo.findById(itemId).orElseThrow(
                () -> new EntityNotFoundException("Item not found: " + itemId));
        List<PracticeSetItem> siblings = item.getPracticeSet().getItems();
        int idx = siblings.indexOf(item);
        int neighbor = idx + delta;
        if (neighbor < 0 || neighbor >= siblings.size()) return;
        PracticeSetItem other = siblings.get(neighbor);
        int tmp = item.getOrderIndex();
        item.setOrderIndex(other.getOrderIndex());
        other.setOrderIndex(tmp);
        itemRepo.save(item);
        itemRepo.save(other);
    }
}
