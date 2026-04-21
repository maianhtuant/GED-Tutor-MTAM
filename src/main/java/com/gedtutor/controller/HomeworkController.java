package com.gedtutor.controller;

import com.gedtutor.model.Homework;
import com.gedtutor.model.HomeworkSubmission;
import com.gedtutor.model.Subject;
import com.gedtutor.model.User;
import com.gedtutor.service.HomeworkService;
import com.gedtutor.service.SubjectService;
import com.gedtutor.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Controller
@RequestMapping("/homework")
public class HomeworkController {

    private final HomeworkService homeworkService;
    private final UserService userService;
    private final SubjectService subjectService;

    public HomeworkController(HomeworkService homeworkService,
                              UserService userService,
                              SubjectService subjectService) {
        this.homeworkService = homeworkService;
        this.userService = userService;
        this.subjectService = subjectService;
    }

    @GetMapping
    public String list(Model model, @AuthenticationPrincipal UserDetails principal) {
        User me = userService.findByUsername(principal.getUsername());
        List<Homework> all = homeworkService.listPublished();
        List<Subject>  subjects = subjectService.listAll();

        // subjectId → (category → homeworks)
        Map<Long, Map<String, List<Homework>>> hwByCat = new LinkedHashMap<>();
        Map<Long, Integer> hwCountBySubject = new LinkedHashMap<>();
        for (Subject s : subjects) {
            Map<String, List<Homework>> byCategory = new TreeMap<>();
            int count = 0;
            for (Homework hw : all) {
                if (hw.getSubject() != null && hw.getSubject().getId().equals(s.getId())) {
                    String cat = (hw.getCategory() != null && !hw.getCategory().isBlank())
                            ? hw.getCategory().trim() : "General";
                    byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(hw);
                    count++;
                }
            }
            hwByCat.put(s.getId(), byCategory);
            hwCountBySubject.put(s.getId(), count);
        }

        model.addAttribute("allHomework",       all);
        model.addAttribute("subjects",          subjects);
        model.addAttribute("hwByCat",           hwByCat);
        model.addAttribute("hwCountBySubject",  hwCountBySubject);
        model.addAttribute("mySubmissions",     homeworkService.mySubmissions(me));
        return "homework";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails principal,
                         Model model) {
        Homework hw = homeworkService.findById(id);
        User me = userService.findByUsername(principal.getUsername());
        Optional<HomeworkSubmission> mine = homeworkService.findMySubmission(hw, me);
        model.addAttribute("hw", hw);
        model.addAttribute("submission", mine.orElse(null));
        return "homework-detail";
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id,
                         @RequestParam String answer,
                         @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes ra) {
        User me = userService.findByUsername(principal.getUsername());
        homeworkService.submit(id, me, answer);
        ra.addFlashAttribute("message", "Your answer has been submitted.");
        return "redirect:/homework/" + id;
    }
}
