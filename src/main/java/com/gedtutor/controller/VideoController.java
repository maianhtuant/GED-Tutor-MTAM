package com.gedtutor.controller;

import com.gedtutor.model.Subject;
import com.gedtutor.model.Video;
import com.gedtutor.model.VideoVisibility;
import com.gedtutor.service.SubjectService;
import com.gedtutor.service.VideoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/videos")
public class VideoController {

    private final VideoService videoService;
    private final SubjectService subjectService;

    public VideoController(VideoService videoService, SubjectService subjectService) {
        this.videoService = videoService;
        this.subjectService = subjectService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long subjectId, Model model) {
        List<Subject> subjects = subjectService.listAll();
        List<Video>   allVideos = videoService.listPublic();

        // Build a map: subjectId → videos for that subject (used by the tab panels)
        Map<Long, List<Video>> videosBySubject = new LinkedHashMap<>();
        for (Subject s : subjects) {
            videosBySubject.put(s.getId(), videoService.listPublicBySubject(s.getId()));
        }

        model.addAttribute("subjects",        subjects);
        model.addAttribute("allVideos",        allVideos);
        model.addAttribute("videosBySubject",  videosBySubject);
        return "videos";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Video v = videoService.findById(id);
        if (v.getVisibility() != VideoVisibility.PUBLIC) {
            ra.addFlashAttribute("error", "This video is not available.");
            return "redirect:/videos";
        }
        model.addAttribute("video", v);
        model.addAttribute("embedUrl", videoService.embedUrl(v));
        return "video-detail";
    }
}
