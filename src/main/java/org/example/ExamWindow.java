package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents the main examination window where a student attempts to pass an exam
 * for a specific discipline by spinning a wheel. The outcome of the spin
 * determines a question from a professor, and the answer to that question
 * contributes to the student's final score. The window manages multiple attempts,
 * passing scores, and student expulsion logic.
 */
public class ExamWindow extends JDialog {
    private Discipline discipline;
    private Student student;
    private int currentAttempt;
    private JLabel scoreLabel;
    private JButton spinButton;
    private WheelPanel wheelPanel;
    private Integer currentSpunScore = null;
    private JLabel attemptLabel;

    private static final int MAX_REGULAR_ATTEMPTS = 2;
    private static final int MAX_TOTAL_ATTEMPTS = 3;
    private static final int PASSING_SCORE = 60;

    // Custom colors inspired by The Sims aesthetic
    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_ACCENT_COLOR = new Color(255, 179, 186);

    private StudyProgressGUI parentGUI;

    /**
     * Inner class to store a professor's question, along with correct and partially correct answers.
     */
    private static class ProfessorQuestion {
        String question;
        List<String> correctAnswers; // List of possible correct answers
        List<String> partialAnswers; // List of possible partially correct answers

        /**
         * Constructs a {@code ProfessorQuestion}.
         *
         * @param question The question text.
         * @param correctAnswers A list of strings considered correct answers.
         * @param partialAnswers A list of strings considered partially correct answers.
         */
        public ProfessorQuestion(String question, List<String> correctAnswers, List<String> partialAnswers) {
            this.question = question;
            this.correctAnswers = correctAnswers;
            this.partialAnswers = partialAnswers;
        }
    }

    /**
     * A static map holding hardcoded professors and their associated questions and answers.
     * This map is initialized once when the class is loaded.
     */
    private static final Map<String, ProfessorQuestion> professorsAndQuestions = new LinkedHashMap<>();
    static {
        // Example: "What is the maximum speed of light if moving perpendicular to it?"
        // Correct: "the same", "unchanged"
        // Partial: "probably the same"
        professorsAndQuestions.put("Проф. Довгань", new ProfessorQuestion(
                "Яка максимальна швидкість світла, якщо рухатися перпендикулярно до нього?",
                List.of("така ж", "незмінна", "швидкість світла", "с"),
                List.of("мабуть така ж", "не міняється", "як завжди")
        ));
        professorsAndQuestions.put("Доц. Коваль", new ProfessorQuestion(
                "Чи може програма без багів написати програму без багів?",
                List.of("ні", "ні, не може", "немає"),
                List.of("навряд чи", "складно", "теоретично ні")
        ));
        professorsAndQuestions.put("Викл. Мельник", new ProfessorQuestion(
                "Якщо викладач спізнюється, це затримка чи просто \"альтернативний\" початок пари?",
                List.of("альтернативний початок", "це альтернативний початок", "інший початок"),
                List.of("затримка", "спізнення", "альтернативний")
        ));
        professorsAndQuestions.put("Проф. Захаров", new ProfessorQuestion(
                "Яким буде результат ділення на нуль, якщо його виконати в реальному житті?",
                List.of("помилка", "нескінченність", "error", "виняток"),
                List.of("проблема", "хаос", "невідомість")
        ));
        professorsAndQuestions.put("Доц. Петренко", new ProfessorQuestion(
                "Як перетворити каву на код, і чи є зворотний алгоритм?",
                List.of("випити каву", "написати код", "програмування", "надихнутися кавою", "зворотного алгоритму немає"),
                List.of("просто пити каву", "написати код з кавою", "немає")
        ));
        professorsAndQuestions.put("Викл. Сидоренко", new ProfessorQuestion(
                "Чи вважається сон під час лекції формою активного слухання?",
                List.of("ні", "звичайно ні", "не вважається"),
                List.of("можливо, якщо це сон про лекцію", "інколи", "частково")
        ));
        professorsAndQuestions.put("Проф. Іваненко", new ProfessorQuestion(
                "Скільки програмістів потрібно, щоб вкрутити лампочку, якщо вона не входить в ТЗ?",
                List.of("жодного", "0", "не входить в тз", "залежить від тз"),
                List.of("один, якщо напише скрипт", "багато, щоб обговорити")
        ));
        professorsAndQuestions.put("Доц. Бондар", new ProfessorQuestion(
                "Якщо студент не здав ДЗ, чи можна вважати, що він оптимізував час?",
                List.of("ні", "звичайно ні", "не оптимізував"),
                List.of("це залежить", "можливо, для інших справ", "не завжди")
        ));
        professorsAndQuestions.put("Викл. Шевченко", new ProfessorQuestion(
                "Який зв'язок між дедлайном і швидкістю вивчення матеріалу?",
                List.of("прямий", "дуже сильний", "дедлайн стимулює", "чим ближче дедлайн, тим швидше"),
                List.of("є зв'язок", "залежить", "дедлайн прискорює")
        ));
        professorsAndQuestions.put("Проф. Мороз", new ProfessorQuestion(
                "Чи можна довести теорему Піфагора за допомогою гугл-перекладача?",
                List.of("ні", "не можна", "це неможливо"),
                List.of("навряд чи", "тільки якщо дуже постаратись", "теоретично ні")
        ));

        // Add more default questions to fill up to 60 professor questions
        String[] defaultQuestions = {
                "Що робити, якщо курс лекцій виявився нескінченним циклом?",
                "Яка оптимальна кількість бутербродів для кодування?",
                "Чи можна вважати, що ваш мозок - це комп'ютер, якщо ви вчитесь на програміста?",
                "Чи можна написати штучний інтелект, який буде писати заліковки?",
                "Як ефективно використовувати час, коли до сесії залишилося 5 хвилин?",
                "Чи може студент отримати відрахування, якщо він занадто геніальний?",
                "Яке рішення буде оптимальним: здати все вчасно чи здати ідеально, але пізно?",
                "Якщо комп'ютер завис, чи можна вважати, що він медитує?"
        };
        for (int i = 0; professorsAndQuestions.size() < 60; i++) {
            String profName = "Викл. Ім'я " + (professorsAndQuestions.size() + 1);
            String q = defaultQuestions[i % defaultQuestions.length];
            professorsAndQuestions.put(profName, new ProfessorQuestion(
                    q, List.of("правильна відповідь " + i), List.of("частково правильна " + i)
            ));
        }
    }
    private ImageIcon[] examImages;
    private JLabel imageLabel;

    private final List<String> professorNames = new ArrayList<>(professorsAndQuestions.keySet());

    /**
     * Returns a random image from the preloaded exam images.
     *
     * @return A random {@link ImageIcon}.
     */
    private ImageIcon getRandomImage() {
        int index = (int) (Math.random() * examImages.length);
        return examImages[index];
    }

    /**
     * Constructs an {@code ExamWindow}.
     *
     * @param owner The parent {@link Frame} of this dialog, typically a {@link StudyProgressGUI}.
     * @param discipline The {@link Discipline} for which the exam is being taken.
     * @param student The {@link Student} taking the exam.
     */
    public ExamWindow(Frame owner, Discipline discipline, Student student) {
        super(owner, "Екзамен", true); // Call to JDialog constructor with modal property
        if (owner instanceof StudyProgressGUI) {
            this.parentGUI = (StudyProgressGUI) owner;
        } else {
            System.err.println("Помилка: CreditWindow повинен бути викликаний з StudyProgressGUI.");
        }

        this.discipline = discipline;
        this.student = student;
        // currentAttempt is initialized based on student's existing attempts in updateUIBasedOnAttempts()

        setSize(800, 600); // Sets the size of the dialog
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Disposes the dialog on close
        setResizable(false); // Prevents resizing the dialog
        setLocationRelativeTo(owner); // Centers the dialog relative to its owner

        initComponents(); // Initializes UI components
        applySimsStyle(); // Applies custom Sims-like styling
        updateUIBasedOnAttempts(); // Updates the UI state based on student's current attempts
    }

    /**
     * Initializes and arranges the UI components of the exam window.
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10)); // Main layout for the dialog
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Vertical box layout for main content
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding

        // Load exam images
        examImages = new ImageIcon[] {
                new ImageIcon(getClass().getResource("/exam/img_1.png").getFile()),
                new ImageIcon(getClass().getResource("/exam/img_3.png").getFile()),
                new ImageIcon(getClass().getResource("/exam/img_4.png").getFile()),
                new ImageIcon(getClass().getResource("/exam/img_5.png").getFile()),
        };

        // Image label
        imageLabel = new JLabel();
        ImageIcon randomImage = getRandomImage(); // Get a random image
        imageLabel.setIcon(randomImage);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(200, 200)); // Set preferred size for the image
        add(imageLabel, BorderLayout.WEST); // Add image to the west side

        // Title label for the discipline
        JLabel titleLabel = new JLabel("Отримання балів за екзамен з дисципліни «" + discipline.getName() + "»");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Spacer

        // Attempt label
        attemptLabel = new JLabel();
        attemptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        attemptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(attemptLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer

        // Wheel panel for spinning mechanism
        wheelPanel = new WheelPanel();
        wheelPanel.setPreferredSize(new Dimension(350, 350));
        wheelPanel.setMaximumSize(new Dimension(350, 350));
        wheelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(wheelPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Spacer

        // Score display label
        scoreLabel = new JLabel(" ");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        scoreLabel.setForeground(SIMS_DARK_TEXT);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(scoreLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Spacer

        // Spin button
        spinButton = new JButton("Крутити колесо!");
        spinButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        spinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        spinButton.addActionListener(e -> spinWheel()); // Attach action listener
        spinButton.setBackground(SIMS_LIGHT_BLUE); // Custom background color
        spinButton.setForeground(SIMS_DARK_TEXT); // Custom text color
        spinButton.setFocusPainted(false); // No focus border
        spinButton.setBorderPainted(false); // No border
        mainPanel.add(spinButton);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
        add(mainPanel, BorderLayout.CENTER); // Add the main panel to the center
    }

    /**
     * Applies a custom "Sims-like" visual style to the dialog and its components.
     * This includes setting background colors and styling buttons and labels.
     */
    private void applySimsStyle() {
        getContentPane().setBackground(SIMS_LIGHT_PINK); // Set background color for the content pane

        // Iterate through components to apply styling
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JPanel panel) {
                for (Component inner : panel.getComponents()) {
                    styleComponent(inner);
                }
            } else {
                styleComponent(comp);
            }
        }
    }

    /**
     * Applies styling to individual Swing components based on their type.
     *
     * @param comp The {@link Component} to be styled.
     */
    private void styleComponent(Component comp) {
        if (comp instanceof JButton button) {
            button.setBackground(SIMS_MEDIUM_PINK); // Button background color
            button.setForeground(SIMS_DARK_TEXT); // Button text color
            button.setFocusPainted(false); // No focus border
            button.setBorder(BorderFactory.createLineBorder(SIMS_LIGHT_BLUE, 2)); // Custom border
        } else if (comp instanceof JLabel) {
            comp.setForeground(SIMS_DARK_TEXT); // Label text color
        }
    }

    /**
     * Updates the UI elements (spin button, score label, attempt label)
     * based on the student's current exam attempts and score for the discipline.
     * It handles regular attempts, re-takes, passing status, and expulsion.
     */
    private void updateUIBasedOnAttempts() {
        Integer currentTotalScore = student.getTrimesterScore(discipline.getDisciplineId());
        int actualAttemptsMade = student.getZalikAttempts(discipline.getDisciplineId());

        if (student.isExpelled()) {
            spinButton.setEnabled(false);
            spinButton.setText("Відраховано");
            scoreLabel.setText("Студента відраховано");
            attemptLabel.setText(" ");
            return;
        }

        // Logic for regular attempts or first re-take
        if (actualAttemptsMade < MAX_REGULAR_ATTEMPTS ||
                (actualAttemptsMade == MAX_REGULAR_ATTEMPTS && (currentTotalScore == null || currentTotalScore < PASSING_SCORE))) {
            spinButton.setEnabled(true);
            scoreLabel.setText("Поточний бал: " + (currentTotalScore != null ? currentTotalScore : "40")); // Default base score is 40
            if (actualAttemptsMade < MAX_REGULAR_ATTEMPTS) {
                spinButton.setText("Крутити колесо!");
                attemptLabel.setText("Спроба: " + (actualAttemptsMade + 1) + "/" + MAX_REGULAR_ATTEMPTS);
            } else {
                spinButton.setText("Крутити колесо (перездача)!"); // Special text for re-take
                attemptLabel.setText("ПЕРЕЗДАЧА");
            }
        }
        // Logic if exam is already passed
        else if (currentTotalScore != null && currentTotalScore >= PASSING_SCORE) {
            spinButton.setEnabled(false);
            spinButton.setText("Екзамен складено");
            scoreLabel.setText("Складено: " + currentTotalScore + " балів");
            attemptLabel.setText("Екзамен складено успішно!");
        }
        // Logic if all attempts are exhausted and score is not passing
        else {
            spinButton.setEnabled(false);
            spinButton.setText("Спроби вичерпано");
            scoreLabel.setText("Бал недостатній: " + (currentTotalScore != null ? currentTotalScore : "0") + "!");
            attemptLabel.setText("Всі спроби вичерпано!");
        }
    }

    /**
     * Initiates the wheel spinning animation and subsequently handles the outcome.
     * After the spin, it presents a question from a randomly selected professor,
     * evaluates the user's answer, assigns points, and then automatically saves the score.
     */
    private void spinWheel() {
        spinButton.setEnabled(false); // Disable button during spin
        MusicPlayer.getInstance().playSpin(); // Play spin sound

        Random random = new Random();
        int finalScoreFromWheel = random.nextInt(60) + 1; // Score from 1 to 60, determines professor/segment
        int rotations = 5 + random.nextInt(5); // Number of full rotations for animation
        double sectorSize = 360.0 / 60; // Degrees per segment on the wheel
        double targetAngleInWheelCoordinates = (finalScoreFromWheel - 1) * sectorSize + sectorSize / 2; // Angle to land on
        double rotationOffset = (360 - targetAngleInWheelCoordinates); // Offset to align arrow
        double totalTargetRotation = rotations * 360 + rotationOffset; // Total rotation for animation

        wheelPanel.startSpinning(totalTargetRotation, rotations, resultScore -> {
            // Determine the professor based on the wheel's landing spot
            int professorIndex = (resultScore - 1) % professorNames.size();
            String landedProfessorName = professorNames.get(professorIndex);
            ProfessorQuestion pq = professorsAndQuestions.get(landedProfessorName);

            String question = (pq != null) ? pq.question : "Цікаве питання від викладача!"; // Get question or default

            // Prompt user for answer
            String userAnswer = JOptionPane.showInputDialog(this,
                    "Питання від \"" + landedProfessorName + "\":\n\n" + question + "\n\nВаша відповідь:",
                    "Питання від викладача",
                    JOptionPane.QUESTION_MESSAGE);

            int pointsFromAnswer = 0;
            if (userAnswer != null) {
                userAnswer = userAnswer.toLowerCase().trim(); // Normalize user input
                if (pq != null) {
                    String finalUserAnswer = userAnswer;
                    // Check if answer is fully correct
                    if (pq.correctAnswers.stream().anyMatch(ans -> finalUserAnswer.contains(ans.toLowerCase()))) {
                        pointsFromAnswer = 40;
                        JOptionPane.showMessageDialog(this, "Ваша відповідь правильна! Отримано +40 балів.", "Відповідь вірна", JOptionPane.INFORMATION_MESSAGE);
                    }
                    // Check if answer is partially correct
                    else if (pq.partialAnswers.stream().anyMatch(ans -> finalUserAnswer.contains(ans.toLowerCase()))) {
                        pointsFromAnswer = 20;
                        JOptionPane.showMessageDialog(this, "Ваша відповідь частково правильна! Отримано +20 балів.", "Відповідь часткова", JOptionPane.INFORMATION_MESSAGE);
                    }
                    // Answer is incorrect
                    else {
                        pointsFromAnswer = 5;
                        JOptionPane.showMessageDialog(this, "Ваша відповідь неправильна. Отримано +5 балів за присутність.", "Відповідь невірна", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // Fallback if question not found (shouldn't happen with current logic)
                    pointsFromAnswer = 5;
                    JOptionPane.showMessageDialog(this, "Не вдалося знайти питання. Отримано +5 балів.", "Помилка питання", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // If user cancels or provides no input
                pointsFromAnswer = 5;
                JOptionPane.showMessageDialog(this, "Ви не відповіли. Отримано +5 балів за присутність.", "Без відповіді", JOptionPane.WARNING_MESSAGE);
            }

            currentSpunScore = pointsFromAnswer; // Store the points obtained from the answer
            scoreLabel.setText("Отримано: " + currentSpunScore + " балів");
            saveScoreAutomatically(); // Save the score and update state
        });
    }

    /**
     * Saves the score obtained from the current spin and updates the student's total score
     * for the discipline. It handles the logic for regular attempts and re-takes,
     * ensuring the score does not exceed 100. It also checks for passing criteria
     * and handles student expulsion if all attempts are exhausted without passing.
     */
    private void saveScoreAutomatically() {
        if (currentSpunScore != null) {
            Integer existingScore = student.getTrimesterScore(discipline.getDisciplineId());
            int scoreToSave;
            int actualAttemptsMade = student.getZalikAttempts(discipline.getDisciplineId());

            if (actualAttemptsMade < MAX_REGULAR_ATTEMPTS) {
                // For first and second attempts, add to existing base score (which is 40 if no previous score)
                int currentBaseScore = (existingScore != null) ? existingScore : 40;
                scoreToSave = currentBaseScore + currentSpunScore;
            } else {
                // For the third attempt (re-take), base score is 40, and take the max if previous score was better
                scoreToSave = 40 + currentSpunScore;
                if (existingScore != null) {
                    scoreToSave = Math.max(existingScore, scoreToSave);
                }
            }

            // Cap the score at 100
            if (scoreToSave > 100) {
                scoreToSave = 100;
            }

            student.setTrimesterScore(discipline.getDisciplineId(), scoreToSave); // Update student's score
            student.incrementZalikAttempts(discipline.getDisciplineId()); // Increment attempt count

            currentAttempt = student.getZalikAttempts(discipline.getDisciplineId()); // Update internal attempt counter

            JOptionPane.showMessageDialog(this,
                    "Додано " + currentSpunScore + " балів. Ваш поточний бал за екзамен: " + scoreToSave + "!",
                    "Бал збережено",
                    JOptionPane.INFORMATION_MESSAGE);

            // Check passing condition
            if (scoreToSave >= PASSING_SCORE) {
                JOptionPane.showMessageDialog(this,
                        "Екзамен успішно складено! Бал: " + scoreToSave + ".",
                        "Успіх!",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Close the exam window if passed
            }
            // If not passed and more attempts available
            else if (currentAttempt < MAX_TOTAL_ATTEMPTS) {
                currentSpunScore = null; // Reset spun score for next attempt
                scoreLabel.setText(" "); // Clear score display
                updateUIBasedOnAttempts(); // Update UI for next attempt
            }
            // If not passed and all attempts exhausted
            else {
                JOptionPane.showMessageDialog(this,
                        "На жаль, Ви не набрали достатньо балів після " + MAX_TOTAL_ATTEMPTS + " спроб. Вас відраховано з університету!",
                        "Невдача",
                        JOptionPane.ERROR_MESSAGE);
                student.expel(); // Expel the student
                dispose(); // Close the exam window
            }
            if (parentGUI != null) {
                parentGUI.updateProgressDisplay(); // Update the main GUI's display
            }
        }
    }

    /**
     * A custom JPanel that draws and animates a spinning wheel for the exam score.
     * It displays sectors with colors and potentially professor names (or numbers).
     */
    class WheelPanel extends JPanel {
        private double rotationAngle = 0;
        private Timer timer;
        private long startTime;
        private long duration = 3000; // Spin duration in milliseconds
        private double totalRotation = 0; // Total degrees to rotate
        private SpinCompletionListener completionListener;

        // Colors for the wheel sectors
        private final Color[] sectorColors = {
                SIMS_LIGHT_BLUE, SIMS_ACCENT_COLOR, SIMS_MEDIUM_PINK, SIMS_LIGHT_PINK
        };

        /**
         * Constructs a {@code WheelPanel}.
         */
        public WheelPanel() {
            setBackground(SIMS_LIGHT_PINK); // Set background color
        }

        /**
         * Starts the spinning animation of the wheel.
         *
         * @param finalTargetRotation The total rotation angle in degrees for the wheel to land on.
         * @param rotations The number of full 360-degree rotations the wheel should make. (Used in old logic, not directly in eased progress)
         * @param listener The {@link SpinCompletionListener} to be called when the spin animation completes.
         */
        public void startSpinning(double finalTargetRotation, int rotations, SpinCompletionListener listener) {
            this.completionListener = listener;
            this.totalRotation = finalTargetRotation;
            this.rotationAngle = 0; // Reset rotation
            this.startTime = System.currentTimeMillis(); // Record start time

            if (timer != null && timer.isRunning()) {
                timer.stop(); // Stop any existing timer
            }

            // Timer for animation
            timer = new Timer(15, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    if (elapsed < duration) {
                        double progress = (double) elapsed / duration;
                        // Easing function for smoother animation (starts fast, slows down)
                        double easedProgress = 1 - Math.pow(1 - progress, 3);
                        rotationAngle = totalRotation * easedProgress;
                        repaint(); // Redraw the wheel
                    } else {
                        timer.stop(); // Stop timer when animation is done
                        rotationAngle = totalRotation; // Ensure final position is exact
                        repaint();
                        int resultScore = calculateScoreFromResultAngle(rotationAngle); // Calculate score based on final angle
                        if (completionListener != null) {
                            completionListener.onSpinComplete(resultScore); // Notify listener
                        }
                    }
                }
            });
            timer.start(); // Start the timer
        }

        /**
         * Calculates the score based on the final rotation angle of the wheel.
         * This determines which segment the arrow points to.
         *
         * @param angle The final rotation angle in degrees.
         * @return The calculated score (1-60) from the wheel.
         */
        private int calculateScoreFromResultAngle(double angle) {
            // Normalize angle to be within 0-360 degrees
            double normalizedAngle = (angle % 360 + 360) % 360;
            // Adjust angle to be relative to the arrow's position (arrow points right, 0 degrees)
            double angleAtArrow = (360 - normalizedAngle) % 360;

            // Calculate score based on sector size
            int score = (int) (angleAtArrow / (360.0 / 60)) + 1;
            // Handle edge cases for 60/0 boundary
            if (score == 61) score = 60;
            if (score == 0) score = 60;
            return score;
        }

        /**
         * Overrides the {@code paintComponent} method to draw the spinning wheel.
         * It draws 60 segments, each with a color, and attempts to draw numbers or professor names.
         * It also draws the central pivot and the arrow indicator.
         *
         * @param g The {@code Graphics} context used for drawing.
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create(); // Create a copy of the Graphics object
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Enable anti-aliasing

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = Math.min(centerX, centerY) - 20; // Calculate wheel radius

            java.awt.geom.AffineTransform oldTransform = g2d.getTransform(); // Save current transform

            g2d.rotate(Math.toRadians(rotationAngle), centerX, centerY); // Rotate the entire wheel

            // Draw sectors
            for (int i = 0; i < 60; i++) {
                double startAngle = i * (360.0 / 60);
                g2d.setColor(sectorColors[i % sectorColors.length]); // Assign color from palette
                g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2,
                        (int) startAngle, (int) (360.0 / 60)); // Fill arc
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2,
                        (int) startAngle, (int) (360.0 / 60)); // Draw arc border
            }

            g2d.setColor(SIMS_DARK_TEXT);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 9)); // Smaller font for text
            FontMetrics fm = g2d.getFontMetrics();

            // Draw professor names (or numbers for demonstration)
            for (int i = 0; i < 60; i++) {
                double angleInRadians = Math.toRadians(i * (360.0 / 60) + (360.0 / 60) / 2); // Center of the segment
                int textRadius = radius - fm.getHeight() / 2 - 5; // Position for text

                int x = (int) (centerX + textRadius * Math.cos(angleInRadians));
                int y = (int) (centerY + textRadius * Math.sin(angleInRadians));

                // Currently displays numbers for demonstration. Can be professor names if desired.
                String text = " " + i; // Example: display segment number

                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();

                // Rotate text to align with segment
                g2d.rotate(angleInRadians + Math.toRadians(90), x, y);
                g2d.drawString(text, x - textWidth / 2, y + textHeight / 4);
                g2d.rotate(-(angleInRadians + Math.toRadians(90)), x, y);
            }

            g2d.setTransform(oldTransform); // Restore original transform (for arrow)

            // Draw the arrow indicator
            g2d.setColor(Color.RED);
            int arrowBaseSize = 9;
            int arrowLength = radius - 136; // Length of the arrow from center

            int[] xPoints = {
                    centerX + radius, // Rightmost point of the arrow
                    centerX + radius,
                    centerX + radius - arrowLength // Tip of the arrow
            };
            int[] yPoints = {
                    centerY - arrowBaseSize / 2, // Top base point
                    centerY + arrowBaseSize / 2, // Bottom base point
                    centerY // Middle point (tip)
            };

            g2d.fillPolygon(xPoints, yPoints, 3); // Fill arrow
            g2d.drawPolygon(xPoints, yPoints, 3); // Draw arrow border

            // Draw central pivot
            g2d.setColor(SIMS_DARK_TEXT);
            g2d.fillOval(centerX - 15, centerY - 15, 30, 30);
            g2d.setColor(Color.WHITE);
            g2d.drawOval(centerX - 15, centerY - 15, 30, 30);

            g2d.dispose(); // Release Graphics resources
        }
    }

    /**
     * A functional interface for a callback that is invoked when the wheel spinning animation completes.
     */
    @FunctionalInterface
    interface SpinCompletionListener {
        /**
         * Called when the wheel spin animation has finished.
         *
         * @param resultScore The score (segment number) where the wheel landed.
         */
        void onSpinComplete(int resultScore);
    }
}