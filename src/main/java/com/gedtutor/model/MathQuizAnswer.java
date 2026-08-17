package com.gedtutor.model;

import jakarta.persistence.*;

/**
 * One graded question within a math-quiz-mode {@link QuizAttempt}. The
 * generated problem itself only ever lives in the HTTP session
 * ({@code MathQuizState}) — this row is the durable record of exactly
 * what the student saw and typed, used by the admin "Overview" page.
 */
@Entity
@Table(name = "math_quiz_answers")
public class MathQuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

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

    public MathQuizAnswer() {}

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QuizAttempt getAttempt() { return attempt; }
    public void setAttempt(QuizAttempt attempt) { this.attempt = attempt; }

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
