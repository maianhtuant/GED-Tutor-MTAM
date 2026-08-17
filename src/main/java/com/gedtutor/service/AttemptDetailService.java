package com.gedtutor.service;

import com.gedtutor.dto.AnswerRow;
import com.gedtutor.dto.AttemptDetail;
import com.gedtutor.model.Homework;
import com.gedtutor.model.MathQuizAnswer;
import com.gedtutor.model.PracticeAnswer;
import com.gedtutor.model.PracticeAttempt;
import com.gedtutor.model.PracticeSet;
import com.gedtutor.model.QuizAnswer;
import com.gedtutor.model.QuizAttempt;
import com.gedtutor.repository.MathQuizAnswerRepository;
import com.gedtutor.repository.PracticeAnswerRepository;
import com.gedtutor.repository.PracticeAttemptRepository;
import com.gedtutor.repository.QuizAnswerRepository;
import com.gedtutor.repository.QuizAttemptRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a question-by-question {@link AttemptDetail} for one completed
 * {@link QuizAttempt} (static-question quiz, or math-quiz-mode homework —
 * dispatched on {@code Homework.isMathQuiz()}) or {@link PracticeAttempt},
 * for the admin "Overview" page reached from the Activity list.
 */
@Service
public class AttemptDetailService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final MathQuizAnswerRepository mathQuizAnswerRepository;
    private final PracticeAttemptRepository practiceAttemptRepository;
    private final PracticeAnswerRepository practiceAnswerRepository;

    public AttemptDetailService(QuizAttemptRepository quizAttemptRepository,
                                QuizAnswerRepository quizAnswerRepository,
                                MathQuizAnswerRepository mathQuizAnswerRepository,
                                PracticeAttemptRepository practiceAttemptRepository,
                                PracticeAnswerRepository practiceAnswerRepository) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizAnswerRepository = quizAnswerRepository;
        this.mathQuizAnswerRepository = mathQuizAnswerRepository;
        this.practiceAttemptRepository = practiceAttemptRepository;
        this.practiceAnswerRepository = practiceAnswerRepository;
    }

    @Transactional(readOnly = true)
    public AttemptDetail getQuizAttemptDetail(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Attempt not found: " + attemptId));
        Homework hw = attempt.getHomework();
        String subjectName = hw.getSubject() != null ? hw.getSubject().getName() : "—";
        String kind = hw.isMathQuiz() ? "Math Quiz" : "Quiz";

        List<AnswerRow> rows = new ArrayList<>();
        if (hw.isMathQuiz()) {
            List<MathQuizAnswer> answers = mathQuizAnswerRepository.findByAttemptOrderByQuestionIndexAsc(attempt);
            for (MathQuizAnswer a : answers) {
                rows.add(new AnswerRow(a.getQuestionIndex(), a.getQuestionText(), a.getExpectedAnswer(),
                        a.getStudentAnswer(), a.isCorrect(), null));
            }
        } else {
            List<QuizAnswer> answers = quizAnswerRepository.findByAttemptWithQuestion(attempt);
            int i = 0;
            for (QuizAnswer a : answers) {
                rows.add(new AnswerRow(i++, a.getQuestion().getQuestionText(),
                        a.getQuestion().getCorrectAnswer(), a.getStudentAnswer(), a.isCorrect(),
                        a.getQuestion().getExplanation()));
            }
        }

        int score = attempt.getScore() != null ? attempt.getScore() : 0;
        int total = attempt.getTotalQuestions() != null ? attempt.getTotalQuestions() : 0;

        return new AttemptDetail(kind, hw.getTitle(), subjectName,
                attempt.getStudent() != null ? attempt.getStudent().getUsername() : "—",
                score, total, attempt.getStartedAt(), attempt.getCompletedAt(), rows);
    }

    @Transactional(readOnly = true)
    public AttemptDetail getPracticeAttemptDetail(Long attemptId) {
        PracticeAttempt attempt = practiceAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Attempt not found: " + attemptId));
        PracticeSet set = attempt.getPracticeSet();
        String subjectName = set.getSubject() != null ? set.getSubject().getName() : "—";

        List<AnswerRow> rows = new ArrayList<>();
        List<PracticeAnswer> answers = practiceAnswerRepository.findByAttemptOrderByQuestionIndexAsc(attempt);
        for (PracticeAnswer a : answers) {
            rows.add(new AnswerRow(a.getQuestionIndex(), a.getQuestionText(), a.getExpectedAnswer(),
                    a.getStudentAnswer(), a.isCorrect(), null));
        }

        int score = attempt.getScore() != null ? attempt.getScore() : 0;
        int total = attempt.getTotalQuestions() != null ? attempt.getTotalQuestions() : 0;

        return new AttemptDetail("Practice", set.getTitle(), subjectName,
                attempt.getStudent() != null ? attempt.getStudent().getUsername() : "—",
                score, total, attempt.getStartedAt(), attempt.getCompletedAt(), rows);
    }
}
