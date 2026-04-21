package com.gedtutor.dto;

import com.gedtutor.model.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class QuestionForm {

    private Long id;

    @NotNull
    private QuestionType type;

    @NotBlank
    private String questionText;

    @NotBlank
    private String correctAnswer;

    private String explanation;

    private int orderIndex;

    // For MULTIPLE_CHOICE: list of choice texts
    private List<String> choices = new ArrayList<>();

    public QuestionForm() {
        choices.add("");
        choices.add("");
        choices.add("");
        choices.add("");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public List<String> getChoices() { return choices; }
    public void setChoices(List<String> choices) { this.choices = choices; }
}
