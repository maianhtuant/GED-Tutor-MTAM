package com.gedtutor.dto;

import jakarta.validation.constraints.*;

/**
 * Form-backing DTO for creating / editing a QUADRATIC math template.
 * The six coefficient-range fields are serialised into
 * {@code MathProblemTemplate.parametersJson} as JSON (matching
 * {@code QuadraticSolver.Config}).
 */
public class QuadraticTemplateForm {

    private Long id;   // null → create, non-null → edit

    @NotBlank(message = "Label is required")
    @Size(max = 120, message = "Label must be 120 characters or fewer")
    private String label;

    // ── Coefficient ranges ────────────────────────────────────────────────────
    // These map directly to QuadraticSolver.Config fields.

    @Min(1)  @Max(20)  private int aMin = 1;
    @Min(1)  @Max(20)  private int aMax = 5;

    @Min(-20) @Max(0)  private int bMin = -10;
    @Min(0)  @Max(20)  private int bMax = 10;

    @Min(-20) @Max(0)  private int cMin = -10;
    @Min(0)  @Max(20)  private int cMax = 10;

    @Size(max = 500)
    private String videoUrl;

    private Long subjectId;

    private boolean active = true;

    /** Relative tolerance (%) used when grading numeric answers. */
    @NotNull(message = "Tolerance is required")
    @DecimalMin(value = "0.0", message = "Tolerance must be 0 or greater")
    private Double tolerancePercent = 0.5;

    /** Whether a rounded decimal hint is shown/accepted alongside exact fractions/roots. */
    private boolean roundAnswer = true;

    /** Decimal places to round displayed/graded answers to. */
    @Min(value = 0, message = "Decimal places must be 0 or more")
    @Max(value = 6, message = "Decimal places must be 6 or fewer")
    private int decimalPlaces = 2;

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId()               { return id; }
    public void setId(Long id)        { this.id = id; }

    public String getLabel()          { return label; }
    public void setLabel(String l)    { this.label = l; }

    public int getAMin()              { return aMin; }
    public void setAMin(int v)        { this.aMin = v; }

    public int getAMax()              { return aMax; }
    public void setAMax(int v)        { this.aMax = v; }

    public int getBMin()              { return bMin; }
    public void setBMin(int v)        { this.bMin = v; }

    public int getBMax()              { return bMax; }
    public void setBMax(int v)        { this.bMax = v; }

    public int getCMin()              { return cMin; }
    public void setCMin(int v)        { this.cMin = v; }

    public int getCMax()              { return cMax; }
    public void setCMax(int v)        { this.cMax = v; }

    public String getVideoUrl()       { return videoUrl; }
    public void setVideoUrl(String v) { this.videoUrl = v; }

    public Long getSubjectId()        { return subjectId; }
    public void setSubjectId(Long v)  { this.subjectId = v; }

    public boolean isActive()         { return active; }
    public void setActive(boolean v)  { this.active = v; }

    public Double getTolerancePercent()            { return tolerancePercent; }
    public void setTolerancePercent(Double v)      { this.tolerancePercent = v; }

    public boolean isRoundAnswer()                 { return roundAnswer; }
    public void setRoundAnswer(boolean v)          { this.roundAnswer = v; }

    public int getDecimalPlaces()                  { return decimalPlaces; }
    public void setDecimalPlaces(int v)             { this.decimalPlaces = v; }
}
