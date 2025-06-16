package org.example;

import javax.swing.*;
import java.awt.*;

//start window of session
public class ExamSessionWindow extends JFrame {
    private ExamCalendar calendarPanel;
    private JButton success;

    public ExamSessionWindow(ExamTicket ticket) {
        setTitle("Рівень 3: Сесія");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        JLabel schedule = new JLabel("Сесія: Розклад іспитів", SwingConstants.CENTER);
        schedule.setFont(new Font("Verdana", Font.BOLD, 24));
        schedule.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(schedule, BorderLayout.NORTH);

        calendarPanel = new ExamCalendar();
        add(calendarPanel, BorderLayout.CENTER);

        success = new JButton("Успіху на сесії!");
        success.setFont(new Font("Verdana", Font.BOLD, 16));
        success.setBackground(new Color(77, 123, 10));
        success.setForeground(Color.WHITE);
        success.setFocusPainted(false);

//        success.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                JOptionPane.showMessageDialog(ExamSessionWindow.this, "Ви склали сесію!", "Результат", JOptionPane.INFORMATION_MESSAGE);
//            }});
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(success);
        add(bottomPanel, BorderLayout.SOUTH);

//        if (calendarPanel.showReminder()) {
//            Reminder.showReminder(this, "Через 3 дні — іспит з програмування!");
//        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String ticketName = " ";
            new ExamSessionWindow(new ExamTicket(ticketName)).setVisible(true);
        });
    }
}




