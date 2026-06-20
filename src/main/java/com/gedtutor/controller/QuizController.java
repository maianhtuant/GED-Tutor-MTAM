package com.gedtutor.controller;

import com.gedtutor.dto.MathAnswerResult;
import com.gedtutor.dto.MathQuizState;
import com.gedtutor.model.*;
import com.gedtutor.service.HomeworkService;
import com.gedtutor.service.MathQuizService;
import com.gedtutor.service.QuizService;
import com.gedtutor.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    private final QuizService quizService;
    private final HomeworkService homeworkService;
    private final UserService userService;
    private final MathQuizService mathQuizService;

    public QuizController(QuizService quizService, HomeworkService homeworkService,
                          UserService userService, MathQuizService mathQuizService) {
        this.quizService = quizService;
        this.homeworkService = homeworkService;
        this.userService = userService;
        this.mathQuizService = mathQuizService;
    }

    @GetMapping("/{homeworkId}")
    public String quizPage(@PathVariable Long homeworkId,
                           @AuthenticationPrincipal UserDetails principal,
                           Model model) {
        Homework hw = homeworkService.findById(homeworkId);
        User student = userService.findByUsername(principal.getUsername());

        if (hw.isMathQuiz()) {
            // Math-quiz landing page — no question pool, just attempt history
            // and a "Start" button. The actual problems are generated only
            // when the student starts.
            model.addAttribute("hw", hw);
            model.addAttribute("canAttempt", quizService.canAttempt(student, hw));
            model.addAttribute("attemptCount", quizService.getAttemptCount(student, hw));
            model.addAttribute("maxAttempts", quizService.getMaxAttempts());
            model.addAttribute("attempts", quizService.getAttempts(student, hw));
            model.addAttribute("quizStarted", false);
            return "homework/math-quiz-start";
        }

        List<Question> questions = quizService.getQuestions(hw);
        model.addAttribute("hw", hw);
        model.addAttribute("questions", questions);
        model.addAttribute("canAttempt", quizService.canAttempt(student, hw));
        model.addAttribute("attemptCount", quizService.getAttemptCount(student, hw));
        model.addAttribute("maxAttempts", quizService.getMaxAttempts());
        model.addAttribute("attempts", quizService.getAttempts(student, hw));
        model.addAttribute("quizStarted", false);
        return "homework/quiz";
    }

    @PostMapping("/{homeworkId}/start")
    public String startAttempt(@PathVariable Long homeworkId,
                               @AuthenticationPrincipal UserDetails principal,
                               HttpSession session,
                               Model model) {
        Homework hw = homeworkService.findById(homeworkId);
        User student = userService.findByUsername(principal.getUsername());

        if (hw.isMathQuiz()) {
            QuizAttempt attempt = mathQuizService.startAttempt(student, hw, session);
            MathQuizState state = mathQuizService.load(session, attempt.getId());
            model.addAttribute("hw", hw);
            model.addAttribute("attempt", attempt);
            model.addAttribute("state", state);
            return "homework/math-quiz-run";
        }

        // Existing static-question flow.
        QuizAttempt attempt = quizService.startAttempt(student, hw);
        List<Question> questions = quizService.getQuestionsForAttempt(attempt);

        model.addAttribute("hw", hw);
        model.addAttribute("questions", questions);
        model.addAttribute("attemptId", attempt.getId());
        model.addAttribute("canAttempt", quizService.canAttempt(student, hw));
        model.addAttribute("attemptCount", quizService.getAttemptCount(student, hw));
        model.addAttribute("maxAttempts", quizService.getMaxAttempts());
        model.addAttribute("attempts", quizService.getAttempts(student, hw));
        model.addAttribute("quizStarted", true);
        return "homework/quiz";
    }

    // ── Ownership guard ───────────────────────────────────────────────────────
    // All three attempt endpoints below require that the attempt belongs to
    // the currently authenticated user. Failing this check returns 403 so
    // that a student cannot submit into, read answers from, or complete
    // another student's attempt by guessing the attempt ID.

    private ResponseEntity<Map<String, Object>> forbiddenAttempt() {
        return ResponseEntity.status(403)
                .body(Map.of("error", "This attempt does not belong to you."));
    }

    private boolean attemptBelongsTo(QuizAttempt attempt, UserDetails principal) {
        return attempt.getStudent() != null
                && attempt.getStudent().getUsername().equals(principal.getUsername());
    }

    // --- Static-question grading endpoint. ---

    @PostMapping("/attempt/{attemptId}/answer")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitAnswer(
            @PathVariable Long attemptId,
            @RequestParam Long questionId,
            @RequestParam String answer,
            @AuthenticationPrincipal UserDetails principal) {

        QuizAttempt attempt = quizService.findAttemptById(attemptId);
        if (!attemptBelongsTo(attempt, principal)) return forbiddenAttempt();

        boolean correct = quizService.submitAnswer(attemptId, questionId, answer);
        Question q = quizService.findQuestionById(questionId);

        Map<String, Object> body = new HashMap<>();
        body.put("correct", correct);
        // Only reveal the correct answer and explanation after the student
        // answered — prevents scraping answers without actually submitting.
        if (correct) {
            body.put("correctAnswer", q.getCorrectAnswer() != null ? q.getCorrectAnswer() : "");
            body.put("explanation",   q.getExplanation()  != null ? q.getExplanation()   : "");
        } else {
            body.put("correctAnswer", "");
            body.put("explanation",   q.getExplanation()  != null ? q.getExplanation()   : "");
        }
        return ResponseEntity.ok(body);
    }

    // --- Math quiz grading endpoint. ---

    @PostMapping("/attempt/{attemptId}/math-answer")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitMathAnswer(
            @PathVariable Long attemptId,
            @RequestParam int questionIndex,
            @RequestParam String token,
            @RequestParam(required = false, defaultValue = "") String answer,
            HttpSession session,
            @AuthenticationPrincipal UserDetails principal) {

        QuizAttempt attempt = quizService.findAttemptById(attemptId);
        if (!attemptBelongsTo(attempt, principal)) return forbiddenAttempt();

        MathAnswerResult result = mathQuizService.gradeQuestion(session, attemptId, questionIndex, token, answer);
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

        MathQuizState state = mathQuizService.load(session, attemptId);
        if (state != null) {
            body.put("totalAsked", state.totalAsked());
            body.put("totalCorrect", state.totalCorrect());
            body.put("totalQuestions", state.totalQuestions());
            body.put("isDone", state.isDone());
        }
        return ResponseEntity.ok(body);
    }

    @PostMapping("/attempt/{attemptId}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeAttempt(
            @PathVariable Long attemptId,
            HttpSession session,
            @AuthenticationPrincipal UserDetails principal) {

        QuizAttempt attempt = quizService.findAttemptById(attemptId);
        if (!attemptBelongsTo(attempt, principal)) return forbiddenAttempt();

        if (attempt.getHomework() != null && attempt.getHomework().isMathQuiz()) {
            attempt = mathQuizService.completeAttempt(session, attemptId);
        } else {
            attempt = quizService.completeAttempt(attemptId);
        }
        int total = attempt.getTotalQuestions() != null ? attempt.getTotalQuestions() : 0;
        int score = attempt.getScore() != null ? attempt.getScore() : 0;
        int pct = total > 0 ? (score * 100 / total) : 0;

        return ResponseEntity.ok(Map.of(
                "score", score,
                "total", total,
                "percentage", pct
        ));
    }
}
