package com.gedtutor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Admin form-binding DTO for practice sets. Mirrors the Homework /
 * Subject form pattern: raw ids on the form so Thymeleaf doesn't need a
 * Subject converter.
 */
public class PracticeSetForm {

    private Long id;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    /** May be null — subject is optional. */
    private Long subjectId;

    private boolean active = true;

    public PracticeSetForm() {}

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
