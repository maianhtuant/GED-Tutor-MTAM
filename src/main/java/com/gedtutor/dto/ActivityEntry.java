package com.gedtutor.dto;

import java.time.LocalDateTime;

/**
 * One completed quiz/math-quiz/practice run, flattened for display on the
 * admin Users page and the per-user activity page. Built entirely inside
 * {@code UserActivityService} (see that class) so the templates never
 * touch lazy JPA associations. {@code overviewUrl} is the ready-made link
 * to this attempt's question-by-question detail page (admin-only), routed
 * server-side so the template doesn't need to branch on attempt type.
 */
public class ActivityEntry {

    private final String kind;
    private final String subjectName;
    private final String title;
    private final int score;
    private final int total;
    private final LocalDateTime completedAt;
    private final String overviewUrl;

    public ActivityEntry(String kind, String subjectName, String title, int score, int total,
                         LocalDateTime completedAt, String overviewUrl) {
        this.kind = kind;
        this.subjectName = subjectName;
        this.title = title;
        this.score = score;
        this.total = total;
        this.completedAt = completedAt;
        this.overviewUrl = overviewUrl;
    }

    public String getKind() { return kind; }
    public String getSubjectName() { return subjectName; }
    public String getTitle() { return title; }
    public int getScore() { return score; }
    public int getTotal() { return total; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getOverviewUrl() { return overviewUrl; }

    public int getPercent() {
        return total > 0 ? Math.round(score * 100f / total) : 0;
    }
}
