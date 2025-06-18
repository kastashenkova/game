package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * The NAUKMAEntrance class is a JPanel that renders a stylized view of an entrance
 * resembling the National University of Kyiv-Mohyla Academy (NAUKMA).
 * It includes elements like walls, floor, ceiling, an entrance door, a security desk
 * with a monitor and keyboard, a chair, and wall decorations.
 */
public class NAUKMAEntrance extends JPanel {

    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_TURQUOISE = new Color(64, 224, 208);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);
    private final Color SIMS_BUTTON_HOVER = new Color(255, 240, 245);
    private static final Color SIMS_GREEN_CORRECT = new Color(144, 238, 144);
    private static final Color SIMS_RED_INCORRECT = new Color(255, 99, 71);

    /**
     * Overrides the paintComponent method to custom draw the NAUKMA entrance scene.
     * @param g The Graphics object to protect.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for better rendering quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // Define the floor height from the bottom edge of the panel
        int floorLevelY = panelHeight - 250; // Increased floor height to raise the "horizon"

        // --- Room Background: Walls and Floor ---
        // Walls (light cream with a subtle gradient)
        GradientPaint wallGradient = new GradientPaint(0, 0, new Color(245, 245, 220),
                0, floorLevelY, new Color(230, 230, 210));
        g2d.setPaint(wallGradient);
        g2d.fillRect(0, 0, panelWidth, floorLevelY); // Walls up to the floor level

        // Floor (light beige with a slight gradient and texture)
        GradientPaint floorGradient = new GradientPaint(0, floorLevelY, new Color(220, 220, 200),
                0, panelHeight, new Color(190, 190, 170));
        g2d.setPaint(floorGradient);
        g2d.fillRect(0, floorLevelY, panelWidth, panelHeight - floorLevelY); // Floor from its level to the bottom of the panel

        // Add imitation tiles to the floor
        g2d.setColor(new Color(180, 180, 160, 80)); // Transparent grey-brown
        int tileSize = 30;
        for (int i = 0; i < panelWidth; i += tileSize) {
            g2d.drawLine(i, floorLevelY, i, panelHeight);
        }
        for (int i = floorLevelY; i < panelHeight; i += tileSize) {
            g2d.drawLine(0, i, panelWidth, i);
        }

        // --- Ceiling ---
        g2d.setColor(new Color(220, 220, 200));
        g2d.fillRect(0, 0, panelWidth, 50); // Ceiling

        // Central light fixture (simple)
        g2d.setColor(new Color(255, 255, 200, 150)); // Light yellow translucent
        g2d.fillOval(panelWidth / 2 - 50, 10, 100, 30);
        g2d.setColor(new Color(100, 100, 80));
        g2d.drawOval(panelWidth / 2 - 50, 10, 100, 30);

        // --- NAUKMA Entrance ---
        int entranceWidth = 250; // Increased door width
        int entranceHeight = 350; // Increased door height
        int entranceX = panelWidth / 2 - entranceWidth / 2 - 80; // Changed: shifted left by 80 pixels
        int entranceY = floorLevelY - entranceHeight; // Position relative to the new floor level

        // Outer wall of the entrance
        g2d.setColor(new Color(80, 80, 80)); // Darker gray
        g2d.fill(new RoundRectangle2D.Double(entranceX - 15, entranceY - 15, entranceWidth + 30, entranceHeight + 30, 20, 20));

        // Door frame
        g2d.setColor(new Color(120, 120, 120)); // Gray
        g2d.fill(new RoundRectangle2D.Double(entranceX, entranceY, entranceWidth, entranceHeight, 15, 15));

        // Wooden panels (doors)
        Color woodColor1 = new Color(139, 69, 19); // Dark brown
        Color woodColor2 = new Color(160, 82, 45); // Medium brown

        // Left door panel
        GradientPaint woodGradient1 = new GradientPaint(entranceX + 15, entranceY + 10, woodColor1,
                entranceX + entranceWidth / 2 - 5, entranceY + entranceHeight - 10, woodColor2);
        g2d.setPaint(woodGradient1);
        g2d.fill(new RoundRectangle2D.Double(entranceX + 15, entranceY + 10, entranceWidth / 2 - 20, entranceHeight - 20, 10, 10));

        // Right door panel
        GradientPaint woodGradient2 = new GradientPaint(entranceX + entranceWidth / 2 + 5, entranceY + 10, woodColor1,
                entranceX + entranceWidth - 15, entranceY + entranceHeight - 10, woodColor2);
        g2d.setPaint(woodGradient2);
        g2d.fill(new RoundRectangle2D.Double(entranceX + entranceWidth / 2 + 5, entranceY + 10, entranceWidth / 2 - 20, entranceHeight - 20, 10, 10));

        // Door handles
        g2d.setColor(new Color(180, 180, 180));
        g2d.fill(new RoundRectangle2D.Double(entranceX + entranceWidth / 4 - 5, entranceY + entranceHeight / 2 - 30, 15, 60, 8, 8));
        g2d.fill(new RoundRectangle2D.Double(entranceX + entranceWidth * 3 / 4 - 10, entranceY + entranceHeight / 2 - 30, 15, 60, 8, 8));

        // "NAUKMA" inscription above the entrance
        g2d.setColor(new Color(30, 30, 30)); // Darker color
        g2d.setFont(new Font("Arial", Font.BOLD, 32)); // Larger font
        FontMetrics fm = g2d.getFontMetrics();
        String naukmaText = "НАУКМА";
        int textWidth = fm.stringWidth(naukmaText);
        g2d.drawString(naukmaText, entranceX + entranceWidth / 2 - textWidth / 2, entranceY - 25);

        // --- Security Guard's Desk ---
        int deskWidth = 400; // Even greater width
        int deskHeight = 150; // Even greater height
        int deskDepth = 100; // Even greater depth for 3D imitation
        int deskX = panelWidth - deskWidth - 80; // Changed position for a larger desk
        int deskY = floorLevelY - deskHeight; // Position relative to the new floor level

        // Desk shadow
        g2d.setColor(new Color(0, 0, 0, 80)); // More noticeable shadow
        g2d.fillOval(deskX + 40, floorLevelY - 30, deskWidth - 80, 50);

        // Back part of the desk (darker)
        g2d.setColor(new Color(100, 50, 10)); // Dark brown
        g2d.fillRect(deskX + deskDepth / 2, deskY, deskWidth - deskDepth, deskHeight);

        // Top surface of the desk
        GradientPaint deskTopGradient = new GradientPaint(deskX, deskY, new Color(160, 82, 45),
                deskX + deskWidth, deskY + deskDepth, new Color(120, 60, 20));
        g2d.setPaint(deskTopGradient);
        g2d.fill(new Polygon(new int[]{deskX, deskX + deskDepth, deskX + deskWidth, deskX + deskWidth - deskDepth},
                new int[]{deskY, deskY, deskY + deskHeight, deskY + deskHeight}, 4));

        // Front part of the desk
        g2d.setColor(new Color(139, 69, 19)); // Dark brown
        g2d.fillRect(deskX, deskY + deskDepth, deskWidth, deskHeight - deskDepth);


        // --- Objects on the Desk ---
        // Monitor
        g2d.setColor(new Color(50, 50, 50)); // Dark gray
        g2d.fill(new RoundRectangle2D.Double(deskX + 60, deskY - 60, 120, 90, 20, 20)); // Larger monitor
        g2d.setColor(new Color(100, 100, 100)); // Monitor stand
        g2d.fillRect(deskX + 115, deskY + 30, 10, 40);
        g2d.setColor(new Color(0, 100, 200)); // Screen (blue)
        g2d.fill(new RoundRectangle2D.Double(deskX + 65, deskY - 55, 110, 80, 15, 15));

        // Keyboard
        g2d.setColor(new Color(80, 80, 80));
        g2d.fill(new RoundRectangle2D.Double(deskX + 200, deskY + 40, 120, 45, 10, 10)); // Larger keyboard

        // Mouse
        g2d.setColor(new Color(80, 80, 80));
        g2d.fillOval(deskX + 330, deskY + 45, 25, 25); // Larger mouse

        // --- Security Guard's Chair (empty) ---
        int chairWidth = 120; // Increased chair width
        int chairHeight = 160; // Increased chair height
        int chairX = deskX + 100; // Position relative to the new desk
        int chairY = floorLevelY - chairHeight + 30; // Position relative to the new floor level

        // Chair shadow
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillOval(chairX + 20, floorLevelY - 20, chairWidth - 40, 30);

        // Chair backrest
        g2d.setColor(new Color(90, 90, 90)); // Dark gray
        g2d.fill(new RoundRectangle2D.Double(chairX, chairY, chairWidth, chairHeight - 60, 30, 30));

        // Chair seat
        g2d.setColor(new Color(120, 120, 120)); // Lighter gray
        g2d.fill(new Ellipse2D.Double(chairX, chairY + chairHeight - 80, chairWidth, 70)); // Larger seat

        // Chair legs
        g2d.setColor(new Color(70, 70, 70));
        g2d.fillRect(chairX + 25, chairY + chairHeight - 30, 10, 35);
        g2d.fillRect(chairX + chairWidth - 35, chairY + chairHeight - 30, 10, 35);


        // --- Wall Decorations ---
        // Simple painting/poster
        g2d.setColor(new Color(180, 180, 180)); // Light frame
        g2d.fill(new RoundRectangle2D.Double(50, 80, 150, 100, 15, 15)); // Larger painting
        g2d.setColor(new Color(100, 150, 200)); // "Drawing"
        g2d.fill(new RoundRectangle2D.Double(55, 85, 140, 90, 10, 10));

        // Clock
        g2d.setColor(new Color(100, 100, 100)); // Clock frame
        g2d.fillOval(panelWidth - 180, 80, 80, 80); // Larger clock
        g2d.setColor(Color.WHITE); // Clock face
        g2d.fillOval(panelWidth - 175, 85, 70, 70);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(panelWidth - 140, 100, 2, 30); // Hour hand
        g2d.fillRect(panelWidth - 140, 100, 2, 40); // Minute hand (slightly longer)
    }

    /**
     * Main method to create and display the NAUKMAEntrance panel within a JFrame.
     * Sets the look and feel to Nimbus and applies custom SIMS-like colors.
     * @param args Command line arguments (not used).
     * @throws UnsupportedLookAndFeelException If the specified look and feel is not supported.
     * @throws ClassNotFoundException If the class for the look and feel cannot be found.
     * @throws InstantiationException If a new instance of the look and feel class cannot be created.
     * @throws IllegalAccessException If the application does not have access to the definition of the specified class.
     */
    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        UIManager.put("nimbusBase", SIMS_MEDIUM_PINK);
        UIManager.put("nimbusBlueGrey", SIMS_LIGHT_BLUE);
        UIManager.put("control", SIMS_LIGHT_PINK);
        UIManager.put("textForeground", SIMS_DARK_TEXT);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Вхід до НаУКМА");
            NAUKMAEntrance panel = new NAUKMAEntrance();
            frame.add(panel);
            frame.setSize(1200, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null); // Center on screen
            frame.setVisible(true);
        });
    }
}