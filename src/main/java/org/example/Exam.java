package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.List;
//class for the main exam where we have frame with all buttons for usage while answering questions
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

    private void showResult() {
        removeAll();
        JLabel resultL = new JLabel("Ваш результат: " + correctAnswers + " з " + ticket.getQuestions().size() + " правильних", SwingConstants.CENTER);
        resultL.setFont(new Font("Sans-Serif", Font.BOLD, 20));
        add(resultL, BorderLayout.CENTER);
        JButton back = new JButton("Повернутись до білетів");
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
