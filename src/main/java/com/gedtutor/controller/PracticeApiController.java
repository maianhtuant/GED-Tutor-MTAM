package com.gedtutor.controller;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.model.MathProblemTemplate;
import com.gedtutor.service.MathAnswerChecker;
import com.gedtutor.service.MathProblemService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Lightweight JSON API used by the video-detail sidebar practice widget.
 * No authentication required (videos are public).
 */
@RestController
@RequestMapping("/api/practice")
public class PracticeApiController {

    private static final String SESSION_KEY = "sidebar_problem_";

    private final MathProblemService mathProblemService;
    private final MathAnswerChecker checker;

    public PracticeApiController(MathProblemService mathProblemService, MathAnswerChecker checker) {
        this.mathProblemService = mathProblemService;
        this.checker = checker;
    }

    /**
     * Generate a fresh problem from the given template.
     * Returns: { token, questionText, shape, hint }
     */
    @GetMapping("/problem/{templateId}")
    public ResponseEntity<?> generate(@PathVariable Long templateId, HttpSession session) {
        Optional<MathProblemTemplate> tpl = mathProblemService.findById(templateId);
        if (tpl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        GeneratedMathProblem problem = mathProblemService.generate(tpl.get());
        String token = UUID.randomUUID().toString();
        session.setAttribute(SESSION_KEY + token, problem);

        String hint = switch (problem.shape()) {
            case SCALAR    -> "Enter a number — fraction (2/3) or decimal (0.67) both accepted";
            case UNORDERED -> "Enter two values separated by a comma (fractions or decimals ok)";
            case ORDERED   -> "Enter x, y separated by a comma (fractions or decimals ok)";
        };

        // Split "Solve for x: 8x^2 + 4x + 10 = 0" into a text preamble and a LaTeX equation.
        // FreeForm templates with no preamble return the equation directly in questionEq.
        String rawText = problem.questionText();
        String questionPre;
        String questionEq;
        int colonIdx = rawText.indexOf(": ");
        if (colonIdx >= 0) {
            questionPre = rawText.substring(0, colonIdx + 1); // "Solve for x:"
            questionEq  = rawText.substring(colonIdx + 2);    // "8x^2 + 4x + 10 = 0"
        } else {
            questionPre = "";
            questionEq  = rawText;
        }

        var resp = new java.util.HashMap<String, Object>();
        resp.put("token",        token);
        resp.put("questionText", rawText);    // plain-text fallback
        resp.put("questionPre",  questionPre); // "Solve for x:" (text label)
        resp.put("questionEq",   questionEq);  // LaTeX equation string
        resp.put("shape",        problem.shape().name());
        resp.put("hint",         hint);
        return ResponseEntity.ok(resp);
    }

    /**
     * Check a student's answer.
     * Request body: { token, answer }
     * Returns: { correct, feedback, expected }
     */
    @PostMapping("/check")
    public ResponseEntity<?> check(@RequestBody Map<String, String> body, HttpSession session) {
        String token  = body.getOrDefault("token",  "");
        String answer = body.getOrDefault("answer", "");

        Object stashed = session.getAttribute(SESSION_KEY + token);
        if (!(stashed instanceof GeneratedMathProblem problem)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Session expired — refresh the page to get a new problem."));
        }

        var result = checker.check(problem, answer);
        // Use a HashMap so null values (rare edge cases) don't cause NPE like Map.of() would
        var resp = new java.util.HashMap<String, Object>();
        resp.put("correct",  result.correct());
        resp.put("feedback", result.message()   != null ? result.message()   : "");
        resp.put("expected", result.expected()  != null ? result.expected()  : "");
        return ResponseEntity.ok(resp);
    }
}
