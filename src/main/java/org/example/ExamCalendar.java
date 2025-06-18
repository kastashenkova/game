package org.example;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

/**
 * A Swing JPanel that displays a calendar view, specifically highlighting exam dates.
 * It also provides a method to check for exam reminders.
 */
class ExamCalendar extends JPanel {
    private List<LocalDate> examDates;

    /**
     * Constructs a new {@code ExamCalendar} panel.
     * Initializes the panel's preferred size, background color, and a list of predefined exam dates.
     */
    public ExamCalendar() {
        setPreferredSize(new Dimension(700, 400));
        setBackground(Color.WHITE);
        examDates = new ArrayList<>();
        // Hardcoded exam dates for demonstration
        examDates.add(LocalDate.of(2024, Month.NOVEMBER,20));
        examDates.add(LocalDate.of(2024, Month.NOVEMBER, 22));
        examDates.add(LocalDate.of(2024, Month.NOVEMBER,18));

    }

    /**
     * Checks if today's date (hardcoded as November 17, 2024) is three days before any of the scheduled exam dates.
     *
     * @return {@code true} if a reminder should be shown (i.e., today is 3 days before an exam), {@code false} otherwise.
     */
    public boolean showReminder() {
        LocalDate today = LocalDate.of(2024, Month.NOVEMBER, 17); // Hardcoded "today" for this reminder logic
        for (LocalDate exam : examDates) {
            if (exam.minusDays(3).equals(today)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Overrides the {@code paintComponent} method to draw the calendar grid and highlight exam days.
     * It draws a week-long view, with specific dates marked as "іспит!" (exam!) in a distinct color.
     *
     * @param g The {@code Graphics} context used for drawing.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("Sans-Serif", Font.PLAIN, 14));

        int w = 80; // Width of each day cell
        int h = 60; // Height of each day cell
        int startX = 50; // Starting X coordinate for the first cell
        int startY = 50; // Starting Y coordinate for the first cell
        int space = 10; // Space between cells
        int[] days = {17, 18, 19, 20, 21, 22, 23}; // Days to display in the calendar view

        for (int i = 0; i < days.length; i++) {
            int x = startX + i * (w + space);
            int y = startY;
            // Determine if the current day is an exam day (hardcoded logic)
            boolean isExam = days[i] == 20 || days[i] == 22;
            if (isExam) {
                g2.setColor(new Color(236, 73, 33)); // Reddish color for exam days
                g2.fillRect(x, y, w, h);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, y, w,h);
                g2.drawString("іспит!", x + 15, y + 35); // Draw "іспит!"
            } else {
                g2.setColor(new Color(176, 236, 211)); // Greenish color for non-exam days
                g2.fillRect(x, y, w,h);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, y, w, h);
            }
            g2.drawString(String.valueOf(days[i]), x + 30, y + 15); // Draw the day number
        }
    }
}