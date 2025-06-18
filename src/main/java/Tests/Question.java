package Tests;

/**
 * Represents a single question in a test, including the question text,
 * an array of possible answer options, and the index of the correct answer.
 */
public class Question {
    /**
     * The text of the question.
     */
    public String question;
    /**
     * An array of strings representing the possible answer options for the question.
     * The order of options matters for referencing the correct answer.
     */
    public String[] options;
    /**
     * The zero-based index of the correct answer within the {@code options} array.
     */
    public int correctAnswer;

    /**
     * Constructs a new Question object.
     *
     * @param question The text of the question.
     * @param options An array of strings containing the answer options.
     * @param correctAnswer The zero-based index of the correct answer within the {@code options} array.
     */
    public Question(String question, String[] options, int correctAnswer) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }
}