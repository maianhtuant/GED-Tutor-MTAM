package com.gedtutor.controller;

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
        model.addAttribute("videos", videoService.listPublicBySubject(subjectId));
        // Show ALL subjects in the filter dropdown (including deactivated ones) so
        // that videos under a deactivated subject are still discoverable on the
        // public videos page. Deactivation only hides a subject from the admin
        // "new video / new homework" pickers.
        model.addAttribute("subjects", subjectService.listAll());
        model.addAttribute("selectedSubjectId", subjectId);
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
