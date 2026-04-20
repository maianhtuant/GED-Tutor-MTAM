package com.gedtutor.dto;

import com.gedtutor.model.GedSubject;
import com.gedtutor.model.VideoVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VideoForm {
    private Long id;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    @NotBlank
    @Size(max = 500)
    private String videoUrl;

    @NotNull
    private GedSubject subject;

    @NotNull
    private VideoVisibility visibility;

    public VideoForm() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public GedSubject getSubject() { return subject; }
    public void setSubject(GedSubject subject) { this.subject = subject; }

    public VideoVisibility getVisibility() { return visibility; }
    public void setVisibility(VideoVisibility visibility) { this.visibility = visibility; }
}
