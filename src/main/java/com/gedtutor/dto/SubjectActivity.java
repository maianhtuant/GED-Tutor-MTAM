package com.gedtutor.dto;

/**
 * One user's rolled-up performance in one subject: quiz average (homework
 * quizzes, including math-quiz-mode homework) and practice-set average,
 * each with how many completed runs fed the average. Used by the
 * admin "Activity by Subject" matrix (one row per user, one column per
 * subject).
 */
public class SubjectActivity {

    private final Integer quizAvgPercent;
    private final int quizCount;
    private final Integer practiceAvgPercent;
    private final int practiceCount;

    public SubjectActivity(Integer quizAvgPercent, int quizCount, Integer practiceAvgPercent, int practiceCount) {
        this.quizAvgPercent = quizAvgPercent;
        this.quizCount = quizCount;
        this.practiceAvgPercent = practiceAvgPercent;
        this.practiceCount = practiceCount;
    }

    public Integer getQuizAvgPercent() { return quizAvgPercent; }
    public int getQuizCount() { return quizCount; }
    public Integer getPracticeAvgPercent() { return practiceAvgPercent; }
    public int getPracticeCount() { return practiceCount; }

    public boolean hasQuiz() { return quizCount > 0; }
    public boolean hasPractice() { return practiceCount > 0; }
    public boolean isEmpty() { return quizCount == 0 && practiceCount == 0; }
}
