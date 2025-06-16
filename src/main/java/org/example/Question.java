package org.example;

import java.util.List;
//class for questions
public class Question {
    private String questionText;
    private String type;
    private String correctAnswer;
    private List<String> options;

    public Question(String questionText, String type, String correctAnswer){//для питання з введенням відповіді власноруч
        this.questionText = questionText;
        this.type = type;
        this.correctAnswer = correctAnswer;
    }
    public Question(String questionText, String type, List<String> options, String correctAnswer){//дано варіанти відповідей
        this.questionText = questionText;
        this.type = type;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }
    public String getQuestionText() {
        return questionText;
    }
    public String getType() {
        return type;
    }
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    public List<String> getOptions() {
        return options;
    }
}
