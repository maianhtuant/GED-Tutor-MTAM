package com.gedtutor.dto;

import jakarta.validation.constraints.*;

/**
 * Form-backing DTO for creating / editing a FREE_FORM math template.
 * The three generator-specific fields (template, minValue, maxValue) are
 * serialised into {@code MathProblemTemplate.parametersJson} as JSON by the
 * controller before saving.
 */
public class FreeFormTemplateForm {

    private Long id;   // null → create, non-null → edit

    @NotBlank(message = "Label is required")
    @Size(max = 120, message = "Label must be 120 characters or fewer")
    private String label;

    /**
     * The raw template string, e.g. "{a}x^2 + {b}x + {c} = 0".
     * Supports any LaTeX-friendly text; {word} tokens become random integers.
     */
    @NotBlank(message = "Template is required")
    @Size(max = 500, message = "Template must be 500 characters or fewer")
    private String template;

    /**
     * Arithmetic formula for the correct answer using the same placeholder names
     * as the template, e.g. {@code a/b + c/d}.
     * Supports +, -, *, / and parentheses. Leave blank for display-only templates.
     */
    @Size(max = 200)
    private String answerFormula;

    @Min(value = 1, message = "Min value must be at least 1")
    private int minValue = 1;

    @Min(value = 2, message = "Max value must be at least 2")
    @Max(value = 9999, message = "Max value must be 9999 or less")
    private int maxValue = 20;

    @Size(max = 500)
    private String videoUrl;

    private Long subjectId;

    private boolean active = true;

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }

    public int getMinValue() { return minValue; }
    public void setMinValue(int minValue) { this.minValue = minValue; }

    public int getMaxValue() { return maxValue; }
    public void setMaxValue(int maxValue) { this.maxValue = maxValue; }

    public String getAnswerFormula() { return answerFormula; }
    public void setAnswerFormula(String answerFormula) { this.answerFormula = answerFormula; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
