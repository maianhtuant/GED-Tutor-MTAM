package com.gedtutor.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;

import java.time.LocalDateTime;

/**
 * A reusable template for randomly-generated math problems. Each
 * generator interprets the {@link #parametersJson} field according to its
 * own configuration shape — that lets a single table support many
 * problem kinds without an exploding column list.
 *
 * <p>For QUADRATIC, the JSON looks like
 * <pre>{@code {"aMin":1,"aMax":10,"bMin":-10,"bMax":10,"cMin":-10,"cMax":10}}</pre>
 * Other kinds document their own shape inside their generator class.
 */
@Entity
@Table(name = "math_problem_templates")
public class MathProblemTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MathProblemKind kind = MathProblemKind.QUADRATIC;

    /** Human-readable label shown in admin lists, e.g. "Quadratic — easy". */
    @Column(nullable = false, length = 120)
    private String label = "Quadratic";

    /**
     * Generator-specific configuration as JSON. May be null for generators
     * that have a sensible all-defaults mode. See the generator class for
     * the exact shape it expects.
     */
    @Column(name = "parameters_json", columnDefinition = "TEXT")
    private String parametersJson;

    /**
     * Tolerance used when grading numeric answers (relative %).
     * Default 0.5%.
     */
    @DecimalMin("0.0")
    @Column(nullable = false)
    private Double tolerancePercent = 0.5;

    /**
     * Whether a rounded decimal hint is shown alongside exact fraction answers
     * (e.g. "2/3 ≈ 0.67"). When false, only the exact fraction is shown/accepted
     * unless the answer is inherently irrational, in which case it's always rounded.
     */
    @Column(nullable = false)
    private boolean roundAnswer = true;

    /** Number of decimal places to round displayed/graded answers to. */
    @Column(nullable = false)
    private int decimalPlaces = 2;

    /**
     * Fill-in-the-blank (student types the answer) or multiple-choice
     * (student picks from auto-generated options). See {@link AnswerMode}
     * for which problem shapes actually support multiple-choice.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "answer_mode", nullable = false, length = 20)
    private AnswerMode answerMode = AnswerMode.FILL_IN_BLANK;

    /** Optional URL of the lesson video. */
    @Column(length = 500)
    private String videoUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public MathProblemTemplate() {}

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public MathProblemKind getKind() { return kind; }
    public void setKind(MathProblemKind kind) { this.kind = kind; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getParametersJson() { return parametersJson; }
    public void setParametersJson(String parametersJson) { this.parametersJson = parametersJson; }

    public Double getTolerancePercent() { return tolerancePercent; }
    public void setTolerancePercent(Double tolerancePercent) { this.tolerancePercent = tolerancePercent; }

    public boolean isRoundAnswer() { return roundAnswer; }
    public void setRoundAnswer(boolean roundAnswer) { this.roundAnswer = roundAnswer; }

    public int getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(int decimalPlaces) { this.decimalPlaces = decimalPlaces; }

    public AnswerMode getAnswerMode() { return answerMode; }
    public void setAnswerMode(AnswerMode answerMode) { this.answerMode = answerMode; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
