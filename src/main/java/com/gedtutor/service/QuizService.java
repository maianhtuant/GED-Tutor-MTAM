package com.gedtutor.service;

import com.gedtutor.dto.QuestionForm;
import com.gedtutor.model.*;
import com.gedtutor.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuizService {

    private static final int MAX_ATTEMPTS = 3;

    private final QuestionRepository questionRepo;
    private final QuizAttemptRepository attemptRepo;
    private final QuizAnswerRepository answerRepo;
    private final HomeworkRepository homeworkRepo;

    public QuizService(QuestionRepository questionRepo,
                       QuizAttemptRepository attemptRepo,
                       QuizAnswerRepository answerRepo,
                       HomeworkRepository homeworkRepo) {
        this.questionRepo = questionRepo;
        this.attemptRepo = attemptRepo;
        this.answerRepo = answerRepo;
        this.homeworkRepo = homeworkRepo;
    }

    // ── Questions ──────────────────────────────────────────────

    public List<Question> getQuestions(Homework hw) {
        return questionRepo.findByHomeworkWithChoices(hw);
    }

    public Question findQuestionById(Long id) {
        return questionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
    }

    @Transactional
    public void saveQuestion(Long homeworkId, QuestionForm form) {
        Homework hw = homeworkRepo.findById(homeworkId)
                .orElseThrow(() -> new IllegalArgumentException("Homework not found"));

        Question q;
        if (form.getId() != null) {
            q = findQuestionById(form.getId());
            q.getChoices().clear();
        } else {
            q = new Question();
            q.setHomework(hw);
            int count = (int) questionRepo.countByHomework(hw);
            q.setOrderIndex(form.getOrderIndex() > 0 ? form.getOrderIndex() : count);
        }

        q.setType(form.getType());
        q.setQuestionText(form.getQuestionText());
        q.setCorrectAnswer(form.getCorrectAnswer().trim());
        q.setExplanation(form.getExplanation());

        if (form.getType() == QuestionType.MULTIPLE_CHOICE) {
            List<String> choiceTexts = form.getChoices();
            for (int i = 0; i < choiceTexts.size(); i++) {
                String text = choiceTexts.get(i);
                if (text != null && !text.isBlank()) {
                    QuestionChoice c = new QuestionChoice(q, text.trim(), i);
                    q.getChoices().add(c);
                }
            }
        }

        questionRepo.save(q);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        questionRepo.deleteById(id);
    }

    // ── Quiz Attempts ──────────────────────────────────────────

    public boolean canAttempt(User student, Homework hw) {
        long count = attemptRepo.countByStudentAndHomework(student, hw);
        return count < MAX_ATTEMPTS;
    }

    public int getAttemptCount(User student, Homework hw) {
        return (int) attemptRepo.countByStudentAndHomework(student, hw);
    }

    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }

    public List<QuizAttempt> getAttempts(User student, Homework hw) {
        return attemptRepo.findByStudentAndHomeworkOrderByAttemptNumberDesc(student, hw);
    }

    @Transactional
    public QuizAttempt startAttempt(User student, Homework hw) {
        if (!canAttempt(student, hw)) {
            throw new IllegalStateException("Maximum attempts reached");
        }
        long count = attemptRepo.countByStudentAndHomework(student, hw);
        QuizAttempt attempt = new QuizAttempt();
        attempt.setStudent(student);
        attempt.setHomework(hw);
        attempt.setAttemptNumber((int) count + 1);
        attempt.setTotalQuestions((int) questionRepo.countByHomework(hw));
        return attemptRepo.save(attempt);
    }

    @Transactional
    public boolean submitAnswer(Long attemptId, Long questionId, String studentAnswer) {
        QuizAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));
        Question question = findQuestionById(questionId);

        boolean correct = checkAnswer(question, studentAnswer);

        QuizAnswer answer = new QuizAnswer();
        answer.setAttempt(attempt);
        answer.setQuestion(question);
        answer.setStudentAnswer(studentAnswer != null ? studentAnswer.trim() : "");
        answer.setCorrect(correct);
        answerRepo.save(answer);

        return correct;
    }

    @Transactional
    public QuizAttempt completeAttempt(Long attemptId) {
        QuizAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));
        List<QuizAnswer> answers = answerRepo.findByAttemptWithQuestion(attempt);
        long correctCount = answers.stream().filter(QuizAnswer::isCorrect).count();
        attempt.setScore((int) correctCount);
        attempt.setCompletedAt(LocalDateTime.now());
        return attemptRepo.save(attempt);
    }

    private boolean checkAnswer(Question q, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.isBlank()) return false;
        String correct = q.getCorrectAnswer().trim().toLowerCase();
        String given = studentAnswer.trim().toLowerCase();

        return switch (q.getType()) {
            case MULTIPLE_CHOICE -> correct.equals(given);
            case TRUE_FALSE -> correct.equals(given);
            case FILL_BLANK -> correct.equals(given) || correct.contains(given) || given.contains(correct);
        };
    }
}
