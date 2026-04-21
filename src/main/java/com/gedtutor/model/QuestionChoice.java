package com.gedtutor.model;

import jakarta.persistence.*;

@Entity
@Table(name = "question_choices")
public class QuestionChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String choiceText;

    @Column(nullable = false)
    private int orderIndex = 0;

    public QuestionChoice() {}

    public QuestionChoice(Question question, String choiceText, int orderIndex) {
        this.question = question;
        this.choiceText = choiceText;
        this.orderIndex = orderIndex;
    }

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public String getChoiceText() { return choiceText; }
    public void setChoiceText(String choiceText) { this.choiceText = choiceText; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
