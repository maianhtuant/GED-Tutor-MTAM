package com.gedtutor.model;

import jakarta.persistence.*;

/**
 * One graded question within a {@link PracticeAttempt}. The generated
 * problem itself only ever lives in the HTTP session ({@code
 * PracticeRunState}) — this row is the durable record of exactly what
 * the student saw and typed, used by the admin "Overview" page.
 */
@Entity
@Table(name = "practice_answers")
public class PracticeAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private PracticeAttempt attempt;

    @Column(nullable = false)
    private int questionIndex;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    @Column(columnDefinition = "TEXT")
    private String expectedAnswer;

    @Column(columnDefinition = "TEXT")
    private String studentAnswer;

    @Column(nullable = false)
    private boolean correct = false;

    public PracticeAnswer() {}

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PracticeAttempt getAttempt() { return attempt; }
    public void setAttempt(PracticeAttempt attempt) { this.attempt = attempt; }

    public int getQuestionIndex() { return questionIndex; }
    public void setQuestionIndex(int questionIndex) { this.questionIndex = questionIndex; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getExpectedAnswer() { return expectedAnswer; }
    public void setExpectedAnswer(String expectedAnswer) { this.expectedAnswer = expectedAnswer; }

    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}
