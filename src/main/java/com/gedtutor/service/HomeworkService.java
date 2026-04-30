package com.gedtutor.service;

import com.gedtutor.dto.HomeworkForm;
import com.gedtutor.model.Homework;
import com.gedtutor.model.HomeworkSubmission;
import com.gedtutor.model.Question;
import com.gedtutor.model.QuizAttempt;
import com.gedtutor.model.Subject;
import com.gedtutor.model.User;
import com.gedtutor.model.Video;
import com.gedtutor.repository.HomeworkRepository;
import com.gedtutor.repository.HomeworkSubmissionRepository;
import com.gedtutor.repository.QuestionRepository;
import com.gedtutor.repository.QuizAttemptRepository;
import com.gedtutor.repository.VideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class HomeworkService {

    private final HomeworkRepository homeworkRepository;
    private final HomeworkSubmissionRepository submissionRepository;
    private final VideoRepository videoRepository;
    private final SubjectService subjectService;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public HomeworkService(HomeworkRepository homeworkRepository,
                           HomeworkSubmissionRepository submissionRepository,
                           VideoRepository videoRepository,
                           SubjectService subjectService,
                           QuestionRepository questionRepository,
                           QuizAttemptRepository quizAttemptRepository) {
        this.homeworkRepository = homeworkRepository;
        this.submissionRepository = submissionRepository;
        this.videoRepository = videoRepository;
        this.subjectService = subjectService;
        this.questionRepository = questionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    public List<Homework> listPublished() {
        return homeworkRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }

    public List<Homework> listAll() {
        return homeworkRepository.findAllByOrderByCreatedAtDesc();
    }

    public Homework findById(Long id) {
        return homeworkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Homework not found: " + id));
    }

    @Transactional
    public Homework save(HomeworkForm form) {
        boolean isNew = (form.getId() == null);
        Homework hw = isNew ? new Homework() : findById(form.getId());
        hw.setTitle(form.getTitle());
        hw.setInstructions(form.getInstructions());
        hw.setSubject(subjectService.findById(form.getSubjectId()));
        hw.setDueDate(form.getDueDate());
        hw.setPublished(form.isPublished());
        // null / 0 / negative = all questions; positive = random sample of that size
        Integer qpa = form.getQuestionsPerAttempt();
        hw.setQuestionsPerAttempt(qpa != null && qpa > 0 ? qpa : null);

        // Target pool size drives the auto-pick behavior on create.
        Integer pool = form.getPoolSize();
        hw.setPoolSize(pool != null && pool > 0 ? pool : null);

        // Math-quiz mode: when on, attempts auto-generate problems from
        // every active math template and ignore hand-authored questions.
        hw.setMathQuiz(form.isMathQuiz());
        Integer mqc = form.getMathQuestionCount();
        hw.setMathQuestionCount(mqc != null && mqc > 0 ? mqc : 40);

        if (form.getVideoId() != null) {
            Video v = videoRepository.findById(form.getVideoId()).orElse(null);
            hw.setVideo(v);
        } else {
            hw.setVideo(null);
        }

        Homework saved = homeworkRepository.save(hw);

        // On NEW homework with a poolSize target, randomly pick that many
        // questions from the subject's question bank and link them via
        // homework_questions. If the bank doesn't have enough, we link
        // whatever's available and let the admin see the count to adjust.
        // Math quizzes auto-generate, so skip the bank step for those.
        if (isNew && !hw.isMathQuiz()
                && saved.getPoolSize() != null && saved.getPoolSize() > 0) {
            linkRandomQuestionsFromBank(saved, saved.getPoolSize());
            saved = homeworkRepository.save(saved);
        }
        return saved;
    }

    /**
     * Pull all questions from the bank that match this homework's subject,
     * shuffle them, and attach up to {@code target} of them to the homework
     * via the homework_questions join.
     */
    private void linkRandomQuestionsFromBank(Homework hw, int target) {
        Subject subject = hw.getSubject();
        if (subject == null) return;
        List<Question> bank = new ArrayList<>(questionRepository.findBySubject(subject));
        if (bank.isEmpty()) return;
        Collections.shuffle(bank);
        int take = Math.min(target, bank.size());
        hw.setQuestions(new ArrayList<>(bank.subList(0, take)));
    }

    /**
     * How many bank questions the homework ended up linked to. Useful for
     * surfacing "you asked for 10 but only 7 were available" in the UI.
     */
    public int linkedQuestionCount(Long homeworkId) {
        return findById(homeworkId).getQuestions().size();
    }

    /**
     * Delete a homework along with every row that depends on it:
     *   - quiz_attempts (and their quiz_answers + quiz_attempt_questions via JPA cascade)
     *   - homework_submissions
     *   - homework_questions join rows (via clearing the collection)
     *
     * Questions themselves are NOT deleted — they live in the shared bank
     * and may be linked by other homeworks.
     */
    @Transactional
    public void delete(Long id) {
        Homework hw = findById(id);

        // 1. Quiz attempts — load then deleteAll so JPA cascades answers
        //    and the quiz_attempt_questions ElementCollection rows.
        List<QuizAttempt> attempts = quizAttemptRepository.findByHomework(hw);
        if (!attempts.isEmpty()) {
            quizAttemptRepository.deleteAll(attempts);
            quizAttemptRepository.flush();
        }

        // 2. Homework submissions.
        List<HomeworkSubmission> subs = submissionRepository.findByHomework(hw);
        if (!subs.isEmpty()) {
            submissionRepository.deleteAll(subs);
            submissionRepository.flush();
        }

        // 3. Unlink bank questions — clearing the collection removes the
        //    homework_questions join rows only. The questions themselves
        //    are left intact in the bank.
        hw.getQuestions().clear();
        homeworkRepository.save(hw);
        homeworkRepository.flush();

        // 4. Finally the homework itself.
        homeworkRepository.delete(hw);
    }

    // === Submissions ===

    public Optional<HomeworkSubmission> findMySubmission(Homework hw, User student) {
        return submissionRepository.findByHomeworkAndStudent(hw, student);
    }

    public List<HomeworkSubmission> mySubmissions(User student) {
        return submissionRepository.findByStudentOrderBySubmittedAtDesc(student);
    }

    public List<HomeworkSubmission> submissionsFor(Homework hw) {
        return submissionRepository.findByHomeworkOrderBySubmittedAtDesc(hw);
    }

    @Transactional
    public HomeworkSubmission submit(Long homeworkId, User student, String answer) {
        Homework hw = findById(homeworkId);
        HomeworkSubmission sub = submissionRepository.findByHomeworkAndStudent(hw, student)
                .orElseGet(() -> {
                    HomeworkSubmission s = new HomeworkSubmission();
                    s.setHomework(hw);
                    s.setStudent(student);
                    s.setSubmittedAt(LocalDateTime.now());
                    return s;
                });
        sub.setAnswer(answer);
        sub.setSubmittedAt(LocalDateTime.now());
        // Reset grade when re-submitting
        sub.setGrade(null);
        sub.setFeedback(null);
        sub.setGradedAt(null);
        return submissionRepository.save(sub);
    }

    @Transactional
    public HomeworkSubmission grade(Long submissionId, Integer grade, String feedback) {
        HomeworkSubmission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));
        sub.setGrade(grade);
        sub.setFeedback(feedback);
        sub.setGradedAt(LocalDateTime.now());
        return submissionRepository.save(sub);
    }
}
