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
        if (selectedDegree.equals("Бакалаврат")){
            initializeInitialDataB(selectedCourse);
            currentStudent = enrollmentSystem.getStudentById("І 005/24 бп").orElse(null);
            if (currentStudent == null) {
                JOptionPane.showMessageDialog(this, "Помилка. Студента не знайдено після вибору курсу.", "Помилка", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        } else if (selectedDegree.equals("Магістратура")){
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

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                autoEnrollTimer.stop();
                System.exit(0);
            }
        });
    }

    /**
     * Відображає вікно з інструкціями для користувача перед початком роботи із системою запису.
     */
    private void showInstructionsDialog() {
        String instructions = "<html>" +
                "<body style='font-family: \"Segoe UI\"; font-size: 12px;'>" +
                "<h2>Інструкція щодо запису на вибіркові дисципліни</h2>" +
                "<p>Ласкаво просимо до Системи автоматизованого запису (САЗ) " +
                "Національного університету «Києво-Могилянська академія» (НаУКМА).</p>" +
                "<p>Будь ласка, дотримуйтеся цих кроків для успішного запису</p>" +
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
                "<p>Успішного запису!</p>" +
                "</body></html>";

        // Використовуємо JEditorPane для відображення HTML тексту, щоб він правильно форматувався
        JEditorPane editorPane = new JEditorPane("text/html", instructions);
        editorPane.setEditable(false);
        editorPane.setBackground(new Color(255, 251, 253)); // simsLightPink
        editorPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(600, 450)); // Зручний розмір для інструкцій

        JOptionPane.showMessageDialog(this, scrollPane, "Інструкції", JOptionPane.INFORMATION_MESSAGE);
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
        enrollmentSystem.addStudent(new Student("І 005/24 бп", "Студент", selectedCourse, "121 Інженерія програмного забезпечення"));

        if (selectedCourse == 1) {
            enrollmentSystem.addDiscipline(new Discipline("315203", "Алгоритми і структури даних", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М., ст. викл. Кирієнко О. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314809", "Англійська мова", "ст. викл. Гісем С. О.", 7, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315275", "Вступ до програмування", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315213", "Диференціальні рівняння", "доц. Митник Ю. В., ст. викл. Силенко І. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315231", "Комп'ютерні архітектури", "ст. викл. Медвідь С. О.", 4, 65, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315200", "Лінійна алгебра та аналітична геометрія", "доц. Чорней Р. К., ас. Сарана М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315223", "Моделі обчислень в програмній інженерії", "доц. Проценко В. С.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315283", "Основи вебтехнологій", "ст. викл. Зважій Д. В. ", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315202", "Основи дискретної математики", "ст. викл. Щеглов М. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315199", "Основи матаналізу", "ст. викл. Іванюк А. О.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315277", "Основи мережевих технологій", "ст. викл. Вознюк О. М., ст. викл. Вознюк Я. І.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314810", "Українська мова за професійним спрямуванням", "доц. Сегін Л. В., ст. викл. Калиновська О. М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314804", "Фізичне виховання (1 р.н. БП)", "викл. Жуков В. О.", 4, 65, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315204", "Практика навчальна", "ст. викл. Пєчкурова О. М., ст. викл. Кирієнко О. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        }

        if (selectedCourse == 2) {
            enrollmentSystem.addDiscipline(new Discipline("340214", "Англійська мова (за проф. спрямуванням)", "Проф. А.С. Дерев'янко", 3.5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340447", "Бази даних", "Викл. О.В. Кушнір", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340443", "Вступ до тестування програмного забезпечення", "Доц. М.І. Сидоров", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340418", "Об'єктно-орієнтоване програмування", "Проф. В.П. Залізняк", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340370", "Основи комп'ютерних алгоритмів", "Проф. Л.М. Іванов", 6, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340362", "Побудова і використання комунікаційних мереж", "Доц. Р.Т. Коваленко", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340356", "Процедурне програмування (на базі Сі/Сі++) (ПІ)", "Проф. Г.М. Сахаров", 6, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340377", "Теорія алгоритмів і математична логіка", "Проф. Г.М. Сахаров", 6, 0, MANDATORY_DISCIPLINE_CAPACITY, true, 2));

            enrollmentSystem.addDiscipline(new Discipline("340439", "Автоматизація роботи з програмними проєктами мовою Java", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340393", "Інформаційний пошук", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315191", "Методи та засоби збору чутливої інформації", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315306", "Мова програмування Kotlin", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315261", "Програмування на C#", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340402", "Розробка клієнт-серверних застосувань", "проф. Гришко О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Управління цифровим продуктом", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Обробка зображень", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Життя у цифровому світі", "доц. Сидоренко А. К.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Введення у Хмарні технології", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Мемологічні студії", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));

        } else if (selectedCourse == 3) {
            enrollmentSystem.addDiscipline(new Discipline("C3M01", "Вища математика III", "Проф. А.Б. Математикус", 6, 0, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("C3M02", "Комп'ютерні мережі", "Доц. О.П. Мережевик", 5, 0, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("C3M03", "Операційні системи", "Викл. В.І. Системний", 5, 0, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("C3M04", "Чисельні методи", "Проф. К.Л. Обчислювач", 4, 0, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("C3M05", "Архітектура комп'ютерів", "Доц. П.Р. Залізяка", 4, 0, MANDATORY_DISCIPLINE_CAPACITY, true, 3));
            enrollmentSystem.addDiscipline(new Discipline("C3M06", "Дискретна математика", "Викл. Т.О. Логік", 4, 0, MANDATORY_DISCIPLINE_CAPACITY, true, 3));

            enrollmentSystem.addDiscipline(new Discipline("340439", "Автоматизація роботи з програмними проєктами мовою Java", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340393", "Інформаційний пошук", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315191", "Методи та засоби збору чутливої інформації", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315306", "Мова програмування Kotlin", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("315261", "Програмування на C#", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340402", "Розробка клієнт-серверних застосувань", "проф. Гришко О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Управління цифровим продуктом", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Обробка зображень", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Життя у цифровому світі", "доц. Сидоренко А. К.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Введення у Хмарні технології", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 3));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Мемологічні студії", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 3));
            //ДОДАТИ ЩЕ ДИСЦИПЛІНИ

        } else if (selectedCourse == 4) {
            enrollmentSystem.addDiscipline(new Discipline("C4M01", "Штучний інтелект", "Проф. Г.М. Розумний", 6, 0, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("C4M02", "Машинне навчання", "Доц. М.В. Алгоритмов", 5, 0, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("C4M03", "Розробка мобільних застосунків", "Викл. Д.С. Мобільний", 5, 0, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("C4M04", "Проектний менеджмент в ІТ", "Проф. В.В. Керівник", 4, 0, MANDATORY_DISCIPLINE_CAPACITY, true, 4));
            enrollmentSystem.addDiscipline(new Discipline("C4M05", "Безпека інформаційних систем", "Доц. Р.Р. Захисник", 4, 0, MANDATORY_DISCIPLINE_CAPACITY, true, 4));

            enrollmentSystem.addDiscipline(new Discipline("340439", "Автоматизація роботи з програмними проєктами мовою Java", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340393", "Інформаційний пошук", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315191", "Методи та засоби збору чутливої інформації", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315306", "Мова програмування Kotlin", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("315261", "Програмування на C#", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340402", "Розробка клієнт-серверних застосувань", "проф. Гришко О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Управління цифровим продуктом", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Обробка зображень", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Життя у цифровому світі", "доц. Сидоренко А. К.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Введення у Хмарні технології", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 4));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Мемологічні студії", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 4));
            //ДОДАТИ ЩЕ ДИСЦИПЛІНИ

        }
    }

    private void initializeInitialDataM(int selectedCourse) {
        enrollmentSystem.addStudent(new Student("І 005/24 мп", "Студент", selectedCourse, "121 Інженерія програмного забезпечення"));

        //ЗМІНИТИ ДИСЦИПЛІНИ
        if (selectedCourse == 1) {
            enrollmentSystem.addDiscipline(new Discipline("315203", "Алгоритми і структури даних", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М., ст. викл. Кирієнко О. В.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314809", "Англійська мова", "ст. викл. Гісем С. О.", 7, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315275", "Вступ до програмування", "проф. Глибовець А. М., ст. викл. Пєчкурова О. М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315213", "Диференціальні рівняння", "доц. Митник Ю. В., ст. викл. Силенко І. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315231", "Комп'ютерні архітектури", "ст. викл. Медвідь С. О.", 4, 65, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315200", "Лінійна алгебра та аналітична геометрія", "доц. Чорней Р. К., ас. Сарана М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315223", "Моделі обчислень в програмній інженерії", "доц. Проценко В. С.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315283", "Основи вебтехнологій", "ст. викл. Зважій Д. В. ", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315202", "Основи дискретної математики", "ст. викл. Щеглов М. В.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315199", "Основи матаналізу", "ст. викл. Іванюк А. О.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315277", "Основи мережевих технологій", "ст. викл. Вознюк О. М., ст. викл. Вознюк Я. І.", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314810", "Українська мова за професійним спрямуванням", "доц. Сегін Л. В., ст. викл. Калиновська О. М.", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("314804", "Фізичне виховання (1 р.н. БП)", "викл. Жуков В. О.", 4, 65, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
            enrollmentSystem.addDiscipline(new Discipline("315204", "Практика навчальна", "ст. викл. Пєчкурова О. М., ст. викл. Кирієнко О. В.", 3, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 1));
        }

        if (selectedCourse == 2) {
            enrollmentSystem.addDiscipline(new Discipline("340214", "Англійська мова (за проф. спрямуванням)", "Проф. А.С. Дерев'янко", 3.5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340447", "Бази даних", "Викл. О.В. Кушнір", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340443", "Вступ до тестування програмного забезпечення", "Доц. М.І. Сидоров", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340418", "Об'єктно-орієнтоване програмування", "Проф. В.П. Залізняк", 5, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340370", "Основи комп'ютерних алгоритмів", "Проф. Л.М. Іванов", 6, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340362", "Побудова і використання комунікаційних мереж", "Доц. Р.Т. Коваленко", 4, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340356", "Процедурне програмування (на базі Сі/Сі++) (ПІ)", "Проф. Г.М. Сахаров", 6, MANDATORY_DISCIPLINE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, true, 2));
            enrollmentSystem.addDiscipline(new Discipline("340377", "Теорія алгоритмів і математична логіка", "Проф. Г.М. Сахаров", 6, 0, MANDATORY_DISCIPLINE_CAPACITY, true, 2));

            enrollmentSystem.addDiscipline(new Discipline("340439", "Автоматизація роботи з програмними проєктами мовою Java", "ст. викл. Прокопенко П. О.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340393", "Інформаційний пошук", "доц. Мельничук  А. В.", 3.5, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315191", "Методи та засоби збору чутливої інформації", "ст. викл. Бабич Т. А.", 2, EnrollmentSystem.ELECTIVE_CAPACITY, 40, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315306", "Мова програмування Kotlin", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, UNLIMITED_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("315261", "Програмування на C#", "доц. Осадча Н. П.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340402", "Розробка клієнт-серверних застосувань", "проф. Гришко О. М.", 4, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Управління цифровим продуктом", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Обробка зображень", "ст. викл. Сидорук І. Ю.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Життя у цифровому світі", "доц. Сидоренко А. К.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Введення у Хмарні технології", "ст. викл. Сидорчук Л. Н.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
            enrollmentSystem.addDiscipline(new Discipline("340351", "Мемологічні студії", "ст. викл. Ведель К. А.", 3, EnrollmentSystem.ELECTIVE_CAPACITY, MANDATORY_DISCIPLINE_CAPACITY, false, 2));
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

        public static void main(String[] args) {
            SwingUtilities.invokeLater(EnrollmentSystemGUI::new);
        }
}
