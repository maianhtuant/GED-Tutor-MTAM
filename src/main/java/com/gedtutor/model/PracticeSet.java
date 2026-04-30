package com.gedtutor.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A curated bundle of math-problem templates. Students "run" a practice
 * set sequentially: each {@link PracticeSetItem} contributes
 * {@code questionCount} freshly-generated problems from its template.
 *
 * <p>The set itself is admin-managed (same pattern as Homework).
 * Question generation happens at runtime — the set never stores the
 * actual problems, only the recipe.
 */
@Entity
@Table(name = "practice_sets")
public class PracticeSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // EAGER for the same reason items is eager — the admin list and the
    // student start page both render the subject name, and open-in-view
    // is off so the session is closed by render time.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Ordered list of items. Eagerly fetched — every page that touches a
     * practice set wants the items too, and Spring's default
     * {@code open-in-view=false} closes the session before Thymeleaf
     * renders, so a lazy fetch would blow up.
     */
    @OneToMany(mappedBy = "practiceSet", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orderIndex ASC, id ASC")
    private List<PracticeSetItem> items = new ArrayList<>();

    public PracticeSet() {}

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    /** Total questions a student will see in one full run. */
    public int totalQuestionCount() {
        int total = 0;
        for (PracticeSetItem it : items) {
            total += Math.max(0, it.getQuestionCount());
        }
        return total;
    }

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<PracticeSetItem> getItems() { return items; }
    public void setItems(List<PracticeSetItem> items) { this.items = items; }
}
