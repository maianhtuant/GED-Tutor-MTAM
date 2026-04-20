package com.gedtutor.dto;

import com.gedtutor.model.GedSubject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class HomeworkForm {
    private Long id;

    @NotBlank
    private String title;

    private String instructions;

    @NotNull
    private GedSubject subject;

    private Long videoId;

    private LocalDateTime dueDate;

    private boolean published = true;

    public HomeworkForm() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public GedSubject getSubject() { return subject; }
    public void setSubject(GedSubject subject) { this.subject = subject; }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
}
