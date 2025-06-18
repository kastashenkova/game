package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.awt.geom.AffineTransform; // Import for AffineTransform

/**
 * The `CreditWindow` class represents a JDialog that allows a student to attempt to pass a "zalik" (credit)
 * discipline by spinning a wheel to earn points. It manages attempts, displays scores,
 * and handles passing/failing logic, including potential expulsion.
 */
public class CreditWindow extends JDialog {
    private Discipline discipline;
    private Student student;
    private int currentAttempt; // Tracks the current attempt number within this window session
    private JLabel scoreLabel;
    private JButton spinButton;
    private WheelPanel wheelPanel;
    private Integer currentSpunScore = null; // Stores the score obtained from the last wheel spin
    private JLabel attemptLabel;

    private static final int MAX_REGULAR_ATTEMPTS = 2; // Maximum attempts before it's considered a "resit"
    private static final int MAX_TOTAL_ATTEMPTS = 3; // Maximum total attempts before expulsion
    private static final int PASSING_SCORE = 60; // Minimum score required to pass the zalik

    // Define custom colors for the UI, inspired by Sims
    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_ACCENT_COLOR = new Color(255, 179, 186); // Accent color for wheel sectors

    private StudyProgressGUI parentGUI; // Reference to the parent GUI to update progress display

    /**
     * Constructs a new `CreditWindow`.
     *
     * @param owner The parent {@link Frame} of this dialog, typically a {@link StudyProgressGUI}.
     * @param discipline The {@link Discipline} for which the student is attempting to get a credit.
     * @param student The {@link Student} object attempting the credit.
     * @param currentAttempt The current attempt number being made (used for display and logic).
     */
    public CreditWindow(Frame owner, Discipline discipline, Student student, int currentAttempt) {
        super(owner, "Залік", true); // Call JDialog constructor with modal behavior
        // Check if the owner is an instance of StudyProgressGUI to set the callback reference
        if (owner instanceof StudyProgressGUI) {
            this.parentGUI = (StudyProgressGUI) owner;
        } else {
            System.err.println("Помилка: CreditWindow повинен бути викликаний з StudyProgressGUI.");
        }

        this.discipline = discipline;
        this.student = student;
        this.currentAttempt = currentAttempt;

        setSize(800, 600); // Set dialog size
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Close operation
        setResizable(false); // Prevent resizing
        setLocationRelativeTo(owner); // Center relative to the owner frame

        initComponents(); // Initialize UI components
        applySimsStyle(); // Apply custom Sims-like styling
        updateUIBasedOnAttempts(); // Update UI elements based on current attempt status
    }

    /**
     * Initializes and arranges all the Swing UI components within the dialog.
     * This includes labels for title, attempt status, score, the spinning wheel, and the spin button.
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10)); // Main layout for the dialog
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Vertical box layout for main content
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding

        // Title label for the discipline
        JLabel titleLabel = new JLabel("Отримання балів за залік з дисципліни «" + discipline.getName() + "»");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Vertical spacing

        // Label to display current attempt status
        attemptLabel = new JLabel();
        attemptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        attemptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(attemptLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Spinning wheel panel
        wheelPanel = new WheelPanel();
        wheelPanel.setPreferredSize(new Dimension(350, 350)); // Set preferred size
        wheelPanel.setMaximumSize(new Dimension(350, 350)); // Set maximum size
        wheelPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally
        mainPanel.add(wheelPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Label to display the spun score or current total score
        scoreLabel = new JLabel(" ");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        scoreLabel.setForeground(SIMS_DARK_TEXT);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(scoreLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Spin button
        spinButton = new JButton("Крутити колесо!");
        spinButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        spinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        // If the student has an "avtomat" (automatic pass), disable the spin button and notify
        if (discipline.getAvtomat()) {
            JOptionPane.showMessageDialog(this, "Вітаємо! у вас автомат!");
            spinButton.setEnabled(false);
            spinButton.setText("Залік складено (Автомат)"); // Update button text
        }
        // Add action listener to the spin button
        spinButton.addActionListener(e -> {
            spinWheel(); // Trigger the wheel spin
        });
        spinButton.setBackground(SIMS_LIGHT_BLUE); // Button background color
        spinButton.setForeground(SIMS_DARK_TEXT); // Button text color
        spinButton.setFocusPainted(false); // Remove focus border
        spinButton.setBorderPainted(false); // Remove border
        mainPanel.add(spinButton);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        add(mainPanel, BorderLayout.CENTER); // Add the main panel to the dialog's center
    }

    /**
     * Applies custom Sims-like colors and styling to the dialog's components.
     */
    private void applySimsStyle() {
        getContentPane().setBackground(SIMS_LIGHT_PINK); // Set dialog background
        // Iterate through components in the main panel to apply styles
        for (Component comp : ((JPanel) getContentPane().getComponent(0)).getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.setBackground(SIMS_MEDIUM_PINK); // Button background
                button.setForeground(SIMS_DARK_TEXT); // Button text
                button.setFocusPainted(false); // No focus border
                button.setBorder(BorderFactory.createLineBorder(SIMS_LIGHT_BLUE, 2)); // Custom border
            } else if (comp instanceof JLabel) {
                comp.setForeground(SIMS_DARK_TEXT); // Label text color
            }
        }
    }

    /**
     * Updates the UI elements (spin button text, score label, attempt label)
     * based on the student's current score and number of attempts for the discipline.
     * It handles states like "ready to spin," "passed," "attempts exhausted," and "expelled."
     */
    private void updateUIBasedOnAttempts() {
        Integer currentTotalScore = student.getTrimesterScore(discipline.getDisciplineId()); // Current score for discipline
        int actualAttemptsMade = student.getZalikAttempts(discipline.getDisciplineId()); // Attempts made by student

        // Check for expulsion status first
        if (student.isExpelled()) {
            spinButton.setEnabled(false);
            spinButton.setText("Відраховано");
            scoreLabel.setText("Студента відраховано");
            attemptLabel.setText(" ");
            return; // Stop further updates
        }

        // Logic for regular attempts and resits
        if (actualAttemptsMade < MAX_REGULAR_ATTEMPTS ||
                (actualAttemptsMade == MAX_REGULAR_ATTEMPTS && (currentTotalScore == null || currentTotalScore < PASSING_SCORE))) {
            spinButton.setEnabled(true);
            // Display current total score, defaulting to 40 if not set
            scoreLabel.setText("Поточний бал: " + (currentTotalScore != null ? currentTotalScore : "40"));
            if (actualAttemptsMade < MAX_REGULAR_ATTEMPTS) {
                // First or second attempt
                spinButton.setText("Крутити колесо!");
                attemptLabel.setText("Спроба: " + (actualAttemptsMade + 1) + "/" + MAX_REGULAR_ATTEMPTS);
            } else {
                // Third attempt (resit)
                spinButton.setText("Крутити колесо (перездача)!");
                attemptLabel.setText("ПЕРЕЗДАЧА");
            }
        } else if (currentTotalScore != null && currentTotalScore >= PASSING_SCORE) {
            // Student has already passed the zalik
            spinButton.setEnabled(false);
            spinButton.setText("Залік складено");
            scoreLabel.setText("Складено: " + currentTotalScore + " балів");
            attemptLabel.setText("Залік складено успішно!");
            discipline.setCurrentStudentsMark(currentTotalScore); // Update discipline's mark
        } else {
            // All attempts exhausted and score is not sufficient
            spinButton.setEnabled(false);
            spinButton.setText("Спроби вичерпано");
            scoreLabel.setText("Бал недостатній: " + (currentTotalScore != null ? currentTotalScore : "0") + "!");
            attemptLabel.setText("Всі спроби вичерпано!");
            discipline.setCurrentStudentsMark(currentTotalScore); // Update discipline's mark

        }
    }

    /**
     * Initiates the spinning animation of the wheel.
     * Disables the spin button during the animation and calculates the final score.
     * Upon completion, it calls `saveScoreAutomatically()`.
     */
    private void spinWheel() {
        spinButton.setEnabled(false); // Disable button during spin

        Random random = new Random();
        int finalScore = random.nextInt(60) + 1; // Generate random score from 1 to 60
        int rotations = 5 + random.nextInt(5); // Random number of full rotations (5-9)
        double sectorSize = 360.0 / 60; // Angle size of each score sector
        // Calculate the target angle for the arrow to point to the `finalScore`
        double targetAngleInWheelCoordinates = (finalScore - 1) * sectorSize + sectorSize / 2;
        double rotationOffset = (360 - targetAngleInWheelCoordinates); // Offset to align target score with arrow
        double totalTargetRotation = rotations * 360 + rotationOffset; // Total rotation angle for animation

        // Start the wheel spinning animation
        wheelPanel.startSpinning(totalTargetRotation, rotations, resultScore -> {
            currentSpunScore = resultScore; // Store the score returned from the wheel
            scoreLabel.setText("Отримано: " + currentSpunScore + " балів"); // Display the spun score
            saveScoreAutomatically(); // Automatically save the score after spin
        });
    }

    /**
     * Saves the spun score to the student's record for the current discipline.
     * It handles adding the spun score to the existing score (if any),
     * applies penalties for resits (if any), checks for passing, and manages expulsion logic.
     * Displays messages to the user about the score and updates the UI accordingly.
     */
    private void saveScoreAutomatically() {
        if (currentSpunScore != null) {
            Integer existingScore = student.getTrimesterScore(discipline.getDisciplineId()); // Get current score
            int scoreToSave;
            int actualAttemptsMade = student.getZalikAttempts(discipline.getDisciplineId()); // Get actual attempts made

            // Logic for calculating the score to save based on attempts
            if (actualAttemptsMade < MAX_REGULAR_ATTEMPTS) {
                // For first/second attempt, add to existing base score (or 40 if none)
                int currentBaseScore = (existingScore != null) ? existingScore : 40; // Base score for credit
                scoreToSave = currentBaseScore + currentSpunScore;
            } else {
                // For resit (third attempt), score is 40 + spun score.
                // It takes the maximum of the current score or the new calculated resit score.
                scoreToSave = 40 + currentSpunScore;
                if (existingScore != null) {
                    scoreToSave = Math.max(existingScore, scoreToSave);
                }
            }

            // Cap score at 100
            if (scoreToSave > 100) {
                scoreToSave = 100;
            }

            // Update student's trimester score and increment attempts
            student.setTrimesterScore(discipline.getDisciplineId(), scoreToSave);
            student.incrementZalikAttempts(discipline.getDisciplineId());
            discipline.setCurrentStudentsMark(scoreToSave); // Update discipline's mark

            currentAttempt = student.getZalikAttempts(discipline.getDisciplineId()); // Update current attempt count

            // Inform user about the added score
            JOptionPane.showMessageDialog(this,
                    "Додано " + currentSpunScore + " балів. Ваш поточний бал за залік: " + scoreToSave + "!",
                    "Бал збережено",
                    JOptionPane.INFORMATION_MESSAGE);

            // Check if passed after this attempt
            if (scoreToSave >= PASSING_SCORE && currentAttempt <= MAX_TOTAL_ATTEMPTS) {
                JOptionPane.showMessageDialog(this,
                        "Залік успішно складено! Бал: " + scoreToSave + ".",
                        "Успіх!",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Close the dialog if passed
            } else if (currentAttempt < MAX_TOTAL_ATTEMPTS) {
                // If not passed but still has attempts left, reset for next spin
                currentSpunScore = null; // Clear spun score for next attempt
                scoreLabel.setText(" "); // Clear score label
                updateUIBasedOnAttempts(); // Update UI for the next attempt
            } else {
                // All attempts exhausted and not passed - expulsion
                JOptionPane.showMessageDialog(this,
                        "На жаль, Ви не набрали достатньо балів після " + MAX_TOTAL_ATTEMPTS + " спроб. Вас відраховано з університету!",
                        "Відрахування",
                        JOptionPane.ERROR_MESSAGE);
                student.expel(); // Expel the student
                dispose(); // Close the dialog
                System.exit(0); // Terminate application
            }
            // Update parent GUI's progress display
            if (parentGUI != null) {
                parentGUI.updateProgressDisplay();
            }
        }
    }

    /**
     * The `WheelPanel` is an inner JPanel responsible for drawing and animating
     * the spinning wheel for the credit exam. It displays score sectors and an arrow.
     */
    class WheelPanel extends JPanel {
        private double rotationAngle = 0; // Current rotation angle of the wheel
        private Timer timer; // Timer for animation
        private long startTime; // Start time of the spin animation
        private long duration = 3000; // Duration of the spin animation in milliseconds
        private double totalRotation = 0; // Total target rotation angle for the wheel
        private SpinCompletionListener completionListener; // Listener for spin completion

        // Colors for the different sectors of the wheel
        private final Color[] sectorColors = {
                SIMS_LIGHT_BLUE, SIMS_ACCENT_COLOR, SIMS_MEDIUM_PINK, SIMS_LIGHT_PINK
        };

        /**
         * Constructs a new `WheelPanel`.
         */
        public WheelPanel() {
            setBackground(SIMS_LIGHT_PINK); // Set background color
        }

        /**
         * Starts the spinning animation of the wheel.
         *
         * @param finalTargetRotation The final target angle the wheel should reach.
         * @param rotations The number of full rotations the wheel should complete.
         * @param listener The {@link SpinCompletionListener} to be notified when the spin is complete.
         */
        public void startSpinning(double finalTargetRotation, int rotations, SpinCompletionListener listener) {
            this.completionListener = listener;
            this.totalRotation = finalTargetRotation;
            this.rotationAngle = 0; // Reset rotation
            this.startTime = System.currentTimeMillis(); // Record start time

            // Stop any existing timer to prevent multiple spins at once
            if (timer != null && timer.isRunning()) {
                timer.stop();
            }

            // Initialize and start the animation timer
            timer = new Timer(15, new ActionListener() { // Timer fires every 15ms
                @Override
                public void actionPerformed(ActionEvent e) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    if (elapsed < duration) {
                        double progress = (double) elapsed / duration;
                        // Apply ease-out cubic function for a smoother deceleration effect
                        double easedProgress = 1 - Math.pow(1 - progress, 3);
                        rotationAngle = totalRotation * easedProgress; // Calculate current angle
                        repaint(); // Request repaint to update wheel position
                    } else {
                        timer.stop(); // Stop the timer when animation ends
                        rotationAngle = totalRotation; // Ensure final angle is exact
                        repaint();
                        // Calculate final score and notify listener
                        int resultScore = calculateScoreFromResultAngle(rotationAngle);
                        if (completionListener != null) {
                            completionListener.onSpinComplete(resultScore);
                        }
                    }
                }
            });
            timer.start();
        }

        /**
         * Calculates the score based on the final rotation angle of the wheel.
         * The wheel has 60 sectors, each representing a score from 1 to 60.
         *
         * @param angle The final rotation angle of the wheel in degrees.
         * @return The integer score (1-60) corresponding to the final position.
         */
        private int calculateScoreFromResultAngle(double angle) {
            // Normalize angle to be between 0 and 360
            double normalizedAngle = (angle % 360 + 360) % 360;
            // Adjust angle to be relative to the arrow's pointing direction (right side)
            double angleAtArrow = (360 - normalizedAngle) % 360;

            // Calculate score based on sector position
            int score = (int) (angleAtArrow / (360.0 / 60)) + 1;
            // Handle edge cases for scores 61 and 0 (should be 60)
            if (score == 61) score = 60;
            if (score == 0) score = 60;

            return score;
        }

        /**
         * Overrides the `paintComponent` method to draw the spinning wheel,
         * its sectors, score numbers, and the indicator arrow.
         *
         * @param g The {@link Graphics} context used for drawing.
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Call superclass method for default painting
            Graphics2D g2d = (Graphics2D) g.create(); // Create a copy of the Graphics object
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Enable anti-aliasing for smooth edges

            int centerX = getWidth() / 2; // Center X of the panel
            int centerY = getHeight() / 2; // Center Y of the panel
            int radius = Math.min(centerX, centerY) - 20; // Radius of the wheel, with some margin

            // Save the current transform to restore it later
            java.awt.geom.AffineTransform oldTransform = g2d.getTransform();

            // Rotate the entire drawing context around the center of the wheel
            g2d.rotate(Math.toRadians(rotationAngle), centerX, centerY);

            // Draw wheel sectors
            for (int i = 0; i < 60; i++) {
                double startAngle = i * (360.0 / 60); // Start angle for each sector
                g2d.setColor(sectorColors[i % sectorColors.length]); // Assign cyclical colors
                g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, // Fill the arc
                        (int) startAngle, (int) (360.0 / 60));
                g2d.setColor(Color.DARK_GRAY); // Border color for sectors
                g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, // Draw the arc border
                        (int) startAngle, (int) (360.0 / 60));
            }

            // Set font and get font metrics for drawing score numbers
            g2d.setColor(SIMS_DARK_TEXT);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 9));
            FontMetrics fm = g2d.getFontMetrics();

            // Draw score numbers on the wheel
            for (int i = 1; i <= 60; i++) {
                // Calculate angle for the center of each sector
                double angleInRadians = Math.toRadians((i - 1) * (360.0 / 60) + (360.0 / 60) / 2);
                int textRadius = radius - fm.getHeight() / 2 - 5; // Radius for text placement

                // Calculate X, Y coordinates for the text
                int x = (int) (centerX + textRadius * Math.cos(angleInRadians));
                int y = (int) (centerY + textRadius * Math.sin(angleInRadians));

                String text = String.valueOf(i);
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();

                // Rotate the graphics context to align text with the arc
                g2d.rotate(angleInRadians + Math.toRadians(90), x, y);
                g2d.drawString(text, x - textWidth / 2, y + textHeight / 4); // Draw the text
                g2d.rotate(-(angleInRadians + Math.toRadians(90)), x, y); // Rotate back
            }

            g2d.setTransform(oldTransform); // Restore original transform for drawing non-rotating elements

            // Draw the indicator arrow
            g2d.setColor(Color.RED); // Arrow color
            int arrowBaseSize = 9; // Base width of the arrow
            int arrowLength = radius - 136; // Length of the arrow (adjusted to fit)

            // Define points for a triangle representing the arrow
            int[] xPoints = {
                    centerX + radius, // Tip of the arrow
                    centerX + radius,
                    centerX + radius - arrowLength
            };
            int[] yPoints = {
                    centerY - arrowBaseSize / 2, // Top base point
                    centerY + arrowBaseSize / 2, // Bottom base point
                    centerY // Middle point for the tip
            };

            g2d.fillPolygon(xPoints, yPoints, 3); // Fill the arrow
            g2d.drawPolygon(xPoints, yPoints, 3); // Draw arrow border

            // Draw the central circle
            g2d.setColor(SIMS_DARK_TEXT);
            g2d.fillOval(centerX - 15, centerY - 15, 30, 30);
            g2d.setColor(Color.WHITE);
            g2d.drawOval(centerX - 15, centerY - 15, 30, 30);

            g2d.dispose(); // Dispose of the Graphics2D object
        }
    }

    /**
     * A functional interface to be implemented by a listener that wants to be notified
     * when the wheel spinning animation completes and a result score is available.
     */
    @FunctionalInterface
    interface SpinCompletionListener {
        /**
         * Called when the wheel spinning animation has finished.
         *
         * @param resultScore The final score determined by the wheel spin.
         */
        void onSpinComplete(int resultScore);
    }
}