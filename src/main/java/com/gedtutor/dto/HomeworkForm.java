package com.gedtutor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class HomeworkForm {
    private Long id;

    @NotBlank
    private String title;

    private String instructions;

    @NotNull
    private Long subjectId;

    private Long videoId;

    private LocalDateTime dueDate;

    private boolean published = true;

    /** Null or 0 = use all questions. Otherwise, random sample of this many. */
    private Integer questionsPerAttempt;

    /** Target pool size — how many questions the admin plans to add.
     *  Drives the "Question X of Y" counter on the bulk-add flow. */
    private Integer poolSize;

    public HomeworkForm() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public Integer getQuestionsPerAttempt() { return questionsPerAttempt; }
    public void setQuestionsPerAttempt(Integer questionsPerAttempt) { this.questionsPerAttempt = questionsPerAttempt; }

    public Integer getPoolSize() { return poolSize; }
    public void setPoolSize(Integer poolSize) { this.poolSize = poolSize; }
}
