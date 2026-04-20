package com.gedtutor.controller;

import com.gedtutor.model.GedSubject;
import com.gedtutor.model.Video;
import com.gedtutor.model.VideoVisibility;
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

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) GedSubject subject, Model model) {
        model.addAttribute("videos", videoService.listPublicBySubject(subject));
        model.addAttribute("subjects", GedSubject.values());
        model.addAttribute("selectedSubject", subject);
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
