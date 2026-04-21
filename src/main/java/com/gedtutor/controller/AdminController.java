package com.gedtutor.controller;

import com.gedtutor.dto.HomeworkForm;
import com.gedtutor.dto.QuestionForm;
import com.gedtutor.dto.VideoForm;
import com.gedtutor.model.*;
import com.gedtutor.service.HomeworkService;
import com.gedtutor.service.QuizService;
import com.gedtutor.service.UserService;
import com.gedtutor.service.VideoService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final VideoService videoService;
    private final UserService userService;
    private final HomeworkService homeworkService;
    private final QuizService quizService;

    public AdminController(VideoService videoService, UserService userService,
                           HomeworkService homeworkService, QuizService quizService) {
        this.videoService = videoService;
        this.userService = userService;
        this.homeworkService = homeworkService;
        this.quizService = quizService;
    }

    // ===================== Dashboard =====================

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("videoCount", videoService.listAll().size());
        model.addAttribute("userCount", userService.findAll().size());
        model.addAttribute("homeworkCount", homeworkService.listAll().size());
        return "admin/dashboard";
    }

    // ===================== Videos =====================

    @GetMapping("/videos")
    public String videos(Model model) {
        model.addAttribute("videos", videoService.listAll());
        return "admin/videos";
    }

    @GetMapping("/videos/new")
    public String newVideo(Model model) {
        VideoForm form = new VideoForm();
        form.setVisibility(VideoVisibility.PRIVATE);
        model.addAttribute("form", form);
        model.addAttribute("subjects", GedSubject.values());
        model.addAttribute("visibilities", VideoVisibility.values());
        return "admin/video-form";
    }

    @PostMapping("/videos/new")
    public String createVideo(@Valid @ModelAttribute("form") VideoForm form,
                              BindingResult binding,
                              @AuthenticationPrincipal UserDetails principal,
                              Model model,
                              RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("subjects", GedSubject.values());
            model.addAttribute("visibilities", VideoVisibility.values());
            return "admin/video-form";
        }
        User uploader = userService.findByUsername(principal.getUsername());
        videoService.create(form, uploader);
        ra.addFlashAttribute("message", "Video created.");
        return "redirect:/admin/videos";
    }

    @GetMapping("/videos/{id}/edit")
    public String editVideo(@PathVariable Long id, Model model) {
        Video v = videoService.findById(id);
        VideoForm form = new VideoForm();
        form.setId(v.getId());
        form.setTitle(v.getTitle());
        form.setDescription(v.getDescription());
        form.setVideoUrl(v.getVideoUrl());
        form.setSubject(v.getSubject());
        form.setVisibility(v.getVisibility());
        model.addAttribute("form", form);
        model.addAttribute("subjects", GedSubject.values());
        model.addAttribute("visibilities", VideoVisibility.values());
        return "admin/video-form";
    }

    @PostMapping("/videos/{id}/edit")
    public String updateVideo(@PathVariable Long id,
                              @Valid @ModelAttribute("form") VideoForm form,
                              BindingResult binding,
                              Model model,
                              RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("subjects", GedSubject.values());
            model.addAttribute("visibilities", VideoVisibility.values());
            return "admin/video-form";
        }
        videoService.update(id, form);
        ra.addFlashAttribute("message", "Video updated.");
        return "redirect:/admin/videos";
    }

    @PostMapping("/videos/{id}/visibility")
    public String changeVisibility(@PathVariable Long id,
                                   @RequestParam VideoVisibility visibility,
                                   RedirectAttributes ra) {
        videoService.setVisibility(id, visibility);
        ra.addFlashAttribute("message", "Visibility updated to " + visibility + ".");
        return "redirect:/admin/videos";
    }

    @PostMapping("/videos/{id}/delete")
    public String deleteVideo(@PathVariable Long id, RedirectAttributes ra) {
        videoService.delete(id);
        ra.addFlashAttribute("message", "Video deleted.");
        return "redirect:/admin/videos";
    }

    // ===================== Users =====================

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String setRole(@PathVariable Long id, @RequestParam Role role, RedirectAttributes ra) {
        userService.setRole(id, role);
        ra.addFlashAttribute("message", "Role updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/enabled")
    public String setEnabled(@PathVariable Long id, @RequestParam boolean enabled, RedirectAttributes ra) {
        userService.setEnabled(id, enabled);
        ra.addFlashAttribute("message", enabled ? "User enabled." : "User disabled.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, @RequestParam String newPassword, RedirectAttributes ra) {
        userService.resetPassword(id, newPassword);
        ra.addFlashAttribute("message", "Password reset.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.delete(id);
        ra.addFlashAttribute("message", "User deleted.");
        return "redirect:/admin/users";
    }

    // ===================== Homework =====================

    @GetMapping("/homework")
    public String homeworkList(Model model) {
        model.addAttribute("homeworkList", homeworkService.listAll());
        return "admin/homework";
    }

    @GetMapping("/homework/new")
    public String newHomework(Model model) {
        model.addAttribute("form", new HomeworkForm());
        model.addAttribute("subjects", GedSubject.values());
        model.addAttribute("videos", videoService.listAll());
        return "admin/homework-form";
    }

    @PostMapping("/homework/new")
    public String createHomework(@Valid @ModelAttribute("form") HomeworkForm form,
                                 BindingResult binding,
                                 Model model,
                                 RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("subjects", GedSubject.values());
            model.addAttribute("videos", videoService.listAll());
            return "admin/homework-form";
        }
        homeworkService.save(form);
        ra.addFlashAttribute("message", "Homework created.");
        return "redirect:/admin/homework";
    }

    @GetMapping("/homework/{id}/edit")
    public String editHomework(@PathVariable Long id, Model model) {
        Homework hw = homeworkService.findById(id);
        HomeworkForm form = new HomeworkForm();
        form.setId(hw.getId());
        form.setTitle(hw.getTitle());
        form.setInstructions(hw.getInstructions());
        form.setSubject(hw.getSubject());
        form.setDueDate(hw.getDueDate());
        form.setPublished(hw.isPublished());
        form.setVideoId(hw.getVideo() != null ? hw.getVideo().getId() : null);
        model.addAttribute("form", form);
        model.addAttribute("subjects", GedSubject.values());
        model.addAttribute("videos", videoService.listAll());
        return "admin/homework-form";
    }

    @PostMapping("/homework/{id}/edit")
    public String updateHomework(@PathVariable Long id,
                                 @Valid @ModelAttribute("form") HomeworkForm form,
                                 BindingResult binding,
                                 Model model,
                                 RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("subjects", GedSubject.values());
            model.addAttribute("videos", videoService.listAll());
            return "admin/homework-form";
        }
        form.setId(id);
        homeworkService.save(form);
        ra.addFlashAttribute("message", "Homework updated.");
        return "redirect:/admin/homework";
    }

    @PostMapping("/homework/{id}/delete")
    public String deleteHomework(@PathVariable Long id, RedirectAttributes ra) {
        homeworkService.delete(id);
        ra.addFlashAttribute("message", "Homework deleted.");
        return "redirect:/admin/homework";
    }

    @GetMapping("/homework/{id}/submissions")
    public String submissions(@PathVariable Long id, Model model) {
        Homework hw = homeworkService.findById(id);
        model.addAttribute("hw", hw);
        model.addAttribute("submissions", homeworkService.submissionsFor(hw));
        return "admin/submissions";
    }

    @PostMapping("/submissions/{id}/grade")
    public String grade(@PathVariable Long id,
                        @RequestParam(required = false) Integer grade,
                        @RequestParam(required = false) String feedback,
                        @RequestParam Long homeworkId,
                        RedirectAttributes ra) {
        homeworkService.grade(id, grade, feedback);
        ra.addFlashAttribute("message", "Grade saved.");
        return "redirect:/admin/homework/" + homeworkId + "/submissions";
    }

    // ===================== Questions =====================

    @GetMapping("/homework/{hwId}/questions")
    public String questions(@PathVariable Long hwId, Model model) {
        Homework hw = homeworkService.findById(hwId);
        model.addAttribute("hw", hw);
        model.addAttribute("questions", quizService.getQuestions(hw));
        return "admin/questions";
    }

    @GetMapping("/homework/{hwId}/questions/new")
    public String newQuestion(@PathVariable Long hwId, Model model) {
        model.addAttribute("hw", homeworkService.findById(hwId));
        model.addAttribute("form", new QuestionForm());
        model.addAttribute("types", QuestionType.values());
        return "admin/question-form";
    }

    @PostMapping("/homework/{hwId}/questions/new")
    public String createQuestion(@PathVariable Long hwId,
                                 @Valid @ModelAttribute("form") QuestionForm form,
                                 BindingResult binding,
                                 Model model,
                                 RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("hw", homeworkService.findById(hwId));
            model.addAttribute("types", QuestionType.values());
            return "admin/question-form";
        }
        quizService.saveQuestion(hwId, form);
        ra.addFlashAttribute("message", "Question added.");
        return "redirect:/admin/homework/" + hwId + "/questions";
    }

    @GetMapping("/homework/{hwId}/questions/{qId}/edit")
    public String editQuestion(@PathVariable Long hwId, @PathVariable Long qId, Model model) {
        Homework hw = homeworkService.findById(hwId);
        Question q = quizService.findQuestionById(qId);
        QuestionForm form = new QuestionForm();
        form.setId(q.getId());
        form.setType(q.getType());
        form.setQuestionText(q.getQuestionText());
        form.setCorrectAnswer(q.getCorrectAnswer());
        form.setExplanation(q.getExplanation());
        form.setOrderIndex(q.getOrderIndex());
        if (q.getType() == QuestionType.MULTIPLE_CHOICE) {
            form.setChoices(q.getChoices().stream()
                    .map(QuestionChoice::getChoiceText).toList());
        }
        model.addAttribute("hw", hw);
        model.addAttribute("form", form);
        model.addAttribute("types", QuestionType.values());
        return "admin/question-form";
    }

    @PostMapping("/homework/{hwId}/questions/{qId}/edit")
    public String updateQuestion(@PathVariable Long hwId, @PathVariable Long qId,
                                 @Valid @ModelAttribute("form") QuestionForm form,
                                 BindingResult binding,
                                 Model model,
                                 RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("hw", homeworkService.findById(hwId));
            model.addAttribute("types", QuestionType.values());
            return "admin/question-form";
        }
        form.setId(qId);
        quizService.saveQuestion(hwId, form);
        ra.addFlashAttribute("message", "Question updated.");
        return "redirect:/admin/homework/" + hwId + "/questions";
    }

    @PostMapping("/homework/{hwId}/questions/{qId}/delete")
    public String deleteQuestion(@PathVariable Long hwId, @PathVariable Long qId, RedirectAttributes ra) {
        quizService.deleteQuestion(qId);
        ra.addFlashAttribute("message", "Question deleted.");
        return "redirect:/admin/homework/" + hwId + "/questions";
    }
}
