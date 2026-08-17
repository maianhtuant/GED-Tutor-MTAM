package com.gedtutor.dto;

import java.util.List;

/**
 * Per-user rollup of every completed quiz / math-quiz / practice-set run,
 * used by the admin Users page's "Activity" column.
 */
public class UserActivitySummary {

    private final int totalAttempts;
    private final int avgPercent;
    private final List<ActivityEntry> entries;

    public UserActivitySummary(int totalAttempts, int avgPercent, List<ActivityEntry> entries) {
        this.totalAttempts = totalAttempts;
        this.avgPercent = avgPercent;
        this.entries = entries;
    }

    public int getTotalAttempts() { return totalAttempts; }
    public int getAvgPercent() { return avgPercent; }
    public List<ActivityEntry> getEntries() { return entries; }
}
