package studies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.Discipline;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class StudyProgressGUI extends JFrame {
    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);
    private static final Color SIMS_GREEN_CORRECT = new Color(144, 238, 144);
    private static final Color SIMS_GREEN_INCORRECT = new Color(182, 209, 182);

    private Student currentStudent;
    private JTable progressTable;
    private DefaultTableModel tableModel;
    private JLabel averageScoreLabel;
    private JButton endSessionButton;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String ENROLLMENT_FILE = "enrollment_data.json";

    public StudyProgressGUI() {
        setTitle("Сесія");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadStudentData();
        updateProgressDisplay();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Ваш прогрес по дисциплінах"));

        String[] columnNames = {"Дисципліна", "Тип контролю", "Поточний бал", "Сесія"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        progressTable = new JTable(tableModel);
        progressTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        progressTable.setRowHeight(25);
        progressTable.setShowGrid(true);
        progressTable.setGridColor(Color.LIGHT_GRAY);
        progressTable.setIntercellSpacing(new Dimension(1, 1));

        progressTable.getColumn("Сесія").setCellRenderer(new ButtonRenderer());
        progressTable.getColumn("Сесія").setCellEditor(new ButtonEditor(new JCheckBox()));

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

        JLabel studentInfoLabel = new JLabel("Завантажую інформацію про студента...");
        studentInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        studentInfoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(studentInfoLabel, BorderLayout.NORTH);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        averageScoreLabel = new JLabel(" ");
        averageScoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        averageScoreLabel.setForeground(SIMS_DARK_TEXT);
        southPanel.add(averageScoreLabel);

        // Add the new "End Session" button
        endSessionButton = new JButton("Завершити сесію");
        endSessionButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        endSessionButton.setBackground(SIMS_MEDIUM_PINK);
        endSessionButton.setForeground(SIMS_DARK_TEXT);
        endSessionButton.setEnabled(true);
        endSessionButton.addActionListener(e -> handleEndSession());
        southPanel.add(endSessionButton);

        add(southPanel, BorderLayout.SOUTH);
    }

    private void loadStudentData() {
        try (FileReader reader = new FileReader(ENROLLMENT_FILE)) {
            currentStudent = gson.fromJson(reader, Student.class);
            if (currentStudent == null) {
                System.err.println("Помилка: не вдалося завантажити дані студента з JSON. Можливо, файл порожній або пошкоджений.");
                currentStudent = new Student("UNKNOWN", "Невідомий студент", 0, "Невідомо");
            }

            if (currentStudent != null) {
                for (Discipline disc : currentStudent.getEnrolledDisciplines()) {
                    if (disc.getControlType() == null || disc.getControlType().isEmpty()) {
                        disc.setControlType(Discipline.CONTROL_TYPE_ZALIK);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Помилка завантаження даних студента з JSON: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Не вдалося завантажити дані студента. Буде створено порожнього студента.",
                    "Помилка завантаження",
                    JOptionPane.ERROR_MESSAGE);
            currentStudent = new Student("NO_DATA", "Студент без даних", 0, "Н/Д");
        }
        updateStudentInfoLabel();
    }

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

    void updateProgressDisplay() {
        tableModel.setRowCount(0);

        if (currentStudent == null || currentStudent.getEnrolledDisciplines().isEmpty()) {
            averageScoreLabel.setText("Середній бал: 0.00");
            endSessionButton.setEnabled(false);
            return;
        }

        double totalScoresSum = 0;
        int disciplinesCountedForAverage = currentStudent.getEnrolledDisciplines().size();
        boolean allScoresAbove60 = true;

        for (Discipline disc : currentStudent.getEnrolledDisciplines()) {
            Integer actualScore = currentStudent.getTrimesterScore(disc.getDisciplineId());
            String scoreDisplay;
            String displayControlType = disc.getControlType();
            int scoreForAverageCalculation;

            if (actualScore == null) {
                if (Objects.equals(displayControlType, Discipline.CONTROL_TYPE_ZALIK)) {
                    int attempts = currentStudent.getZalikAttempts(disc.getDisciplineId());
                    if (attempts == 0) {
                        scoreDisplay = "40";
                        scoreForAverageCalculation = 40;
                    } else {
                        scoreDisplay = "0";
                        scoreForAverageCalculation = 0;
                    }
                } else {
                    scoreDisplay = "0";
                    scoreForAverageCalculation = 0;
                }
            } else {
                scoreDisplay = String.valueOf(actualScore);
                scoreForAverageCalculation = actualScore;
            }

            totalScoresSum += scoreForAverageCalculation;

            if (actualScore == null || actualScore < 60) {
                if (!(Objects.equals(displayControlType, Discipline.CONTROL_TYPE_ZALIK) && currentStudent.getZalikAttempts(disc.getDisciplineId()) == 0)) {
                    allScoresAbove60 = false;
                } else if (Objects.equals(displayControlType, Discipline.CONTROL_TYPE_ZALIK) && currentStudent.getZalikAttempts(disc.getDisciplineId()) == 0 && scoreForAverageCalculation <= 60) {
                    allScoresAbove60 = false;
                } else if (actualScore != null && actualScore < 60) {
                    allScoresAbove60 = false;
                }
            }

            String actionButtonText = "Допуск";

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

        endSessionButton.setEnabled(allScoresAbove60);
    }

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
            JOptionPane.showMessageDialog(this, "Не вдалося знайти деталі для цієї дисципліни.", "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // New method to handle "End Session" button click
    private void handleEndSession() {
        UIManager.put("OptionPane.yesButtonText", "Так");
        UIManager.put("OptionPane.noButtonText", "Ні");

        boolean allAttemptsUsed = true;
        for (Discipline disc : currentStudent.getEnrolledDisciplines()) {
            Integer actualScore = currentStudent.getTrimesterScore(disc.getDisciplineId());

            if (actualScore == null) {
                if (Objects.equals(disc.getControlType(), Discipline.CONTROL_TYPE_ZALIK)) {
                    if (currentStudent.getZalikAttempts(disc.getDisciplineId()) < 2) {
                        allAttemptsUsed = false;
                        break;
                    }
                } else if (Objects.equals(disc.getControlType(), Discipline.CONTROL_TYPE_EXAM)) {
                    allAttemptsUsed = false;
                    break;
                }
            } else if (actualScore < 60) {
                allAttemptsUsed = false;
                break;
            }
        }

        int confirmResult = JOptionPane.YES_OPTION;
        if (!allAttemptsUsed) {
            confirmResult = JOptionPane.showConfirmDialog(
                    this,
                    "Ви впевнені, що хочете завершити сесію? Деякі іспити ще не складено або деякі предмети мають низький бал.",
                    "Підтвердження завершення сесії",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
        }

        if (confirmResult == JOptionPane.YES_OPTION) {
            // Session completion logic
            JOptionPane.showMessageDialog(this,
                    "Вітаємо! Ви успішно пройшли 3 рівень гри!",
                    "Сесію завершено!",
                    JOptionPane.INFORMATION_MESSAGE);

            // Check for scholarship
            double currentAverageScore = Double.parseDouble(averageScoreLabel.getText().replace("Середній бал: ", "").replace(",", "."));
            if (currentAverageScore > 95.0) {
                JOptionPane.showMessageDialog(this,
                        "Вітаємо! Тепер щомісяця Ви отримуватиме стипендію у розмірі 2000 грн!",
                        "ЮХУУУУ!!!! Стипендія!!!",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            dispose();
        }
    }


    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            Object scoreObj = table.getModel().getValueAt(row, 2);
            Integer score = null;
            if (scoreObj instanceof Integer) {
                score = (Integer) scoreObj;
            } else if (scoreObj instanceof String && !scoreObj.equals("N/A")) {
                try {
                    // Парсимо, враховуючи " (поч.)"
                    String scoreStr = (String) scoreObj;
                    if (scoreStr.contains(" ")) {
                        scoreStr = scoreStr.substring(0, scoreStr.indexOf(" "));
                    }
                    score = Integer.parseInt(scoreStr);
                } catch (NumberFormatException e) {
                    score = null;
                }
            }

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
                    setEnabled(false);
                    setText("Іспит складено");
                } else {
                    setEnabled(true);
                    setText("Допуск");
                }
            } else {
                setEnabled(true);
                setText("Допуск");
            }


            if (score != null && score >= 40) {
                setBackground(SIMS_GREEN_CORRECT);
            } else {
                setBackground(SIMS_GREEN_INCORRECT);
            }


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

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            currentRow = row;
            isPushed = true;

            Object scoreObj = table.getModel().getValueAt(row, 2);
            Integer score = null;
            if (scoreObj instanceof Integer) {
                score = (Integer) scoreObj;
            } else if (scoreObj instanceof String && !scoreObj.equals("N/A")) {
                try {
                    // Парсимо, враховуючи " (поч.)"
                    String scoreStr = (String) scoreObj;
                    if (scoreStr.contains(" ")) {
                        scoreStr = scoreStr.substring(0, scoreStr.indexOf(" "));
                    }
                    score = Integer.parseInt(scoreStr);
                } catch (NumberFormatException e) {
                    score = null;
                }
            }

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
                    button.setEnabled(false);
                } else {
                    button.setEnabled(true);
                }
            } else {
                button.setEnabled(true); // Залишаємо активною для екзаменів
            }

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
                    System.err.println("Помилка. Дисципліну не знайдено.");
                    return label;
                }

                Object tableScoreObj = tableModel.getValueAt(currentRow, 2);
                Integer currentDisplayedScore = null;
                if (tableScoreObj instanceof Integer) {
                    currentDisplayedScore = (Integer) tableScoreObj;
                } else if (tableScoreObj instanceof String && !tableScoreObj.equals("N/A")) {
                    try {
                        // Парсимо, враховуючи " (поч.)"
                        String scoreStr = (String) tableScoreObj;
                        if (scoreStr.contains(" ")) {
                            scoreStr = scoreStr.substring(0, scoreStr.indexOf(" "));
                        }
                        currentDisplayedScore = Integer.parseInt(scoreStr);
                    } catch (NumberFormatException e) {
                        currentDisplayedScore = null;
                    }
                }

                if (currentDisplayedScore != null && currentDisplayedScore >= 40) {
                    String controlType = selectedDiscipline.getControlType();
                    String controlTypeName = (controlType != null) ? controlType : "контролю";

                    if (Objects.equals(controlType, Discipline.CONTROL_TYPE_EXAM)) {
                        //ExamWindow examWindow = new ExamWindow(StudyProgressGUI.this, selectedDiscipline, currentStudent);
                        //examWindow.setVisible(true);
                        //StudyProgressGUI.this.updateProgressDisplay();
                    } else if (Objects.equals(controlType, Discipline.CONTROL_TYPE_ZALIK)) {
                        int attemptsMade = currentStudent.getZalikAttempts(selectedDiscipline.getDisciplineId());
                        if (attemptsMade < 2) {
                            CreditWindow zalikWindow = new CreditWindow(StudyProgressGUI.this, selectedDiscipline, currentStudent, attemptsMade);
                            zalikWindow.setVisible(true);
                            StudyProgressGUI.this.updateProgressDisplay();
                        } else {
                            JOptionPane.showMessageDialog(StudyProgressGUI.this,
                                    "Ви використали всі 2 спроби для заліку з " + selectedDiscipline.getName() + ".",
                                    "Спроби вичерпано",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        System.out.println("  -> Невизначений тип контролю, неможливо перейти до сесії.");
                    }
                } else {
                    JOptionPane.showMessageDialog(StudyProgressGUI.this,
                            "Бал " + ((currentDisplayedScore != null) ? currentDisplayedScore : "0") + " недостатній для допуску до екзамену. Потрібно щонайменше 40 балів!",
                            "Недопущено",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
            isPushed = false;
            return label;
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

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        UIManager.put("nimbusBase", SIMS_MEDIUM_PINK);
        UIManager.put("nimbusBlueGrey", SIMS_LIGHT_BLUE);
        UIManager.put("control", SIMS_LIGHT_PINK);
        UIManager.put("textForeground", SIMS_DARK_TEXT);
        UIManager.put("nimbusLightBackground", SIMS_LIGHT_PINK);

        ToolTipManager.sharedInstance().setInitialDelay(500);
        ToolTipManager.sharedInstance().setDismissDelay(10000);

        SwingUtilities.invokeLater(() -> new StudyProgressGUI().setVisible(true));
    }
}