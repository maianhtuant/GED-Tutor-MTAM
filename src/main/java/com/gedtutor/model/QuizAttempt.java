package com.gedtutor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "homework_id", nullable = false)
    private Homework homework;

    @Column(nullable = false)
    private int attemptNumber = 1;

    private Integer score;

    private Integer totalQuestions;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizAnswer> answers = new ArrayList<>();

    /**
     * Question IDs selected for this attempt (supports "pick N of M" quizzes).
     * Populated on startAttempt. Empty list means "use the homework's full pool".
     * EAGER because the set is small (N ≤ pool size) and we always read it when
     * we render the quiz page — avoids LazyInitializationException when the
     * attempt is detached (open-in-view=false).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "quiz_attempt_questions",
            joinColumns = @JoinColumn(name = "attempt_id")
    )
    @Column(name = "question_id")
    @OrderColumn(name = "order_index")
    private List<Long> questionIds = new ArrayList<>();

    public QuizAttempt() {}

    @PrePersist
    void onCreate() {
        if (startedAt == null) startedAt = LocalDateTime.now();
    }

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Homework getHomework() { return homework; }
    public void setHomework(Homework homework) { this.homework = homework; }

    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public List<QuizAnswer> getAnswers() { return answers; }
    public void setAnswers(List<QuizAnswer> answers) { this.answers = answers; }

    public List<Long> getQuestionIds() { return questionIds; }
    public void setQuestionIds(List<Long> questionIds) { this.questionIds = questionIds; }
}
