package com.gedtutor.dto;

/**
 * One question-and-answer row on the admin attempt "Overview" page.
 * Flattened from a QuizAnswer / MathQuizAnswer / PracticeAnswer so the
 * template never touches a JPA entity directly.
 */
public class AnswerRow {

    private final int index;
    private final String questionText;
    private final String expectedAnswer;
    private final String studentAnswer;
    private final boolean correct;
    private final String explanation;

    public AnswerRow(int index, String questionText, String expectedAnswer, String studentAnswer,
                     boolean correct, String explanation) {
        this.index = index;
        this.questionText = questionText;
        this.expectedAnswer = expectedAnswer;
        this.studentAnswer = studentAnswer;
        this.correct = correct;
        this.explanation = explanation;
    }

    public int getIndex() { return index; }
    public String getQuestionText() { return questionText; }
    public String getExpectedAnswer() { return expectedAnswer; }
    public String getStudentAnswer() { return studentAnswer; }
    public boolean isCorrect() { return correct; }
    public String getExplanation() { return explanation; }
}
