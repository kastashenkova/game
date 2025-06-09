package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Random;

import static java.awt.Color.*;

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

    private Student currentStudent;
    private Timer autoEnrollTimer;
    private Random randomGlitches = new Random();

    // Internal class for customizing list item display in JList
    static class DisciplineListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Discipline) {
                Discipline disc = (Discipline) value;
                if (!disc.isMandatory()) {
                    label.setText(disc.getName() + " (Зайнято місць: " + disc.getCurrentEnrollment() + "/" + disc.getMaxCapacity() + ")");
                    if (!disc.hasAvailableSlots()) {
                        label.setForeground(RED.darker());
                    } else if (disc.getCurrentEnrollment() > disc.getMaxCapacity() * 0.75) {
                        label.setForeground(new Color(200, 100, 0)); // Orange for nearing capacity
                    } else {
                        label.setForeground(list.getForeground());
                    }
                } else {
                    label.setText(disc.getName());
                }
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

        // --- Course Selection ---
        int selectedCourse = showCourseSelectionDialog();
        if (selectedCourse == -1) { // User cancelled
            System.exit(0);
        }

        // Initialize data based on selected course
        initializeInitialData(selectedCourse);

        currentStudent = enrollmentSystem.getStudentById("І 005/24 бп").orElse(null);
        if (currentStudent == null) {
            JOptionPane.showMessageDialog(this, "Помилка. Студента не знайдено після вибору курсу.", "Помилка", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Automatically enroll currentStudent in all mandatory disciplines for their course
        for (Discipline mandatoryDisc : enrollmentSystem.getMandatoryDisciplines(currentStudent.getCourse())) {
            currentStudent.enrollDiscipline(mandatoryDisc);
        }

        setSize(1200, 800); // Збільшено розмір для кращого відображення
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

        // Додаємо electiveSplitPane до верхньої панелі
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

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                autoEnrollTimer.stop();
                System.exit(0);
            }
        });
    }

    // --- Course Selection Dialog ---
    private int showCourseSelectionDialog() {
        String[] courses = {"2", "3", "4"};

        // Встановлюємо український текст для кнопок "OK" та "Cancel"
        UIManager.put("OptionPane.okButtonText", "ОК");
        UIManager.put("OptionPane.cancelButtonText", "Скасувати");

        String selectedCourseStr = (String) JOptionPane.showInputDialog(
                this,
                "Будь ласка, оберіть Ваш курс!",
                "Вибір курсу",
                JOptionPane.QUESTION_MESSAGE,
                null,
                courses,
                courses[0]);

        if (selectedCourseStr != null) {
            try {
                return Integer.parseInt(selectedCourseStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Неправильний формат курсу. Спробуйте ще раз.", "Помилка", JOptionPane.ERROR_MESSAGE);
                return showCourseSelectionDialog(); // Re-prompt on error
            }
        }
        return -1; // User cancelled
    }

    // Method to initialize test data based on selected course
    // ТРЕБА ВІДРЕДАГУВАТИ ВИКЛАДАЧІВ, ОБОВ'ЯЗКОВІСТЬ І КУРСИ
    private void initializeInitialData(int selectedCourse) {
        enrollmentSystem.addStudent(new Student("І 005/24 бп", "Студент", selectedCourse, "Інженерія програмного забезпечення"));

        enrollmentSystem.addDiscipline(new Discipline("315212", "Алгебра і теорія чисел", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315212", "Алгебра і теорія чисел", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315212", "Алгебра і теорія чисел", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("319948", "Базові алгоритми обробки природної мови", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("319948", "Базові алгоритми обробки природної мови", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("319948", "Базові алгоритми обробки природної мови", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));


        enrollmentSystem.addDiscipline(new Discipline("315264", "Електроніка та цифрова електроніка", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315264", "Електроніка та цифрова електроніка", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315264", "Електроніка та цифрова електроніка", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315271", "Історія розвитку кібернетики в Україні", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315271", "Історія розвитку кібернетики в Україні", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315271", "Історія розвитку кібернетики в Україні", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315237", "Математичні методи обробки зображень", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315237", "Математичні методи обробки зображень", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315237", "Математичні методи обробки зображень", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));

        enrollmentSystem.addDiscipline(new Discipline("315193", "Обробка зображень", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315193", "Обробка зображень", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315193", "Обробка зображень", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));


        enrollmentSystem.addDiscipline(new Discipline("315209", "Основи комп`ютерних алгоритмів", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315209", "Основи комп`ютерних алгоритмів", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315209", "Основи комп`ютерних алгоритмів", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));


        enrollmentSystem.addDiscipline(new Discipline("315305", "Програмування на Python для Big Data та Data Science", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315305", "Програмування на Python для Big Data та Data Science", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315305", "Програмування на Python для Big Data та Data Science", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));


        enrollmentSystem.addDiscipline(new Discipline("319959", "Соціальна інженерія", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("319959", "Соціальна інженерія", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("319959", "Соціальна інженерія", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));


        enrollmentSystem.addDiscipline(new Discipline("315296", "Машинне навчання та доповнена реальність на мобільних пристроях на базі iOS", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315296", "Машинне навчання та доповнена реальність на мобільних пристроях на базі iOS", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315296", "Машинне навчання та доповнена реальність на мобільних пристроях на базі iOS", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315298", "Мова програмування Swift", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315298", "Мова програмування Swift", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315298", "Мова програмування Swift", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315307", "Мова програмування Rust", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315307", "Мова програмування Rust", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315307", "Мова програмування Rust", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315294", "Низькорівневі вразливості програмного забезпечення", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315294", "Низькорівневі вразливості програмного забезпечення", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315294", "Низькорівневі вразливості програмного забезпечення", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("319983", "Big Data та аналітика", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("319983", "Big Data та аналітика", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("319983", "Big Data та аналітика", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("322608", "Computer Vision", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("322608", "Computer Vision", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("322608", "Computer Vision", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("316204", "Алгоритми на графах", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("316204", "Алгоритми на графах", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("316204", "Алгоритми на графах", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("316151", "Аналіз даних", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("316151", "Аналіз даних", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("316151", "Аналіз даних", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("322548", "Архітектура програмних систем", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("322548", "Архітектура програмних систем", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("322548", "Архітектура програмних систем", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315263", "Багатозадачне та паралельне програмування", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315263", "Багатозадачне та паралельне програмування", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315263", "Багатозадачне та паралельне програмування", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315297", "Вибрані фреймворки для iOS", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315297", "Вибрані фреймворки для iOS", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315297", "Вибрані фреймворки для iOS", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315215", "Забезпечення якості програмних продуктів", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315215", "Забезпечення якості програмних продуктів", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315215", "Забезпечення якості програмних продуктів", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315470", "Інтелектуальні мережі", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315470", "Інтелектуальні мережі", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315470", "Інтелектуальні мережі", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315224", "Інтелектуальні системи", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315224", "Інтелектуальні системи", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315224", "Інтелектуальні системи", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315295", "Інформаційна безпека цільових систем", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315295", "Інформаційна безпека цільових систем", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315295", "Інформаційна безпека цільових систем", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315443", "Комп`ютерна вірусологія", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315443", "Комп`ютерна вірусологія", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315443", "Комп`ютерна вірусологія", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315206", "Комп`ютерна графіка", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315206", "Комп`ютерна графіка", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315206", "Комп`ютерна графіка", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315230", "Об`єктно-орієнтований аналіз і дизайн", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315230", "Об`єктно-орієнтований аналіз і дизайн", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315230", "Об`єктно-орієнтований аналіз і дизайн", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315269", "Основи IT-права", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315269", "Основи IT-права", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315269", "Основи IT-права", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));

        enrollmentSystem.addDiscipline(new Discipline("315299", "Реактивне програмування в iOS", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315299", "Реактивне програмування в iOS", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 3));
        enrollmentSystem.addDiscipline(new Discipline("315299", "Реактивне програмування в iOS", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 4));


        // Course-specific disciplines
        if (selectedCourse == 2) {
            enrollmentSystem.addDiscipline(new Discipline("340214", "Англійська мова (за проф. спрямуванням)", "Проф. А.С. Дерев'янко", 6, 0, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340447", "Бази даних", "Викл. О.В. Кушнір", 4, 0, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340443", "Вступ до тестування програмного забезпечення", "Доц. М.І. Сидоров", 5, 0, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340418", "Об`єктно-орієнтоване програмування", "Проф. В.П. Залізняк", 5, 0, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340370", "Основи комп`ютерних алгоритмів", "Проф. Л.М. Іванов", 6, 0, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340362", "Побудова і використання комунікаційних мереж", "Доц. Р.Т. Коваленко", 4, 0, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340356", "Процедурне програмування (на базі Сі/Сі++) (ПІ)", "Проф. Г.М. Сахаров", 6, 0, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340377", "Теорія алгоритмів і математична логіка", "Проф. Г.М. Сахаров", 6, 0, true, 2));

            enrollmentSystem.addDiscipline(new Discipline("340439", "Автоматизація роботи з програмними проектами мовою Java", "Викл. П.О. Прокопенко", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340393", "Інформаційний пошук", "Доц. А.В. Мельничук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340317", "Методи та засоби збору чутливої інформації", "Проф. С.В. Кравченко", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340464", "Мова програмування Kotlin", "Викл. Д.І. Бондар", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("E005", "Мова програмування С#", "Доц. Н.П. Осадча", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340402", "Розробка клієнт-серверних застосувань", "Проф. О.М. Гришко", 4, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Управління цифровим продуктом", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));

        } else if (selectedCourse == 3) {
            enrollmentSystem.addDiscipline(new Discipline("C3M01", "Вища математика III", "Проф. А.Б. Математикус", 6, 0, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("C3M02", "Комп'ютерні мережі", "Доц. О.П. Мережевик", 5, 0, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("C3M03", "Операційні системи", "Викл. В.І. Системний", 5, 0, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("C3M04", "Чисельні методи", "Проф. К.Л. Обчислювач", 4, 0, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("C3M05", "Архітектура комп'ютерів", "Доц. П.Р. Залізяка", 4, 0, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("C3M06", "Дискретна математика", "Викл. Т.О. Логік", 4, 0, true, 3));


        } else if (selectedCourse == 4) {
            enrollmentSystem.addDiscipline(new Discipline("C4M01", "Штучний інтелект", "Проф. Г.М. Розумний", 6, 0, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("C4M02", "Машинне навчання", "Доц. М.В. Алгоритмов", 5, 0, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("C4M03", "Розробка мобільних застосунків", "Викл. Д.С. Мобільний", 5, 0, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("C4M04", "Проектний менеджмент в ІТ", "Проф. В.В. Керівник", 4, 0, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("C4M05", "Безпека інформаційних систем", "Доц. Р.Р. Захисник", 4, 0, true, 4));
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
        appendOutput("Пошук дисциплін за запитом: '" + searchText + "'\n");
    }

    // --- Update Student Information Display ---
    private void updateStudentInfoDisplay() {
        int totalCredits = currentStudent.getEnrolledDisciplines().stream()
                .mapToInt(Discipline::getCredits)
                .sum();
        int courseCreditLimit = 0;
        switch (currentStudent.getCourse()) {
            case 2:
                courseCreditLimit = 62;
                break;
            case 3:
                courseCreditLimit = 63;
                break;
            case 4:
                courseCreditLimit = 60;
                break;
            default:
                courseCreditLimit = Integer.MAX_VALUE;
        }

        studentInfoLabel.setText("Студент (ID: " + currentStudent.getStudentId() +
                ", курс: " + currentStudent.getCourse() +
                ", кредити: " + totalCredits + "/" + courseCreditLimit + ")");
    }


    // --- Update "Готово" button state based on total credits ---
    private void updateConfirmButtonState() {
        int totalCredits = currentStudent.getEnrolledDisciplines().stream()
                .mapToInt(Discipline::getCredits)
                .sum();
        confirmSelectionButton.setText("Готово (Кредитів: " + totalCredits + ")");
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
                "Ви впевнені, що хочете виписатися з дисципліни '" + selectedDiscipline.getName() + "'?",
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
        int totalCredits = currentStudent.getEnrolledDisciplines().stream()
                .mapToInt(Discipline::getCredits)
                .sum();

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

            appendOutput("Вибір дисциплін завершено. Подальші зміни не можливі.\n");

            // Повідомлення про проходження першого рівня
            JOptionPane.showMessageDialog(this,
                    "Вітаємо! Ви успішно обрали дисципліни на " + totalCredits + " кредитів.\n" +
                            "Перший рівень пройдено!",
                    "Вибір завершено",
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
            if (e.getClickCount() == 2) { // Double-click to show info
                int index = list.locationToIndex(e.getPoint());
                if (index != -1) {
                    Discipline selectedDiscipline = (Discipline) list.getModel().getElementAt(index);
                    String info = "Інформація про дисципліну:\n" +
                            "Код: " + selectedDiscipline.getDisciplineId() + "\n" +
                            "Назва: " + selectedDiscipline.getName() + "\n" +
                            "Викладач: " + selectedDiscipline.getInstructor() + "\n" +
                            "Кількість кредитів: " + selectedDiscipline.getCredits() + "\n" +
                            "Максимальна кількість студентів: " + selectedDiscipline.getMaxCapacity() + "\n" +
                            "Зараз записано: " + selectedDiscipline.getCurrentEnrollment();
                    JOptionPane.showMessageDialog(EnrollmentSystemGUI.this, info, "Деталі курсу", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EnrollmentSystemGUI::new);
    }
}