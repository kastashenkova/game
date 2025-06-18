package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gui.LoadingFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

/**
 * The StudyProgressGUI class provides a graphical user interface for displaying
 * and managing a student's academic progress during a session. It shows disciplines,
 * current scores, and allows interaction for taking exams or credits (zalik).
 */
public class StudyProgressGUI extends JFrame {
    // UI Colors consistent with a "Sims" theme
    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);
    private static final Color SIMS_GREEN_CORRECT = new Color(144, 238, 144);
    private static final Color SIMS_GREEN_INCORRECT = new Color(182, 209, 182);

    private Student currentStudent;
    double totalScoresSum = 0; // Accumulates scores for average calculation

    private Hero hero; // The player's character
    private JTable progressTable; // Table to display discipline progress
    private DefaultTableModel tableModel; // Model for the progress table
    private JLabel averageScoreLabel; // Label to display the average score
    private JButton endSessionButton; // Button to conclude the session
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create(); // GSON for JSON operations
    private static final String ENROLLMENT_FILE = "enrollment_data.json"; // File to store student enrollment data

    /**
     * Constructs a new StudyProgressGUI.
     * @param hero The Hero object associated with this study session.
     */
    public StudyProgressGUI(Hero hero) {
        MusicPlayer.getInstance().setMusicEnabled(true);
        MusicPlayer.getInstance().playMusic("/assets/Sounds/sessionBack.wav"); // Play background music for the session

        this.hero = hero;
        setTitle("Сесія"); // Window title
        setSize(1200, 800); // Window size
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close operation
        setLocationRelativeTo(null); // Center the window

        initComponents(); // Initialize UI components
        loadStudentData(); // Load student's enrolled disciplines
        showInstructionsDialog(); // Show instructions at the start
        updateProgressDisplay(); // Update the table and average score
    }

    /**
     * Initializes the UI components of the StudyProgressGUI, including the table,
     * labels, and buttons.
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10)); // Main layout for the frame

        // Panel for the progress table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Ваш прогрес по дисциплінах"));

        String[] columnNames = {"Дисципліна", "Тип контролю", "Поточний бал", "Сесія"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only the "Сесія" column (index 3) is editable
                return column == 3;
            }
        };
        progressTable = new JTable(tableModel);
        progressTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        progressTable.setRowHeight(25);
        progressTable.setShowGrid(true);
        progressTable.setGridColor(Color.LIGHT_GRAY);
        progressTable.setIntercellSpacing(new Dimension(1, 1));

        // Set custom renderer and editor for the "Сесія" column (button)
        progressTable.getColumn("Сесія").setCellRenderer(new ButtonRenderer());
        progressTable.getColumn("Сесія").setCellEditor(new ButtonEditor(new JCheckBox()));

        // Add mouse listener for double-click to display discipline details
        progressTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    int row = progressTable.getSelectedRow();
                    if (row != -1) {
                        displayDisciplineDetails(row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(progressTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        // Label for student information at the top
        JLabel studentInfoLabel = new JLabel("Завантажую інформацію про студента...");
        studentInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        studentInfoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(studentInfoLabel, BorderLayout.NORTH);

        // Panel for average score and "End Session" button at the bottom
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        averageScoreLabel = new JLabel(" "); // Initial empty label
        averageScoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        averageScoreLabel.setForeground(SIMS_DARK_TEXT);
        southPanel.add(averageScoreLabel);

        // "End Session" button
        endSessionButton = new JButton("Завершити сесію");
        endSessionButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        endSessionButton.setBackground(SIMS_MEDIUM_PINK);
        endSessionButton.setForeground(SIMS_DARK_TEXT);
        endSessionButton.setEnabled(true); // Initially enabled
        endSessionButton.addActionListener(e -> {
            handleEndSession(); // Call method to handle session conclusion
        });

        southPanel.add(endSessionButton);

        add(southPanel, BorderLayout.SOUTH);
    }

    /**
     * Loads student data from a JSON file. If the file is not found or corrupted,
     * a default empty student will be created.
     */
    private void loadStudentData() {
        try (FileReader reader = new FileReader(ENROLLMENT_FILE)) {
            currentStudent = gson.fromJson(reader, Student.class);
            if (currentStudent == null) {
                System.err.println("Error: Failed to load student data from JSON. File might be empty or corrupted.");
                currentStudent = new Student("Невідомий студент", 0, "Невідомо"); // Fallback to default student
            }

            // Ensure control type is set for all disciplines
            for (Discipline disc : currentStudent.getEnrolledDisciplines()) {
                if (disc.getControlType() == null || disc.getControlType().isEmpty()) {
                    disc.setControlType(Discipline.CONTROL_TYPE_ZALIK); // Default to "Zalik" if not specified
                }
            }

        } catch (IOException e) {
            System.err.println("Error loading student data from JSON: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load student data. An empty student will be created.",
                    "Loading Error",
                    JOptionPane.ERROR_MESSAGE);
            currentStudent = new Student("Студент без даних", 0, "Н/Д"); // Fallback on error
        }
        updateStudentInfoLabel(); // Update the student info display
    }

    /**
     * Updates the student information label at the top of the GUI.
     */
    private void updateStudentInfoLabel() {
        JLabel studentInfoLabel = (JLabel) ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.NORTH);
        if (currentStudent != null) {
            double totalCredits = 0;
            for (Discipline discipline : currentStudent.getEnrolledDisciplines()) {
                totalCredits += discipline.getCredits();
            }
            studentInfoLabel.setText("Студент: " + currentStudent.getName() + " (ID: " + currentStudent.getStudentId() +
                    ", курс: " + currentStudent.getCourse() + ", спеціальність: " + currentStudent.getMajor() +
                    ", кредити: " + totalCredits + ")");
        } else {
            studentInfoLabel.setText("Інформація про студента недоступна.");
        }
    }

    /**
     * Updates the progress table and recalculates the average score based on
     * the current student's enrolled disciplines and their scores.
     */
    void updateProgressDisplay() {
        tableModel.setRowCount(0); // Clear existing rows

        if (currentStudent == null || currentStudent.getEnrolledDisciplines().isEmpty()) {
            averageScoreLabel.setText("Середній бал: 0.00");
            endSessionButton.setEnabled(false); // Disable end session if no disciplines
            return;
        }

        totalScoresSum = 0; // Reset sum for recalculation
        int disciplinesCountedForAverage = 0; // Count disciplines that contribute to average
        boolean allScoresAbove60 = true; // Flag to check if all scores are sufficient

        for (Discipline disc : currentStudent.getEnrolledDisciplines()) {
            Integer actualScore = currentStudent.getTrimesterScore(disc.getDisciplineId());
            String scoreDisplay;
            String displayControlType = disc.getControlType();
            int scoreForAverageCalculation;

            if (actualScore == null) {
                // If no actual score is recorded (e.g., first attempt or not yet taken)
                if (Objects.equals(displayControlType, Discipline.CONTROL_TYPE_ZALIK)) {
                    int attempts = currentStudent.getZalikAttempts(disc.getDisciplineId());
                    if (attempts == 0) {
                        scoreDisplay = "40"; // Default initial score for Zalik
                        scoreForAverageCalculation = 40;
                    } else {
                        scoreDisplay = "0"; // Score if attempts made but no success
                        scoreForAverageCalculation = 0;
                    }
                } else {
                    scoreDisplay = String.valueOf(disc.getCurrentStudentsMark()); // Use current mark from discipline
                    scoreForAverageCalculation = disc.getCurrentStudentsMark();
                }
            } else {
                // If actual score is recorded
                scoreDisplay = String.valueOf(actualScore);
                scoreForAverageCalculation = actualScore;
            }

            totalScoresSum += scoreForAverageCalculation;
            disciplinesCountedForAverage++; // Increment count for average calculation

            // Check if score is sufficient for overall session completion (>=60)
            if (scoreForAverageCalculation < 60) {
                // Special handling for Zalik with 0 attempts and initial score of 40
                if (!(Objects.equals(displayControlType, Discipline.CONTROL_TYPE_ZALIK) && currentStudent.getZalikAttempts(disc.getDisciplineId()) == 0)) {
                    allScoresAbove60 = false;
                }
            }

            String actionButtonText;
            if (disc.getAvtomat()) {
                actionButtonText = "Автомат";
            } else if (Objects.equals(displayControlType, Discipline.CONTROL_TYPE_ZALIK)) {
                actionButtonText = "Допуск"; // For Zalik, button text is "Допуск"
            } else {
                actionButtonText = "Допуск"; // For Exam, button text is "Допуск"
            }


            Object[] rowData = {
                    disc.getName(),
                    displayControlType,
                    scoreDisplay,
                    actionButtonText
            };
            tableModel.addRow(rowData);
        }

        double average = (disciplinesCountedForAverage > 0) ? (totalScoresSum / disciplinesCountedForAverage) : 0;
        averageScoreLabel.setText(String.format("Середній бал: %.2f", average));

        // Enable or disable the "End Session" button based on overall scores
        endSessionButton.setEnabled(allScoresAbove60); // Enable only if all scores are >= 60
    }

    /**
     * Displays detailed information about a selected discipline in a dialog.
     * @param row The row index of the selected discipline in the table.
     */
    private void displayDisciplineDetails(int row) {
        String disciplineName = (String) tableModel.getValueAt(row, 0);

        Discipline selectedDiscipline = null;
        for (Discipline disc : currentStudent.getEnrolledDisciplines()) {
            if (disc.getName().equals(disciplineName)) {
                selectedDiscipline = disc;
                break;
            }
        }

        if (selectedDiscipline != null) {
            StringBuilder details = new StringBuilder();
            details.append("Назва: ").append(selectedDiscipline.getName()).append("\n");
            details.append("Викладач: ").append(selectedDiscipline.getInstructor()).append("\n");
            details.append("Кредити: ").append(selectedDiscipline.getCredits()).append("\n");
            details.append("Тип контролю: ").append(selectedDiscipline.getControlType()).append("\n");

            if (Objects.equals(selectedDiscipline.getControlType(), Discipline.CONTROL_TYPE_ZALIK)) {
                details.append("Використано спроб крутити колесо: ").append(currentStudent.getZalikAttempts(selectedDiscipline.getDisciplineId())).append(" з 2\n");
            }


            JOptionPane.showMessageDialog(this, details.toString(), "Інформація про дисципліну", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to find details for this discipline.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles the action when the "End Session" button is clicked.
     * It prompts the user for confirmation and, if confirmed, transitions to the next game stage.
     */
    private void handleEndSession() {
        // Customize JOptionPane button texts
        UIManager.put("OptionPane.yesButtonText", "Так");
        UIManager.put("OptionPane.noButtonText", "Ні");

        int result = JOptionPane.showConfirmDialog(
                this,
                "Ви впевнені, що хочете завершити сесію? Деякі іспити ще не складено або деякі предмети мають низький бал.",
                "Підтвердження завершення сесії",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result != JOptionPane.YES_OPTION) {
            return; // User clicked "No"
        }

        // Check average score for scholarship
        double currentAverageScore = Double.parseDouble(averageScoreLabel.getText().replace("Середній бал: ", "").replace(",", "."));
        if (currentAverageScore > 95.0) {
            JOptionPane.showMessageDialog(this,
                    "Вітаємо! Тепер щомісяця Ви отримуватиме стипендію у розмірі 2000 грн!",
                    "ЮХУУУУ!!!! Стипендія!!!",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        dispose(); // Close the session GUI
        SwingUtilities.invokeLater(() -> {
            LoadingFrame loading = new LoadingFrame(); // Show loading screen
            loading.startLoading(() -> {
                // Apply post-session effects to the hero
                hero.levelUp(); // Level up the hero
                hero.setLevel(4); // Set hero level to 4 (example, might be dynamic)
                hero.setStudent(currentStudent); // Update hero's student object
                hero.decreaseEnergy(40); // Decrease energy
                hero.decreaseHunger(-30); // Decrease hunger (increase hunger stat - less hungry)
                hero.decreaseMood(-30); // Decrease mood (increase mood stat - happier)
                GameFrame gameFrame = new GameFrame(hero); // Create new game frame
                gameFrame.getGamePanel().getHintPanel().setHint(4); // Set a hint for the next game stage
                gameFrame.setVisible(true); // Make the game frame visible
            });
        });
    }

    /**
     * Custom TableCellRenderer for rendering buttons in the "Сесія" column.
     * It styles the button based on the student's score in the discipline.
     */
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString()); // Set button text

            // Get the score from the "Поточний бал" column
            Object scoreObj = table.getModel().getValueAt(row, 2);
            Integer score = null;
            if (scoreObj instanceof Integer) {
                score = (Integer) scoreObj;
            } else if (scoreObj instanceof String && !scoreObj.equals("N/A")) {
                try {
                    // Parse string score, handling cases like "40 (поч.)"
                    String scoreStr = (String) scoreObj;
                    if (scoreStr.contains(" ")) {
                        scoreStr = scoreStr.substring(0, scoreStr.indexOf(" "));
                    }
                    score = Integer.parseInt(scoreStr);
                } catch (NumberFormatException e) {
                    score = null; // If parsing fails
                }
            }

            // Determine discipline and its control type for button state
            String disciplineName = (String) table.getModel().getValueAt(row, 0);
            Discipline currentDisc = null;
            for (Discipline disc : currentStudent.getEnrolledDisciplines()) {
                if (disc.getName().equals(disciplineName)) {
                    currentDisc = disc;
                    break;
                }
            }

            if (currentDisc != null && Objects.equals(currentDisc.getControlType(), Discipline.CONTROL_TYPE_ZALIK)) {
                int attempts = currentStudent.getZalikAttempts(currentDisc.getDisciplineId());
                if (attempts >= 2) {
                    setEnabled(false); // Disable button if all Zalik attempts used
                    setText("Іспит складено"); // Indicate completion
                } else if (currentDisc.getAvtomat()){
                    setEnabled(false);
                    setText("Автомат"); // If 'Avtomat' (automatic pass)
                } else {
                    setEnabled(true); // Enable for Zalik if attempts remain
                    setText("Допуск");
                }
            } else {
                setEnabled(true); // Enable for Exams
                setText("Допуск"); // Default text for exams or other controls
            }

            // Set button background color based on score
            if (score != null && score >= 40) {
                setBackground(SIMS_GREEN_CORRECT); // Green for sufficient score
            } else {
                setBackground(SIMS_GREEN_INCORRECT); // Lighter green for insufficient score
            }

            // Set tooltip text based on score
            if (score != null) {
                if (score >= 40) {
                    setToolTipText("Студентка допущена до контролю");
                } else {
                    setToolTipText("Бал " + score + " недостатній для допуску до екзамену. Потрібно щонайменше 40 балів!");
                }
            } else {
                setToolTipText("Бал для " + disciplineName + " не встановлено.");
            }
            return this;
        }
    }

    /**
     * Custom TableCellEditor for handling button clicks in the "Сесія" column.
     * When clicked, it opens the corresponding exam or credit window.
     */
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped()); // Fire event when button is clicked
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            currentRow = row;
            isPushed = true; // Flag to indicate button was pushed

            // Get the score from the "Поточний бал" column for enabling/disabling logic
            Object scoreObj = table.getModel().getValueAt(row, 2);
            Integer score = null;
            if (scoreObj instanceof Integer) {
                score = (Integer) scoreObj;
            } else if (scoreObj instanceof String && !scoreObj.equals("N/A")) {
                try {
                    String scoreStr = (String) scoreObj;
                    if (scoreStr.contains(" ")) {
                        scoreStr = scoreStr.substring(0, scoreStr.indexOf(" "));
                    }
                    score = Integer.parseInt(scoreStr);
                } catch (NumberFormatException e) {
                    score = null;
                }
            }

            // Get discipline for its control type and attempts
            String disciplineName = (String) table.getModel().getValueAt(row, 0);
            Discipline currentDisc = null;
            for (Discipline disc : currentStudent.getEnrolledDisciplines()) {
                if (disc.getName().equals(disciplineName)) {
                    currentDisc = disc;
                    break;
                }
            }

            if (currentDisc != null && Objects.equals(currentDisc.getControlType(), Discipline.CONTROL_TYPE_ZALIK)) {
                int attempts = currentStudent.getZalikAttempts(currentDisc.getDisciplineId());
                if (attempts >= 2) {
                    button.setEnabled(false); // Disable if Zalik attempts exhausted
                } else {
                    button.setEnabled(true); // Enable if Zalik attempts remain
                }
            } else {
                button.setEnabled(true); // Keep enabled for exams
            }

            // Set button background color based on score
            if (score != null && score >= 40) {
                button.setBackground(SIMS_GREEN_CORRECT);
            } else {
                button.setBackground(SIMS_GREEN_INCORRECT);
            }
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                String disciplineName = (String) tableModel.getValueAt(currentRow, 0);
                Discipline selectedDiscipline = null;

                for (Discipline disc : currentStudent.getEnrolledDisciplines()) {
                    if (disc.getName().equals(disciplineName)) {
                        selectedDiscipline = disc;
                        break;
                    }
                }

                if (selectedDiscipline == null) {
                    System.err.println("Error. Discipline not found.");
                    return label; // Return original label if discipline not found
                }

                // Get the current displayed score from the table model
                Object tableScoreObj = tableModel.getValueAt(currentRow, 2);
                Integer currentDisplayedScore = null;
                if (tableScoreObj instanceof Integer) {
                    currentDisplayedScore = (Integer) tableScoreObj;
                } else if (tableScoreObj instanceof String && !tableScoreObj.equals("N/A")) {
                    try {
                        String scoreStr = (String) tableScoreObj;
                        if (scoreStr.contains(" ")) {
                            scoreStr = scoreStr.substring(0, scoreStr.indexOf(" "));
                        }
                        currentDisplayedScore = Integer.parseInt(scoreStr);
                    } catch (NumberFormatException e) {
                        currentDisplayedScore = null;
                    }
                }

                // Proceed if score is sufficient (>= 40)
                if (currentDisplayedScore != null && currentDisplayedScore >= 40) {
                    String controlType = selectedDiscipline.getControlType();

                    if (Objects.equals(controlType, Discipline.CONTROL_TYPE_EXAM)) {
                        if (selectedDiscipline.getAvtomat()){
                            JOptionPane.showMessageDialog(null, "Цей предмет вже складено автоматично. Вікно іспиту не відкриватиметься.",
                                    "Автомат",
                                    JOptionPane.INFORMATION_MESSAGE);
                            return "Автомат"; // Indicate automatic pass
                        }
                        // Open ExamWindow
                        ExamWindow examWindow = new ExamWindow(StudyProgressGUI.this, selectedDiscipline, currentStudent);
                        examWindow.setVisible(true);
                        // StudyProgressGUI.this.updateProgressDisplay(); // Update display after exam (might be done by ExamWindow)
                    } else if (Objects.equals(controlType, Discipline.CONTROL_TYPE_ZALIK)) {
                        int attemptsMade = currentStudent.getZalikAttempts(selectedDiscipline.getDisciplineId());
                        if (attemptsMade < 2) { // Check if attempts are available
                            // Open CreditWindow (Zalik)
                            CreditWindow zalikWindow = new CreditWindow(StudyProgressGUI.this, selectedDiscipline, currentStudent, attemptsMade);
                            zalikWindow.setVisible(true);
                            StudyProgressGUI.this.updateProgressDisplay(); // Update display after Zalik
                        } else {
                            JOptionPane.showMessageDialog(StudyProgressGUI.this,
                                    "Ви використали всі 2 спроби для заліку з " + selectedDiscipline.getName() + ".",
                                    "Спроби вичерпано",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        System.out.println("  -> Undefined control type, cannot proceed to session.");
                    }
                } else {
                    // Show warning if score is insufficient
                    JOptionPane.showMessageDialog(StudyProgressGUI.this,
                            "Бал " + ((currentDisplayedScore != null) ? currentDisplayedScore : "0") + " недостатній для допуску до екзамену. Потрібно щонайменше 40 балів!",
                            "Недопущено",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
            isPushed = false; // Reset push flag
            return label; // Return the button's label
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    /**
     * Displays an instruction dialog to the user at the start of the session.
     */
    private void showInstructionsDialog() {
        String instructions = "<html>" +
                "<body style='font-family: \"Arial\"; font-size: 13px; color: #00000;'>" +
                "<h1 style='color: #00000;'>Інструкція до складання сесії</h1>" +
                "<p>Вітаємо з тим, що ви дожили до сесії!</p>" +
                "<p>Будь ласка, дотримуйтеся цих простих кроків!</p>" +
                "<ol>" +
                "<li>Кожна залікова дисципліна має колесо фортуни</li>" +
                "<li><b>Вам буде надато 2 спроби крутити колесо та отримати бали!</li>" +
                "<li>Натисніть на <b>«Допуск»</b> для проходження заліка чи іспита.</li>" +
                "<li><b>Ви зможете</b> перескласти дисципліну у разі потреби.</li>" +
                "<li>Поставтеся серйозно до іспитів!!!</li>" +
                "<li>Після того, як Ви складете всі дисципліни, натисніть <b>«Завершити сесію»</b>.</li>" +
                "<li> <b>НЕ НАТИСКАЙТЕ ЗАВЕРШИТИ ПОКИ НЕ СКЛАДЕТЕ</b>.</li>" +
                "</ol>" +
                "<p><b>Бажаємо успіху!</b></p>" +
                "</body></html>";

        JEditorPane editorPane = new JEditorPane("text/html", instructions);
        editorPane.setEditable(false);
        editorPane.setBackground(SIMS_LIGHT_PINK);
        editorPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(600, 450));

        JOptionPane.showMessageDialog(this, scrollPane, "Інструкція", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Gets the current Hero object associated with this session.
     * @return The Hero object.
     */
    public Hero getHero() {
        return hero;
    }

    /**
     * Sets the Hero object for this session.
     * @param hero The new Hero object.
     */
    public void setHero(Hero hero) {
        this.hero = hero;
    }

}