package org.example;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
//calendar with dates of exams
class ExamCalendar extends JPanel {
    private List<LocalDate> examDates;
    public ExamCalendar() {
        setPreferredSize(new Dimension(700, 400));
        setBackground(Color.WHITE);
        examDates = new ArrayList<>();
        examDates.add(LocalDate.of(2024, Month.NOVEMBER,20));
        examDates.add(LocalDate.of(2024, Month.NOVEMBER, 22));
        examDates.add(LocalDate.of(2024, Month.NOVEMBER,18));

    }
    public boolean showReminder() {
        LocalDate today = LocalDate.of(2024, Month.NOVEMBER, 17);
        for (LocalDate exam : examDates) {
            if (exam.minusDays(3).equals(today)) {
                return true;
            }
        }
        return false;
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("Sans-Serif", Font.PLAIN, 14));

        int w = 80;
        int h = 60;
        int startX = 50;
        int startY = 50;
        int space = 10;
        int[] days = {17, 18, 19, 20, 21, 22, 23};

        for (int i = 0; i < days.length; i++) {
            int x = startX + i * (w + space);
            int y = startY;
            boolean isExam = days[i] == 20 || days[i] == 22;
            if (isExam) {
                g2.setColor(new Color(236, 73, 33));
                g2.fillRect(x, y, w, h);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, y, w,h);
                g2.drawString("іспит!", x + 15, y + 35);
            } else {
                g2.setColor(new Color(176, 236, 211));
                g2.fillRect(x, y, w,h);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, y, w, h);
            }
            g2.drawString(String.valueOf(days[i]), x + 30, y + 15);
        }
    }
}

