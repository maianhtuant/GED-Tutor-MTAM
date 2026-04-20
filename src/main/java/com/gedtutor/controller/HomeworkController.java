package com.gedtutor.controller;

import com.gedtutor.model.Homework;
import com.gedtutor.model.HomeworkSubmission;
import com.gedtutor.model.User;
import com.gedtutor.service.HomeworkService;
import com.gedtutor.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/homework")
public class HomeworkController {

    private final HomeworkService homeworkService;
    private final UserService userService;

    public HomeworkController(HomeworkService homeworkService, UserService userService) {
        this.homeworkService = homeworkService;
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model, @AuthenticationPrincipal UserDetails principal) {
        User me = userService.findByUsername(principal.getUsername());
        model.addAttribute("homework", homeworkService.listPublished());
        model.addAttribute("mySubmissions", homeworkService.mySubmissions(me));
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
