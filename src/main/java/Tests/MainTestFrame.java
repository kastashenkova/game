package Tests;

import Tests.Question;
import gui.PauseAction;
import org.example.Discipline;
import org.example.Hero;
import org.example.MusicPlayer;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The MainTestFrame class represents the graphical user interface for taking a test
 * in the Sims NaUKMA game. It displays questions, allows users to select answers,
 * navigates through questions, and shows test results.
 */
public class MainTestFrame extends JFrame {

    private Question[] questions;
    private int currentQuestionIndex = 0;
    private int[] userAnswers;

    private JLabel questionLabel;
    private JRadioButton[] optionButtons;
    private ButtonGroup optionButtonGroup;
    private JButton nextButton;
    private JButton finishButton;
    private JPanel questionPanel;
    private JPanel optionsPanel;
    private JPanel navigationPanel;
    private JPanel mainPanel;
    private JPanel resultPanel;


    private Discipline discipline;

    private int score;

    private Runnable onFinishCallback;

    private TestManager testManager;
    Hero hero;

    // Define custom colors for the UI, inspired by Sims
    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_TURQUOISE = new Color(64, 224, 208);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);
    private final Color SIMS_BUTTON_HOVER = new Color(255, 240, 245);
    private static final Color SIMS_GREEN_CORRECT = new Color(144, 238, 144);
    private static final Color SIMS_RED_INCORRECT = new Color(255, 99, 71);


    /**
     * Constructs a new MainTestFrame.
     * Initializes the test environment, sets up the UI, and prepares questions for the specified discipline.
     *
     * @param hero The {@link Hero} object representing the player.
     * @param discipline The {@link Discipline} for which the test is being taken.
     * @param onFinishCallback A {@link Runnable} to be executed when the test is finished.
     */
    public MainTestFrame(Hero hero, Discipline discipline, Runnable onFinishCallback) {
        // Start playing test background music
        MusicPlayer.getInstance().setMusicEnabled(true);
        MusicPlayer.getInstance().playMusic("/assets/Sounds/testBack.wav");

        this.hero = hero;
        this.testManager = new TestManager(hero); // Initialize TestManager for question generation
        setTitle(discipline.getName()); // Set frame title to the discipline name
        setSize(1200, 800);

        // Create and configure a pause button
        PauseAction pauseAction = new PauseAction(""); // Pass empty string as it is not used in action
        JButton pauseButton = new JButton(pauseAction);
        // Load and scale pause button icon
        ImageIcon iconBtn = new ImageIcon(getClass().getResource("/button/pause.png"));
        Image scaledImage = iconBtn.getImage().getScaledInstance(140, 30, Image.SCALE_SMOOTH);
        iconBtn = new ImageIcon(scaledImage);
        pauseButton.setIcon(iconBtn);
        // Make the button transparent and borderless
        pauseButton.setContentAreaFilled(false);
        pauseButton.setBorderPainted(false);
        pauseButton.setFocusPainted(false);
        pauseButton.setOpaque(false);

        // Create a top bar panel for the pause button
        JPanel topBar = new JPanel();
        topBar.setOpaque(false); // Make it transparent
        topBar.add(pauseButton);
        add(topBar, BorderLayout.NORTH); // Add to the top of the frame

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Default close operation
        setLocationRelativeTo(null); // Center the frame on the screen

        this.onFinishCallback = onFinishCallback; // Store the callback for when the test finishes

        // Generate questions for the discipline
        testManager.generateQuestions(discipline);
        questions = discipline.questions;

        // Set custom text for JOptionPane buttons in Ukrainian
        UIManager.put("OptionPane.okButtonText", "ОК");
        UIManager.put("OptionPane.cancelButtonText", "Скасувати");
        UIManager.put("OptionPane.yesButtonText", "Так");
        UIManager.put("OptionPane.noButtonText", "Ні");

        // Set global fonts for various Swing components
        UIManager.put("OptionPane.messageFont", new Font("Arial", Font.PLAIN, 16));
        UIManager.put("OptionPane.buttonFont", new Font("Arial", Font.BOLD, 14));
        UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 16));
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 18));
        UIManager.put("TitledBorder.font", new Font("Arial", Font.BOLD, 18));


        initializeQuestions(questions); // Re-initialize questions array (redundant if already set above)
        userAnswers = new int[questions.length];
        // Initialize user answers to -1 (indicating no answer selected)
        for (int i = 0; i < userAnswers.length; i++) {
            userAnswers[i] = -1;
        }

        createUI(); // Build the main UI components
        showInstructionsDialog(); // Display test instructions
        showStartAnimation(); // Show a brief start animation
        displayQuestion(); // Display the first question
    }

    /**
     * Shows a brief animated dialog with "Старт!", "Увага!", "Руш!" messages.
     * This serves as a visual cue before the test begins.
     */
    private void showStartAnimation() {
        JDialog animationDialog = new JDialog(this, true); // Modal dialog
        animationDialog.setUndecorated(true); // No title bar or borders

        // Define Sims-like colors for the animation
        Color simsPink = new Color(255, 168, 205, 255);
        Color simsAccent1 = new Color(66, 244, 180);
        Color simsAccent2 = new Color(34, 149, 107);
        Color simsAccent3 = new Color(66, 244, 191);

        animationDialog.getContentPane().setBackground(simsPink); // Set background color of the dialog

        JLabel animationLabel = new JLabel("", SwingConstants.CENTER);
        animationLabel.setFont(new Font("Segoe UI", Font.BOLD, 80)); // Large bold font
        animationLabel.setForeground(simsAccent1); // Initial text color

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false); // Make content panel transparent
        contentPanel.add(animationLabel); // Add the label to the content panel

        animationDialog.setContentPane(contentPanel);
        animationDialog.setSize(400, 200); // Set dialog size
        animationDialog.setLocationRelativeTo(this); // Center relative to the parent frame

        final String[] phases = {"Старт!", "Увага!", "Руш!"}; // Animation phases
        final Color[] textColors = {simsAccent1, simsAccent2, simsAccent3}; // Colors for each phase
        final int[] currentPhase = {0}; // Current phase index

        // Timer for animating the phases
        Timer timer = new Timer(150, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentPhase[0] < phases.length) {
                    animationLabel.setText(phases[currentPhase[0]]); // Update text
                    animationLabel.setForeground(textColors[currentPhase[0]]); // Update color
                    currentPhase[0]++;
                } else {
                    ((Timer) e.getSource()).stop(); // Stop the timer after all phases
                    animationDialog.dispose(); // Close the dialog
                }
            }
        });
        timer.setInitialDelay(50); // Short initial delay
        timer.start(); // Start the timer

        animationDialog.setVisible(true); // Make the dialog visible
    }

    /**
     * Initializes the internal questions array. This method seems redundant as
     * `questions` is already set in the constructor.
     *
     * @param questions An array of {@link Question} objects for the test.
     */
    private void initializeQuestions(Question[] questions) {
        this.questions = questions;
    }

    /**
     * Creates and arranges all the Swing UI components for the test frame,
     * including question display, answer options, and navigation buttons.
     */
    private void createUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(20, 20)); // Main panel layout with gaps
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30)); // Padding around the main content
        mainPanel.setBackground(SIMS_MEDIUM_PINK); // Background color for the main panel

        // Question Panel setup
        questionPanel = new JPanel();
        questionPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center align content
        questionPanel.setBackground(SIMS_TURQUOISE); // Background color
        questionPanel.setBorder(BorderFactory.createLineBorder(SIMS_LIGHT_BLUE, 5)); // Border with custom color
        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 20)); // Custom font for question text
        questionLabel.setForeground(SIMS_DARK_TEXT); // Text color
        questionPanel.add(questionLabel); // Add label to question panel
        mainPanel.add(questionPanel, BorderLayout.NORTH); // Add question panel to the top of main panel

        // Options Panel setup
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridBagLayout()); // GridBagLayout for flexible centering
        optionsPanel.setBackground(SIMS_MEDIUM_PINK); // Background color

        JPanel innerOptionsPanel = new JPanel();
        innerOptionsPanel.setLayout(new BoxLayout(innerOptionsPanel, BoxLayout.Y_AXIS)); // Vertical layout for radio buttons
        innerOptionsPanel.setBackground(SIMS_LIGHT_PINK); // Background color
        innerOptionsPanel.setBorder(new EmptyBorder(20, 30, 20, 30)); // Padding

        optionButtons = new JRadioButton[4]; // Array to hold radio buttons for options
        optionButtonGroup = new ButtonGroup(); // Group to ensure only one radio button can be selected
        String[] optionLetters = {"А", "Б", "В", "Г"}; // Letters for options

        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font("Arial", Font.PLAIN, 20)); // Font for options
            optionButtons[i].setBackground(SIMS_LIGHT_PINK); // Background color
            optionButtons[i].setForeground(SIMS_DARK_TEXT); // Text color
            optionButtons[i].setFocusPainted(false); // No focus border
            optionButtons[i].setHorizontalAlignment(SwingConstants.LEFT); // Left align text
            optionButtons[i].setAlignmentX(Component.LEFT_ALIGNMENT); // Align components to the left in BoxLayout

            optionButtonGroup.add(optionButtons[i]); // Add to button group
            innerOptionsPanel.add(optionButtons[i]); // Add to inner options panel
            innerOptionsPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Add vertical spacing
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Expand horizontally
        gbc.weighty = 1.0; // Expand vertically
        gbc.anchor = GridBagConstraints.CENTER; // Center the inner panel
        optionsPanel.add(innerOptionsPanel, gbc); // Add inner options panel to options panel

        mainPanel.add(optionsPanel, BorderLayout.CENTER); // Add options panel to the center of main panel

        // Navigation Panel setup
        navigationPanel = new JPanel();
        navigationPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 15)); // FlowLayout for buttons with gaps
        navigationPanel.setBackground(SIMS_TURQUOISE); // Background color
        navigationPanel.setBorder(BorderFactory.createLineBorder(SIMS_LIGHT_BLUE, 5)); // Border

        nextButton = createSimsButton("Наступна"); // Create custom styled "Next" button
        finishButton = createSimsButton("Завершити тест"); // Create custom styled "Finish" button

        navigationPanel.add(nextButton); // Add buttons to navigation panel
        navigationPanel.add(finishButton);
        mainPanel.add(navigationPanel, BorderLayout.SOUTH); // Add navigation panel to the bottom of main panel

        add(mainPanel); // Add the main panel to the frame

        // Action Listeners for navigation buttons
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentAnswer(); // Save the user's answer for the current question
                if (currentQuestionIndex < questions.length - 1) {
                    currentQuestionIndex++; // Move to the next question
                    displayQuestion(); // Display the new question
                } else {
                    finishTest(); // If no more questions, finish the test
                }
            }
        });

        finishButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentAnswer(); // Save the current answer
                finishTest(); // Immediately finish the test
            }
        });

        // Window listener to handle closing the frame (asks for confirmation)
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent direct closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                int confirm = JOptionPane.showConfirmDialog(
                        MainTestFrame.this,
                        "Ви дійсно хочете вийти з тесту?", // Confirmation message
                        "Підтвердження виходу", // Dialog title
                        JOptionPane.YES_NO_OPTION, // Yes/No options
                        JOptionPane.QUESTION_MESSAGE // Icon type
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0); // Exit the application if confirmed
                }
            }
        });
    }

    /**
     * Helper method to create a JButton with Sims-style aesthetics.
     * Includes custom font, colors, and hover effects.
     *
     * @param text The text to display on the button.
     * @return A styled {@link JButton} object.
     */
    private JButton createSimsButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16)); // Custom font
        button.setBackground(SIMS_LIGHT_BLUE); // Default background color
        button.setForeground(SIMS_DARK_TEXT); // Text color
        button.setFocusPainted(false); // No focus border
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor on hover
        // Add mouse listeners for hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(SIMS_BUTTON_HOVER); // Change background on hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SIMS_LIGHT_BLUE); // Revert background when mouse exits
            }
        });
        return button;
    }

    /**
     * Displays the current question and its options in the UI.
     * Updates the question label and radio buttons based on `currentQuestionIndex`.
     */
    private void displayQuestion() {
        Question q = questions[currentQuestionIndex];
        questionLabel.setText(q.question); // Set question text

        optionButtonGroup.clearSelection(); // Clear previous selections
        String[] optionLetters = {"А", "Б", "В", "Г"}; // Labels for options
        for (int i = 0; i < q.options.length; i++) {
            optionButtons[i].setText(optionLetters[i] + ". " + q.options[i]); // Set option text
        }

        // Enable/disable 'Next' button based on whether it's the last question
        nextButton.setEnabled(currentQuestionIndex < questions.length - 1);
        finishButton.setEnabled(true); // 'Finish' button is always enabled
    }

    /**
     * Saves the user's selected answer for the current question into the `userAnswers` array.
     * If no option is selected, it records -1.
     */
    private void saveCurrentAnswer() {
        for (int i = 0; i < optionButtons.length; i++) {
            if (optionButtons[i].isSelected()) {
                userAnswers[currentQuestionIndex] = i; // Save the index of the selected answer
                return;
            }
        }
        userAnswers[currentQuestionIndex] = -1; // No answer selected
    }

    /**
     * Finalizes the test, calculates the score, and displays the results.
     * Based on the score, it either congratulates the user or prompts for a retry.
     * It also updates the discipline's mark and then disposes the frame,
     * executing the {@code onFinishCallback}.
     */
    private void finishTest() {
        score = 0;
        // Calculate the score by comparing user answers with correct answers
        for (int i = 0; i < questions.length; i++) {
            if (userAnswers[i] == questions[i].correctAnswer) {
                score++;
            }
        }

        mainPanel.setVisible(false); // Hide the main test panel

        boolean passed = (score >= 10); // Check if the test was passed (10 or more points)

        // Show test result message
        String message = "Ваш результат: " + score + " з " + questions.length + " балів.";
        if (passed) {
            message += "\nВітаємо! Ви успішно пройшли тест із " + this.getTitle();
            JOptionPane.showMessageDialog(this, message, "Успіх", JOptionPane.INFORMATION_MESSAGE);
            showResultPanel(score); // Show detailed result panel if passed
        } else {
            message += "\nНа жаль, Ви набрали менше ніж 10 балів. Спробуйте ще раз!";
            int option = JOptionPane.showConfirmDialog(
                    this,
                    message,
                    "Результати тесту",
                    JOptionPane.YES_NO_OPTION, // Offer retry option
                    JOptionPane.INFORMATION_MESSAGE
            );
            if (option == JOptionPane.YES_OPTION) {
                resetTest(); // If user chooses to retry, reset the test
                return; // Do not dispose frame or call callback yet
            }
        }

        // Update discipline mark based on score
        if (score >= 13) {
            discipline.setAvtomat(); // Set special 'Avtomat' status
            discipline.setCurrentStudentsMark(99); // Assign high mark
        } else {
            discipline.setCurrentStudentsMark(score * 4); // Scale score to a 100-point system
        }

        dispose(); // Close the test frame
        if (onFinishCallback != null) onFinishCallback.run(); // Execute the callback
    }

    /**
     * Displays a detailed panel showing the user's score and all questions
     * with their correct answers, highlighting user's answers.
     *
     * @param score The final score achieved by the user.
     */
    private void showResultPanel(int score) {
        resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout(10, 10)); // Layout with gaps
        resultPanel.setBorder(new EmptyBorder(30, 30, 30, 30)); // Padding
        resultPanel.setBackground(SIMS_MEDIUM_PINK); // Background color

        JLabel scoreLabel = new JLabel("Ваш результат: " + score + " з " + questions.length, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 20)); // Font for score display
        scoreLabel.setForeground(SIMS_DARK_TEXT); // Text color
        resultPanel.add(scoreLabel, BorderLayout.NORTH); // Add score label to the top

        JTextPane answerPane = new JTextPane(); // Use JTextPane for HTML content
        answerPane.setContentType("text/html"); // Enable HTML rendering

        answerPane.setEditable(false); // Make it read-only
        answerPane.setBackground(SIMS_LIGHT_PINK); // Background color
        answerPane.setBorder(new EmptyBorder(15, 15, 15, 15)); // Padding

        // Build HTML content for detailed answers
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><body style='font-family: \"Arial\"; font-size: 14px; color: ")
                .append(toHex(SIMS_DARK_TEXT)) // Convert color to hex for HTML
                .append(";'>");
        htmlContent.append("<h3 style='color: ").append(toHex(SIMS_TURQUOISE)).append(";'>Правильні відповіді</h3>");

        String[] optionLetters = {"А", "Б", "В", "Г"}; // Option labels
        for (int i = 0; i < questions.length; i++) {
            Question q = questions[i];
            int userAnswer = userAnswers[i];
            int correctAnswer = q.correctAnswer;

            htmlContent.append("<p>");
            htmlContent.append("<b>").append(q.question).append("</b><br>"); // Bold question text

            for (int j = 0; j < q.options.length; j++) {
                String optionColor = toHex(SIMS_DARK_TEXT); // Default color for options

                // Highlight correct answer in green, incorrect user answer in red
                if (j == correctAnswer) {
                    optionColor = toHex(SIMS_GREEN_CORRECT.darker());
                } else if (j == userAnswer && userAnswer != correctAnswer) {
                    optionColor = toHex(SIMS_RED_INCORRECT.darker());
                }

                htmlContent.append("<span style='color: ").append(optionColor).append(";'>")
                        .append(optionLetters[j]).append(". ").append(q.options[j])
                        .append("</span>");

                // Add checkmark for correct answer, cross for incorrect user answer
                if (j == correctAnswer) {
                    htmlContent.append(" &nbsp;&#10003;"); // Checkmark symbol
                } else if (j == userAnswer && userAnswer != correctAnswer) {
                    htmlContent.append(" &nbsp;&#10007;"); // Cross symbol
                }
                htmlContent.append("<br>");
            }
            htmlContent.append("</p><hr style='border: 0.5px solid ").append(toHex(SIMS_LIGHT_BLUE)).append(";'>"); // Separator
        }
        htmlContent.append("</body></html>");
        answerPane.setText(htmlContent.toString()); // Set generated HTML to the text pane

        JScrollPane scrollPane = new JScrollPane(answerPane); // Make the text pane scrollable
        scrollPane.setPreferredSize(new Dimension(800, 500)); // Preferred size for scroll pane

        resultPanel.add(scrollPane, BorderLayout.CENTER); // Add scroll pane to the center of result panel

        JButton doneButton = createSimsButton("Готово"); // "Done" button
        JPanel resultButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        resultButtonsPanel.setBackground(SIMS_TURQUOISE); // Background color
        resultButtonsPanel.add(doneButton);
        resultPanel.add(resultButtonsPanel, BorderLayout.SOUTH); // Add buttons panel to the bottom

        add(resultPanel); // Add the result panel to the frame
        revalidate(); // Revalidate the layout
        repaint(); // Repaint the frame

        // Action listener for the "Done" button on the result panel
        doneButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                    this,
                    "Вітаємо з успішним проходженням контрольної роботи !", // Final success message
                    "Успіх!",
                    JOptionPane.INFORMATION_MESSAGE
            );
            System.exit(0); // Exit the application (consider calling the callback instead for game flow)
        });
    }

    /**
     * Resets the test to its initial state, allowing the user to retake it.
     * Clears user answers, resets question index, and hides the result panel if shown.
     */
    private void resetTest() {
        currentQuestionIndex = 0; // Reset to the first question
        for (int i = 0; i < userAnswers.length; i++) {
            userAnswers[i] = -1; // Reset all answers
        }
        if (resultPanel != null) {
            resultPanel.setVisible(false); // Hide result panel
            remove(resultPanel); // Remove it from the frame
        }
        mainPanel.setVisible(true); // Show the main test panel again
        displayQuestion(); // Display the first question
        revalidate(); // Revalidate the layout
        repaint(); // Repaint the frame
    }

    /**
     * Displays a dialog box with instructions on how to take the test.
     */
    private void showInstructionsDialog() {
        String instructions = "<html>" +
                "<body style='font-family: \"Arial\"; font-size: 13px; color: " + toHex(SIMS_DARK_TEXT) + ";'>" +
                "<h1 style='color: " + toHex(Color.BLACK) + ";'>Інструкція до проходження тесту</h1>" +
                "<p>Ласкаво просимо до тесту!</p>" +
                "<p>Будь ласка, дотримуйтеся цих простих кроків!</p>" +
                "<ol>" +
                "<li>Кожне запитання має <b>чотири варіанти відповіді</b> (А, Б, В, Г).</li>" +
                "<li><b>Оберіть лише одну</b> правильну, на Вашу думку, відповідь.</li>" +
                "<li>Використовуйте кнопку <b>«Наступна»</b> для переходу до наступного запитання.</li>" +
                "<li><b>Ви не зможете повернутися</b> до попередніх запитань.</li>" + // Updated instruction
                "<li>Ваша відповідь на поточне запитання буде збережена під час переходу до наступного.</li>" +
                "<li>Після того, як Ви відповісте на всі запитання, натисніть <b>«Завершити тест»</b>.</li>" +
                "<li>Тест буде вважатися успішно пройденим, якщо Ви наберете <b>10 або більше балів</b>.</li>" +
                "<li>Якщо Ви наберете менше за 10 балів, Вам буде запропоновано <b>пройти тест ще раз</b>.</li>" +
                "<li>Якщо Ви успішно пройдете тест (10 або більше балів), Ви зможете переглянути <b>всі правильні відповіді</b>.</li>" + // Updated instruction
                "</ol>" +
                "<p><b>Бажаємо успіху!</b></p>" +
                "</body></html>";

        JEditorPane editorPane = new JEditorPane("text/html", instructions); // Use JEditorPane for HTML
        editorPane.setEditable(false); // Make it read-only
        editorPane.setBackground(SIMS_LIGHT_PINK); // Background color
        editorPane.setBorder(new EmptyBorder(10, 10, 10, 10)); // Padding

        JScrollPane scrollPane = new JScrollPane(editorPane); // Make it scrollable
        scrollPane.setPreferredSize(new Dimension(600, 450)); // Preferred size

        JOptionPane.showMessageDialog(this, scrollPane, "Інструкція", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Helper method to convert a {@link Color} object to its hexadecimal string representation.
     * This is useful for embedding colors directly into HTML/CSS strings.
     *
     * @param color The {@link Color} object to convert.
     * @return A string representing the color in hexadecimal format (e.g., "#RRGGBB").
     */
    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Gets the {@link Discipline} object associated with this test.
     *
     * @return The discipline of the test.
     */
    public Discipline getDiscipline() {
        return discipline;
    }

    /**
     * Sets the {@link Discipline} object for this test.
     *
     * @param discipline The new discipline for the test.
     */
    public void setDiscipline(Discipline discipline) {
        this.discipline = discipline;
    }

    /**
     * Gets the score obtained by the user in the test.
     *
     * @return The test score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the score for the test.
     *
     * @param score The score to set.
     */
    public void setScore(int score) {
        this.score = score;
    }
}