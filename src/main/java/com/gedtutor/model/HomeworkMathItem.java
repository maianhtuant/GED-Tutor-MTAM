package com.gedtutor.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

/**
 * One row in a {@link Homework}'s math-quiz recipe: "use this template,
 * generate this many questions from it". Mirrors {@link PracticeSetItem}
 * on a practice set. Order in the parent's list is driven by
 * {@link #orderIndex}.
 */
@Entity
@Table(name = "homework_math_items")
public class HomeworkMathItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "homework_id", nullable = false)
    private Homework homework;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private MathProblemTemplate template;

    @Min(1)
    @Column(nullable = false)
    private int questionCount = 5;

    @Column(nullable = false)
    private int orderIndex = 0;

    public HomeworkMathItem() {}

    public HomeworkMathItem(Homework homework, MathProblemTemplate template, int questionCount, int orderIndex) {
        this.homework = homework;
        this.template = template;
        this.questionCount = questionCount;
        this.orderIndex = orderIndex;
    }

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Homework getHomework() { return homework; }
    public void setHomework(Homework homework) { this.homework = homework; }

    public MathProblemTemplate getTemplate() { return template; }
    public void setTemplate(MathProblemTemplate template) { this.template = template; }

    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
