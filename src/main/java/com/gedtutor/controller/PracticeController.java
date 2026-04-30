package com.gedtutor.controller;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.MathAnswerResult;
import com.gedtutor.dto.PracticeRunState;
import com.gedtutor.model.MathProblemTemplate;
import com.gedtutor.model.PracticeSet;
import com.gedtutor.service.MathAnswerChecker;
import com.gedtutor.service.MathProblemService;
import com.gedtutor.service.PracticeRunService;
import com.gedtutor.service.PracticeSetService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Student-facing practice routes:
 * <ul>
 *   <li>{@code /practice} — hub: list of practice sets + quick random link</li>
 *   <li>{@code /practice/math} — one-off random problem (legacy, still useful)</li>
 *   <li>{@code /practice/math/{templateId}} — random problem from a specific template</li>
 *   <li>{@code /practice/sets/{id}} — start page for a curated set</li>
 *   <li>{@code /practice/sets/{id}/run} — all questions on one page</li>
 *   <li>{@code /practice/sets/{id}/answer} — AJAX grader for one question</li>
 *   <li>{@code /practice/sets/{id}/done} — score summary</li>
 * </ul>
 */
@Controller
@RequestMapping("/practice")
public class PracticeController {

    private static final String QUICK_PROBLEM_KEY_PREFIX = "mathProblem:";

    private final MathProblemService mathProblemService;
    private final MathAnswerChecker checker;
    private final PracticeSetService practiceSetService;
    private final PracticeRunService runService;

    public PracticeController(MathProblemService mathProblemService,
                              MathAnswerChecker checker,
                              PracticeSetService practiceSetService,
                              PracticeRunService runService) {
        this.mathProblemService = mathProblemService;
        this.checker = checker;
        this.practiceSetService = practiceSetService;
        this.runService = runService;
    }

    // -----------------------------------------------------------------
    // Hub
    // -----------------------------------------------------------------

    @GetMapping
    public String hub(Model model) {
        model.addAttribute("sets", practiceSetService.listActive());
        return "practice/hub";
    }

    // -----------------------------------------------------------------
    // Quick random math (one-off)
    // -----------------------------------------------------------------

    @GetMapping("/math")
    public String mathPractice(HttpSession session, Model model) {
        GeneratedMathProblem problem = mathProblemService.generateRandomActive();
        return renderQuickProblem(session, model, problem);
    }

    @GetMapping("/math/{templateId}")
    public String mathPracticeForTemplate(@PathVariable Long templateId,
                                          HttpSession session, Model model) {
        Optional<MathProblemTemplate> template = mathProblemService.findById(templateId);
        if (template.isEmpty()) {
            return "redirect:/practice/math";
        }
        GeneratedMathProblem problem = mathProblemService.generate(template.get());
        return renderQuickProblem(session, model, problem);
    }

    private String renderQuickProblem(HttpSession session, Model model, GeneratedMathProblem problem) {
        String token = UUID.randomUUID().toString();
        session.setAttribute(QUICK_PROBLEM_KEY_PREFIX + token, problem);
        model.addAttribute("problem", problem);
        model.addAttribute("token", token);
        return "practice/math";
    }

    @PostMapping("/math/submit")
    public String submitQuick(@RequestParam String token,
                              @RequestParam(required = false, defaultValue = "") String answer,
                              HttpSession session,
                              Model model) {
        Object stashed = session.getAttribute(QUICK_PROBLEM_KEY_PREFIX + token);
        if (!(stashed instanceof GeneratedMathProblem problem)) {
            return "redirect:/practice/math";
        }
        MathAnswerResult result = checker.check(problem, answer);
        model.addAttribute("problem", problem);
        model.addAttribute("token", token);
        model.addAttribute("result", result);
        return "practice/math";
    }

    // -----------------------------------------------------------------
    // Practice sets
    // -----------------------------------------------------------------

    @GetMapping("/sets/{id}")
    public String setStart(@PathVariable Long id, Model model) {
        PracticeSet set = practiceSetService.findById(id);
        model.addAttribute("set", set);
        return "practice/set-start";
    }

    @PostMapping("/sets/{id}/start")
    public String setRun(@PathVariable Long id, HttpSession session) {
        PracticeRunState state = runService.start(session, id);
        if (state.problems.isEmpty()) {
            return "redirect:/practice/sets/" + id;
        }
        return "redirect:/practice/sets/" + id + "/run";
    }

    @GetMapping("/sets/{id}/run")
    public String setRunPage(@PathVariable Long id, HttpSession session, Model model) {
        PracticeSet set = practiceSetService.findById(id);
        PracticeRunState state = runService.load(session, id);
        if (state == null || state.problems.isEmpty()) {
            return "redirect:/practice/sets/" + id;
        }
        model.addAttribute("set", set);
        model.addAttribute("state", state);
        return "practice/set-run";
    }

    /**
     * AJAX endpoint: grade one question. Returns the result as JSON so
     * the run page can flip the matching card to correct/incorrect
     * without a full reload.
     */
    @PostMapping("/sets/{id}/answer")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> setAnswer(
            @PathVariable Long id,
            @RequestParam int questionIndex,
            @RequestParam String token,
            @RequestParam(required = false, defaultValue = "") String answer,
            HttpSession session) {
        MathAnswerResult result = runService.gradeQuestion(session, id, questionIndex, token, answer);
        Map<String, Object> body = new HashMap<>();
        if (result == null) {
            body.put("ok", false);
            body.put("message", "Question expired or unknown — please refresh.");
            return ResponseEntity.ok(body);
        }
        body.put("ok", true);
        body.put("correct", result.correct());
        body.put("expected", result.expected() != null ? result.expected() : "");
        body.put("message", result.message() != null ? result.message() : "");
        // include running totals so the client can update progress without recounting
        PracticeRunState state = runService.load(session, id);
        if (state != null) {
            body.put("totalAsked", state.totalAsked());
            body.put("totalCorrect", state.totalCorrect());
            body.put("totalQuestions", state.totalQuestions());
            body.put("isDone", state.isDone());
        }
        return ResponseEntity.ok(body);
    }

    @GetMapping("/sets/{id}/done")
    public String setDone(@PathVariable Long id, HttpSession session, Model model) {
        PracticeSet set = practiceSetService.findById(id);
        PracticeRunState state = runService.load(session, id);
        model.addAttribute("set", set);
        model.addAttribute("state", state);
        return "practice/set-done";
    }
}
