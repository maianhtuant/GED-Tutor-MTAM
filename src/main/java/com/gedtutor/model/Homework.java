package com.gedtutor.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "homework")
public class Homework {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /** Optional: link homework to a specific video lesson. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id")
    private Video video;

    private LocalDateTime dueDate;

    @Column(nullable = false)
    private boolean published = true;

    /**
     * If set, each quiz attempt presents this many randomly selected questions
     * from the homework's pool. If null or 0, all questions are shown.
     */
    @Column
    private Integer questionsPerAttempt;

    /**
     * Target size of the question pool — the number of questions the admin
     * plans to fill in. Drives the "Question X of Y" counter on the
     * bulk-add flow. Independent of questionsPerAttempt (which is how many
     * get randomly drawn per attempt). Null means no target was declared.
     */
    @Column
    private Integer poolSize;

    /**
     * When true, this quiz auto-generates fill-in-the-blank math problems
     * from the math problem templates. The hand-authored {@code questions}
     * list is ignored on attempts.
     */
    @Column(nullable = false)
    private boolean mathQuiz = false;

    /** Total math questions to generate per attempt when {@link #mathQuiz} is true. */
    @Column
    private Integer mathQuestionCount = 40;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Questions attached to this homework. These are shared across
     * homeworks — the same question can appear in multiple. The join
     * table is created/maintained by JPA.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "homework_questions",
            joinColumns = @JoinColumn(name = "homework_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions = new ArrayList<>();

    /**
     * Random-math-problem templates linked to this homework. They are
     * shown on the homework detail page as practice exercises (each
     * template generates a fresh problem on every visit) and are graded
     * separately from the static MCQ/fill-blank questions above.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "homework_math_templates",
            joinColumns = @JoinColumn(name = "homework_id"),
            inverseJoinColumns = @JoinColumn(name = "template_id")
    )
    private List<MathProblemTemplate> mathProblemTemplates = new ArrayList<>();

    public Homework() {}

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public Integer getQuestionsPerAttempt() { return questionsPerAttempt; }
    public void setQuestionsPerAttempt(Integer questionsPerAttempt) { this.questionsPerAttempt = questionsPerAttempt; }

    public Integer getPoolSize() { return poolSize; }
    public void setPoolSize(Integer poolSize) { this.poolSize = poolSize; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public List<MathProblemTemplate> getMathProblemTemplates() { return mathProblemTemplates; }
    public void setMathProblemTemplates(List<MathProblemTemplate> mathProblemTemplates) {
        this.mathProblemTemplates = mathProblemTemplates;
    }

    public boolean isMathQuiz() { return mathQuiz; }
    public void setMathQuiz(boolean mathQuiz) { this.mathQuiz = mathQuiz; }

    public Integer getMathQuestionCount() { return mathQuestionCount; }
    public void setMathQuestionCount(Integer mathQuestionCount) { this.mathQuestionCount = mathQuestionCount; }
}
