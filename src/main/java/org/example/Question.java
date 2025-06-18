package org.example;

import java.util.List;

/**
 * The Question class represents a quiz question, supporting different types of questions
 * with either free-text answers or multiple-choice options.
 */
public class Question {
    private String questionText;
    private String type;
    private String correctAnswer;
    private List<String> options;

    /**
     * Constructor for a question that requires a manually entered answer.
     *
     * @param questionText The text of the question.
     * @param type The type of question (e.g., "text_input").
     * @param correctAnswer The correct answer for the question.
     */
    public Question(String questionText, String type, String correctAnswer){ // For a question with manual answer input
        this.questionText = questionText;
        this.type = type;
        this.correctAnswer = correctAnswer;
    }

    /**
     * Constructor for a multiple-choice question with predefined answer options.
     *
     * @param questionText The text of the question.
     * @param type The type of question (e.g., "multiple_choice").
     * @param options A list of possible answer options.
     * @param correctAnswer The correct answer for the question, which should be one of the options.
     */
    public Question(String questionText, String type, List<String> options, String correctAnswer){ // Given answer options
        this.questionText = questionText;
        this.type = type;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    /**
     * Gets the text of the question.
     * @return The question text.
     */
    public String getQuestionText() {
        return questionText;
    }

    /**
     * Gets the type of the question.
     * @return The question type.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the correct answer for the question.
     * @return The correct answer.
     */
    public String getCorrectAnswer() {
        return correctAnswer;
    }

    /**
     * Gets the list of options for multiple-choice questions.
     * @return A list of answer options, or null if it's a text input question.
     */
    public List<String> getOptions() {
        return options;
    }
}