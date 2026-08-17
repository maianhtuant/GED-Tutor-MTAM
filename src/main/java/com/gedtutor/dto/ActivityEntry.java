package com.gedtutor.dto;

import java.time.LocalDateTime;

/**
 * One completed quiz/math-quiz/practice run, flattened for display on the
 * admin Users page. Built entirely inside {@code UserActivityService} (see
 * that class) so the admin/users.html template never touches lazy JPA
 * associations.
 */
public class ActivityEntry {

    private final String kind;
    private final String title;
    private final int score;
    private final int total;
    private final LocalDateTime completedAt;

    public ActivityEntry(String kind, String title, int score, int total, LocalDateTime completedAt) {
        this.kind = kind;
        this.title = title;
        this.score = score;
        this.total = total;
        this.completedAt = completedAt;
    }

    public String getKind() { return kind; }
    public String getTitle() { return title; }
    public int getScore() { return score; }
    public int getTotal() { return total; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    public int getPercent() {
        return total > 0 ? Math.round(score * 100f / total) : 0;
    }
}
