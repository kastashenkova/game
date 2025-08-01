package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gui.LoadingFrame; // Assuming this class exists for loading animations
import gui.PauseAction; // Assuming this class exists for handling pause functionality

import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Random;

import static org.example.EnrollmentSystem.UNLIMITED_CAPACITY; // Static import for unlimited capacity constant

import static java.awt.Color.*; // Static import for common Color constants
import static org.example.EnrollmentSystem.MANDATORY_DISCIPLINE_CAPACITY; // Static import for mandatory discipline capacity

/**
 * The `EnrollmentSystemGUI` class provides a graphical user interface for students
 * to enroll in and drop from disciplines within a simulated university enrollment system.
 * It displays mandatory, available elective, and enrolled elective disciplines,
 * handles enrollment/unenrollment logic, manages credit limits, and includes
 * features like search, instructions, and an animated start.
 */
public class EnrollmentSystemGUI extends JFrame {

    /**
     * The file path for saving/loading enrollment data using GSON.
     */
    private static final String ENROLLMENT_FILE = "enrollment_data.json";
    /**
     * GSON instance for JSON serialization and deserialization, configured for pretty printing.
     */
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * The core enrollment system logic instance.
     */
    private EnrollmentSystem enrollmentSystem;
    /**
     * JLabel to display information about the current student.
     */
    private JLabel studentInfoLabel;

    /**
     * JList to display mandatory disciplines.
     */
    private JList<Discipline> mandatoryDisciplineList;
    /**
     * DefaultListModel for managing data in `mandatoryDisciplineList`.
     */
    private DefaultListModel<Discipline> mandatoryListModel;

    /**
     * JList to display available elective disciplines.
     */
    private JList<Discipline> electiveDisciplineList;
    /**
     * DefaultListModel for managing data in `electiveDisciplineList`.
     */
    private DefaultListModel<Discipline> electiveListModel;

    /**
     * JList to display disciplines the student has already enrolled in.
     */
    private JList<Discipline> enrolledElectiveList;
    /**
     * DefaultListModel for managing data in `enrolledElectiveList`.
     */
    private DefaultListModel<Discipline> enrolledElectiveListModel;

    /**
     * Button to enroll in a selected elective discipline.
     */
    private JButton enrollElectiveButton;
    /**
     * Button to drop from a selected enrolled elective discipline.
     */
    private JButton dropElectiveButton;
    /**
     * Button to confirm the student's elective selections.
     */
    private JButton confirmSelectionButton;

    /**
     * Text field for entering search queries.
     */
    private JTextField searchField;
    /**
     * Button to trigger the search functionality.
     */
    private JButton searchButton;
    /**
     * Combo box for selecting search criteria (e.g., by name, ID, instructor).
     */
    private JComboBox<String> searchCriteriaCombo;

    /**
     * Reference to the {@link Hero} object, which contains the main student character.
     */
    private Hero hero;
    /**
     * Reference to the {@link Student} object, representing the player's character.
     */
    private Student student;
    /**
     * Timer for automatically enrolling virtual students in elective disciplines.
     */
    private Timer autoEnrollTimer;
    /**
     * Random instance used for simulating "glitches" or events (e.g., warnings).
     */
    private Random randomGlitches = new Random();

    // Define custom colors for the UI, inspired by Sims
    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);

    /**
     * An internal static class that customizes how {@link Discipline} objects
     * are rendered in a {@link JList}. It displays discipline name, and enrollment
     * capacity, applying color-coding based on availability.
     */
    static class DisciplineListRenderer extends DefaultListCellRenderer {
        /**
         * Returns the component used for drawing the cell.
         *
         * @param list The JList we're painting.
         * @param value The value returned by {@code list.getModel().getElementAt(index)}.
         * @param index The cell's index.
         * @param isSelected True if the specified cell was selected.
         * @param cellHasFocus True if the specified cell has the focus.
         * @return A component configured to display the specified value.
         */
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Discipline) {
                Discipline disc = (Discipline) value;
                String capacityText;

                if (disc.isMandatory()) {
                    // For mandatory disciplines, show max capacity as fixed enrollment
                    capacityText = "Зайнято місць: " + disc.getMaxCapacity() + "/" + disc.getMaxCapacity();
                    label.setForeground(list.getForeground()); // Default text color
                } else {
                    // For elective disciplines
                    if (disc.getMaxCapacity() == UNLIMITED_CAPACITY) {
                        capacityText = "Зайнято місць: " + disc.getCurrentEnrollment() + "/необмежено";
                        label.setForeground(new Color(0, 100, 0)); // Green for unlimited
                    } else {
                        capacityText = "Зайнято місць: " + disc.getCurrentEnrollment() + "/" + disc.getMaxCapacity();
                        // Color logic for limited electives based on availability
                        if (!disc.hasAvailableSlots()) {
                            label.setForeground(RED.darker()); // Dark red if no slots
                        } else if (disc.getCurrentEnrollment() > disc.getMaxCapacity() * 0.75) {
                            label.setForeground(new Color(200, 100, 0)); // Orange if nearly full
                        } else {
                            label.setForeground(list.getForeground()); // Default if plenty of slots
                        }
                    }
                }
                // Set the display text for the list item
                label.setText(disc.getName() + " (" + capacityText + ")");
            }
            return label;
        }
    }

    /**
     * Constructs the `EnrollmentSystemGUI`.
     * This involves setting up the main window, initializing the enrollment system,
     * prompting the user for degree and course selection, loading initial data,
     * and setting up all GUI components and their listeners.
     *
     * @param hero The {@link Hero} object representing the player character.
     */
    public EnrollmentSystemGUI(Hero hero) {
        this.hero = hero;
        this.student = hero.getStudent();

        // Initialize and play background music
        MusicPlayer.getInstance().setMusicEnabled(true);
        MusicPlayer.getInstance().playMusic("/assets/Sounds/sessionBack.wav");

        // Apply custom UI Manager settings for consistent font styles
        UIManager.put("OptionPane.messageFont", new Font("Segoi UI", Font.BOLD, 12));
        UIManager.put("OptionPane.buttonFont", new Font("Segoi UI", Font.BOLD, 12));
        UIManager.put("Label.font", new Font("Segoi UI", Font.BOLD, 12));
        UIManager.put("Button.font", new Font("Segoi UI", Font.BOLD, 12));
        UIManager.put("TitledBorder.font", new Font("Segoi UI", Font.BOLD, 12));

        super("Система автоматизованого запису на дисципліни (САЗ)"); // Set window title
        enrollmentSystem = new EnrollmentSystem(); // Create core enrollment system

        // Set default text for JOptionPane buttons
        UIManager.put("OptionPane.okButtonText", "ОК");
        UIManager.put("OptionPane.cancelButtonText", "Скасувати");

        // Show initial instructions to the user
        showInstructionsDialog();

        // --- Degree and Course Selection ---
        String selectedDegree = showDegreeSelectionDialog();
        if (selectedDegree == null) {
            System.exit(0); // Exit if degree selection is canceled
        }

        int selectedCourse = showCourseSelectionDialog(selectedDegree);
        if (selectedCourse == -1) {
            System.exit(0); // Exit if course selection is canceled or invalid
        }

        showStartAnimation(); // Show a brief start animation

        // Initialize student and discipline data based on selected degree and course
        if (selectedDegree.equals("Бакалаврат")) {
            initializeInitialDataB(selectedCourse); // Load Bachelor's data
        } else if (selectedDegree.equals("Магістратура")) {
            initializeInitialDataM(selectedCourse); // Load Master's data
        }

        // Automatically enroll the student in all mandatory disciplines for their course
        for (Discipline mandatoryDisc : enrollmentSystem.getMandatoryDisciplines(student.getCourse())) {
            student.enrollDiscipline(mandatoryDisc);
        }

        setSize(1200, 800); // Set window size
        setLocationRelativeTo(null); // Center the window on screen
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Custom close behavior

        // Main panel setup
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Student Info Panel at the top
        JPanel studentInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        studentInfoLabel = new JLabel();
        studentInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        studentInfoPanel.add(studentInfoLabel);
        mainPanel.add(studentInfoPanel, BorderLayout.NORTH);

        // Panel for elective and enrolled elective disciplines (top part of the content)
        JPanel topContentPanel = new JPanel(new BorderLayout());
        topContentPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Elective Disciplines Panel (Available for Enrollment)
        JPanel electivePanel = new JPanel(new BorderLayout(5, 5));
        electivePanel.setBorder(BorderFactory.createTitledBorder("Вибіркові дисципліни (доступні для запису)"));
        electiveListModel = new DefaultListModel<>();
        electiveDisciplineList = new JList<>(electiveListModel);
        electiveDisciplineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        electiveDisciplineList.setCellRenderer(new DisciplineListRenderer()); // Apply custom renderer
        electiveDisciplineList.setFont(new Font("Segoe UI", Font.BOLD, 13));
        electivePanel.add(new JScrollPane(electiveDisciplineList), BorderLayout.CENTER);
        electiveDisciplineList.addMouseListener(new DisciplineInfoMouseAdapter()); // Add mouse listener for info dialogs

        // Search panel for electives
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        searchCriteriaCombo = new JComboBox<>(new String[] {
                "за назвою", "за кодом", "за викладачем_кою"
        });
        searchCriteriaCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        searchPanel.add(new JLabel("Пошук:"));
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchButton = new JButton("Пошук");
        searchButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchPanel.add(searchCriteriaCombo);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        electivePanel.add(searchPanel, BorderLayout.NORTH);

        // Buttons for elective enrollment/drop/confirmation
        JPanel electiveButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        enrollElectiveButton = new JButton("Записатися на вибіркову");
        enrollElectiveButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dropElectiveButton = new JButton("Виписатися з вибіркової");
        dropElectiveButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        confirmSelectionButton = new JButton("Готово (кредитів: 0)"); // Initial text, updated dynamically
        confirmSelectionButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        confirmSelectionButton.setEnabled(false); // Disabled initially

        // Add action listeners to buttons
        enrollElectiveButton.addActionListener(e -> attemptEnrollment());
        dropElectiveButton.addActionListener(e -> attemptDrop());
        confirmSelectionButton.addActionListener(e -> confirmSelection());

        searchButton.addActionListener(e -> {
            String text = searchField.getText();
            String criterion = (String) searchCriteriaCombo.getSelectedItem();
            filterElectiveDisciplines(text, criterion); // Filter electives based on search
        });

        searchField.addActionListener(e -> { // Also trigger search on Enter key in search field
            String text = searchField.getText();
            String criterion = (String) searchCriteriaCombo.getSelectedItem();
            filterElectiveDisciplines(text, criterion);
        });

        electiveButtonsPanel.add(enrollElectiveButton);
        electiveButtonsPanel.add(dropElectiveButton);
        electiveButtonsPanel.add(confirmSelectionButton);
        electivePanel.add(electiveButtonsPanel, BorderLayout.SOUTH);

        // Panel for Enrolled Electives
        JPanel enrolledElectivePanel = new JPanel(new BorderLayout(5, 5));
        enrolledElectivePanel.setBorder(BorderFactory.createTitledBorder("Обрані вибіркові дисципліни"));
        enrolledElectiveListModel = new DefaultListModel<>();
        enrolledElectiveList = new JList<>(enrolledElectiveListModel);
        enrolledElectiveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrolledElectiveList.setCellRenderer(new DisciplineListRenderer()); // Apply custom renderer
        enrolledElectiveList.setFont(new Font("Segoe UI", Font.BOLD, 13));
        enrolledElectivePanel.add(new JScrollPane(enrolledElectiveList), BorderLayout.CENTER);
        enrolledElectiveList.addMouseListener(new DisciplineInfoMouseAdapter()); // Add mouse listener for info dialogs

        // JSplitPane to divide available electives and enrolled electives horizontally
        JSplitPane electiveSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        electiveSplitPane.setResizeWeight(0.5); // Equal split
        electiveSplitPane.setLeftComponent(electivePanel);
        electiveSplitPane.setRightComponent(enrolledElectivePanel);

        // Special handling for 1st year Bachelor's students (no electives)
        if ("Бакалаврат".equals(selectedDegree) && selectedCourse == 1) {
            enrollElectiveButton.setEnabled(false);
            dropElectiveButton.setEnabled(false);
            searchField.setEnabled(false);
            searchButton.setEnabled(false);

            // Display message instead of elective list
            JLabel noElectivesMessage = new JLabel("На 1-ому курсі бакалаврату немає можливости обирати додаткові дисципліни", SwingConstants.CENTER);
            noElectivesMessage.setFont(new Font("Segoe UI", Font.BOLD, 14));
            noElectivesMessage.setForeground(RED.darker());

            electivePanel.removeAll();
            electivePanel.setLayout(new BorderLayout());
            electivePanel.add(noElectivesMessage, BorderLayout.CENTER);
            electivePanel.add(electiveButtonsPanel, BorderLayout.SOUTH); // Keep buttons visible
            electivePanel.setBorder(BorderFactory.createTitledBorder("Вибіркові дисципліни (недоступно)"));

            enrolledElectivePanel.removeAll();
            enrolledElectivePanel.setLayout(new BorderLayout());
            enrolledElectivePanel.add(new JLabel("Ваші обрані вибіркові дисципліни будуть відображені з 2-го курсу", SwingConstants.CENTER), BorderLayout.CENTER);
            ((JLabel) enrolledElectivePanel.getComponent(0)).setFont(new Font("Segoe UI", Font.PLAIN, 12));
            ((JLabel) enrolledElectivePanel.getComponent(0)).setForeground(BLACK.darker());
            enrolledElectivePanel.setBorder(BorderFactory.createTitledBorder("Обрані вибіркові дисципліни"));

            updateConfirmButtonState(); // Update confirm button state
        }

        topContentPanel.add(electiveSplitPane, BorderLayout.CENTER);

        // Mandatory Disciplines Panel (bottom part of the content)
        JPanel mandatoryPanel = new JPanel(new BorderLayout(5, 5));
        mandatoryPanel.setBorder(BorderFactory.createTitledBorder("Обов'язкові дисципліни"));
        mandatoryListModel = new DefaultListModel<>();
        mandatoryDisciplineList = new JList<>(mandatoryListModel);
        mandatoryDisciplineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mandatoryDisciplineList.setCellRenderer(new DisciplineListRenderer());
        mandatoryDisciplineList.setEnabled(false); // Mandatory disciplines are not selectable
        mandatoryDisciplineList.setFont(new Font("Segoe UI", Font.BOLD, 13));
        mandatoryPanel.add(new JScrollPane(mandatoryDisciplineList), BorderLayout.CENTER);
        mandatoryDisciplineList.addMouseListener(new DisciplineInfoMouseAdapter()); // Add mouse listener for info dialogs

        // --- Apply Sims-inspired Colors ---
        Color simsPink = new Color(255, 233, 243, 255);
        Color simsAccent = new Color(66, 244, 180); // A green-ish accent
        Color simsLightPink = new Color(255, 251, 253);

        mainPanel.setBackground(simsPink);
        studentInfoPanel.setBackground(simsPink);
        searchPanel.setBackground(simsPink);
        electiveButtonsPanel.setBackground(simsPink);
        mandatoryPanel.setBackground(simsPink);
        electivePanel.setBackground(simsPink);
        enrolledElectivePanel.setBackground(simsPink);
        topContentPanel.setBackground(simsPink);

        enrollElectiveButton.setBackground(simsAccent);
        dropElectiveButton.setBackground(simsAccent);
        confirmSelectionButton.setBackground(simsAccent);
        searchButton.setBackground(simsAccent);

        electiveDisciplineList.setBackground(simsLightPink);
        enrolledElectiveList.setBackground(simsLightPink);
        mandatoryDisciplineList.setBackground(simsLightPink);

        // Set title color for titled borders
        ((javax.swing.border.TitledBorder) electivePanel.getBorder()).setTitleColor(BLACK);
        ((javax.swing.border.TitledBorder) enrolledElectivePanel.getBorder()).setTitleColor(BLACK);
        ((javax.swing.border.TitledBorder) mandatoryPanel.getBorder()).setTitleColor(BLACK);

        mainPanel.add(topContentPanel, BorderLayout.CENTER);
        mainPanel.add(mandatoryPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Pause button setup
        PauseAction pauseAction = new PauseAction(""); // Assuming PauseAction constructor
        JButton pauseButton = new JButton(pauseAction);
        ImageIcon iconBtn = new ImageIcon(getClass().getResource( "/button/pause.png"));
        Image scaledImage = iconBtn.getImage().getScaledInstance(140, 30, Image.SCALE_SMOOTH);
        iconBtn = new ImageIcon(scaledImage);
        pauseButton.setIcon(iconBtn);
        pauseButton.setContentAreaFilled(false);
        pauseButton.setBorderPainted(false);
        pauseButton.setFocusPainted(false);
        pauseButton.setOpaque(false);

        JPanel topBar = new JPanel();
        topBar.setOpaque(false);
        topBar.add(pauseButton);
        add(topBar, BorderLayout.NORTH); // Add pause button to the top of the frame

        setVisible(true); // Make the main window visible

        // Initial updates for UI elements
        updateDisciplineLists();
        updateStudentInfoDisplay();
        updateConfirmButtonState();

        // Timer for automatic virtual student enrollment simulation
        autoEnrollTimer = new Timer(0, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Stop the timer if max virtual students have completed their picks
                if (enrollmentSystem.getVirtualStudentsEnrolledCount() >= EnrollmentSystem.MAX_VIRTUAL_STUDENTS_TO_ENROLL) {
                    autoEnrollTimer.stop();
                    return;
                }
                enrollmentSystem.randomlyIncrementElectiveEnrollment(); // Simulate enrollment
                electiveDisciplineList.repaint(); // Repaint lists to show changes
                enrolledElectiveList.repaint();
            }
        });
        autoEnrollTimer.start(); // Start the auto-enrollment simulation

        // Custom window close listener for confirmation
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                UIManager.put("OptionPane.yesButtonText", "Так"); // Set custom text for Yes/No buttons
                UIManager.put("OptionPane.noButtonText", "Ні");

                int confirm = JOptionPane.showConfirmDialog(
                        EnrollmentSystemGUI.this,
                        "Ви дійсно хочете завершити поточну гру?",
                        "Завершити гру",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    if (autoEnrollTimer != null) {
                        autoEnrollTimer.stop(); // Stop the timer before closing
                    }
                    EnrollmentSystemGUI.this.dispose();
                }
            }
        });
    }

    /**
     * Displays an introductory instruction dialog to the user at the start of the GUI.
     * The instructions explain how to use the enrollment system.
     */
    private void showInstructionsDialog() {
        String instructions = "<html>" +
                "<body style='font-family: \"Arial\"; font-size: 13px; color: " + toHex(SIMS_DARK_TEXT) + ";'>" +
                "<h1 style='color: " + toHex(Color.BLACK) + ";'>Інструкція до запису на вибіркові дисципліни</h1>" +
                "<p>Ласкаво просимо до Системи автоматизованого запису (САЗ) НаУКМА!</p>" +
                "<p>Будь ласка, дотримуйтеся цих кроків для успішного запису!</p>" +
                "<ol>" +
                "<li><b>Виберіть свій освітній ступінь</b> (Бакалаврат/Магістратура).</li>" +
                "<li><b>Виберіть свій курс</b> відповідно до обраного ступеня.</li>" +
                "<li>У лівому списку <b>«Вибіркові дисципліни»</b> Ви знайдете предмети, доступні для запису.</li>" +
                "<li>Виберіть дисципліну з переліку і натисніть <b>«Записатися»</b>, щоб додати її до своїх обраних.</li>" +
                "<li>У правому списку <b>«Обрані вибіркові дисципліни»</b> Ви побачите предмети, на які вже записалися.</li>" +
                "<li>Щоб виписатися з дисципліни, виберіть її з правого списку та натисніть <b>«Виписатися»</b>.</li>" +
                "<li><b>Обов'язкові дисципліни</b> відображаються в нижньому списку, вони записуються автоматично і не можуть бути змінені.</li>" +
                "<li>Ваша <b>загальна кількість кредитів</b> відображається у верхній частині вікна та на кнопці «Готово».</li>" +
                "<li>Вам потрібно набрати мінімум 55 кредитів</b>. Кнопка «Готово» стане активною, коли ви досягнете цього мінімуму.</li>" +
                "<li>Натисніть <b>«Готово»</b>, щоб підтвердити свій вибір.</li>" +
                "</ol>" +
                "<p><b>Успішного запису!</b></p>" +
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
     * Converts a {@link Color} object to its hexadecimal string representation.
     * This is useful for embedding colors directly into HTML strings for JEditorPane.
     * @param color The {@link Color} object to convert.
     * @return A hexadecimal string representing the color (e.g., "#RRGGBB").
     */
    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Displays a warning dialog to the user when they attempt to "cheat" the system
     * (e.g., selecting a degree/course that doesn't match their current student data).
     * This deducts energy from the hero.
     */
    private void showWarningDialog(){
        MusicPlayer.getInstance().playError(); // Play an error sound
        JOptionPane.showMessageDialog(this, "За намагання обдурити систему ваш сім втрачає 20 очок енергії! Обирайте правильні дані!", "УВАГА!", JOptionPane.WARNING_MESSAGE);
        hero.setEnergy(hero.getEnergy()-20); // Deduct energy from the hero
    }

    /**
     * Displays a dialog prompting the user to select their educational degree (Bachelor's or Master's).
     * Includes a check to ensure the selected degree is consistent with the student's current course.
     * If inconsistency is detected, a warning is shown, and the degree is corrected to match the student.
     *
     * @return The selected degree string ("Бакалаврат" or "Магістратура"), or null if canceled.
     */
    private String showDegreeSelectionDialog() {
        String[] degrees = {"Бакалаврат", "Магістратура"};

        String selectedDegreeStr = (String) JOptionPane.showInputDialog(
                this,
                "Будь ласка, оберіть ваш освітній ступінь!",
                "Вибір освітнього ступеня",
                JOptionPane.QUESTION_MESSAGE,
                null,
                degrees,
                degrees[0]); // Default selection

        // Logic to prevent "cheating" by selecting incorrect degree
        if (selectedDegreeStr != null) { // If user didn't cancel
            if (selectedDegreeStr.equals("Бакалаврат")) {
                if (student.getCourse() > 4) { // Bachelor's courses are typically 1-4
                    showWarningDialog();
                    selectedDegreeStr = "Магістратура"; // Force to Master's if student is in higher course
                }
            } else if (selectedDegreeStr.equals("Магістратура")) {
                if (student.getCourse() <= 4) { // Master's courses are typically 5-6 (or 1-2 for Master's specifically)
                    showWarningDialog();
                    selectedDegreeStr = "Бакалаврат"; // Force to Bachelor's if student is in lower course
                }
            }
        }
        System.out.println("Selected Degree: " + selectedDegreeStr);
        return selectedDegreeStr;
    }

    /**
     * Displays a dialog prompting the user to select their academic course (year)
     * based on their previously chosen degree.
     * Includes a check to ensure the selected course matches the student's actual course.
     * If inconsistency is detected, a warning is shown, and the course is corrected.
     *
     * @param degree The previously selected educational degree ("Бакалаврат" or "Магістратура").
     * @return The selected course number, or -1 if canceled or an error occurs.
     */
    private int showCourseSelectionDialog(String degree) {
        String[] courses;
        String dialogTitle;
        String dialogMessage;

        if ("Бакалаврат".equals(degree)) {
            courses = new String[]{"1", "2", "3", "4"};
            dialogTitle = "Вибір курсу (бакалаврат)";
            dialogMessage = "Будь ласка, оберіть ваш курс бакалаврату!";
        } else if ("Магістратура".equals(degree)) {
            courses = new String[]{"1", "2"}; // Master's courses are typically 1st and 2nd year of magistracy
            dialogTitle = "Вибір курсу (магістратура)";
            dialogMessage = "Будь ласка, оберіть ваш курс магістратури!";
        } else {
            JOptionPane.showMessageDialog(this, "Неправильний освітній ступінь.", "Помилка", JOptionPane.ERROR_MESSAGE);
            return -1; // Indicate error
        }

        String selectedCourseStr = (String) JOptionPane.showInputDialog(
                this,
                dialogMessage,
                dialogTitle,
                JOptionPane.QUESTION_MESSAGE,
                null,
                courses,
                courses[0]); // Default selection

        int selectedCourse = -1;

        if (selectedCourseStr != null) {
            try {
                selectedCourse = Integer.parseInt(selectedCourseStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Неправильний формат курсу. Спробуйте ще раз.", "Помилка", JOptionPane.ERROR_MESSAGE);
                return showCourseSelectionDialog(degree); // Recursively ask again if format error
            }
        }

        // Logic to prevent "cheating" by selecting incorrect course
        // Note: student.getCourse() presumably returns the actual current course,
        // where 5 and 6 might represent 1st and 2nd year Master's respectively.
        if (selectedCourse != student.getCourse()) {
            showWarningDialog();
            if (student.getCourse() == 5) {
                selectedCourse = 1; // If student is actually 5th year, force selected course to 1 (for Master's context)
            } else if (student.getCourse() == 6) {
                selectedCourse = 2; // If student is actually 6th year, force selected course to 2 (for Master's context)
            } else {
                selectedCourse = student.getCourse(); // Otherwise, force to actual course
            }
        }
        return selectedCourse;
    }

    /**
     * Displays a short animated sequence (e.g., "Старт!", "Увага!", "Руш!")
     * before the main GUI becomes fully interactive.
     */
    private void showStartAnimation() {
        JDialog animationDialog = new JDialog(this, true); // Modal dialog
        animationDialog.setUndecorated(true); // No window decorations

        // Define colors for the animation
        Color simsPink = new Color(255, 168, 205, 255);
        Color simsAccent1 = new Color(66, 244, 180);
        Color simsAccent2 = new Color(34, 149, 107);
        Color simsAccent3 = new Color(66, 244, 191);

        animationDialog.getContentPane().setBackground(simsPink); // Dialog background

        JLabel animationLabel = new JLabel("", SwingConstants.CENTER); // Label to display animation text
        animationLabel.setFont(new Font("Segoe UI", Font.BOLD, 80));
        animationLabel.setForeground(simsAccent1);

        JPanel contentPanel = new JPanel(new GridBagLayout()); // Centering panel
        contentPanel.setOpaque(false);
        contentPanel.add(animationLabel);

        animationDialog.setContentPane(contentPanel);
        animationDialog.setSize(400, 200); // Fixed size for animation
        animationDialog.setLocationRelativeTo(this); // Center relative to parent frame

        final String[] phases = {"Старт!", "Увага!", "Руш!"}; // Animation phases
        final Color[] textColors = {simsAccent1, simsAccent2, simsAccent3}; // Colors for each phase
        final int[] currentPhase = {0}; // Counter for current animation phase

        // Timer to control the animation sequence
        Timer timer = new Timer(150, new ActionListener() { // Fires every 150ms
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentPhase[0] < phases.length) {
                    animationLabel.setText(phases[currentPhase[0]]); // Set text
                    animationLabel.setForeground(textColors[currentPhase[0]]); // Set color
                    currentPhase[0]++; // Move to next phase
                } else {
                    ((Timer) e.getSource()).stop(); // Stop timer when all phases complete
                    animationDialog.dispose(); // Close the animation dialog
                }
            }
        });
        timer.setInitialDelay(50); // Short initial delay
        timer.start(); // Start the animation timer

        animationDialog.setVisible(true); // Make the animation dialog visible
    }

    /**
     * Initializes the discipline data for Bachelor's degree programs based on the given course.
     * This method adds predefined mandatory and elective disciplines to the `enrollmentSystem`.
     *
     * @param selectedCourse The academic course for which to initialize data.
     */
    private void initializeInitialDataB(int selectedCourse) {
        enrollmentSystem.addStudent(student);

        if ((student.getMajor().equals("Інженерія програмного забезпечення")) && (selectedCourse == 1)) {
            enrollmentSystem.addDiscipline(new Discipline("315203", "Алгоритми і структури даних", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М., ст. викл. Кирієнко О. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("314809", "Англійська мова", "ст. викл. Гісем С. О.", 7, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315275", "Вступ до програмування", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315213", "Диференціальні рівняння", "доц. Митник Ю. В., ст. викл. Силенко І. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315231", "Комп'ютерні архітектури", "ст. викл. Медвідь С. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315200", "Лінійна алгебра та аналітична геометрія", "доц. Чорней Р. К., ас. Сарана М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315223", "Моделі обчислень в програмній інженерії", "доц. Проценко В. С.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315283", "Основи вебтехнологій", "ст. викл. Зважій Д. В. ", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("315202", "Основи дискретної математики", "ст. викл. Щеглов М. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315199", "Основи матаналізу", "ст. викл. Іванюк А. О.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315277", "Основи мережевих технологій", "ст. викл. Вознюк О. М., ст. викл. Вознюк Я. І.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("314810", "Українська мова", "доц. Сегін Л. В., ст. викл. Калиновська О. М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314804", "Фізичне виховання", "викл. Жуков В. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315204", "Практика навчальна", "ст. викл. Пєчкурова О. М., ст. викл. Кирієнко О. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        }

        else if ((student.getMajor().equals("Інженерія програмного забезпечення")) && (selectedCourse == 2)) {
            enrollmentSystem.addDiscipline(new Discipline("340214", "Англійська мова (за проф. спрямуванням)", "проф. Дерев'янко А. С.", 7, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340447", "Бази даних", "ст. викл. Кушнір О. В.", 8, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("340443", "Вступ до тестування програмного забезпечення", "доц. Сидоров М. І.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340418", "Об'єктно-орієнтоване програмування", "проф. Бублик В. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("340370", "Основи комп'ютерних алгоритмів", "проф. Іванов Л. М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("340362", "Побудова і використання комунікаційних мереж", "ст. викл. Черкасов Д. І., ст. викл. Савченко Т. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340356", "Процедурне програмування (на базі Сі/Сі++) (ПІ)", "проф. Бублик В. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340377", "Теорія алгоритмів і математична логіка", "проф. Сахаров Г. М.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("315366", "Практика дослідницька", "ст. викл. Пєчкурова О. М., ст. викл. Кирієнко О. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));

            enrollmentSystem.addDiscipline(new Discipline("315491", "Обчислювальне суспільствознавство", "проф. Глибовець А. М.", 2.5, EnrollmentSystem.ELECTIVE_CAPACITY, 43, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("344466", "Вступ до ігрової розробки", "проф. Глибовець А. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("344467", "Комп'ютерна мережа Інтернет", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 45, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("344468", "Локальні мережі", "ст. викл. Вознюк Я. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 54, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340466", "Архітектура ПЕОМ", "проф. Глибовець А. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 57, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("316200", "Додаткові розділи теорії графів", "ст. викл. Козеренко С. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 35, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("319949", "Основи теорії груп", "ст. викл. Козеренко С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 57, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315185", "Спектральна теорія графів", "ст. викл. Тимошкевич Л. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 26, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340456", "Математичне мислення", "ст. викл. Щеглов М. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 56, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("320163", "Обчислювальна геометрія", "доц. Чорней Р. К.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 50, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("320012", "Мова розмітки LaTeX", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 37, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("316847", "Математичні методи в хімії", "доц. Будзінська В. Л., ас. Носач В. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 32, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("316161", "Символьні обчислення", "ст. викл. Прокоф'єв П. Г.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 24, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340446", "Додаткові розділи алгебри", "доц. Чорней Р. К.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 62, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340445", "Додаткові розділи дискретної математики", "ст. викл. Щеглов М. В.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340439", "Автоматизація роботи з програмними проєктами мовою Java", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 18, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340393", "Інформаційний пошук", "доц. Мельничук А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, 14, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315191", "Методи та засоби збору чутливої інформації", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315306", "Мова програмування Kotlin", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315261", "Програмування на C#", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 39, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340402", "Розробка клієнт-серверних застосувань", "проф. Гришко О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 59, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340333", "Управління цифровим продуктом", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340334", "Обробка зображень", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 53, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340335", "Життя у цифровому світі", "доц. Сидоренко А. К.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 15, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340336", "Введення у Хмарні технології", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 44, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340337", "Мемологічні студії", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 19, false, 2));

        } else if ((student.getMajor().equals("Інженерія програмного забезпечення")) && (selectedCourse == 3)) {
            enrollmentSystem.addDiscipline(new Discipline("315429", "Функціональне програмування", "проф. Математикус А. Б.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("315225", "Системне програмування", "доц. Жежерун О. П.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("315425", "Логічне програмування", "ст. викл. В.І. Системний", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("315218", "Теорія ймовірностей", "ст. викл. Братик М. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("315217", "Вебпрограмування", "ст. викл. Олецький О. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("315198", "Курсова робота", "ст. викл. Логік Т.О.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));

            enrollmentSystem.addDiscipline(new Discipline("316208", "Вступ до загальної топології", "ст. викл. Козеренко С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 47, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316210", "Загальна топологія", "ст. викл. Козеренко С.О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 26, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316156", "Актуарна математика", "ст. викл. Братик М. В.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("317933", "Обчислюваність", "проф. Олійник Б. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 36, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316192", "Статистичні основи вебаналітики", "доц. Крюкова Г. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 14, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315490", "Дані та суспільство", "доц. Крюкова Г. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 43, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("314810", "Експлуатація розподіленої хмарної інфраструктури та сервісів (DevOps)", "ст. викл. Сидорчук Л. Н.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316195", "Візуалізація та комп'ютерна графіка", "доц. Крюковчук Г. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 38, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315491", "Обчислювальне суспільствознавство", "проф. Глибовець А. М.", 2.5, EnrollmentSystem.ELECTIVE_CAPACITY, 43, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("344466", "Вступ до ігрової розробки", "проф. Глибовець А. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("344467", "Комп'ютерна мережа Інтернет", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 45, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("344468", "Локальні мережі", "ст. викл. Вознюк Я. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 54, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340466", "Архітектура ПЕОМ", "проф. Глибовець А. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 57, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316200", "Додаткові розділи теорії графів", "ст. викл. Козеренко С. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 35, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("319949", "Основи теорії груп", "ст. викл. Козеренко С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 57, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315185", "Спектральна теорія графів", "ст. викл. Тимошкевич Л. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 26, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340456", "Математичне мислення", "ст. викл. Щеглов М. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 56, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("320163", "Обчислювальна геометрія", "доц. Чорней Р. К.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 50, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("320012", "Мова розмітки LaTeX", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 37, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316847", "Математичні методи в хімії", "доц. Будзінська В. Л., ас. Носач В. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 32, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316161", "Символьні обчислення", "ст. викл. Прокоф'єв П. Г.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 24, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340446", "Додаткові розділи алгебри", "доц. Чорней Р. К.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 62, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340445", "Додаткові розділи дискретної математики", "ст. викл. Щеглов М. В.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340439", "Автоматизація роботи з програмними проєктами мовою Java", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 18, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340393", "Інформаційний пошук", "доц. Мельничук А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, 14, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315191", "Методи та засоби збору чутливої інформації", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315306", "Мова програмування Kotlin", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315261", "Програмування на C#", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 39, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340402", "Розробка клієнт-серверних застосувань", "проф. Гришко О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 59, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340333", "Управління цифровим продуктом", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340334", "Обробка зображень", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 53, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340335", "Життя у цифровому світі", "доц. Сидоренко А. К.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 15, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340336", "Введення у Хмарні технології", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 44, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340337", "Мемологічні студії", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 19, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315293", "Інформаційна безпека вебзастосунків", "ст. викл. Бабич Т. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 18, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315279", "Інструменти та принципи веброзробки", "ст. викл. Зважій Д. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 31, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Архітектура високонавантажених систем", "ст. викл. Ведель К. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 27, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315245", "Пошукова оптимізація вебзастосувань", "ст. викл. Цуд В. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 44, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316198", "Комп'ютерна вірусологія", "ст. викл. Кирієнко О. В., ст. викл. Пєчкурова О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 21, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340384", "Архітектура прикладних програм рівня підприємства", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 52, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340303", "Глобальні мережі", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 19, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("314833", "Низькорівневі вразливості програмного забезпечення", "ст. викл. Коренчук А. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 37, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315253", "Кібербезпека", "ст. викл. Вознюк Я. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 53, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315284", "Практичні аспекти роботи з базами даних в Spring Boot", "ст. викл. Андрощук М. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340321", "Технології обчислювального експерименту", "ст. викл. Міхновський О. Л.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 24, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315211", "Програмування на основі .NET", "ст. викл. Борозенний С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 29, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315280", "Backend-розробка на базі NodeJS", "ст. викл. Петлюра С. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 33, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("322535", "Алгоритми паралельних обчислень", "доц. Винниченко В. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 12, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315246", "Технології електронних видань", "ст. викл. Афонін А. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 42, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315838", "Дизайн вебінтерфейсів", "ст. викл. Яковлєв М.В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 46, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315479", "Інформаційна безпека мереж", "ст. викл. Коновалець Є. П.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 39, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316821", "Банківські комп'ютерні системи", "д. е. н., доц. Гладких Д. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 61, false, 3));

        } else if ((student.getMajor().equals("Інженерія програмного забезпечення")) && (selectedCourse == 4)) {
            enrollmentSystem.addDiscipline(new Discipline("315263", "Багатозадачне та паралельне програмування", "ст. викл. Сідько А. А., ст. викл. Гречко А. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("315222", "Проєктування програмних систем", "ст. викл. Афонін А. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("315230", "Об'єктно-орієнтований аналіз і дизайн", "ст. викл. Мобільний Д. С.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("315233", "Структура програмних проєктів", "проф. Керівник В. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("315224", "Інтелектуальні системи", "ст. викл. Жежерун О. П.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("315215", "Забезпечення якости програмних продуктів", "ст. викл. Афонін А. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4, "Екзамен"));

            enrollmentSystem.addDiscipline(new Discipline("316213", "Основи фінансової математики / Basics of Financial Mathematics (англ. мовою)", "доц. Щестюк Н. Ю.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 12, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315991", "Системи та методи ухвалення рішеннь", "проф. Глибовець А. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 12, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316208", "Вступ до загальної топології", "ст. викл. Козеренко С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 47, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316210", "Загальна топологія", "ст. викл. Козеренко С.О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 26, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316156", "Актуарна математика", "ст. викл. Братик М. В.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("317933", "Обчислюваність", "проф. Олійник Б. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 36, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316192", "Статистичні основи вебаналітики", "доц. Крюкова Г. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 14, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315490", "Дані та суспільство", "доц. Крюкова Г. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 43, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("314810", "Експлуатація розподіленої хмарної інфраструктури та сервісів (DevOps)", "ст. викл. Сидорчук Л. Н.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316195", "Візуалізація та комп'ютерна графіка", "доц. Крюковчук Г. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 38, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315491", "Обчислювальне суспільствознавство", "проф. Глибовець А. М.", 2.5, EnrollmentSystem.ELECTIVE_CAPACITY, 43, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("344466", "Вступ до ігрової розробки", "проф. Глибовець А. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("344467", "Комп'ютерна мережа Інтернет", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 45, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("344468", "Локальні мережі", "ст. викл. Вознюк Я. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 54, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340466", "Архітектура ПЕОМ", "проф. Глибовець А. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 57, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316200", "Додаткові розділи теорії графів", "ст. викл. Козеренко С. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 35, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("319949", "Основи теорії груп", "ст. викл. Козеренко С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 57, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315185", "Спектральна теорія графів", "ст. викл. Тимошкевич Л. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 26, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340456", "Математичне мислення", "ст. викл. Щеглов М. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 56, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("320163", "Обчислювальна геометрія", "доц. Чорней Р. К.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 50, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("320012", "Мова розмітки LaTeX", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 37, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316847", "Математичні методи в хімії", "доц. Будзінська В. Л., ас. Носач В. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 32, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316161", "Символьні обчислення", "ст. викл. Прокоф'єв П. Г.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 24, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340446", "Додаткові розділи алгебри", "доц. Чорней Р. К.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 62, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340445", "Додаткові розділи дискретної математики", "ст. викл. Щеглов М. В.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340439", "Автоматизація роботи з програмними проєктами мовою Java", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 18, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340393", "Інформаційний пошук", "доц. Мельничук А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, 14, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315191", "Методи та засоби збору чутливої інформації", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315306", "Мова програмування Kotlin", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315261", "Програмування на C#", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 39, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340402", "Розробка клієнт-серверних застосувань", "проф. Гришко О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 59, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340333", "Управління цифровим продуктом", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340334", "Обробка зображень", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 53, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340335", "Життя у цифровому світі", "доц. Сидоренко А. К.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 15, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340336", "Введення у Хмарні технології", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 44, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340337", "Мемологічні студії", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 19, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315293", "Інформаційна безпека вебзастосунків", "ст. викл. Бабич Т. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 18, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315279", "Інструменти та принципи веброзробки", "ст. викл. Зважій Д. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 31, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Архітектура високонавантажених систем", "ст. викл. Ведель К. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 27, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315245", "Пошукова оптимізація вебзастосувань", "ст. викл. Цуд В. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 44, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316198", "Комп'ютерна вірусологія", "ст. викл. Кирієнко О. В., ст. викл. Пєчкурова О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 21, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340384", "Архітектура прикладних програм рівня підприємства", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 52, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340303", "Глобальні мережі", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 19, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("314833", "Низькорівневі вразливості програмного забезпечення", "ст. викл. Коренчук А. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 37, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315253", "Кібербезпека", "ст. викл. Вознюк Я. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 53, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315284", "Практичні аспекти роботи з базами даних в Spring Boot", "ст. викл. Андрощук М. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340321", "Технології обчислювального експерименту", "ст. викл. Міхновський О. Л.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 24, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315211", "Програмування на основі .NET", "ст. викл. Борозенний С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 29, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315280", "Backend-розробка на базі NodeJS", "ст. викл. Петлюра С. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 33, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("322535", "Алгоритми паралельних обчислень", "доц. Винниченко В. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 12, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315246", "Технології електронних видань", "ст. викл. Афонін А. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 42, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315838", "Дизайн вебінтерфейсів", "ст. викл. Яковлєв М.В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 46, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315479", "Інформаційна безпека мереж", "ст. викл. Коновалець Є. П.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 39, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316821", "Банківські комп'ютерні системи", "д. е. н., доц. Гладких Д. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 61, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("313821", "Стандарти обміну даних в Інтернет", "д. е. н., доц. Гладких Д. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 42, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316472", "Методи та засоби офісного програмування", "д. е. н., доц. Гладких Д. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 25, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315295", "Інформаційна безпека цільових систем", "ст. викл. Чередарчук А. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 39, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315221", "Технологія XML", "ст. викл. Захоженко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 31, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315241", "Розробка Frontend-додатків на JavaScript", "проф. Глибовець А. М., ст. викл. Бабич Т. А.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("318273", "Алгоритми комп'ютерної алгебри", "ст. викл. Смиш О. Р.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315297", "Вибрані фреймворки для iOS", "ст. викл. Франків О. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 19, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315206", "Комп'ютерна графіка", "проф. Липинський І. С.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 28, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315464", "Кваліфікаційна робота", "проф. Глибовець А. М.", 12, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));

        } else if ((student.getMajor().equals("Прикладна математика")) && (selectedCourse == 1)){
            enrollmentSystem.addDiscipline(new Discipline("314809", "Англійська мова", "ст. викл. Гісем С. О.", 6, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315275", "Програмування", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("315213", "Історія математики", "доц. Митник Ю. В., ст. викл. Силенко І. В.", 10, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315200", "Алгебра та геометрія", "доц. Чорней Р. К., ас. Сарана М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("315202", "Дискретна математика", "ст. викл. Щеглов М. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("315199", "Математичний аналіз", "ст. викл. Іванюк А. О.", 10, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314810", "Українська мова", "доц. Сегін Л. В., ст. викл. Калиновська О. М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314804", "Фізичне виховання", "викл. Жуков В. О.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315200", "Лінійна алгебра та аналітична геометрія", "доц. Чорней Р. К., ас. Сарана М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315223", "Моделі обчислень в програмній інженерії", "доц. Проценко В. С.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));

        }

        else if ((student.getMajor().equals("Прикладна математика")) && (selectedCourse == 2)){
            enrollmentSystem.addDiscipline(new Discipline("314809", "Англійська мова", "ст. викл. Гісем С. О.", 3.5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("315275", "Програмування", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М.", 7, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("315200", "Алгебра та геометрія", "доц. Чорней Р. К., ас. Сарана М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("315199", "Математичний аналіз", "ст. викл. Іванюк А. О.", 11, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("315434", "Бази даних та інформаційні системи", "проф. Глибовець А. М.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("316215", "Випадкові процеси", "доц. Чорней Р.К., ас. Тищенко С.В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("316214", "Теорія ймовірностей", "доц. Чорней Р.К., ас. Тищенко С.В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("315213", "Диференціальні рівняння", "доц. Митник Ю. В., ст. викл. Силенко І. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));

            enrollmentSystem.addDiscipline(new Discipline("340469", "Комп'ютерні мережі", "ст. викл. Вознюк Я. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 35, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340468", "Процедурне програмування (на базі Ci/Сi++)", "проф. Глибовець А. М.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 49, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340467", "Об`єктно-орієнтоване програмування", "проф. Глибовець А. М.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 53, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315491", "Обчислювальне суспільствознавство", "проф. Глибовець А. М.", 2.5, EnrollmentSystem.ELECTIVE_CAPACITY, 18, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("344466", "Вступ до ігрової розробки", "проф. Глибовець А. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 44, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("344467", "Комп'ютерна мережа Інтернет", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 25, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("344468", "Локальні мережі", "ст. викл. Вознюк Я. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 13, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340466", "Архітектура ПЕОМ", "проф. Глибовець А. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("316200", "Додаткові розділи теорії графів", "ст. викл. Козеренко С. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 12, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("319949", "Основи теорії груп", "ст. викл. Козеренко С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315185", "Спектральна теорія графів", "ст. викл. Тимошкевич Л. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 16, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340456", "Математичне мислення", "ст. викл. Щеглов М. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 58, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("320163", "Обчислювальна геометрія", "доц. Чорней Р. К.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 29, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("320012", "Мова розмітки LaTeX", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY,  UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("316847", "Математичні методи в хімії", "доц. Будзінська В. Л., ас. Носач В. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY,  UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("316161", "Символьні обчислення", "ст. викл. Прокоф'єв П. Г.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 36, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340446", "Додаткові розділи алгебри", "доц. Чорней Р. К.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 17, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340445", "Додаткові розділи дискретної математики", "ст. викл. Щеглов М. В.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 33, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340439", "Автоматизація роботи з програмними проєктами мовою Java", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 43, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340393", "Інформаційний пошук", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, 20, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315191", "Методи та засоби збору чутливої інформації", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315306", "Мова програмування Kotlin", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315261", "Програмування на C#", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 14, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340402", "Розробка клієнт-серверних застосувань", "проф. Гришко О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 27, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340333", "Управління цифровим продуктом", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 19, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340334", "Обробка зображень", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 21, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340335", "Життя у цифровому світі", "доц. Сидоренко А. К.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 56, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340336", "Введення у Хмарні технології", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 31, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340337", "Мемологічні студії", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 50, false, 2));

        }

        else if ((student.getMajor().equals("Прикладна математика")) && (selectedCourse == 3)){
            enrollmentSystem.addDiscipline(new Discipline("316149", "Методи оптимізації та дослідження операцій", "доц. Михалевич В. М., ас. Тищенко С. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("316148", "Теорія алгоритмів та математична логіка", "ст. викл. Прокоф'єв П. Г.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("316209", "Теорія функції комплексної змінної", "доц. Кашпіровський О. І.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("316157", "Чисельні методи", "доц. Кашпіровський О. І., ст. викл. Прокоф'єв П. Г.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("316165", "Математична статистика", "доц. Шпортюк В. Г.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("315198", "Курсова робота", "ст. викл. Логік Т.О.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("316147", "Рівняння математичної фізики", "проф. Авраменко О. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("316149", "Методи оптимізації та дослідження операцій", "доц. Михалевич В. М., ас. Тищенко С. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("315205", "Практика дослідницька", "доц. Шпортюк В. Г.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));

            enrollmentSystem.addDiscipline(new Discipline("340469", "Комп'ютерні мережі", "ст. викл. Вознюк Я. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340468", "Процедурне програмування (на базі Ci/Сi++)", "проф. Глибовець А. М.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340467", "Об`єктно-орієнтоване програмування", "проф. Глибовець А. М.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 3));

            enrollmentSystem.addDiscipline(new Discipline("340469", "Комп'ютерні мережі", "ст. викл. Вознюк Я. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 24, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340468", "Процедурне програмування (на базі Ci/Сi++)", "проф. Глибовець А. М.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340467", "Об'єктно-орієнтоване програмування", "проф. Глибовець А. М.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 32, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316208", "Вступ до загальної топології", "ст. викл. Козеренко С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 18, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316210", "Загальна топологія", "ст. викл. Козеренко С.О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 20, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316156", "Актуарна математика", "ст. викл. Братик М. В.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("317933", "Обчислюваність", "проф. Олійник Б. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 28, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316192", "Статистичні основи вебаналітики", "доц. Крюкова Г. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 19, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315490", "Дані та суспільство", "доц. Крюкова Г. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 16, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("314810", "Експлуатація розподіленої хмарної інфраструктури та сервісів (DevOps)", "ст. викл. Сидорчук Л. Н.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316195", "Візуалізація та комп'ютерна графіка", "доц. Крюковчук Г. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 35, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315491", "Обчислювальне суспільствознавство", "проф. Глибовець А. М.", 2.5, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("344466", "Вступ до ігрової розробки", "проф. Глибовець А. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 30, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("344467", "Комп'ютерна мережа Інтернет", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 26, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("344468", "Локальні мережі", "ст. викл. Вознюк Я. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340466", "Архітектура ПЕОМ", "проф. Глибовець А. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 14, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316200", "Додаткові розділи теорії графів", "ст. викл. Козеренко С. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("319949", "Основи теорії груп", "ст. викл. Козеренко С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 38, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315185", "Спектральна теорія графів", "ст. викл. Тимошкевич Л. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 15, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340456", "Математичне мислення", "ст. викл. Щеглов М. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 33, false, 3));

            enrollmentSystem.addDiscipline(new Discipline("320163", "Обчислювальна геометрія", "доц. Чорней Р. К.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 29, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("320012", "Мова розмітки LaTeX", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY,  UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316847", "Математичні методи в хімії", "доц. Будзінська В. Л., ас. Носач В. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY,  UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316161", "Символьні обчислення", "ст. викл. Прокоф'єв П. Г.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 36, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340446", "Додаткові розділи алгебри", "доц. Чорней Р. К.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 17, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340445", "Додаткові розділи дискретної математики", "ст. викл. Щеглов М. В.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 33, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340439", "Автоматизація роботи з програмними проєктами мовою Java", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 43, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340393", "Інформаційний пошук", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, 20, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315191", "Методи та засоби збору чутливої інформації", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315306", "Мова програмування Kotlin", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315261", "Програмування на C#", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 14, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340402", "Розробка клієнт-серверних застосувань", "проф. Гришко О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 27, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340333", "Управління цифровим продуктом", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 19, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340334", "Обробка зображень", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 21, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340335", "Життя у цифровому світі", "доц. Сидоренко А. К.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 56, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340336", "Введення у Хмарні технології", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 31, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340337", "Мемологічні студії", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 50, false, 3));

            enrollmentSystem.addDiscipline(new Discipline("315293", "Інформаційна безпека вебзастосунків", "ст. викл. Бабич Т. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315279", "Інструменти та принципи веброзробки", "ст. викл. Зважій Д. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Архітектура високонавантажених систем", "ст. викл. Ведель К. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315245", "Пошукова оптимізація вебзастосувань", "ст. викл. Цуд В. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316198", "Комп'ютерна вірусологія", "ст. викл. Кирієнко О. В., ст. викл. Пєчкурова О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340384", "Архітектура прикладних програм рівня підприємства", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340303", "Глобальні мережі", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("314833", "Низькорівневі вразливості програмного забезпечення", "ст. викл. Коренчук А. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315253", "Кібербезпека", "ст. викл. Вознюк Я. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315284", "Практичні аспекти роботи з базами даних в Spring Boot", "ст. викл. Андрощук М. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340321", "Технології обчислювального експерименту", "ст. викл. Міхновський О. Л.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315211", "Програмування на основі .NET", "ст. викл. Борозенний С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315280", "Backend-розробка на базі NodeJS", "ст. викл. Петлюра С. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("322535", "Алгоритми паралельних обчислень", "доц. Винниченко В. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315246", "Технології електронних видань", "ст. викл. Афонін А. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315838", "Дизайн вебінтерфейсів", "ст. викл. Яковлєв М.В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315479", "Інформаційна безпека мереж", "ст. викл. Коновалець Є. П.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316821", "Банківські комп'ютерні системи", "д. е. н., доц. Гладких Д. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
        }

        else if ((student.getMajor().equals("Прикладна математика")) && (selectedCourse == 4)){
            enrollmentSystem.addDiscipline(new Discipline("316150", "Теорія керування", "доц. Чорней Р. К.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("316819", "Економетрика", "к. е. н., доц. Семко Р. Б.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("316141", "Функціональний аналіз", "доц. Кашпіровський О. І.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("316151", "Аналіз даних", "доц. Щестюк Н. Ю., ас. Тищенко С. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("316197", "Математичні методи машинного навчання", "ас. Сарана М. П.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("316199", "Практика переддипломна", "ст. викл. Афонін А. О.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));

            enrollmentSystem.addDiscipline(new Discipline("316213", "Основи фінансової математики / Basics of Financial Mathematics (англ. мовою)", "доц. Щестюк Н. Ю.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 16, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315991", "Системи та методи ухвалення рішеннь", "проф. Глибовець А. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 32, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315491", "Обчислювальне суспільствознавство", "проф. Глибовець А. М.", 2.5, EnrollmentSystem.ELECTIVE_CAPACITY, 28, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("344466", "Вступ до ігрової розробки", "проф. Глибовець А. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 24, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("344467", "Комп'ютерна мережа Інтернет", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("344468", "Локальні мережі", "ст. викл. Вознюк Я. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 44, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340466", "Архітектура ПЕОМ", "проф. Глибовець А. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 12, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316200", "Додаткові розділи теорії графів", "ст. викл. Козеренко С. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("319949", "Основи теорії груп", "ст. викл. Козеренко С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 18, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315185", "Спектральна теорія графів", "ст. викл. Тимошкевич Л. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 32, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340456", "Математичне мислення", "ст. викл. Щеглов М. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 20, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("320163", "Обчислювальна геометрія", "доц. Чорней Р. К.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("320012", "Мова розмітки LaTeX", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 28, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316847", "Математичні методи в хімії", "доц. Будзінська В. Л., ас. Носач В. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316161", "Символьні обчислення", "ст. викл. Прокоф'єв П. Г.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 30, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340446", "Додаткові розділи алгебри", "доц. Чорней Р. К.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340445", "Додаткові розділи дискретної математики", "ст. викл. Щеглов М. В.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 48, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340439", "Автоматизація роботи з програмними проєктами мовою Java", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 36, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340393", "Інформаційний пошук", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, 38, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315191", "Методи та засоби збору чутливої інформації", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315306", "Мова програмування Kotlin", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315261", "Програмування на C#", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 30, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340402", "Розробка клієнт-серверних застосувань", "проф. Гришко О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 26, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Управління цифровим продуктом", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 18, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340350", "Обробка зображень", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 42, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340352", "Життя у цифровому світі", "доц. Сидоренко А. К.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340353", "Введення у Хмарні технології", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 24, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340355", "Мемологічні студії", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 16, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315293", "Інформаційна безпека вебзастосунків", "ст. викл. Бабич Т. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 28, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315279", "Інструменти та принципи веброзробки", "ст. викл. Зважій Д. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 14, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Архітектура високонавантажених систем", "ст. викл. Ведель К. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 30, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315245", "Пошукова оптимізація вебзастосувань", "ст. викл. Цуд В. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316198", "Комп'ютерна вірусологія", "ст. викл. Кирієнко О. В., ст. викл. Пєчкурова О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 38, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340384", "Архітектура прикладних програм рівня підприємства", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 28, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340303", "Глобальні мережі", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 34, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("314833", "Низькорівневі вразливості програмного забезпечення", "ст. викл. Коренчук А. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 24, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315253", "Кібербезпека", "ст. викл. Вознюк Я. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 18, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315284", "Практичні аспекти роботи з базами даних в Spring Boot", "ст. викл. Андрощук М. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340321", "Технології обчислювального експерименту", "ст. викл. Міхновський О. Л.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 26, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315211", "Програмування на основі .NET", "ст. викл. Борозенний С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315280", "Backend-розробка на базі NodeJS", "ст. викл. Петлюра С. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 32, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("322535", "Алгоритми паралельних обчислень", "доц. Винниченко В. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 30, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315246", "Технології електронних видань", "ст. викл. Афонін А. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315838", "Дизайн вебінтерфейсів", "ст. викл. Яковлєв М.В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 16, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315479", "Інформаційна безпека мереж", "ст. викл. Коновалець Є. П.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 26, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316821", "Банківські комп'ютерні системи", "д. е. н., доц. Гладких Д. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 34, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("313821", "Стандарти обміну даних в Інтернет", "д. е. н., доц. Гладких Д. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316472", "Методи та засоби офісного програмування", "д. е. н., доц. Гладких Д. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 24, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315295", "Інформаційна безпека цільових систем", "ст. викл. Чередарчук А. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 44, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315221", "Технологія XML", "ст. викл. Захоженко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 18, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315241", "Розробка Frontend-додатків на JavaScript", "проф. Глибовець А. М., ст. викл. Бабич Т. А.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, 26, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("318273", "Алгоритми комп'ютерної алгебри", "ст. викл. Смиш О. Р.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315297", "Вибрані фреймворки для iOS", "ст. викл. Франків О. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 20, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315206", "Комп'ютерна графіка", "проф. Липинський І. С.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 38, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315464", "Кваліфікаційна робота", "проф. Глибовець А. М.", 12, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));

        }

        else if ((student.getMajor().equals("Комп'ютерні науки")) && (selectedCourse == 1)){
            enrollmentSystem.addDiscipline(new Discipline("315203", "Алгоритми і структури даних", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М., ст. викл. Кирієнко О. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("314835", "Архітектура обчислювальних систем", "ст. викл. Медвідь С. О.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314827", "Історія української кібернетики", "проф. Глибовець А. М.", 2, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315453", "Організація та обробка електронної інформації", "ст. викл. Сініцина Р. Б.", 8, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315202", "Дискретна математика", "ст. викл. Щеглов М. В.", 8, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314809", "Англійська мова", "ст. викл. Гісем С. О.", 8, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315275", "Мови програмування", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М.", 8, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315200", "Алгебра та геометрія", "доц. Чорней Р. К., ас. Сарана М.", 8, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("314804", "Фізичне виховання", "викл. Жуков В. О.", 8, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315199", "Математичний аналіз", "ст. викл. Іванюк А. О.", 8, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));
        }

        else if ((student.getMajor().equals("Комп'ютерні науки")) && (selectedCourse == 2)){
            enrollmentSystem.addDiscipline(new Discipline("340214", "Диференціальні рівняння", "проф. Дерев'янко А. С.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("340447", "Математична логіка та теорія алгоритмів", "ст. викл. Кушнір О. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("340443", "Бази даних та інформаційні системи", "доц. Сидоров М. І.", 9, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("340418", "Методи проєктування алгоритмів", "проф. Бублик В. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340362", "Основи побудови комп'ютерних мереж", "ст. викл. Черкасов Д. І., ст. викл. Савченко Т. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));

            enrollmentSystem.addDiscipline(new Discipline("315491", "Обчислювальне суспільствознавство", "проф. Глибовець А. М.", 2.5, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("344466", "Вступ до ігрової розробки", "проф. Глибовець А. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 35, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("344467", "Комп'ютерна мережа Інтернет", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 18, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("344468", "Локальні мережі", "ст. викл. Вознюк Я. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 12, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340466", "Архітектура ПЕОМ", "проф. Глибовець А. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 27, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("316200", "Додаткові розділи теорії графів", "ст. викл. Козеренко С. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("319949", "Основи теорії груп", "ст. викл. Козеренко С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 48, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315185", "Спектральна теорія графів", "ст. викл. Тимошкевич Л. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 26, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340456", "Математичне мислення", "ст. викл. Щеглов М. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 33, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("320163", "Обчислювальна геометрія", "доц. Чорней Р. К.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 15, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("320012", "Мова розмітки LaTeX", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("316847", "Математичні методи в хімії", "доц. Будзінська В. Л., ас. Носач В. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("316161", "Символьні обчислення", "ст. викл. Прокоф'єв П. Г.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 17, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340446", "Додаткові розділи алгебри", "доц. Чорней Р. К.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 21, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340445", "Додаткові розділи дискретної математики", "ст. викл. Щеглов М. В.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340439", "Автоматизація роботи з програмними проєктами мовою Java", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340393", "Інформаційний пошук", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, 20, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315191", "Методи та засоби збору чутливої інформації", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315306", "Мова програмування Kotlin", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315261", "Програмування на C#", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 24, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340402", "Розробка клієнт-серверних застосувань", "проф. Гришко О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 28, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340333", "Управління цифровим продуктом", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 12, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340334", "Обробка зображень", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340335", "Життя у цифровому світі", "доц. Сидоренко А. К.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 18, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340336", "Введення у Хмарні технології", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340337", "Мемологічні студії", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 14, false, 2));

        }

        else if ((student.getMajor().equals("Комп'ютерні науки")) && (selectedCourse == 3)){
            enrollmentSystem.addDiscipline(new Discipline("315429", "Функціональне програмування", "проф. Математикус А. Б.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("315225", "Методи розробки програмних систем", "доц. Жежерун О. П.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("315425", "Основи штучного інтелекту", "ст. викл. В.І. Системний", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("315218", "Аналіз великих даних (Big Data)", "ст. викл. Братик М. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("315217", "Машинне навчання", "ст. викл. Олецький О. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("315198", "Системний аналіз", "ст. викл. Логік Т.О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("315199", "Практика виробнича", "ст. викл. Логік Т.О.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));

            enrollmentSystem.addDiscipline(new Discipline("316208", "Вступ до загальної топології", "ст. викл. Козеренко С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 33, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316210", "Загальна топологія", "ст. викл. Козеренко С.О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316156", "Актуарна математика", "ст. викл. Братик М. В.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 27, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("317933", "Обчислюваність", "проф. Олійник Б. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 55, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316192", "Статистичні основи вебаналітики", "доц. Крюкова Г. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 13, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315490", "Дані та суспільство", "доц. Крюкова Г. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 15, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("314810", "Експлуатація розподіленої хмарної інфраструктури та сервісів (DevOps)", "ст. викл. Сидорчук Л. Н.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 42, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316195", "Візуалізація та комп'ютерна графіка", "доц. Крюковчук Г. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 63, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315491", "Обчислювальне суспільствознавство", "проф. Глибовець А. М.", 2.5, EnrollmentSystem.ELECTIVE_CAPACITY, 44, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("344466", "Вступ до ігрової розробки", "проф. Глибовець А. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 39, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("344467", "Комп'ютерна мережа Інтернет", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 31, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("344468", "Локальні мережі", "ст. викл. Вознюк Я. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 46, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340466", "Архітектура ПЕОМ", "проф. Глибовець А. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 35, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316200", "Додаткові розділи теорії графів", "ст. викл. Козеренко С. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 58, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("319949", "Основи теорії груп", "ст. викл. Козеренко С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 30, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315185", "Спектральна теорія графів", "ст. викл. Тимошкевич Л. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 49, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340456", "Математичне мислення", "ст. викл. Щеглов М. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 23, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("320163", "Обчислювальна геометрія", "доц. Чорней Р. К.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 60, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("320012", "Мова розмітки LaTeX", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 34, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316847", "Математичні методи в хімії", "доц. Будзінська В. Л., ас. Носач В. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 41, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316161", "Символьні обчислення", "ст. викл. Прокоф'єв П. Г.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 26, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340446", "Додаткові розділи алгебри", "доц. Чорней Р. К.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340445", "Додаткові розділи дискретної математики", "ст. викл. Щеглов М. В.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 57, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340439", "Автоматизація роботи з програмними проєктами мовою Java", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 24, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340393", "Інформаційний пошук", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, 37, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315191", "Методи та засоби збору чутливої інформації", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315306", "Мова програмування Kotlin", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315261", "Програмування на C#", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 32, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340402", "Розробка клієнт-серверних застосувань", "проф. Гришко О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 36, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Управління цифровим продуктом", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 20, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Обробка зображень", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 31, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Життя у цифровому світі", "доц. Сидоренко А. К.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 50, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Введення у Хмарні технології", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 38, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Мемологічні студії", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 29, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315293", "Інформаційна безпека вебзастосунків", "ст. викл. Бабич Т. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 43, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315279", "Інструменти та принципи веброзробки", "ст. викл. Зважій Д. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 54, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Архітектура високонавантажених систем", "ст. викл. Ведель К. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 21, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315245", "Пошукова оптимізація вебзастосувань", "ст. викл. Цуд В. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 47, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316198", "Комп'ютерна вірусологія", "ст. викл. Кирієнко О. В., ст. викл. Пєчкурова О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 16, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340384", "Архітектура прикладних програм рівня підприємства", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 28, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340303", "Глобальні мережі", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 25, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("314833", "Низькорівневі вразливості програмного забезпечення", "ст. викл. Коренчук А. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 18, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315253", "Кібербезпека", "ст. викл. Вознюк Я. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 19, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315284", "Практичні аспекти роботи з базами даних в Spring Boot", "ст. викл. Андрощук М. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 59, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340321", "Технології обчислювального експерименту", "ст. викл. Міхновський О. Л.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 62, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315211", "Програмування на основі .NET", "ст. викл. Борозенний С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 17, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315280", "Backend-розробка на базі NodeJS", "ст. викл. Петлюра С. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 56, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("322535", "Алгоритми паралельних обчислень", "доц. Винниченко В. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 48, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315246", "Технології електронних видань", "ст. викл. Афонін А. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 53, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315838", "Дизайн вебінтерфейсів", "ст. викл. Яковлєв М.В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 14, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315479", "Інформаційна безпека мереж", "ст. викл. Коновалець Є. П.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 12, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("316821", "Банківські комп'ютерні системи", "д. е. н., доц. Гладких Д. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 61, false, 3));

        }

        else if ((student.getMajor().equals("Комп'ютерні науки")) && (selectedCourse == 4)){
            enrollmentSystem.addDiscipline(new Discipline("315470", "Інтелектуальні мережі", "ст. викл. Сідько А. А., ст. викл. Гречко А. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("315206", "Комп'ютерна графіка", "проф. Липинський І. С.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 4, "Екзамен"));

            enrollmentSystem.addDiscipline(new Discipline("316213", "Основи фінансової математики / Basics of Financial Mathematics (англ. мовою)", "доц. Щестюк Н. Ю.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 27, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315991", "Системи та методи ухвалення рішеннь", "проф. Глибовець А. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 39, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315491", "Обчислювальне суспільствознавство", "проф. Глибовець А. М.", 2.5, EnrollmentSystem.ELECTIVE_CAPACITY, 59, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("344466", "Вступ до ігрової розробки", "проф. Глибовець А. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 24, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("344467", "Комп'ютерна мережа Інтернет", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 30, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("344468", "Локальні мережі", "ст. викл. Вознюк Я. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 38, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340466", "Архітектура ПЕОМ", "проф. Глибовець А. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316200", "Додаткові розділи теорії графів", "ст. викл. Козеренко С. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 12, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("319949", "Основи теорії груп", "ст. викл. Козеренко С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 47, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315185", "Спектральна теорія графів", "ст. викл. Тимошкевич Л. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 56, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340456", "Математичне мислення", "ст. викл. Щеглов М. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 18, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("320163", "Обчислювальна геометрія", "доц. Чорней Р. К.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 35, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("320012", "Мова розмітки LaTeX", "ст. викл. Зважій Д. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 43, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316847", "Математичні методи в хімії", "доц. Будзінська В. Л., ас. Носач В. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 44, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316161", "Символьні обчислення", "ст. викл. Прокоф'єв П. Г.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340446", "Додаткові розділи алгебри", "доц. Чорней Р. К.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 51, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340445", "Додаткові розділи дискретної математики", "ст. викл. Щеглов М. В.", 5, EnrollmentSystem.ELECTIVE_CAPACITY, 63, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340439", "Автоматизація роботи з програмними проєктами мовою Java", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 28, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340393", "Інформаційний пошук", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, 33, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315191", "Методи та засоби збору чутливої інформації", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315306", "Мова програмування Kotlin", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315261", "Програмування на C#", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 54, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340402", "Розробка клієнт-серверних застосувань", "проф. Гришко О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 48, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Управління цифровим продуктом", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 31, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Обробка зображень", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 16, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Життя у цифровому світі", "доц. Сидоренко А. К.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 37, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Введення у Хмарні технології", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 22, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Мемологічні студії", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 24, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315293", "Інформаційна безпека вебзастосунків", "ст. викл. Бабич Т. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 42, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315279", "Інструменти та принципи веброзробки", "ст. викл. Зважій Д. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 57, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Архітектура високонавантажених систем", "ст. викл. Ведель К. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 19, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315245", "Пошукова оптимізація вебзастосувань", "ст. викл. Цуд В. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 55, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316198", "Комп'ютерна вірусологія", "ст. викл. Кирієнко О. В., ст. викл. Пєчкурова О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 33, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340384", "Архітектура прикладних програм рівня підприємства", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 44, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340303", "Глобальні мережі", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 46, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("314833", "Низькорівневі вразливості програмного забезпечення", "ст. викл. Коренчук А. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 28, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315253", "Кібербезпека", "ст. викл. Вознюк Я. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 62, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315284", "Практичні аспекти роботи з базами даних в Spring Boot", "ст. викл. Андрощук М. В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 30, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340321", "Технології обчислювального експерименту", "ст. викл. Міхновський О. Л.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 21, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315211", "Програмування на основі .NET", "ст. викл. Борозенний С. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 49, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315280", "Backend-розробка на базі NodeJS", "ст. викл. Петлюра С. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 29, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("322535", "Алгоритми паралельних обчислень", "доц. Винниченко В. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 48, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315246", "Технології електронних видань", "ст. викл. Афонін А. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 32, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315838", "Дизайн вебінтерфейсів", "ст. викл. Яковлєв М.В.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 24, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315479", "Інформаційна безпека мереж", "ст. викл. Коновалець Є. П.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 37, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316821", "Банківські комп'ютерні системи", "д. е. н., доц. Гладких Д. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 20, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("313821", "Стандарти обміну даних в Інтернет", "д. е. н., доц. Гладких Д. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("316472", "Методи та засоби офісного програмування", "д. е. н., доц. Гладких Д. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 28, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315295", "Інформаційна безпека цільових систем", "ст. викл. Чередарчук А. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 44, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315221", "Технологія XML", "ст. викл. Захоженко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 38, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315241", "Розробка Frontend-додатків на JavaScript", "проф. Глибовець А. М., ст. викл. Бабич Т. А.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, 19, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("318273", "Алгоритми комп'ютерної алгебри", "ст. викл. Смиш О. Р.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, 46, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315297", "Вибрані фреймворки для iOS", "ст. викл. Франків О. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, 27, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315464", "Кваліфікаційна робота", "проф. Глибовець А. М.", 12, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));

        }
    }

    /**
     * Initializes the initial data for the enrollment system based on the student's major and selected course.
     * This method adds mandatory and elective disciplines to the enrollment system.
     *
     * @param selectedCourse The course number for which to initialize disciplines (1 or 2).
     */
    private void initializeInitialDataM(int selectedCourse) {
        enrollmentSystem.addStudent(student);

        if ((student.getMajor().equals("Інженерія програмного забезпечення")) && (selectedCourse == 1)) {
            enrollmentSystem.addDiscipline(new Discipline("317203", "Архітектура інформаційних систем", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М., ст. викл. Кирієнко О. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("317809", "Моделі і методи розробки програмного забезпечення", "ст. викл. Гісем С. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("317275", "Паралельне програмування", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("317213", "Проєктування програмних систем", "доц. Митник Ю. В., ст. викл. Силенко І. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("317598", "Структури даних та аналіз алгоритмів", "ст. викл. Медвідь С. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("317200", "Практика науково-дослідницька", "доц. Чорней Р. К., ас. Сарана М.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("317223", "Забезпечення якости програмного продукту", "доц. Проценко В. С.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("317283", "Патерни проєктування та дизайн АРІ", "ст. викл. Зважій Д. В. ", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("317202", "Побудова високонавантажених систем", "ст. викл. Щеглов М. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("317199", "Розподілені бази даних та знань", "ст. викл. Іванюк А. О.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("317277", "Управління проєктами в інженерії програмного забезпечення", "ст. викл. Вознюк О. М., ст. викл. Вознюк Я. І.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("317810", "Практика науково-дослідницька", "доц. Сегін Л. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("317804", "Практика науково-дослідницька", "викл. Жуков В. О.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));

            enrollmentSystem.addDiscipline(new Discipline("317439", "Інформаційна безпека", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("317393", "Математична теорія ігор", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("317191", "Моделі та алгоритми інформаційного пошуку", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("317306", "Хмарні технології", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("317261", "Чистий код та чиста архітектура", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
        }

        else if ((student.getMajor().equals("Інженерія програмного забезпечення")) && (selectedCourse == 2)) {
            enrollmentSystem.addDiscipline(new Discipline("318214", "Аналіз даних великого розміру (Big Data)", "проф. А.С. Дерев'янко", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("318447", "Методологія наукових досліджень в програмній інженерії", "ст. викл. О.В. Кушнір", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("318443", "Магістерська робота", "доц. М.І. Сидоров", 20, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));

            enrollmentSystem.addDiscipline(new Discipline("317439", "Інформаційна безпека", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("317393", "Математична теорія ігор", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("317191", "Моделі та алгоритми інформаційного пошуку", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("317306", "Хмарні технології", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("317261", "Чистий код та чиста архітектура", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318402", "Алгоритми розподіленого обміну криптовалют на основі smart-контрактів (DeFi)", "проф. Гришко О. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318351", "Архітектура сучасних комп'ютерних мереж", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318352", "Бізнес-аналітика", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318353", "Ділова комунікація для інженерів програмного забезпечення", "доц. Сидоренко А. К.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318354", "Доставка цифрового продукту", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318355", "Криптографія в децентралізованих системах", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318356", "Програмна розробка інтелектуальних систем", "ст. викл. Сидорук І. Ю.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318357", "Системне адміністрування", "доц. Сидоренко А. К.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318358", "Дані та суспільство", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318359", "Методика викладання інформатики у вищій школі", "ст. викл. Ведель К. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318361", "Аджайл рівня організації", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318360", "DevOps та безперервна інтеграція", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318362", "Адміністрування інформаційних систем IaS", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318363", "Машинне навчання", "ст. викл. Ведель К. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318364", "Моделі та методи програмування економічних задач", "ст. викл. Ведель К. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318365", "Нереляційні бази даних", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318366", "Розробка smart-контрактів", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318367", "Технічні інструменти регуляції криптовалют на ринку", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318368", "Управління комплексними проєктами/Deliveri Management", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
        }
        if ((student.getMajor().equals("Прикладна математика")) && (selectedCourse == 1)){
            enrollmentSystem.addDiscipline(new Discipline("318403", "Комп'ютерний зір / Computer Vision (англ. мовою)", "доц. Швай Н. О.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("318376", "Динамічні системи", "проф. Авраменко О. В.", 7, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("318404", "Машинне навчання / Machine Learning (англ. мовою)", "доц. Швай Н. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("318382", "Теорія складности алгоритмів", "ст. викл. Морозов Д. І.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("318386", "Технології чисельного моделювання", "доц. Тригуб О. С.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("319980", "Практика науково-дослідна", "доц. Чорней Р. К.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("318388", "Прикладна алгебра та теорія чисел", "доц. Тригуб О. С.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));

            enrollmentSystem.addDiscipline(new Discipline("311433", "Математична біологія", "ст. викл. Сидоренко А. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("311434", "Основи статистичного експерименту", "доц. Абраменко А. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("311435", "Теорія інформації", "ст. викл. Зваженко К. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("311436", "Математичні методи економіки", "ст. викл. Шевченко О. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("311437", "Прикладний статистичний аналіз", "проф. Шашкевич М. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("311438", "Комплексний аналіз та його застосування", "ст. викл. Остроградський М. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("311439", "Практичне застосування математичних моделей обернених задач", "ст. викл. Остроградський М. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("311441", "Проблеми некласичної оптимізації", "ст. викл. Осиповський Т. Я.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("311442", "Комбінаторний аналіз", "ст. викл. Осиповський Т. Я.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("311443", "Алгебраїчна топологія", "доц. Кирієнко О. Л.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("311444", "Навчання з підкріпленням", "доц. Кирієнко О. Л.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("311445", "Математична теорія соціального вибору", "доц. Кирієнко О. Л.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("311446", "Алгоритмічна геометрія", "ст. викл. Кравчук Л. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("311447", "Ймовірнісні графічні моделі / Probabilistic Graphical Models (англ.мовою)", "доц. Швай Н. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
        }

        else if ((student.getMajor().equals("Прикладна математика")) && (selectedCourse == 2)){
            enrollmentSystem.addDiscipline(new Discipline("318368", "Методика викладання математики та інформатики у вищій школі", "проф. Федосова І. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("320170", "Практика асистентська", "ст. викл. О.В. Кушнір", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("318492", "Науково-дослідний семінар", "доц. Михалевич В. М.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("318383", "Стохастична фінансова математика / Stochastic Financial Mathematics (англ.мовою)", "доц. Щестюк Н. Ю.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2, "Екзамен"));
            enrollmentSystem.addDiscipline(new Discipline("320086", "Педагогіка і психологія вищої школи", "проф. Власенко К. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("318500", "Теорія оптимального керування", "доц. Чорней Р. К.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("318377", "Нелінійні процеси та моделі", "проф. Авраменко О. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2, "Екзамен"));

            enrollmentSystem.addDiscipline(new Discipline("310430", "Квантова криптографія", "ст. викл. Сидоренко А. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("310431", "Алгоритми на графах", "доц. Сидоренко П. П.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("310432", "Прикладні задачі аналізу", "доц. Чорней Р. К.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("310433", "Математична теорія ризику та страхова справа", "доц. Чорней Р. К.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("310434", "Розпізнавання образів", "доц. Чорней Р. К.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("310435", "Аналіз часових рядів", "ст. викл. Сидоренко А. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("310436", "Математичні основи захисту інформації", "доц. Чорней Р. К.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("310437", "Прикладний функціональний аналіз", "ст. викл. Вознюк І. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("310438", "Математичні основи криптографії", "ст. викл. Вознюк І. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("310439", "Теорія автоматів", "ст. викл. Вознюк І. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("310440", "Прикладна теорія випадкових процесів", "ст. викл. Козеренко Р. Р.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("310441", "Символьні обчислення та комп'ютерна алгебра", "ст. викл. Сидоренко Є. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("310442", "Розпізнавання образів в аналізі даних", "проф. Шухевич Р. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("311433", "Математична біологія", "ст. викл. Сидоренко А. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("311434", "Основи статистичного експерименту", "доц. Абраменко А. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("311435", "Теорія інформації", "ст. викл. Зваженко К. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("311436", "Математичні методи економіки", "ст. викл. Шевченко О. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("311437", "Прикладний статистичний аналіз", "проф. Шашкевич М. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("311438", "Комплексний аналіз та його застосування", "ст. викл. Остроградський М. І.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("311439", "Практичне застосування математичних моделей обернених задач", "ст. викл. Остроградський М. І.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("311441", "Проблеми некласичної оптимізації", "ст. викл. Осиповський Т. Я.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("311442", "Комбінаторний аналіз", "ст. викл. Осиповський Т. Я.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("311443", "Алгебраїчна топологія", "доц. Кирієнко О. Л.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("311444", "Навчання з підкріпленням", "доц. Кирієнко О. Л.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("311445", "Математична теорія соціального вибору", "доц. Кирієнко О. Л.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("311446", "Алгоритмічна геометрія", "ст. викл. Кравчук Л. В.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("311447", "Ймовірнісні графічні моделі / Probabilistic Graphical Models (англ.мовою)", "доц. Швай Н. О.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
        }

        else if ((student.getMajor().equals("Комп'ютерні науки")) && (selectedCourse == 1)){
            enrollmentSystem.addDiscipline(new Discipline("317722", "Математична теорія ігор", "проф. Глибовець А. М.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1, "Екзамен"));

            enrollmentSystem.addDiscipline(new Discipline("317439", "Інформаційна безпека", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("317393", "Математична теорія ігор", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("317191", "Моделі та алгоритми інформаційного пошуку", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("317306", "Хмарні технології", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
            enrollmentSystem.addDiscipline(new Discipline("317261", "Чистий код та чиста архітектура", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
        }

        else if ((student.getMajor().equals("Комп'ютерні науки")) && (selectedCourse == 2)){
            enrollmentSystem.addDiscipline(new Discipline("317722", "Математична теорія ігор", "проф. Глибовець А. М.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2, "Екзамен"));

            enrollmentSystem.addDiscipline(new Discipline("317439", "Інформаційна безпека", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("317393", "Математична теорія ігор", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("317191", "Моделі та алгоритми інформаційного пошуку", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("317306", "Хмарні технології", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("317261", "Чистий код та чиста архітектура", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318402", "Алгоритми розподіленого обміну криптовалют на основі smart-контрактів (DeFi)", "проф. Гришко О. М.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318351", "Архітектура сучасних комп'ютерних мереж", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318352", "Бізнес-аналітика", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318353", "Ділова комунікація для інженерів програмного забезпечення", "доц. Сидоренко А. К.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318354", "Доставка цифрового продукту", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318355", "Криптографія в децентралізованих системах", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318356", "Програмна розробка інтелектуальних систем", "ст. викл. Сидорук І. Ю.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318357", "Системне адміністрування", "доц. Сидоренко А. К.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318358", "Дані та суспільство", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318359", "Методика викладання інформатики у вищій школі", "ст. викл. Ведель К. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318361", "Аджайл рівня організації", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318360", "DevOps та безперервна інтеграція", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318362", "Адміністрування інформаційних систем IaS", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318363", "Машинне навчання", "ст. викл. Ведель К. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318364", "Моделі та методи програмування економічних задач", "ст. викл. Ведель К. А.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318365", "Нереляційні бази даних", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318366", "Розробка smart-контрактів", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318367", "Технічні інструменти регуляції криптовалют на ринку", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("318368", "Управління комплексними проєктами/Deliveri Management", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
        }
    }

    /**
     * Updates the lists of mandatory, elective, and enrolled elective disciplines in the GUI.
     * It clears existing lists, populates mandatory disciplines, and then filters elective
     * disciplines based on student enrollment and search criteria (if any).
     */
    private void updateDisciplineLists() {
        mandatoryListModel.clear();
        electiveListModel.clear();
        enrolledElectiveListModel.clear();
        int studentCourse = student.getCourse();
        for (Discipline disc : enrollmentSystem.getMandatoryDisciplines(studentCourse)) {
            mandatoryListModel.addElement(disc);
        }

        List<Discipline> studentEnrolledDisciplines = student.getEnrolledDisciplines();
        String searchText = searchField.getText().trim().toLowerCase();
        String selectedCriterion = (String) searchCriteriaCombo.getSelectedItem();

        for (Discipline disc : enrollmentSystem.getElectiveDisciplines(studentCourse)) {
            boolean matchesSearch;

            if (searchText.isEmpty()) {
                matchesSearch = true;
            } else {
                matchesSearch = switch (selectedCriterion) {
                    case "за назвою" -> disc.getName().toLowerCase().contains(searchText);
                    case "за кодом" -> disc.getDisciplineId().toLowerCase().contains(searchText);
                    case "за викладачем_кою" -> disc.getInstructor().toLowerCase().contains(searchText);
                    default -> false;
                };
            }

            if (studentEnrolledDisciplines.contains(disc)) {
                enrolledElectiveListModel.addElement(disc);
            } else if (matchesSearch) {
                electiveListModel.addElement(disc);
            }
        }

        electiveDisciplineList.repaint();
        enrolledElectiveList.repaint();
        updateStudentInfoDisplay();
        updateConfirmButtonState();
    }

    /**
     * Filters the elective disciplines displayed in the GUI based on the provided search text and criterion.
     * This method essentially triggers a refresh of the discipline lists.
     *
     * @param searchText The text to search for in discipline names, codes, or instructors.
     * @param criterion The criterion by which to search (e.g., "за назвою", "за кодом", "за викладачем_кою").
     */
    private void filterElectiveDisciplines(String searchText, String criterion) {
        updateDisciplineLists();
    }

    /**
     * Updates the student information display in the GUI, showing student ID, course,
     * and current total credits along with the course credit limit.
     */
    private void updateStudentInfoDisplay() {
        double totalCredits = 0;
        for (Discipline discipline : student.getEnrolledDisciplines()) {
            double credits = discipline.getCredits();
            totalCredits += credits;
        }
        int courseCreditLimit = 0;
        switch (student.getCourse()) {
            case 1:
                courseCreditLimit = 61;
                break;
            default:
                courseCreditLimit = 62;
        }

        studentInfoLabel.setText("Студент (ID: " + student.getStudentId() +
                ", курс: " + student.getCourse() +
                ", кредити: " + totalCredits + "/" + courseCreditLimit + ")");
    }

    /**
     * Updates the state of the "Готово" (Confirm) button, including its text
     * to display the total credits and enabling/disabling it based on whether
     * the total credits meet the minimum required for confirmation.
     */
    private void updateConfirmButtonState() {
        double totalCredits = 0;
        for (Discipline discipline : student.getEnrolledDisciplines()) {
            double credits = discipline.getCredits();
            totalCredits += credits;
        }
        confirmSelectionButton.setText("Готово (кредитів: " + totalCredits + ")");
        confirmSelectionButton.setEnabled(totalCredits >= EnrollmentSystem.MIN_CREDITS_TO_CONFIRM);
    }

    /**
     * Simulates a random glitch in the system. There's a 70% chance of a glitch occurring.
     * If a glitch happens, it plays an error sound, displays a "system temporarily unavailable" message,
     * and pauses execution. There's also a 30% chance within a glitch for a more severe
     * "unexpected error" message to be displayed via a dialog box.
     *
     * @return true if a glitch occurred and an operation should be halted, false otherwise.
     */
    private boolean simulateGlitch() {
        if (randomGlitches.nextDouble() < 0.7) {
            MusicPlayer.getInstance().playError();
            try {
                int delay = 500 + randomGlitches.nextInt(1500);
                appendOutput("Система тимчасово недоступна. Будь ласка, зачекайте...\n");
                Thread.sleep(delay);

                if (randomGlitches.nextDouble() < 0.3) {
                    JOptionPane.showMessageDialog(this,
                            "Сталася неочікувана помилка САЗ. Спробуйте пізніше.",
                            "Помилка системи",
                            JOptionPane.ERROR_MESSAGE);
                    appendOutput("Помилка САЗ. Операція не виконана.\n");
                    return true;
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                appendOutput("Операція перервана.\n");
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to enroll the student in an elective discipline. This method first simulates
     * a potential system glitch. If a glitch occurs, the enrollment attempt is aborted.
     * (The rest of the enrollment logic is expected to be implemented here.)
     */
    private void attemptEnrollment() {
        if (simulateGlitch()) {
            return;
        }

        Discipline selectedDiscipline = electiveDisciplineList.getSelectedValue();

        if (selectedDiscipline == null) {
            MusicPlayer.getInstance().playError();
            JOptionPane.showMessageDialog(this,
                    "Будь ласка, оберіть вибіркову дисципліну для запису.",
                    "Помилка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return enrollmentSystem.enrollStudentToDiscipline(student.getStudentId(), selectedDiscipline.getDisciplineId());
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    appendOutput(result + "\n");
                    updateDisciplineLists(); // Refresh all lists
                } catch (Exception ex) {
                    MusicPlayer.getInstance().playError();
                    appendOutput("Виникла непередбачувана помилка під час виконання запису: " + ex.getMessage() + "\n");
                }
            }
        }.execute();
    }

    // --- Logic for attempting to drop from an elective discipline ---
    /**
     * Attempts to drop a student from a selected elective discipline.
     * This method first simulates a system glitch. If a glitch occurs, the operation is halted.
     * It then validates the selection, confirms with the user, and if confirmed,
     * attempts to drop the student from the discipline using a SwingWorker for background processing.
     */
    private void attemptDrop() {
        if (simulateGlitch()) {
            return;
        }

        // Only allow dropping from the 'enrolledElectiveList'
        Discipline selectedDiscipline = enrolledElectiveList.getSelectedValue();

        if (selectedDiscipline == null) {
            JOptionPane.showMessageDialog(this,
                    "Будь ласка, для виписки оберіть вибіркову дисципліну з розділу «Ваші обрані».",
                    "Помилка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ensure the selected discipline is indeed one the student is currently enrolled in
        if (!student.getEnrolledDisciplines().contains(selectedDiscipline)) {
            JOptionPane.showMessageDialog(this,
                    "Ви не записані на обрану дисципліну.",
                    "Помилка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Add confirmation dialog for dropping
        Object[] options = {"Так", "Ні"};
        int confirm = JOptionPane.showOptionDialog(this,
                "Ви впевнені, що хочете виписатися з дисципліни " + selectedDiscipline.getName() + "?",
                "Підтвердження виписки",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (confirm == JOptionPane.YES_OPTION) {
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return enrollmentSystem.dropStudentFromDiscipline(student.getStudentId(), selectedDiscipline.getDisciplineId());
                }

                @Override
                protected void done() {
                    try {
                        String result = get();
                        appendOutput(result + "\n");
                        updateDisciplineLists();
                    } catch (Exception ex) {
                        MusicPlayer.getInstance().playError();
                        appendOutput("Виникла непередбачувана помилка під час виконання виписки: " + ex.getMessage() + "\n");
                    }
                }
            }.execute();
        } else {
            appendOutput("Виписка скасована.\n");
        }
    }

    /**
     * Handles the completion of the discipline selection process.
     * It checks if the student has accumulated enough credits. If so, it finalizes the enrollment,
     * disables further changes, saves the enrollment data, plays a success sound, and transitions
     * to the next game level (or phase) after a loading screen.
     * If not enough credits are selected, it displays an error message.
     */
    private void confirmSelection() {
        double totalCredits = 0;
        for (Discipline discipline : student.getEnrolledDisciplines()) {
            double credits = discipline.getCredits();
            totalCredits += credits;
        }

        if (totalCredits >= EnrollmentSystem.MIN_CREDITS_TO_CONFIRM) {
            if (autoEnrollTimer != null && autoEnrollTimer.isRunning()) {
                autoEnrollTimer.stop();
            }

            enrollElectiveButton.setEnabled(false);
            dropElectiveButton.setEnabled(false);
            confirmSelectionButton.setEnabled(false);
            electiveDisciplineList.setEnabled(false);
            enrolledElectiveList.setEnabled(false);
            searchField.setEnabled(false);
            searchButton.setEnabled(false);

            appendOutput("Запис завершено! Подальші зміни неможливі.\n");

            saveEnrollmentDataJson();
            MusicPlayer.getInstance().playSuccess();
            JOptionPane.showMessageDialog(this,
                    "Вітаємо! Перший рівень пройдено!",
                    "Запис завершено",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose()
            ;SwingUtilities.invokeLater(() -> {
                LoadingFrame loading = new LoadingFrame();
                loading.startLoading(() -> {
                    hero.levelUp();
                    hero.setLevel(2);
                    hero.setStudent(student);
                    hero.decreaseEnergy(40);
                    hero.decreaseHunger(-30);
                    hero.decreaseMood(30);
                    GameFrame gameFrame =  new GameFrame(hero);
                    gameFrame.getGamePanel().getHintPanel().setHint(2);

                    gameFrame.setVisible(true);
                });
            });
        } else {
            JOptionPane.showMessageDialog(this,
                    "Ви повинні обрати дисципліни мінімум на " + EnrollmentSystem.MIN_CREDITS_TO_CONFIRM + " кредитів, щоб завершити вибір. Обрано: " + totalCredits + ".\n",
                    "Помилка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Appends a message to the output display, typically a JOptionPane dialog.
     *
     * @param text The text message to be displayed.
     */
    private void appendOutput(String text) {
        JOptionPane.showMessageDialog(this, text, "Повідомлення", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * An inner class that extends MouseAdapter to provide a listener for mouse events on JLists.
     * Specifically, it's used to display detailed information about a discipline when it's double-clicked.
     */
    private class DisciplineInfoMouseAdapter extends MouseAdapter {
        /**
         * Invoked when the mouse button has been clicked (pressed and released) on a component.
         * If a discipline in the list is double-clicked, it retrieves and displays its detailed information
         * (code, name, instructor, credits, type, and enrollment capacity/status).
         *
         * @param e The MouseEvent generated by the click.
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            JList<?> list = (JList<?>) e.getSource();
            if (e.getClickCount() == 2) {
                int index = list.locationToIndex(e.getPoint());
                if (index != -1) {
                    Object selectedObject = list.getModel().getElementAt(index);
                    if (selectedObject instanceof Discipline) {
                        Discipline selectedDiscipline = (Discipline) selectedObject;

                        StringBuilder infoBuilder = new StringBuilder();
                        infoBuilder.append("Код: ").append(selectedDiscipline.getDisciplineId()).append("\n");
                        infoBuilder.append("Назва: ").append(selectedDiscipline.getName()).append("\n");
                        infoBuilder.append("Викладач_ка: ").append(selectedDiscipline.getInstructor()).append("\n");
                        infoBuilder.append("Кількість кредитів: ").append(selectedDiscipline.getCredits()).append("\n");

                        if (selectedDiscipline.isMandatory()) {
                            infoBuilder.append("Тип: обов'язкова\n");
                            infoBuilder.append("Кількість студентів на курсі: ").append(selectedDiscipline.getMaxCapacity()).append("\n");
                            infoBuilder.append("Зараз записано: ").append(selectedDiscipline.getMaxCapacity());
                        } else {
                            infoBuilder.append("Тип: вибіркова\n");
                            if (selectedDiscipline.getMaxCapacity() == UNLIMITED_CAPACITY) {
                                infoBuilder.append("Кількість місць: необмежена\n");
                                infoBuilder.append("Зараз записано: ").append(selectedDiscipline.getCurrentEnrollment()).append("\n");
                            } else {
                                infoBuilder.append("Максимальна кількість студентів: ").append(selectedDiscipline.getMaxCapacity()).append("\n");
                                infoBuilder.append("Зараз записано: ").append(selectedDiscipline.getCurrentEnrollment()).append("\n");
                            }
                        }

                        JOptionPane.showMessageDialog(EnrollmentSystemGUI.this, infoBuilder.toString(), "Деталі курсу", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        }
    }

    /**
     * Saves the current student's enrollment data to a JSON file.
     * The file path is defined by {@code ENROLLMENT_FILE}.
     * Any {@code IOException} during writing will be printed to the stack trace.
     */
    private void saveEnrollmentDataJson() {
        try (FileWriter writer = new FileWriter(ENROLLMENT_FILE)) {
            gson.toJson(student, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}