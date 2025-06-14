package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Random;

import static org.example.EnrollmentSystem.UNLIMITED_CAPACITY;

import static java.awt.Color.*;
import static org.example.EnrollmentSystem.MANDATORY_DISCIPLINE_CAPACITY;

public class EnrollmentSystemGUI extends JFrame {

    private EnrollmentSystem enrollmentSystem;
    private JLabel studentInfoLabel;

    private JList<Discipline> mandatoryDisciplineList;
    private DefaultListModel<Discipline> mandatoryListModel;

    private JList<Discipline> electiveDisciplineList;
    private DefaultListModel<Discipline> electiveListModel; // Поточна модель для відображення

    private JList<Discipline> enrolledElectiveList;
    private DefaultListModel<Discipline> enrolledElectiveListModel;

    private JButton enrollElectiveButton;
    private JButton dropElectiveButton;
    private JButton confirmSelectionButton;

    private JTextField searchField;
    private JButton searchButton;

    private Student currentStudent = new Student("І 001/24 бп", "Тестовий студент", 1, "121 Інженерія програмного забезпечення");;
    private Timer autoEnrollTimer;
    private Random randomGlitches = new Random();

    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_TURQUOISE = new Color(64, 224, 208);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);
    private final Color SIMS_BUTTON_HOVER = new Color(255, 240, 245);
    private static final Color SIMS_GREEN_CORRECT = new Color(144, 238, 144);
    private static final Color SIMS_RED_INCORRECT = new Color(255, 99, 71);

    // Internal class for customizing list item display in JList
    static class DisciplineListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Discipline) {
                Discipline disc = (Discipline) value;
                String capacityText;

                if (disc.isMandatory()) {
                    // Для обов'язкових показуємо конкретну кількість студентів на курсі
                    capacityText = "Зайнято місць: " + disc.getMaxCapacity() + "/" + disc.getMaxCapacity();
                    label.setForeground(list.getForeground());
                } else {
                    // Для вибіркових
                    if (disc.getMaxCapacity() == UNLIMITED_CAPACITY) {
                        capacityText = "Зайнято місць: " + disc.getCurrentEnrollment() + "/необмежено";
                        label.setForeground(new Color(0, 100, 0)); // Темно-зелений для необмежених
                    } else {
                        capacityText = "Зайнято місць: " + disc.getCurrentEnrollment() + "/" + disc.getMaxCapacity();
                        // Колірна логіка для обмежених вибіркових
                        if (!disc.hasAvailableSlots()) {
                            label.setForeground(RED.darker());
                        } else if (disc.getCurrentEnrollment() > disc.getMaxCapacity() * 0.75) {
                            label.setForeground(new Color(200, 100, 0)); // Orange for nearing capacity
                        } else {
                            label.setForeground(list.getForeground());
                        }
                    }
                }
                label.setText(disc.getName() + " (" + capacityText + ")");
            }
            return label;
        }
    }

    public EnrollmentSystemGUI() {
        // Налаштування шрифтів для JOptionPane та інших стандартних компонентів
        UIManager.put("OptionPane.messageFont", new Font("Segoi UI", Font.BOLD, 12));
        UIManager.put("OptionPane.buttonFont", new Font("Segoi UI", Font.BOLD, 12));
        UIManager.put("Label.font", new Font("Segoi UI", Font.BOLD, 12));
        UIManager.put("Button.font", new Font("Segoi UI", Font.BOLD, 12));
        UIManager.put("TitledBorder.font", new Font("Segoi UI", Font.BOLD, 12));

        super("Система автоматизованого запису на дисципліни (САЗ)");
        enrollmentSystem = new EnrollmentSystem();

        // Встановлюємо український текст для кнопок "OK" та "Скасувати" перед викликом діалогів
        UIManager.put("OptionPane.okButtonText", "ОК");
        UIManager.put("OptionPane.cancelButtonText", "Скасувати");

        showInstructionsDialog();

        // --- Degree and Course Selection ---
        String selectedDegree = showDegreeSelectionDialog();
        if (selectedDegree == null) { // User cancelled degree selection
            System.exit(0);
        }

        int selectedCourse = showCourseSelectionDialog(selectedDegree);
        if (selectedCourse == -1) { // User cancelled course selection
            System.exit(0);
        }

        showStartAnimation();

        // Initialize data based on selected course
        if (selectedDegree.equals("Бакалаврат")) {
            initializeInitialDataB(selectedCourse);
            currentStudent = enrollmentSystem.getStudentById("І 005/24 бп").orElse(null);
            if (currentStudent == null) {
                JOptionPane.showMessageDialog(this, "Помилка. Студента не знайдено після вибору курсу.", "Помилка", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        } else if (selectedDegree.equals("Магістратура")) {
            initializeInitialDataM(selectedCourse);
            currentStudent = enrollmentSystem.getStudentById("І 005/24 мп").orElse(null);
            if (currentStudent == null) {
                JOptionPane.showMessageDialog(this, "Помилка. Студента не знайдено після вибору курсу.", "Помилка", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }

        // Automatically enroll currentStudent in all mandatory disciplines for their course
        for (Discipline mandatoryDisc : enrollmentSystem.getMandatoryDisciplines(currentStudent.getCourse())) {
            currentStudent.enrollDiscipline(mandatoryDisc);
        }

        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Student Info Panel at the top
        JPanel studentInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        studentInfoLabel = new JLabel();
        studentInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Шрифт для JLabel
        studentInfoPanel.add(studentInfoLabel);
        mainPanel.add(studentInfoPanel, BorderLayout.NORTH);

        // Панель для верхньої частини (вибіркові та обрані дисципліни)
        JPanel topContentPanel = new JPanel(new BorderLayout());
        topContentPanel.setBorder(new EmptyBorder(0, 0, 10, 0)); // Додаємо невеликий відступ знизу

        // Elective Disciplines Panel (Available for Enrollment)
        JPanel electivePanel = new JPanel(new BorderLayout(5, 5));
        electivePanel.setBorder(BorderFactory.createTitledBorder("Вибіркові дисципліни (доступні для запису)")); // UIManager встановить шрифт
        electiveListModel = new DefaultListModel<>();
        electiveDisciplineList = new JList<>(electiveListModel);
        electiveDisciplineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        electiveDisciplineList.setCellRenderer(new DisciplineListRenderer());
        electiveDisciplineList.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Шрифт для JList
        electivePanel.add(new JScrollPane(electiveDisciplineList), BorderLayout.CENTER);
        electiveDisciplineList.addMouseListener(new DisciplineInfoMouseAdapter());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        searchPanel.add(new JLabel("Пошук за назвою:"));
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Шрифт для JTextField
        searchButton = new JButton("Пошук");
        searchButton.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Шрифт для JButton
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        electivePanel.add(searchPanel, BorderLayout.NORTH);

        JPanel electiveButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        enrollElectiveButton = new JButton("Записатися на вибіркову");
        enrollElectiveButton.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Шрифт для JButton
        dropElectiveButton = new JButton("Виписатися з вибіркової");
        dropElectiveButton.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Шрифт для JButton
        confirmSelectionButton = new JButton("Готово (Кредитів: 0)");
        confirmSelectionButton.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Шрифт для кнопки "Готово"
        confirmSelectionButton.setEnabled(false);

        enrollElectiveButton.addActionListener(e -> attemptEnrollment());
        dropElectiveButton.addActionListener(e -> attemptDrop());
        confirmSelectionButton.addActionListener(e -> confirmSelection());

        searchButton.addActionListener(e -> filterElectiveDisciplines(searchField.getText()));
        searchField.addActionListener(e -> filterElectiveDisciplines(searchField.getText()));

        electiveButtonsPanel.add(enrollElectiveButton);
        electiveButtonsPanel.add(dropElectiveButton);
        electiveButtonsPanel.add(confirmSelectionButton);
        electivePanel.add(electiveButtonsPanel, BorderLayout.SOUTH);

        // Panel for Enrolled Electives
        JPanel enrolledElectivePanel = new JPanel(new BorderLayout(5, 5));
        enrolledElectivePanel.setBorder(BorderFactory.createTitledBorder("Обрані вибіркові дисципліни")); // UIManager встановить шрифт
        enrolledElectiveListModel = new DefaultListModel<>();
        enrolledElectiveList = new JList<>(enrolledElectiveListModel);
        enrolledElectiveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrolledElectiveList.setCellRenderer(new DisciplineListRenderer());
        enrolledElectiveList.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Шрифт для JList
        enrolledElectivePanel.add(new JScrollPane(enrolledElectiveList), BorderLayout.CENTER);
        enrolledElectiveList.addMouseListener(new DisciplineInfoMouseAdapter());

        // JSplitPane для вибіркових і обраних вибіркових
        JSplitPane electiveSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        electiveSplitPane.setResizeWeight(0.5); // Equal split
        electiveSplitPane.setLeftComponent(electivePanel);
        electiveSplitPane.setRightComponent(enrolledElectivePanel);

        if ("Бакалаврат".equals(selectedDegree) && selectedCourse == 1) {
            enrollElectiveButton.setEnabled(false);
            dropElectiveButton.setEnabled(false);
            searchField.setEnabled(false);
            searchButton.setEnabled(false);

            JLabel noElectivesMessage = new JLabel("На першому курсі бакалаврату немає можливості обирати додаткові дисципліни", SwingConstants.CENTER);
            noElectivesMessage.setFont(new Font("Segoe UI", Font.BOLD, 14));
            noElectivesMessage.setForeground(RED.darker());

            electivePanel.removeAll();
            electivePanel.setLayout(new BorderLayout());
            electivePanel.add(noElectivesMessage, BorderLayout.CENTER);
            electivePanel.add(electiveButtonsPanel, BorderLayout.SOUTH); // Додаємо панель кнопок знов, щоб confirmSelectionButton була видима
            electivePanel.setBorder(BorderFactory.createTitledBorder("Вибіркові дисципліни (недоступно)"));

            enrolledElectivePanel.removeAll();
            enrolledElectivePanel.setLayout(new BorderLayout());
            enrolledElectivePanel.add(new JLabel("Ваші обрані вибіркові дисципліни будуть відображені з 2-го курсу", SwingConstants.CENTER), BorderLayout.CENTER);
            ((JLabel) enrolledElectivePanel.getComponent(0)).setFont(new Font("Segoe UI", Font.PLAIN, 12));
            ((JLabel) enrolledElectivePanel.getComponent(0)).setForeground(BLACK.darker());
            enrolledElectivePanel.setBorder(BorderFactory.createTitledBorder("Обрані вибіркові дисципліни"));

            updateConfirmButtonState();
        }

        topContentPanel.add(electiveSplitPane, BorderLayout.CENTER);

        // Mandatory Disciplines Panel
        JPanel mandatoryPanel = new JPanel(new BorderLayout(5, 5));
        mandatoryPanel.setBorder(BorderFactory.createTitledBorder("Обов'язкові дисципліни")); // UIManager встановить шрифт
        mandatoryListModel = new DefaultListModel<>();
        mandatoryDisciplineList = new JList<>(mandatoryListModel);
        mandatoryDisciplineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mandatoryDisciplineList.setCellRenderer(new DisciplineListRenderer());
        mandatoryDisciplineList.setEnabled(false); // Обов'язкові не змінюються
        mandatoryDisciplineList.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Шрифт для JList
        mandatoryPanel.add(new JScrollPane(mandatoryDisciplineList), BorderLayout.CENTER);
        mandatoryDisciplineList.addMouseListener(new DisciplineInfoMouseAdapter());

        Color simsPink = new Color(255, 233, 243, 255);
        Color simsAccent = new Color(66, 244, 180);
        Color simsLightPink = new Color(255, 251, 253);

        // Основне тло
        mainPanel.setBackground(simsPink);
        studentInfoPanel.setBackground(simsPink);
        searchPanel.setBackground(simsPink);
        electiveButtonsPanel.setBackground(simsPink);
        mandatoryPanel.setBackground(simsPink);
        electivePanel.setBackground(simsPink);
        enrolledElectivePanel.setBackground(simsPink);
        topContentPanel.setBackground(simsPink);

        // Кнопки
        enrollElectiveButton.setBackground(simsAccent);
        dropElectiveButton.setBackground(simsAccent);
        confirmSelectionButton.setBackground(simsAccent);
        searchButton.setBackground(simsAccent);

        // Списки
        electiveDisciplineList.setBackground(simsLightPink);
        enrolledElectiveList.setBackground(simsLightPink);
        mandatoryDisciplineList.setBackground(simsLightPink);

        // Заголовки меж
        ((javax.swing.border.TitledBorder) electivePanel.getBorder()).setTitleColor(BLACK);
        ((javax.swing.border.TitledBorder) enrolledElectivePanel.getBorder()).setTitleColor(BLACK);
        ((javax.swing.border.TitledBorder) mandatoryPanel.getBorder()).setTitleColor(BLACK);

        // Головна панель (mainPanel) тепер організовує розташування
        mainPanel.add(topContentPanel, BorderLayout.CENTER); // Вибіркові у центрі
        mainPanel.add(mandatoryPanel, BorderLayout.SOUTH); // Обов'язкові знизу

        add(mainPanel); // Додаємо головну панель до фрейму
        setVisible(true);

        updateDisciplineLists();
        updateStudentInfoDisplay();
        updateConfirmButtonState();

        autoEnrollTimer = new Timer(0, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (enrollmentSystem.getVirtualStudentsEnrolledCount() >= EnrollmentSystem.MAX_VIRTUAL_STUDENTS_TO_ENROLL) {
                    autoEnrollTimer.stop();
                    return;
                }
                enrollmentSystem.randomlyIncrementElectiveEnrollment();
                electiveDisciplineList.repaint();
                enrolledElectiveList.repaint();
            }
        });
        autoEnrollTimer.start();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                UIManager.put("OptionPane.yesButtonText", "Так");
                UIManager.put("OptionPane.noButtonText", "Ні");

                boolean wasTimerRunning = (autoEnrollTimer != null && autoEnrollTimer.isRunning());

                int confirm = JOptionPane.showConfirmDialog(
                        EnrollmentSystemGUI.this,
                        "Ви дійсно хочете завершити поточну гру та повернутися на початок?",
                        "Завершити гру",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    if (autoEnrollTimer != null) {
                        autoEnrollTimer.stop();
                    }
                    EnrollmentSystemGUI.this.dispose();
                    //SwingUtilities.invokeLater(() -> new StartWindow().setVisible(true));
                    //ЗМІНИТИ ТУТ!!!
                }
            }
        });
    }

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

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private String showDegreeSelectionDialog() {
        String[] degrees = {"Бакалаврат", "Магістратура"};

        String selectedDegreeStr = (String) JOptionPane.showInputDialog(
                this, // Використовуємо 'this' оскільки метод знаходиться в класі JFrame
                "Будь ласка, оберіть Ваш освітній ступінь!",
                "Вибір освітнього ступеня",
                JOptionPane.QUESTION_MESSAGE,
                null,
                degrees,
                degrees[0]);
        return selectedDegreeStr;
    }

    private int showCourseSelectionDialog(String degree) {
        String[] courses;
        String dialogTitle;
        String dialogMessage;

        if ("Бакалаврат".equals(degree)) {
            courses = new String[]{"1", "2", "3", "4"};
            dialogTitle = "Вибір курсу (Бакалаврат)";
            dialogMessage = "Будь ласка, оберіть Ваш курс бакалаврату!";
        } else if ("Магістратура".equals(degree)) {
            courses = new String[]{"1", "2"};
            dialogTitle = "Вибір курсу (Магістратура)";
            dialogMessage = "Будь ласка, оберіть Ваш курс магістратури!";
        } else {
            JOptionPane.showMessageDialog(this, "Неправильний освітній ступінь.", "Помилка", JOptionPane.ERROR_MESSAGE);
            return -1;
        }

        String selectedCourseStr = (String) JOptionPane.showInputDialog(
                this, // Використовуємо 'this' оскільки метод знаходиться в класі JFrame
                dialogMessage,
                dialogTitle,
                JOptionPane.QUESTION_MESSAGE,
                null,
                courses,
                courses[0]);

        if (selectedCourseStr != null) {
            try {
                return Integer.parseInt(selectedCourseStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Неправильний формат курсу. Спробуйте ще раз.", "Помилка", JOptionPane.ERROR_MESSAGE);
                // Повторно запитуємо, якщо виникла помилка формату
                return showCourseSelectionDialog(degree);
            }
        }
        return -1;
    }

    private void showStartAnimation() {
        JDialog animationDialog = new JDialog(this, true);
        animationDialog.setUndecorated(true);

        Color simsPink = new Color(255, 168, 205, 255);
        Color simsAccent1 = new Color(66, 244, 180);
        Color simsAccent2 = new Color(34, 149, 107);
        Color simsAccent3 = new Color(66, 244, 191);

        animationDialog.getContentPane().setBackground(simsPink);

        JLabel animationLabel = new JLabel("", SwingConstants.CENTER);
        animationLabel.setFont(new Font("Segoe UI", Font.BOLD, 80));
        animationLabel.setForeground(simsAccent1);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(animationLabel);

        animationDialog.setContentPane(contentPanel);
        animationDialog.setSize(400, 200);
        animationDialog.setLocationRelativeTo(this);

        final String[] phases = {"Старт!", "Увага!", "Руш!"};
        final Color[] textColors = {simsAccent1, simsAccent2, simsAccent3};
        final int[] currentPhase = {0};

        Timer timer = new Timer(150, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentPhase[0] < phases.length) {
                    animationLabel.setText(phases[currentPhase[0]]);
                    animationLabel.setForeground(textColors[currentPhase[0]]);
                    currentPhase[0]++;
                } else {
                    ((Timer) e.getSource()).stop();
                    animationDialog.dispose();
                }
            }
        });
        timer.setInitialDelay(50);
        timer.start();

        animationDialog.setVisible(true);
    }

    // Method to initialize test data based on selected course
    private void initializeInitialDataB(int selectedCourse) {
        enrollmentSystem.addStudent(new Student("І 005/24 бп", "Студент", selectedCourse, currentStudent.getMajor()));

        if ((currentStudent.getMajor().equals("121 Інженерія програмного забезпечення")) && (selectedCourse == 1)) {
            enrollmentSystem.addDiscipline(new Discipline("315203", "Алгоритми і структури даних", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М., ст. викл. Кирієнко О. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314809", "Англійська мова", "ст. викл. Гісем С. О.", 7, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315275", "Вступ до програмування", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315213", "Диференціальні рівняння", "доц. Митник Ю. В., ст. викл. Силенко І. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315231", "Комп'ютерні архітектури", "ст. викл. Медвідь С. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315200", "Лінійна алгебра та аналітична геометрія", "доц. Чорней Р. К., ас. Сарана М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315223", "Моделі обчислень в програмній інженерії", "доц. Проценко В. С.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315283", "Основи вебтехнологій", "ст. викл. Зважій Д. В. ", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315202", "Основи дискретної математики", "ст. викл. Щеглов М. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315199", "Основи матаналізу", "ст. викл. Іванюк А. О.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315277", "Основи мережевих технологій", "ст. викл. Вознюк О. М., ст. викл. Вознюк Я. І.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314810", "Українська мова", "доц. Сегін Л. В., ст. викл. Калиновська О. М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314804", "Фізичне виховання", "викл. Жуков В. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315204", "Практика навчальна", "ст. викл. Пєчкурова О. М., ст. викл. Кирієнко О. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        }

        else if ((currentStudent.getMajor().equals("121 Інженерія програмного забезпечення")) && (selectedCourse == 2)) {
            enrollmentSystem.addDiscipline(new Discipline("340214", "Англійська мова (за проф. спрямуванням)", "проф. Дерев'янко А. С.", 7, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340447", "Бази даних", "ст. викл. Кушнір О. В.", 8, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340443", "Вступ до тестування програмного забезпечення", "доц. Сидоров М. І.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340418", "Об'єктно-орієнтоване програмування", "проф. Бублик В. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340370", "Основи комп'ютерних алгоритмів", "проф. Іванов Л. М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
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

        } else if ((currentStudent.getMajor().equals("121 Інженерія програмного забезпечення")) && (selectedCourse == 3)) {
            enrollmentSystem.addDiscipline(new Discipline("315429", "Функціональне програмування", "проф. Математикус А. Б.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("315225", "Системне програмування", "доц. Жежерун О. П.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("315425", "Логічне програмування", "ст. викл. В.І. Системний", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("315218", "Теорія ймовірностей", "ст. викл. Братик М. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("315217", "Вебпрограмування", "ст. викл. Олецький О. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
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

        } else if ((currentStudent.getMajor().equals("121 Інженерія програмного забезпечення")) && (selectedCourse == 4)) {
            enrollmentSystem.addDiscipline(new Discipline("315263", "Багатозадачне та паралельне програмування", "ст. викл. Сідько А. А., ст. викл. Гречко А. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("315222", "Проєктування програмних систем", "ст. викл. Афонін А. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("315230", "Об'єктно-орієнтований аналіз і дизайн", "ст. викл. Мобільний Д. С.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("315233", "Структура програмних проєктів", "проф. Керівник В. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("315224", "Інтелектуальні системи", "ст. викл. Жежерун О. П.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("315215", "Забезпечення якости програмних продуктів", "ст. викл. Афонін А. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));

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

        } else if ((currentStudent.getMajor().equals("113 Прикладна математика")) && (selectedCourse == 1)){
            enrollmentSystem.addDiscipline(new Discipline("314809", "Англійська мова", "ст. викл. Гісем С. О.", 7, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315275", "Програмування", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М.", 7, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315213", "Історія математики", "доц. Митник Ю. В., ст. викл. Силенко І. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315200", "Алгебра та геометрія", "доц. Чорней Р. К., ас. Сарана М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315202", "Дискретна математика", "ст. викл. Щеглов М. В.", 6, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315199", "Математичний аналіз", "ст. викл. Іванюк А. О.", 11, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314810", "Українська мова", "доц. Сегін Л. В., ст. викл. Калиновська О. М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314804", "Фізичне виховання", "викл. Жуков В. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        }

        else if ((currentStudent.getMajor().equals("113 Прикладна математика")) && (selectedCourse == 2)){
            enrollmentSystem.addDiscipline(new Discipline("314809", "Англійська мова", "ст. викл. Гісем С. О.", 3.5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("315275", "Програмування", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М.", 7, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("315200", "Алгебра та геометрія", "доц. Чорней Р. К., ас. Сарана М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("315199", "Математичний аналіз", "ст. викл. Іванюк А. О.", 11, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("315434", "Бази даних та інформаційні системи", "проф. Глибовець А. М.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("316215", "Випадкові процеси", "доц. Чорней Р.К., ас. Тищенко С.В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("316214", "Теорія ймовірностей", "доц. Чорней Р.К., ас. Тищенко С.В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("315213", "Диференціальні рівняння", "доц. Митник Ю. В., ст. викл. Силенко І. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));

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

        else if ((currentStudent.getMajor().equals("113 Прикладна математика")) && (selectedCourse == 3)){
            enrollmentSystem.addDiscipline(new Discipline("316149", "Методи оптимізації та дослідження операцій", "доц. Михалевич В. М., ас. Тищенко С. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("316148", "Теорія алгоритмів та математична логіка", "ст. викл. Прокоф'єв П. Г.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("316209", "Теорія функції комплексної змінної", "доц. Кашпіровський О. І.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
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

        else if ((currentStudent.getMajor().equals("113 Прикладна математика")) && (selectedCourse == 4)){
            enrollmentSystem.addDiscipline(new Discipline("316150", "Теорія керування", "доц. Чорней Р. К.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("316819", "Економетрика", "к. е. н., доц. Семко Р. Б.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("316141", "Функціональний аналіз", "доц. Кашпіровський О. І.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("316151", "Аналіз даних", "доц. Щестюк Н. Ю., ас. Тищенко С. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
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

        else if ((currentStudent.getMajor().equals("122 Комп'ютерні науки")) && (selectedCourse == 1)){
            enrollmentSystem.addDiscipline(new Discipline("315203", "Алгоритми і структури даних", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М., ст. викл. Кирієнко О. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314835", "Архітектура обчислювальних систем", "ст. викл. Медвідь С. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314827", "Історія української кібернетики", "проф. Глибовець А. М.", 2, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315453", "Організація та обробка електронної інформації", "ст. викл. Сініцина Р. Б.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315202", "Дискретна математика", "ст. викл. Щеглов М. В.", 8, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314809", "Англійська мова", "ст. викл. Гісем С. О.", 7, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315275", "Мови програмування", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315200", "Алгебра та геометрія", "доц. Чорней Р. К., ас. Сарана М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314804", "Фізичне виховання", "викл. Жуков В. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315199", "Математичний аналіз", "ст. викл. Іванюк А. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        }

        else if ((currentStudent.getMajor().equals("122 Комп'ютерні науки")) && (selectedCourse == 2)){
            enrollmentSystem.addDiscipline(new Discipline("340214", "Англійська мова (за проф. спрямуванням)", "проф. Дерев'янко А. С.", 7, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340447", "Бази даних", "ст. викл. Кушнір О. В.", 8, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340443", "Вступ до тестування програмного забезпечення", "доц. Сидоров М. І.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340418", "Об'єктно-орієнтоване програмування", "проф. Бублик В. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340370", "Основи комп'ютерних алгоритмів", "проф. Іванов Л. М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340362", "Побудова і використання комунікаційних мереж", "ст. викл. Черкасов Д. І., ст. викл. Савченко Т. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340356", "Процедурне програмування (на базі Сі/Сі++) (ПІ)", "проф. Бублик В. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340377", "Теорія алгоритмів і математична логіка", "проф. Сахаров Г. М.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("315366", "Практика дослідницька", "ст. викл. Пєчкурова О. М., ст. викл. Кирієнко О. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));

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

        else if ((currentStudent.getMajor().equals("122 Комп'ютерні науки")) && (selectedCourse == 3)){
            enrollmentSystem.addDiscipline(new Discipline("315429", "Функціональне програмування", "проф. Математикус А. Б.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("315225", "Методи розробки програмних систем", "доц. Жежерун О. П.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("315425", "Основи штучного інтелекту", "ст. викл. В.І. Системний", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("315218", "Аналіз великих даних (Big Data)", "ст. викл. Братик М. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
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

        else if ((currentStudent.getMajor().equals("122 Комп'ютерні науки")) && (selectedCourse == 4)){
            enrollmentSystem.addDiscipline(new Discipline("315470", "Інтелектуальні мережі", "ст. викл. Сідько А. А., ст. викл. Гречко А. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("315206", "Комп'ютерна графіка", "проф. Липинський І. С.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 4));

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

private void initializeInitialDataM(int selectedCourse) {
    enrollmentSystem.addStudent(new Student("І 005/24 мп", "Студент", selectedCourse, currentStudent.getMajor()));

    if ((currentStudent.getMajor().equals("121 Інженерія програмного забезпечення")) && (selectedCourse == 1)) {
        enrollmentSystem.addDiscipline(new Discipline("317203", "Архітектура інформаційних систем", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М., ст. викл. Кирієнко О. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("317809", "Моделі і методи розробки програмного забезпечення", "ст. викл. Гісем С. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("317275", "Паралельне програмування", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("317213", "Проєктування програмних систем", "доц. Митник Ю. В., ст. викл. Силенко І. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("317598", "Структури даних та аналіз алгоритмів", "ст. викл. Медвідь С. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("317200", "Практика науково-дослідницька", "доц. Чорней Р. К., ас. Сарана М.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("317223", "Забезпечення якости програмного продукту", "доц. Проценко В. С.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("317283", "Патерни проєктування та дизайн АРІ", "ст. викл. Зважій Д. В. ", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("317202", "Побудова високонавантажених систем", "ст. викл. Щеглов М. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("317199", "Розподілені бази даних та знань", "ст. викл. Іванюк А. О.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("317277", "Управління проєктами в інженерії програмного забезпечення", "ст. викл. Вознюк О. М., ст. викл. Вознюк Я. І.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("317810", "Практика науково-дослідницька", "доц. Сегін Л. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("317804", "Практика науково-дослідницька", "викл. Жуков В. О.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));

        enrollmentSystem.addDiscipline(new Discipline("317439", "Інформаційна безпека", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
        enrollmentSystem.addDiscipline(new Discipline("317393", "Математична теорія ігор", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
        enrollmentSystem.addDiscipline(new Discipline("317191", "Моделі та алгоритми інформаційного пошуку", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
        enrollmentSystem.addDiscipline(new Discipline("317306", "Хмарні технології", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
        enrollmentSystem.addDiscipline(new Discipline("317261", "Чистий код та чиста архітектура", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
    }

    else if ((currentStudent.getMajor().equals("121 Інженерія програмного забезпечення")) && (selectedCourse == 2)) {
        enrollmentSystem.addDiscipline(new Discipline("318214", "Аналіз даних великого розміру (Big Data)", "проф. А.С. Дерев'янко", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
        enrollmentSystem.addDiscipline(new Discipline("318447", "Методологія наукових досліджень в програмній інженерії", "ст. викл. О.В. Кушнір", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
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
    if ((currentStudent.getMajor().equals("113 Прикладна математика")) && (selectedCourse == 1)){
        enrollmentSystem.addDiscipline(new Discipline("318403", "Комп'ютерний зір / Computer Vision (англ. мовою)", "доц. Швай Н. О.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("318376", "Динамічні системи", "проф. Авраменко О. В.", 7, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("318404", "Машинне навчання / Machine Learning (англ. мовою)", "доц. Швай Н. О.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("318382", "Теорія складности алгоритмів", "ст. викл. Морозов Д. І.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        enrollmentSystem.addDiscipline(new Discipline("318386", "Технології чисельного моделювання", "доц. Тригуб О. С.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
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

    else if ((currentStudent.getMajor().equals("113 Прикладна математика")) && (selectedCourse == 2)){
        enrollmentSystem.addDiscipline(new Discipline("318368", "Методика викладання математики та інформатики у вищій школі", "проф. Федосова І. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
        enrollmentSystem.addDiscipline(new Discipline("320170", "Практика асистентська", "ст. викл. О.В. Кушнір", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
        enrollmentSystem.addDiscipline(new Discipline("318492", "Науково-дослідний семінар", "доц. Михалевич В. М.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
        enrollmentSystem.addDiscipline(new Discipline("318383", "Стохастична фінансова математика / Stochastic Financial Mathematics (англ.мовою)", "доц. Щестюк Н. Ю.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
        enrollmentSystem.addDiscipline(new Discipline("320086", "Педагогіка і психологія вищої школи", "проф. Власенко К. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
        enrollmentSystem.addDiscipline(new Discipline("318500", "Теорія оптимального керування", "доц. Чорней Р. К.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
        enrollmentSystem.addDiscipline(new Discipline("318377", "Нелінійні процеси та моделі", "проф. Авраменко О. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));

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
    else if ((currentStudent.getMajor().equals("122 Комп'ютерні науки")) && (selectedCourse == 1)){
        enrollmentSystem.addDiscipline(new Discipline("317722", "Математична теорія ігор", "проф. Глибовець А. М.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));

        enrollmentSystem.addDiscipline(new Discipline("317439", "Інформаційна безпека", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
        enrollmentSystem.addDiscipline(new Discipline("317393", "Математична теорія ігор", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
        enrollmentSystem.addDiscipline(new Discipline("317191", "Моделі та алгоритми інформаційного пошуку", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
        enrollmentSystem.addDiscipline(new Discipline("317306", "Хмарні технології", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
        enrollmentSystem.addDiscipline(new Discipline("317261", "Чистий код та чиста архітектура", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 1));
    }
    else if ((currentStudent.getMajor().equals("122 Комп'ютерні науки")) && (selectedCourse == 2)){
        enrollmentSystem.addDiscipline(new Discipline("317722", "Математична теорія ігор", "проф. Глибовець А. М.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));

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

    // --- Update Discipline Lists in GUI ---
    private void updateDisciplineLists() {
        mandatoryListModel.clear();
        electiveListModel.clear(); // Очищаємо перед оновленням
        enrolledElectiveListModel.clear();

        int studentCourse = currentStudent.getCourse();

        for (Discipline disc : enrollmentSystem.getMandatoryDisciplines(studentCourse)) {
            mandatoryListModel.addElement(disc);
        }

        List<Discipline> studentEnrolledDisciplines = currentStudent.getEnrolledDisciplines();

        String searchText = searchField.getText().toLowerCase(); // Беремо текст пошуку

        for (Discipline disc : enrollmentSystem.getElectiveDisciplines(studentCourse)) {
            // Перевіряємо, чи дисципліна відповідає пошуковому запиту (якщо він є)
            boolean matchesSearch = searchText.isEmpty() || disc.getName().toLowerCase().contains(searchText);

            if (studentEnrolledDisciplines.contains(disc)) {
                enrolledElectiveListModel.addElement(disc);
            } else if (matchesSearch) { // Додаємо до списку доступних, тільки якщо відповідає пошуку
                electiveListModel.addElement(disc);
            }
        }

        electiveDisciplineList.repaint();
        enrolledElectiveList.repaint();
        updateStudentInfoDisplay(); // Update student info with new credit count
        updateConfirmButtonState();
    }

    private void filterElectiveDisciplines(String searchText) {
        updateDisciplineLists();
    }

    // --- Update Student Information Display ---
    private void updateStudentInfoDisplay() {
        double totalCredits = 0;
        for (Discipline discipline : currentStudent.getEnrolledDisciplines()) {
            double credits = discipline.getCredits();
            totalCredits += credits;
        }
        int courseCreditLimit = 0;
        switch (currentStudent.getCourse()) {
            case 1:
                courseCreditLimit = 61;
                break;
            default:
                courseCreditLimit = 62;
        }

        studentInfoLabel.setText("Студент (ID: " + currentStudent.getStudentId() +
                ", курс: " + currentStudent.getCourse() +
                ", кредити: " + totalCredits + "/" + courseCreditLimit + ")");
    }


    // --- Update "Готово" button state based on total credits ---
    private void updateConfirmButtonState() {
        double totalCredits = 0;
        for (Discipline discipline : currentStudent.getEnrolledDisciplines()) {
            double credits = discipline.getCredits();
            totalCredits += credits;
        }
        confirmSelectionButton.setText("Готово (кредитів: " + totalCredits + ")");
        confirmSelectionButton.setEnabled(totalCredits >= EnrollmentSystem.MIN_CREDITS_TO_CONFIRM);
    }

    // --- Simulate random glitches ---
    private boolean simulateGlitch() {
        if (randomGlitches.nextDouble() < 0.7) {
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

    // --- Logic for attempting to enroll in an elective discipline ---
    private void attemptEnrollment() {
        if (simulateGlitch()) {
            return;
        }

        Discipline selectedDiscipline = electiveDisciplineList.getSelectedValue();

        if (selectedDiscipline == null) {
            JOptionPane.showMessageDialog(this,
                    "Будь ласка, оберіть вибіркову дисципліну для запису.",
                    "Помилка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return enrollmentSystem.enrollStudentToDiscipline(currentStudent.getStudentId(), selectedDiscipline.getDisciplineId());
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    appendOutput(result + "\n");
                    updateDisciplineLists(); // Refresh all lists
                } catch (Exception ex) {
                    appendOutput("Виникла непередбачувана помилка під час виконання запису: " + ex.getMessage() + "\n");
                }
            }
        }.execute();
    }

    // --- Logic for attempting to drop from an elective discipline ---
    private void attemptDrop() {
        if (simulateGlitch()) {
            return;
        }

        // Only allow dropping from the 'enrolledElectiveList'
        Discipline selectedDiscipline = enrolledElectiveList.getSelectedValue();

        if (selectedDiscipline == null) {
            JOptionPane.showMessageDialog(this,
                    "Будь ласка, оберіть вибіркову дисципліну для виписки з розділу 'Ваші обрані'.",
                    "Помилка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ensure the selected discipline is indeed one the student is currently enrolled in
        if (!currentStudent.getEnrolledDisciplines().contains(selectedDiscipline)) {
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
                    return enrollmentSystem.dropStudentFromDiscipline(currentStudent.getStudentId(), selectedDiscipline.getDisciplineId());
                }

                @Override
                protected void done() {
                    try {
                        String result = get();
                        appendOutput(result + "\n");
                        updateDisciplineLists(); // Refresh all lists
                    } catch (Exception ex) {
                        appendOutput("Виникла непередбачена помилка під час виконання виписки: " + ex.getMessage() + "\n");
                    }
                }
            }.execute();
        } else {
            appendOutput("Виписка скасована.\n");
        }
    }

    // --- Handling completion of selection based on total credits ---
    private void confirmSelection() {
        double totalCredits = 0;
        for (Discipline discipline : currentStudent.getEnrolledDisciplines()) {
            double credits = discipline.getCredits();
            totalCredits += credits;
        }

        if (totalCredits >= EnrollmentSystem.MIN_CREDITS_TO_CONFIRM) {
            // Зупиняємо таймер автоматичного запису
            if (autoEnrollTimer != null && autoEnrollTimer.isRunning()) {
                autoEnrollTimer.stop();
            }

            // Вимикаємо всі елементи керування, щоб запобігти подальшим змінам
            enrollElectiveButton.setEnabled(false);
            dropElectiveButton.setEnabled(false);
            confirmSelectionButton.setEnabled(false);
            electiveDisciplineList.setEnabled(false);
            enrolledElectiveList.setEnabled(false);
            searchField.setEnabled(false);
            searchButton.setEnabled(false);

            appendOutput("Запис завершено! Подальші зміни неможливі.\n");

            // Повідомлення про проходження першого рівня
            JOptionPane.showMessageDialog(this,
                    "Вітаємо! Перший рівень пройдено!",
                    "Запис завершено",
                    JOptionPane.INFORMATION_MESSAGE);

            // Закриття вікна програми
            dispose(); // Закриває поточне вікно
            System.exit(0); // Завершує виконання програми
        } else {
            JOptionPane.showMessageDialog(this,
                    "Ви повинні обрати дисципліни мінімум на " + EnrollmentSystem.MIN_CREDITS_TO_CONFIRM + " кредитів, щоб завершити вибір. Обрано: " + totalCredits + ".\n",
                    "Помилка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void appendOutput(String text) {
        JOptionPane.showMessageDialog(this, text, "Повідомлення", JOptionPane.INFORMATION_MESSAGE);
    }

    // Inner class for MouseListener to display discipline info
    private class DisciplineInfoMouseAdapter extends MouseAdapter {
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
                        infoBuilder.append("Викладач: ").append(selectedDiscipline.getInstructor()).append("\n");
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

        public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("nimbusBase", SIMS_MEDIUM_PINK);
            UIManager.put("nimbusBlueGrey", SIMS_LIGHT_BLUE);
            UIManager.put("control", SIMS_LIGHT_PINK);
            UIManager.put("textForeground", SIMS_DARK_TEXT);

            SwingUtilities.invokeLater(EnrollmentSystemGUI::new);
        }
}
