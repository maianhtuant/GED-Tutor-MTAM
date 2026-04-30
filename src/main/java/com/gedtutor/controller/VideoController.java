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
import java.util.TreeMap;

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
    public String list(Model model) {
        List<Subject> subjects  = subjectService.listAll();
        List<Video>   allVideos = videoService.listPublic();

        // subjectId → (category → videos)  — used by the tab panels
        // Category "General" catches videos with no category set.
        Map<Long, Map<String, List<Video>>> videosByCat = new LinkedHashMap<>();
        Map<Long, Integer> videoCountBySubject = new LinkedHashMap<>();
        for (Subject s : subjects) {
            List<Video> svids = videoService.listPublicBySubject(s.getId());
            Map<String, List<Video>> byCategory = new TreeMap<>();
            for (Video v : svids) {
                String cat = (v.getCategory() != null && !v.getCategory().isBlank())
                        ? v.getCategory().trim() : "General";
                byCategory.computeIfAbsent(cat, k -> new java.util.ArrayList<>()).add(v);
            }
            videosByCat.put(s.getId(), byCategory);
            videoCountBySubject.put(s.getId(), svids.size());
        }

        model.addAttribute("subjects",            subjects);
        model.addAttribute("allVideos",           allVideos);
        model.addAttribute("videosByCat",         videosByCat);
        model.addAttribute("videoCountBySubject", videoCountBySubject);
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
