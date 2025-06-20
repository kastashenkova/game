package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.List;

/**
 * Represents the main exam panel where users can answer questions from an {@link ExamTicket}.
 * It manages the display of questions, user input, tracking of correct answers,
 * and presenting the final results.
 */
public class Exam extends JPanel {
    private ExamTicket ticket;
    private JFrame frame;
    private int currentQuestionInd = 0;
    private int correctAnswers = 0;
    private JLabel questionL;
    private JTextField textAnswerField;
    private ButtonGroup choice;
    private JPanel answerP;
    private JButton next;

    /**
     * Constructs a new Exam panel.
     *
     * @param ticket The {@link ExamTicket} containing the questions for this exam.
     * @param frame The parent {@link JFrame} where this panel will be displayed.
     */
    public Exam(ExamTicket ticket, JFrame frame) {
        this.ticket = ticket;
        this.frame = frame;
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        questionL = new JLabel();
        questionL.setFont(new Font("Sans-Serif", Font.BOLD, 18));
        add(questionL, BorderLayout.NORTH);
        answerP = new JPanel();
        answerP.setLayout(new BoxLayout(answerP, BoxLayout.Y_AXIS));
        add(answerP, BorderLayout.CENTER);
        next = new JButton("Далі");
        next.setFont(new Font("Sans-Serif", Font.BOLD, 16));
        next.addActionListener(this::handleNextQuestion);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(next);
        add(buttonPanel, BorderLayout.SOUTH);
        showQuestion();
    }

    /**
     * Displays the current question on the panel.
     * It clears the previous answer components and sets up new ones based on the question type (text or choice).
     * If all questions have been answered, it calls {@link #showResult()}.
     */
    private void showQuestion(){
        answerP.removeAll();
        if (currentQuestionInd >= ticket.getQuestions().size()) {
            showResult();
            return;
        }
        Question que = ticket.getQuestions().get(currentQuestionInd);
        questionL.setText((currentQuestionInd + 1) + ". " + que.getQuestionText());
        if ("text".equals(que.getType())) {
            textAnswerField = new JTextField(30);
            answerP.add(textAnswerField);
        } else if ("choice".equals(que.getType())) {
            choice = new ButtonGroup();
            for (String option : que.getOptions()) {
                JRadioButton radio = new JRadioButton(option);
                radio.setFont(new Font("Sans-Serif", Font.PLAIN, 14));
                choice.add(radio);
                answerP.add(radio);
            }
        }
        revalidate();
        repaint();
    }

    /**
     * Handles the action when the "Next" button is clicked.
     * It retrieves the user's answer based on the current question type,
     * compares it with the correct answer, increments the correct answers count if right,
     * and then proceeds to show the next question.
     *
     * @param e The ActionEvent generated by the button click.
     */
    private void handleNextQuestion(ActionEvent e) {
        if (currentQuestionInd >= ticket.getQuestions().size())
            return;

        Question question = ticket.getQuestions().get(currentQuestionInd);
        String usersAnsw = " ";
        if ("text".equals(question.getType())) {
            usersAnsw = textAnswerField.getText().trim();
        } else if ("choice".equals(question.getType())) {
            for (Enumeration<AbstractButton> buttons = choice.getElements(); buttons.hasMoreElements(); ) {
                AbstractButton button = buttons.nextElement();
                if (button.isSelected()) {
                    usersAnsw = button.getText();
                    break;
                }
            }
        }
        if (usersAnsw.equalsIgnoreCase(question.getCorrectAnswer())) {
            correctAnswers++;
        }
        currentQuestionInd++;
        showQuestion();
    }

    /**
     * Displays the final result of the exam, showing the number of correct answers.
     * It also provides a button to return to the ticket selection screen.
     */
    private void showResult() {
        removeAll();
        JLabel resultL = new JLabel("Ваш результат: " + correctAnswers + " з " + ticket.getQuestions().size() + " правильних", SwingConstants.CENTER);
        resultL.setFont(new Font("Sans-Serif", Font.BOLD, 20));
        add(resultL, BorderLayout.CENTER);
        JButton back = new JButton("Повернутися до білетів");
        back.setFont(new Font("Sans-Serif", Font.PLAIN, 16));
        back.addActionListener(e -> {
            List<ExamTicket> tickets = ExamTicket.generateSampleTickets();
            ExamMainWindow window = new ExamMainWindow(tickets);
            window.setTicketSelectedListener(t -> {
                frame.setContentPane(new Exam(t, frame));
                frame.revalidate();});
            frame.setContentPane(window);
            frame.revalidate();
        });
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(back);
        add(bottomPanel, BorderLayout.SOUTH);
        revalidate();
        repaint();
    }
}