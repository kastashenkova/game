package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//start window of session
public class ExamSessionWindow extends JFrame {
    private ExamCalendar calendarPanel;
    private JButton success;

    public ExamSessionWindow() {
        setTitle("Рівень 3: Сесія");
        setSize(800, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        JLabel schedule = new JLabel("Сесія. Розклад іспитів", SwingConstants.CENTER);
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

        success.addActionListener(new ActionListener() {
            @Override
           public void actionPerformed(ActionEvent e) {
                    dispose();
            }});
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(success);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}



