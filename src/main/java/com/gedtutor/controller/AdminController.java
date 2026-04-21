package com.gedtutor.controller;

import com.gedtutor.dto.HomeworkForm;
import com.gedtutor.dto.QuestionForm;
import com.gedtutor.dto.SubjectForm;
import com.gedtutor.dto.VideoForm;
import com.gedtutor.model.*;
import com.gedtutor.service.HomeworkService;
import com.gedtutor.service.QuizService;
import com.gedtutor.service.SubjectService;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final VideoService videoService;
    private final UserService userService;
    private final HomeworkService homeworkService;
    private final QuizService quizService;
    private final SubjectService subjectService;

    public AdminController(VideoService videoService, UserService userService,
                           HomeworkService homeworkService, QuizService quizService,
                           SubjectService subjectService) {
        this.videoService = videoService;
        this.userService = userService;
        this.homeworkService = homeworkService;
        this.quizService = quizService;
        this.subjectService = subjectService;
    }

    // ===================== Dashboard =====================

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("videoCount", videoService.listAll().size());
        model.addAttribute("userCount", userService.findAll().size());
        model.addAttribute("homeworkCount", homeworkService.listAll().size());
        model.addAttribute("questionCount", quizService.listAllQuestions().size());
        model.addAttribute("subjectCount", subjectService.listAll().size());
        return "admin/dashboard";
    }

    // ===================== Subjects =====================

    @GetMapping("/subjects")
    public String subjects(Model model) {
        model.addAttribute("subjects", subjectService.listAll());
        model.addAttribute("form", new SubjectForm());
        return "admin/subjects";
    }

    @GetMapping("/subjects/new")
    public String newSubject(Model model) {
        model.addAttribute("form", new SubjectForm());
        return "admin/subject-form";
    }

    @PostMapping("/subjects/new")
    public String createSubject(@Valid @ModelAttribute("form") SubjectForm form,
                                BindingResult binding,
                                RedirectAttributes ra) {
        if (binding.hasErrors()) {
            return "admin/subject-form";
        }
        try {
            subjectService.save(form);
            ra.addFlashAttribute("message", "Subject '" + form.getName() + "' created.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/subjects/new";
        }
        return "redirect:/admin/subjects";
    }

    @GetMapping("/subjects/{id}/edit")
    public String editSubject(@PathVariable Long id, Model model) {
        Subject s = subjectService.findById(id);
        SubjectForm form = new SubjectForm();
        form.setId(s.getId());
        form.setName(s.getName());
        form.setDescription(s.getDescription());
        form.setDisplayOrder(s.getDisplayOrder());
        form.setActive(s.isActive());
        model.addAttribute("form", form);
        return "admin/subject-form";
    }

    @PostMapping("/subjects/{id}/edit")
    public String updateSubject(@PathVariable Long id,
                                @Valid @ModelAttribute("form") SubjectForm form,
                                BindingResult binding,
                                RedirectAttributes ra) {
        if (binding.hasErrors()) {
            return "admin/subject-form";
        }
        form.setId(id);
        try {
            subjectService.save(form);
            ra.addFlashAttribute("message", "Subject updated.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/subjects/" + id + "/edit";
        }
        return "redirect:/admin/subjects";
    }

    @PostMapping("/subjects/{id}/toggle")
    public String toggleSubject(@PathVariable Long id,
                                @RequestParam boolean active,
                                RedirectAttributes ra) {
        subjectService.setActive(id, active);
        ra.addFlashAttribute("message", active ? "Subject activated." : "Subject deactivated.");
        return "redirect:/admin/subjects";
    }

    @PostMapping("/subjects/{id}/delete")
    public String deleteSubject(@PathVariable Long id, RedirectAttributes ra) {
        try {
            subjectService.delete(id);
            ra.addFlashAttribute("message", "Subject deleted.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/subjects";
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
        model.addAttribute("subjects", subjectService.listActive());
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
            model.addAttribute("subjects", subjectService.listActive());
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
        form.setSubjectId(v.getSubject() != null ? v.getSubject().getId() : null);
        form.setCategory(v.getCategory());
        form.setVisibility(v.getVisibility());
        model.addAttribute("form", form);
        model.addAttribute("subjects", subjectService.listActive());
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
            model.addAttribute("subjects", subjectService.listActive());
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

    /** Flip every video in the DB to PRIVATE in one shot. */
    @PostMapping("/videos/all-private")
    public String setAllVideosPrivate(RedirectAttributes ra) {
        videoService.setAllPrivate();
        ra.addFlashAttribute("message", "All videos set to PRIVATE.");
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
        model.addAttribute("subjects", subjectService.listActive());
        model.addAttribute("videos", videoService.listAll());
        return "admin/homework-form";
    }

    @PostMapping("/homework/new")
    public String createHomework(@Valid @ModelAttribute("form") HomeworkForm form,
                                 BindingResult binding,
                                 Model model,
                                 RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("subjects", subjectService.listActive());
            model.addAttribute("videos", videoService.listAll());
            return "admin/homework-form";
        }
        // Save the homework. If a poolSize was declared, HomeworkService
        // randomly picks that many questions from the subject's bank and
        // links them to the new homework. Questions themselves are managed
        // globally on /admin/questions.
        Homework saved = homeworkService.save(form);
        Integer target = saved.getPoolSize();
        if (target != null && target > 0) {
            int linked = saved.getQuestions().size();
            if (linked == target) {
                ra.addFlashAttribute("message",
                        "Homework '" + saved.getTitle() + "' created with "
                                + linked + " random questions from the "
                                + saved.getSubject().getName() + " bank.");
            } else {
                ra.addFlashAttribute("message",
                        "Homework '" + saved.getTitle() + "' created. Only "
                                + linked + " of " + target + " questions could be linked — "
                                + "the " + saved.getSubject().getName()
                                + " bank doesn't have enough questions yet. "
                                + "Add more on the Questions page.");
            }
        } else {
            ra.addFlashAttribute("message", "Homework '" + saved.getTitle() + "' created.");
        }
        return "redirect:/admin/homework";
    }

    @GetMapping("/homework/{id}/edit")
    public String editHomework(@PathVariable Long id, Model model) {
        Homework hw = homeworkService.findById(id);
        HomeworkForm form = new HomeworkForm();
        form.setId(hw.getId());
        form.setTitle(hw.getTitle());
        form.setInstructions(hw.getInstructions());
        form.setSubjectId(hw.getSubject() != null ? hw.getSubject().getId() : null);
        form.setDueDate(hw.getDueDate());
        form.setPublished(hw.isPublished());
        form.setQuestionsPerAttempt(hw.getQuestionsPerAttempt());
        form.setPoolSize(hw.getPoolSize());
        form.setVideoId(hw.getVideo() != null ? hw.getVideo().getId() : null);
        model.addAttribute("form", form);
        model.addAttribute("subjects", subjectService.listActive());
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
            model.addAttribute("subjects", subjectService.listActive());
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

    /**
     * Per-homework question view: shows questions currently linked to this
     * homework and lets the admin link/unlink questions from the bank.
     */
    @GetMapping("/homework/{id}/questions")
    public String homeworkQuestions(@PathVariable Long id, Model model) {
        Homework hw = homeworkService.findById(id);
        List<Question> linked = quizService.getQuestions(hw);
        // All questions in the subject bank for this homework's subject
        List<Question> bank = hw.getSubject() != null
                ? quizService.getQuestionsBySubject(hw.getSubject())
                : Collections.emptyList();
        // Only bank questions not already linked
        java.util.Set<Long> linkedIds = linked.stream()
                .map(Question::getId)
                .collect(Collectors.toSet());
        List<Question> available = bank.stream()
                .filter(q -> !linkedIds.contains(q.getId()))
                .toList();
        model.addAttribute("hw", hw);
        model.addAttribute("linked", linked);
        model.addAttribute("available", available);
        return "admin/homework-questions";
    }

    @PostMapping("/homework/{id}/questions/link")
    public String linkQuestion(@PathVariable Long id,
                               @RequestParam Long questionId,
                               RedirectAttributes ra) {
        Homework hw = homeworkService.findById(id);
        Question q = quizService.findQuestionById(questionId);
        if (!hw.getQuestions().contains(q)) {
            hw.getQuestions().add(q);
            homeworkService.saveRaw(hw);
        }
        ra.addFlashAttribute("message", "Question linked.");
        return "redirect:/admin/homework/" + id + "/questions";
    }

    @PostMapping("/homework/{id}/questions/{qId}/unlink")
    public String unlinkQuestion(@PathVariable Long id,
                                 @PathVariable Long qId,
                                 RedirectAttributes ra) {
        Homework hw = homeworkService.findById(id);
        hw.getQuestions().removeIf(q -> q.getId().equals(qId));
        homeworkService.saveRaw(hw);
        ra.addFlashAttribute("message", "Question removed.");
        return "redirect:/admin/homework/" + id + "/questions";
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

    // ===================== Questions (global question bank) =====================
    //
    // Questions live in a bank indexed by subject. A homework doesn't "own"
    // its questions anymore — instead the homework links to them via the
    // homework_questions join table. When a homework is created with a
    // poolSize target, HomeworkService randomly picks N bank questions
    // matching the homework's subject and links them. Admins manage the
    // bank on /admin/questions; they no longer manage questions per
    // homework.

    @GetMapping("/questions")
    public String allQuestions(Model model) {
        model.addAttribute("questions", quizService.listAllQuestions());
        return "admin/questions-all";
    }

    @GetMapping("/questions/new")
    public String newQuestionGlobal(Model model) {
        model.addAttribute("form", new QuestionForm());
        model.addAttribute("types", QuestionType.values());
        model.addAttribute("subjects", subjectService.listActive());
        return "admin/question-global-form";
    }

    @PostMapping("/questions/new")
    public String createQuestionGlobal(@Valid @ModelAttribute("form") QuestionForm form,
                                       BindingResult binding,
                                       Model model,
                                       RedirectAttributes ra) {
        if (form.getSubjectId() == null) {
            binding.rejectValue("subjectId", "required", "Please pick a subject.");
        }
        if (binding.hasErrors()) {
            model.addAttribute("types", QuestionType.values());
            model.addAttribute("subjects", subjectService.listActive());
            return "admin/question-global-form";
        }
        quizService.saveQuestion(form.getSubjectId(), form);
        ra.addFlashAttribute("message", "Question added to the bank.");
        return "redirect:/admin/questions";
    }

    @GetMapping("/questions/{qId}/edit")
    public String editQuestionGlobal(@PathVariable Long qId, Model model) {
        // Load with choices + subject eagerly fetched — otherwise the
        // stream over q.getChoices() below hits LazyInitializationException
        // once the tx closes (spring.jpa.open-in-view=false).
        Question q = quizService.findQuestionByIdWithChoices(qId);
        QuestionForm form = new QuestionForm();
        form.setId(q.getId());
        form.setSubjectId(q.getSubject() != null ? q.getSubject().getId() : null);
        form.setType(q.getType());
        form.setQuestionText(q.getQuestionText());
        form.setCorrectAnswer(q.getCorrectAnswer());
        form.setExplanation(q.getExplanation());
        form.setOrderIndex(q.getOrderIndex());
        if (q.getType() == QuestionType.MULTIPLE_CHOICE) {
            form.setChoices(q.getChoices().stream()
                    .map(QuestionChoice::getChoiceText).toList());
        }
        model.addAttribute("form", form);
        model.addAttribute("types", QuestionType.values());
        model.addAttribute("subjects", subjectService.listActive());
        return "admin/question-global-form";
    }

    @PostMapping("/questions/{qId}/edit")
    public String updateQuestionGlobal(@PathVariable Long qId,
                                       @Valid @ModelAttribute("form") QuestionForm form,
                                       BindingResult binding,
                                       Model model,
                                       RedirectAttributes ra) {
        if (form.getSubjectId() == null) {
            binding.rejectValue("subjectId", "required", "Please pick a subject.");
        }
        if (binding.hasErrors()) {
            model.addAttribute("types", QuestionType.values());
            model.addAttribute("subjects", subjectService.listActive());
            return "admin/question-global-form";
        }
        form.setId(qId);
        quizService.saveQuestion(form.getSubjectId(), form);
        ra.addFlashAttribute("message", "Question updated.");
        return "redirect:/admin/questions";
    }

    @PostMapping("/questions/{qId}/delete")
    public String deleteQuestionGlobal(@PathVariable Long qId, RedirectAttributes ra) {
        quizService.deleteQuestion(qId);
        ra.addFlashAttribute("message", "Question deleted.");
        return "redirect:/admin/questions";
    }

    /**
     * Inline "save this row" from the /admin/questions table. Returns JSON
     * so the page can update the row in-place without a reload.
     */
    @PostMapping("/questions/{qId}/inline-update")
    @ResponseBody
    public Map<String, Object> inlineUpdateQuestion(@PathVariable Long qId,
                                                    @RequestParam(required = false) String questionText,
                                                    @RequestParam(required = false) String correctAnswer) {
        Question saved = quizService.updateQuestionInline(qId, questionText, correctAnswer);
        Map<String, Object> res = new HashMap<>();
        res.put("id", saved.getId());
        res.put("questionText", saved.getQuestionText());
        res.put("correctAnswer", saved.getCorrectAnswer());
        return res;
    }
}
