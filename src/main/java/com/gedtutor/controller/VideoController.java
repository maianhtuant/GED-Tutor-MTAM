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
        // Root subjects only appear in the top tab bar
        List<Subject> rootSubjects = subjectService.listRoots();
        // All active subjects (roots + children) — needed for video bucketing
        List<Subject> allSubjects  = subjectService.listActive();
        List<Video>   allVideos    = videoService.listPublic();

        // subjectId → (category → videos)  — used by the tab panels
        Map<Long, Map<String, List<Video>>> videosByCat = new LinkedHashMap<>();
        Map<Long, Integer> videoCountBySubject = new LinkedHashMap<>();
        for (Subject s : allSubjects) {
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

        // parentId → list of child Subject objects — used by Thymeleaf template loops
        Map<Long, List<Subject>> childrenByParentId = new LinkedHashMap<>();
        // parentId → list of {id,name,count} maps — safe for JS inline serialization (no JPA proxies)
        Map<Long, List<Map<String, Object>>> childrenForJs = new LinkedHashMap<>();

        for (Subject s : rootSubjects) {
            List<Subject> children = subjectService.listChildren(s);
            if (!children.isEmpty()) {
                childrenByParentId.put(s.getId(), children);

                // count for parent tab = sum of its own videos + all child videos
                int total = videoCountBySubject.getOrDefault(s.getId(), 0);
                List<Map<String, Object>> childDtos = new java.util.ArrayList<>();
                for (Subject c : children) {
                    int childCount = videoCountBySubject.getOrDefault(c.getId(), 0);
                    total += childCount;
                    Map<String, Object> dto = new java.util.LinkedHashMap<>();
                    dto.put("id",    c.getId());
                    dto.put("name",  c.getName());
                    dto.put("count", childCount);
                    childDtos.add(dto);
                }
                videoCountBySubject.put(s.getId(), total);
                childrenForJs.put(s.getId(), childDtos);
            }
        }

        model.addAttribute("subjects",            rootSubjects);
        model.addAttribute("allVideos",           allVideos);
        model.addAttribute("videosByCat",         videosByCat);
        model.addAttribute("videoCountBySubject", videoCountBySubject);
        model.addAttribute("childrenByParentId",  childrenByParentId);
        model.addAttribute("childrenForJs",       childrenForJs);
        return "videos";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Video v = videoService.findByIdWithTemplate(id);
        if (v.getVisibility() != VideoVisibility.PUBLIC) {
            ra.addFlashAttribute("error", "This video is not available.");
            return "redirect:/videos";
        }
        model.addAttribute("video", v);
        model.addAttribute("embedUrl", videoService.embedUrl(v));
        return "video-detail";
    }
}
