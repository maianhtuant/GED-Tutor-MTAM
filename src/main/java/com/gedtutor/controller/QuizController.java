package com.gedtutor.controller;

import com.gedtutor.model.*;
import com.gedtutor.service.HomeworkService;
import com.gedtutor.service.QuizService;
import com.gedtutor.service.UserService;
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

    public QuizController(QuizService quizService, HomeworkService homeworkService, UserService userService) {
        this.quizService = quizService;
        this.homeworkService = homeworkService;
        this.userService = userService;
    }

    @GetMapping("/{homeworkId}")
    public String quizPage(@PathVariable Long homeworkId,
                           @AuthenticationPrincipal UserDetails principal,
                           Model model) {
        Homework hw = homeworkService.findById(homeworkId);
        User student = userService.findByUsername(principal.getUsername());
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
                               Model model) {
        Homework hw = homeworkService.findById(homeworkId);
        User student = userService.findByUsername(principal.getUsername());

        // startAttempt now selects the subset of questions for this attempt.
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

    @PostMapping("/attempt/{attemptId}/answer")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitAnswer(
            @PathVariable Long attemptId,
            @RequestParam Long questionId,
            @RequestParam String answer) {

        boolean correct = quizService.submitAnswer(attemptId, questionId, answer);
        Question q = quizService.findQuestionById(questionId);

        // HashMap tolerates null values; Map.of would NPE if correctAnswer is null.
        Map<String, Object> body = new HashMap<>();
        body.put("correct", correct);
        body.put("correctAnswer", q.getCorrectAnswer() != null ? q.getCorrectAnswer() : "");
        body.put("explanation", q.getExplanation() != null ? q.getExplanation() : "");
        return ResponseEntity.ok(body);
    }

    @PostMapping("/attempt/{attemptId}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeAttempt(@PathVariable Long attemptId) {
        QuizAttempt attempt = quizService.completeAttempt(attemptId);
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
