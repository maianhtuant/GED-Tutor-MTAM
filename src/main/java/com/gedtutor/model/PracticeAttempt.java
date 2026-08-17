package com.gedtutor.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * A single student's run through a {@link PracticeSet}. Created when the
 * run starts ({@link com.gedtutor.service.PracticeRunService#start}) and
 * finalized (score + completedAt) once every generated question has been
 * answered. Mirrors {@link QuizAttempt}'s shape so admin reporting can
 * treat quiz and practice history the same way.
 */
@Entity
@Table(name = "practice_attempts")
public class PracticeAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practice_set_id", nullable = false)
    private PracticeSet practiceSet;

    private Integer score;

    private Integer totalQuestions;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    public PracticeAttempt() {}

    @PrePersist
    void onCreate() {
        if (startedAt == null) startedAt = LocalDateTime.now();
    }

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public PracticeSet getPracticeSet() { return practiceSet; }
    public void setPracticeSet(PracticeSet practiceSet) { this.practiceSet = practiceSet; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
