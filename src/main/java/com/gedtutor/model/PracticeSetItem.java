package com.gedtutor.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

/**
 * One row in a {@link PracticeSet}: "use this template, generate this
 * many questions from it". Order in the parent's list is driven by
 * {@link #orderIndex}.
 */
@Entity
@Table(name = "practice_set_items")
public class PracticeSetItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practice_set_id", nullable = false)
    private PracticeSet practiceSet;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private MathProblemTemplate template;

    @Min(1)
    @Column(nullable = false)
    private int questionCount = 5;

    @Column(nullable = false)
    private int orderIndex = 0;

    public PracticeSetItem() {}

    public PracticeSetItem(PracticeSet practiceSet, MathProblemTemplate template, int questionCount, int orderIndex) {
        this.practiceSet = practiceSet;
        this.template = template;
        this.questionCount = questionCount;
        this.orderIndex = orderIndex;
    }

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PracticeSet getPracticeSet() { return practiceSet; }
    public void setPracticeSet(PracticeSet practiceSet) { this.practiceSet = practiceSet; }

    public MathProblemTemplate getTemplate() { return template; }
    public void setTemplate(MathProblemTemplate template) { this.template = template; }

    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
