package com.gedtutor.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Question-by-question detail for one completed quiz / math-quiz / practice
 * attempt, built entirely inside {@code AttemptDetailService} so the
 * admin "Overview" template never touches a lazy JPA association.
 */
public class AttemptDetail {

    private final String kind;
    private final String title;
    private final String subjectName;
    private final String studentUsername;
    private final int score;
    private final int total;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
    private final List<AnswerRow> rows;

    public AttemptDetail(String kind, String title, String subjectName, String studentUsername,
                         int score, int total, LocalDateTime startedAt, LocalDateTime completedAt,
                         List<AnswerRow> rows) {
        this.kind = kind;
        this.title = title;
        this.subjectName = subjectName;
        this.studentUsername = studentUsername;
        this.score = score;
        this.total = total;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.rows = rows;
    }

    public String getKind() { return kind; }
    public String getTitle() { return title; }
    public String getSubjectName() { return subjectName; }
    public String getStudentUsername() { return studentUsername; }
    public int getScore() { return score; }
    public int getTotal() { return total; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public List<AnswerRow> getRows() { return rows; }

    public int getPercent() {
        return total > 0 ? Math.round(score * 100f / total) : 0;
    }
}
