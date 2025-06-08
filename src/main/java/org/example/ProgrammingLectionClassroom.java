package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class ProgrammingLectionClassroom extends JPanel {

    private final int DESK_WIDTH = 150;
    private final int DESK_DEPTH = 70;
    private final int CHAIR_WIDTH = 45;
    private final int CHAIR_DEPTH = 50;
    private final int AISLE_WIDTH = 80;
    private final int ROW_SPACING = 100;
    private final int COLUMN_SPACING = 180;
    private final int DESK_HEIGHT = 75;
    private final int CHAIR_SEAT_HEIGHT = 45;
    private final int CHAIR_BACK_HEIGHT = 80;

    private final Color WALL_COLOR = new Color(235, 235, 235);
    private final Color FLOOR_COLOR = new Color(190, 150, 110);
    private final Color DESK_COLOR = new Color(160, 120, 80);
    private final Color CHAIR_COLOR = new Color(80, 80, 80);
    private final Color BLACKBOARD_COLOR = new Color(20, 20, 20);
    private final Color WINDOW_FRAME_COLOR = new Color(100, 100, 100);
    private final Color WINDOW_GLASS_COLOR = new Color(200, 230, 255, 150);
    private final Color DOOR_COLOR = new Color(139, 69, 19);
    private final Color EXIT_BUTTON_COLOR = new Color(220, 0, 0);
    private final Color EXIT_BUTTON_TEXT_COLOR = Color.WHITE;

    public ProgrammingLectionClassroom() {
        setPreferredSize(new Dimension(1200, 800));
        setBackground(WALL_COLOR);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

// --- Floor ---
        g2d.setColor(FLOOR_COLOR);
        g2d.fillRect(0, getHeight() / 3, getWidth(), getHeight() * 2 / 3);

// --- Back Wall ---
        g2d.setColor(WALL_COLOR);
        g2d.fillRect(0, 0, getWidth(), getHeight() / 3);

// --- Blackboard ---
        int blackboardWidth = getWidth() / 2;
        int blackboardHeight = 150;
        int blackboardX = (getWidth() - blackboardWidth) / 2;
        int blackboardY = 20;
        g2d.setColor(BLACKBOARD_COLOR);
        g2d.fillRect(blackboardX, blackboardY, blackboardWidth, blackboardHeight);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.ITALIC, 20));
        g2d.drawString("Лекція", blackboardX + 20, blackboardY + 30);
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(new Color(80, 80, 80));
        g2d.drawRect(blackboardX, blackboardY, blackboardWidth, blackboardHeight);

// --- Window (One window, not above the blackboard) ---
        int windowWidth = 200;
        int windowHeight = 150;
        int windowX = getWidth() - windowWidth - 50;
        int windowY = 25;
        g2d.setColor(WINDOW_FRAME_COLOR);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(windowX, windowY, windowWidth, windowHeight);
        g2d.setColor(WINDOW_GLASS_COLOR);
        g2d.fillRect(windowX + 2, windowY + 2, windowWidth - 4, windowHeight - 4);
        g2d.setColor(WINDOW_FRAME_COLOR);
        g2d.drawLine(windowX + windowWidth / 2, windowY + 5, windowX + windowWidth / 2, windowY + windowHeight - 5);
        g2d.drawLine(windowX + 5, windowY + windowHeight / 2, windowX + windowWidth - 5, windowY + windowHeight / 2);

// --- Door ---
        int doorWidth = 80;
        int doorHeight = 180;
        int doorX = 30;
        int doorY = getHeight() / 3 - doorHeight;
        g2d.setColor(DOOR_COLOR);
        g2d.fillRect(doorX, doorY, doorWidth, doorHeight);
        g2d.setColor(new Color(110, 49, 0));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(doorX, doorY, doorWidth, doorHeight);
        g2d.setColor(new Color(210, 210, 210));
        g2d.fillOval(doorX + doorWidth - 25, doorY + doorHeight / 2 - 10, 20, 20);

// --- Exit Button on Door ---
        int exitButtonWidth = 60;
        int exitButtonHeight = 30;
        int exitButtonX = doorX + doorWidth / 2 - exitButtonWidth / 2;
        int exitButtonY = doorY + 20;
        g2d.setColor(EXIT_BUTTON_COLOR);
        g2d.fillRoundRect(exitButtonX, exitButtonY, exitButtonWidth, exitButtonHeight, 10, 10);
        g2d.setColor(EXIT_BUTTON_TEXT_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        Rectangle2D r = fm.getStringBounds("Вихід", g2d);
        int textX = (int) (exitButtonX + (exitButtonWidth - r.getWidth()) / 2);
        int textY = (int) (exitButtonY + (exitButtonHeight - r.getHeight()) / 2 + fm.getAscent());
        g2d.drawString("Вихід", textX, textY);

// --- Desks and Chairs ---
        g2d.setColor(DESK_COLOR);
        int startX = (getWidth() - (3 * COLUMN_SPACING)) / 2;
        int startY = getHeight() / 3 + 50;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 3; col++) {
                int deskX = startX + col * COLUMN_SPACING;
                int deskY = startY + row * ROW_SPACING;
// Desk
                g2d.fillRect(deskX, deskY, DESK_WIDTH, DESK_DEPTH);
// Desk Legs
                g2d.fillRect(deskX + 5, deskY + DESK_DEPTH, 5, 20);
                g2d.fillRect(deskX + DESK_WIDTH - 10, deskY + DESK_DEPTH, 5, 20);
// Chair
                g2d.setColor(CHAIR_COLOR);
                int chairX = deskX + (DESK_WIDTH - CHAIR_WIDTH) / 2;
                int chairY = deskY + DESK_DEPTH - 5;
// Chair Seat
                g2d.fillRect(chairX, chairY, CHAIR_WIDTH, CHAIR_DEPTH);
// Chair Back
                g2d.fillRect(chairX + CHAIR_WIDTH - 10, chairY - (CHAIR_BACK_HEIGHT - CHAIR_DEPTH), 10, CHAIR_BACK_HEIGHT - CHAIR_DEPTH);
// Chair Legs
                g2d.fillRect(chairX + 5, chairY + CHAIR_DEPTH, 5, 15);
                g2d.fillRect(chairX + CHAIR_WIDTH - 10, chairY + CHAIR_DEPTH, 5, 15);
                g2d.setColor(DESK_COLOR); // Reset desk color for next desk
            }
        }


// --- Teacher's Desk ---
        g2d.setColor(new Color(130, 90, 50));
        int teacherDeskWidth = 200;
        int teacherDeskDepth = 80;
        int teacherDeskX = getWidth() / 2 - teacherDeskWidth / 2;
        int teacherDeskY = blackboardY + blackboardHeight + 30;
        g2d.fillRect(teacherDeskX, teacherDeskY, teacherDeskWidth, teacherDeskDepth);
        g2d.fillRect(teacherDeskX + 10, teacherDeskY + teacherDeskDepth, 10, 30); // Legs
        g2d.fillRect(teacherDeskX + teacherDeskWidth - 20, teacherDeskY + teacherDeskDepth, 10, 30);
    }


//ТЕСТ
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Авдиторія для лекцій із програмування");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            ProgrammingLectionClassroom programmingLectionClassroom = new ProgrammingLectionClassroom();
            frame.add(programmingLectionClassroom, BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

        });

    }

}