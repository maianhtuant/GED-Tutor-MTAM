package com.gedtutor.service;

import com.gedtutor.dto.ActivityEntry;
import com.gedtutor.dto.SubjectActivity;
import com.gedtutor.dto.UserActivitySummary;
import com.gedtutor.model.Homework;
import com.gedtutor.model.PracticeAttempt;
import com.gedtutor.model.PracticeSet;
import com.gedtutor.model.QuizAttempt;
import com.gedtutor.model.Subject;
import com.gedtutor.repository.PracticeAttemptRepository;
import com.gedtutor.repository.QuizAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rolls up every student's completed {@link QuizAttempt} (homework quizzes,
 * including math-quiz-mode homework) and {@link PracticeAttempt} (curated
 * practice-set runs) into a per-user summary for the admin Users page.
 *
 * <p>Runs entirely inside one read-only transaction so the lazy
 * student/homework/practiceSet associations on each attempt are resolved
 * before the entities are flattened into {@link ActivityEntry} — the
 * template never sees a JPA entity here, so open-in-view=false can't bite.
 */
@Service
public class UserActivityService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final PracticeAttemptRepository practiceAttemptRepository;

    public UserActivityService(QuizAttemptRepository quizAttemptRepository,
                               PracticeAttemptRepository practiceAttemptRepository) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.practiceAttemptRepository = practiceAttemptRepository;
    }

    @Transactional(readOnly = true)
    public Map<Long, UserActivitySummary> summarizeAll() {
        Map<Long, List<ActivityEntry>> byStudent = new HashMap<>();

        for (QuizAttempt a : quizAttemptRepository.findAllCompletedWithStudentAndHomework()) {
            if (a.getScore() == null || a.getTotalQuestions() == null || a.getTotalQuestions() == 0) {
                continue; // shouldn't happen for a completed attempt, but be defensive
            }
            Homework hw = a.getHomework();
            String kind = hw.isMathQuiz() ? "Math Quiz" : "Quiz";
            String subjectName = hw.getSubject() != null ? hw.getSubject().getName() : "—";
            ActivityEntry entry = new ActivityEntry(kind, subjectName, hw.getTitle(),
                    a.getScore(), a.getTotalQuestions(), a.getCompletedAt(),
                    "/admin/attempts/quiz/" + a.getId());
            byStudent.computeIfAbsent(a.getStudent().getId(), k -> new ArrayList<>()).add(entry);
        }

        for (PracticeAttempt a : practiceAttemptRepository.findAllCompletedWithStudentAndSet()) {
            if (a.getScore() == null || a.getTotalQuestions() == null || a.getTotalQuestions() == 0) {
                continue;
            }
            PracticeSet set = a.getPracticeSet();
            String subjectName = set.getSubject() != null ? set.getSubject().getName() : "—";
            ActivityEntry entry = new ActivityEntry("Practice", subjectName, set.getTitle(),
                    a.getScore(), a.getTotalQuestions(), a.getCompletedAt(),
                    "/admin/attempts/practice/" + a.getId());
            byStudent.computeIfAbsent(a.getStudent().getId(), k -> new ArrayList<>()).add(entry);
        }

        Map<Long, UserActivitySummary> summaries = new HashMap<>();
        for (Map.Entry<Long, List<ActivityEntry>> e : byStudent.entrySet()) {
            List<ActivityEntry> entries = e.getValue();
            entries.sort(Comparator.comparing(ActivityEntry::getCompletedAt).reversed());
            double avg = entries.stream().mapToInt(ActivityEntry::getPercent).average().orElse(0);
            summaries.put(e.getKey(), new UserActivitySummary(entries.size(), (int) Math.round(avg), entries));
        }
        return summaries;
    }

    /**
     * Per-user, per-subject rollup for the admin "Activity by Subject"
     * matrix: outer key is student id, inner key is subject id. A
     * practice set with no subject assigned (allowed — {@code
     * PracticeSet.subject} is nullable) doesn't contribute to any column
     * here, since there's no subject to attribute it to.
     */
    @Transactional(readOnly = true)
    public Map<Long, Map<Long, SubjectActivity>> summarizeBySubject() {
        // studentId -> subjectId -> that student's percent scores in that subject
        Map<Long, Map<Long, List<Integer>>> quizPercents = new HashMap<>();
        Map<Long, Map<Long, List<Integer>>> practicePercents = new HashMap<>();

        for (QuizAttempt a : quizAttemptRepository.findAllCompletedWithStudentAndHomework()) {
            if (a.getScore() == null || a.getTotalQuestions() == null || a.getTotalQuestions() == 0) {
                continue;
            }
            Subject subject = a.getHomework().getSubject();
            if (subject == null) continue;
            addPercent(quizPercents, a.getStudent().getId(), subject.getId(),
                    percentOf(a.getScore(), a.getTotalQuestions()));
        }

        for (PracticeAttempt a : practiceAttemptRepository.findAllCompletedWithStudentAndSet()) {
            if (a.getScore() == null || a.getTotalQuestions() == null || a.getTotalQuestions() == 0) {
                continue;
            }
            Subject subject = a.getPracticeSet().getSubject();
            if (subject == null) continue; // no subject to attribute this run to
            addPercent(practicePercents, a.getStudent().getId(), subject.getId(),
                    percentOf(a.getScore(), a.getTotalQuestions()));
        }

        Set<Long> studentIds = new HashSet<>();
        studentIds.addAll(quizPercents.keySet());
        studentIds.addAll(practicePercents.keySet());

        Map<Long, Map<Long, SubjectActivity>> result = new HashMap<>();
        for (Long studentId : studentIds) {
            Map<Long, List<Integer>> qBySubject = quizPercents.getOrDefault(studentId, Map.of());
            Map<Long, List<Integer>> pBySubject = practicePercents.getOrDefault(studentId, Map.of());
            Set<Long> subjectIds = new HashSet<>();
            subjectIds.addAll(qBySubject.keySet());
            subjectIds.addAll(pBySubject.keySet());

            Map<Long, SubjectActivity> perSubject = new HashMap<>();
            for (Long subjectId : subjectIds) {
                List<Integer> q = qBySubject.getOrDefault(subjectId, List.of());
                List<Integer> p = pBySubject.getOrDefault(subjectId, List.of());
                perSubject.put(subjectId, new SubjectActivity(average(q), q.size(), average(p), p.size()));
            }
            result.put(studentId, perSubject);
        }
        return result;
    }

    private static int percentOf(int score, int total) {
        return Math.round(score * 100f / total);
    }

    private static void addPercent(Map<Long, Map<Long, List<Integer>>> byStudentBySubject,
                                   Long studentId, Long subjectId, int percent) {
        byStudentBySubject.computeIfAbsent(studentId, k -> new HashMap<>())
                .computeIfAbsent(subjectId, k -> new ArrayList<>())
                .add(percent);
    }

    private static Integer average(List<Integer> percents) {
        if (percents.isEmpty()) return null;
        return (int) Math.round(percents.stream().mapToInt(Integer::intValue).average().orElse(0));
    }
}
