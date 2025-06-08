package org.example;

import org.example.Discipline;
import org.example.Student;
import org.example.EnrollmentSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Random;

public class EnrollmentSystemGUI extends JFrame {

    private EnrollmentSystem enrollmentSystem;
    private JTextArea outputArea;
    private JList<Discipline> mandatoryDisciplineList;
    private DefaultListModel<Discipline> mandatoryListModel;
    private JList<Discipline> electiveDisciplineList;
    private DefaultListModel<Discipline> electiveListModel;

    private JButton enrollElectiveButton;
    private JButton dropElectiveButton;
    private JButton confirmElectivesButton;

    private Student currentStudent;
    private Timer autoEnrollTimer;
    private Random randomGlitches = new Random();

    // Внутрішній клас для кастомізації відображення елементів у JList
    static class DisciplineListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Discipline) {
                Discipline disc = (Discipline) value;
                if (!disc.isMandatory()) {
                    label.setText(disc.getName() + " (Зайнято місць: " + disc.getCurrentEnrollment() + "/" + disc.getMaxCapacity() + ")");
                    if (!disc.hasAvailableSlots()) {
                        label.setForeground(Color.RED.darker());
                    } else if (disc.getCurrentEnrollment() > disc.getMaxCapacity() * 0.75) {
                        label.setForeground(new Color(200, 100, 0));
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
        super("Система автоматизованого запису на дисципліни");
        enrollmentSystem = new EnrollmentSystem();

        initializeInitialData();

        currentStudent = enrollmentSystem.getStudentById("І 005/24 бп").orElse(null);

        // Автоматично записуємо currentStudent на всі обов'язкові дисципліни його курсу
        for (Discipline mandatoryDisc : enrollmentSystem.getMandatoryDisciplines()) {
            if (mandatoryDisc.getTargetCourse() == currentStudent.getCourse()) {
                currentStudent.enrollDiscipline(mandatoryDisc);
            }
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Панель інформації про студента зверху (тепер без імені)
        JPanel studentInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        studentInfoPanel.add(new JLabel("Студент (ID: " + currentStudent.getStudentId() + ", Курс: " + currentStudent.getCourse() + ")"));
        mainPanel.add(studentInfoPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        JPanel mandatoryPanel = new JPanel(new BorderLayout(5, 5));
        mandatoryPanel.setBorder(BorderFactory.createTitledBorder("Обов'язкові дисципліни"));
        mandatoryListModel = new DefaultListModel<>();
        mandatoryDisciplineList = new JList<>(mandatoryListModel);
        mandatoryDisciplineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mandatoryDisciplineList.setCellRenderer(new DisciplineListRenderer());
        mandatoryDisciplineList.setEnabled(false);
        mandatoryPanel.add(new JScrollPane(mandatoryDisciplineList), BorderLayout.CENTER);
        splitPane.setLeftComponent(mandatoryPanel);

        JPanel electivePanel = new JPanel(new BorderLayout(5, 5));
        electivePanel.setBorder(BorderFactory.createTitledBorder("Вибіркові дисципліни (оберіть " + EnrollmentSystem.MAX_ELECTIVES_PER_STUDENT + ")"));
        electiveListModel = new DefaultListModel<>();
        electiveDisciplineList = new JList<>(electiveListModel);
        electiveDisciplineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        electiveDisciplineList.setCellRenderer(new DisciplineListRenderer());
        electivePanel.add(new JScrollPane(electiveDisciplineList), BorderLayout.CENTER);

        JPanel electiveButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        enrollElectiveButton = new JButton("Записатися на вибіркову");
        dropElectiveButton = new JButton("Виписатися з вибіркової");
        confirmElectivesButton = new JButton("Завершити вибір (обрано: 0/" + EnrollmentSystem.MAX_ELECTIVES_PER_STUDENT + ")");
        confirmElectivesButton.setEnabled(false);

        enrollElectiveButton.addActionListener(e -> attemptEnrollment());
        dropElectiveButton.addActionListener(e -> attemptDrop());
        confirmElectivesButton.addActionListener(e -> confirmSelection());

        electiveButtonsPanel.add(enrollElectiveButton);
        electiveButtonsPanel.add(dropElectiveButton);
        electiveButtonsPanel.add(confirmElectivesButton);
        electivePanel.add(electiveButtonsPanel, BorderLayout.SOUTH);
        splitPane.setRightComponent(electivePanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        outputArea = new JTextArea(10, 50);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);

        updateDisciplineLists();
        updateConfirmButtonState();

        autoEnrollTimer = new Timer(0, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Перевіряємо, чи досягнуто ліміту віртуальних студентів
                if (enrollmentSystem.getVirtualStudentsEnrolledCount() >= EnrollmentSystem.MAX_VIRTUAL_STUDENTS_TO_ENROLL) {
                    autoEnrollTimer.stop(); // Зупиняємо таймер
                    return;
                }

                enrollmentSystem.randomlyIncrementElectiveEnrollment();
                electiveDisciplineList.repaint();
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

    // Метод для ініціалізації тестових даних
    private void initializeInitialData() {
        enrollmentSystem.addStudent(new Student("І 005/24 бп", "", 2, "Комп'ютерні науки")); // Ім'я зберігається, але не відображається

        // Обов'язкові дисципліни (рандомні викладачі)
        enrollmentSystem.addDiscipline(new Discipline("340214", "Англійська мова (за проф. спрямуванням)", "Проф. А.С. Дерев'янко", 6, 0, true, 2));
        enrollmentSystem.addDiscipline(new Discipline("340447", "Бази даних", "Викл. О.В. Кушнір", 4, 0, true, 2));
        enrollmentSystem.addDiscipline(new Discipline("340443", "Вступ до тестування програмного забезпечення", "Доц. М.І. Сидоров", 5, 0, true, 2));
        enrollmentSystem.addDiscipline(new Discipline("340418", "Об`єктно-орієнтоване програмування", "Проф. В.П. Залізняк", 5, 0, true, 2));
        enrollmentSystem.addDiscipline(new Discipline("340370", "Основи комп`ютерних алгоритмів", "Проф. Л.М. Іванов", 6, 0, true, 2));
        enrollmentSystem.addDiscipline(new Discipline("340362", "Побудова і використання комунікаційних мереж", "Доц. Р.Т. Коваленко", 4, 0, true, 2));
        enrollmentSystem.addDiscipline(new Discipline("340356", "Процедурне програмування (на базі Сі/Сі++) (ПІ)", "Проф. Г.М. Сахаров", 6, 0, true, 2));
        enrollmentSystem.addDiscipline(new Discipline("340377", "Теорія алгоритмів і математична логіка", "Проф. Г.М. Сахаров", 6, 0, true, 2));

        // Вибіркові дисципліни (рандомні викладачі)
        enrollmentSystem.addDiscipline(new Discipline("340439", "Автоматизація роботи з програмними проектами мовою Java", "Викл. П.О. Прокопенко", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("340393", "Інформаційний пошук", "Доц. А.В. Мельничук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("340317", "Методи та засоби збору чутливої інформації", "Проф. С.В. Кравченко", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("340464", "Мова програмування Kotlin", "Викл. Д.І. Бондар", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("E005", "Мова програмування С#", "Доц. Н.П. Осадча", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("340402", "Розробка клієнт-серверних застосувань", "Проф. О.М. Гришко", 4, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("340351", "Управління цифровим продуктом", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315212", "Алгебра і теорія чисел", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("319948", "Базові алгоритми обробки природної мови", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315264", "Електроніка та цифрова електроніка", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315271", "Історія розвитку кібернетики в Україні", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315237", "Математичні методи обробки зображень", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315193", "Обробка зображень", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315209", "Основи комп`ютерних алгоритмів", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315305", "Програмування на Python для Big Data та Data Science", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("319959", "Соціальна інженерія", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315296", "Машинне навчання та доповнена реальність на мобільних пристроях на базі iOS", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315298", "Мова програмування Swift", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315307", "Мова програмування Rust", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
        enrollmentSystem.addDiscipline(new Discipline("315294", "Низькорівневі вразливості програмного забезпечення", "Викл. І.Ю. Сидорук", 3, EnrollmentSystem.ELECTIVE_CAPACITY, false, 2));
    }

    // --- Оновлення списків дисциплін в GUI ---
    private void updateDisciplineLists() {
        mandatoryListModel.clear();
        for (Discipline disc : enrollmentSystem.getMandatoryDisciplines()) {
            if (currentStudent.getCourse() == disc.getTargetCourse()) {
                mandatoryListModel.addElement(disc);
            }
        }

        electiveListModel.clear();
        for (Discipline disc : enrollmentSystem.getElectiveDisciplines()) {
            if (currentStudent.getCourse() == disc.getTargetCourse()) {
                electiveListModel.addElement(disc);
            }
        }
        electiveDisciplineList.repaint();
        updateConfirmButtonState();
    }

    // --- Оновлення стану кнопки "Завершити вибір" ---
    private void updateConfirmButtonState() {
        int enrolledElectives = currentStudent.getElectiveCount();
        confirmElectivesButton.setText("Завершити вибір (обрано: " + enrolledElectives + "/" + EnrollmentSystem.MAX_ELECTIVES_PER_STUDENT + ")");
        confirmElectivesButton.setEnabled(enrolledElectives == EnrollmentSystem.MAX_ELECTIVES_PER_STUDENT);
    }

    // --- Імітація рандомних глюків САЗ ---
    private boolean simulateGlitch() {
        if (randomGlitches.nextDouble() < 0.15) {
            try {
                int delay = 500 + randomGlitches.nextInt(1500);
                appendOutput("Система тимчасово недоступна. Будь ласка, зачекайте...\n");
                Thread.sleep(delay);

                if (randomGlitches.nextDouble() < 0.5) {
                    JOptionPane.showMessageDialog(this,
                            "Сталася неочікувана помилка САЗ. Спробуйте пізніше.",
                            "Помилка системи",
                            JOptionPane.ERROR_MESSAGE);
                    appendOutput("Помилка САЗ: Операція не виконана.\n");
                    return true;
                }
                appendOutput("З'єднання відновлено. Спробуйте ще раз.\n");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                appendOutput("Операція перервана.\n");
                return true;
            }
        }
        return false;
    }

    // --- Логіка спроби запису на вибіркову дисципліну ---
    private void attemptEnrollment() {
        if (simulateGlitch()) {
            return;
        }

        Discipline selectedDiscipline = electiveDisciplineList.getSelectedValue();

        if (selectedDiscipline == null) {
            appendOutput("Будь ласка, оберіть вибіркову дисципліну для запису.\n");
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
                    updateDisciplineLists();
                } catch (Exception ex) {
                    appendOutput("Виникла непередбачена помилка під час виконання запису: " + ex.getMessage() + "\n");
                }
            }
        }.execute();
    }

    // --- Логіка спроби відписки від вибіркової дисципліни ---
    private void attemptDrop() {
        if (simulateGlitch()) {
            return;
        }

        Discipline selectedDiscipline = electiveDisciplineList.getSelectedValue();

        if (selectedDiscipline == null) {
            appendOutput("Будь ласка, оберіть вибіркову дисципліну для відписки.\n");
            return;
        }

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
                    updateDisciplineLists();
                } catch (Exception ex) {
                    appendOutput("Виникла непередбачена помилка під час виконанні виписки: " + ex.getMessage() + "\n");
                }
            }
        }.execute();
    }

    // --- Обробка завершення вибору ---
    private void confirmSelection() {
        if (currentStudent.getElectiveCount() == EnrollmentSystem.MAX_ELECTIVES_PER_STUDENT) {
            JOptionPane.showMessageDialog(this,
                    "Вітаємо! Ви успішно обрали " + EnrollmentSystem.MAX_ELECTIVES_PER_STUDENT + " вибіркові дисципліни. Ваш вибір зафіксовано.",
                    "Вибір завершено",
                    JOptionPane.INFORMATION_MESSAGE);
            autoEnrollTimer.stop();

            enrollElectiveButton.setEnabled(false);
            dropElectiveButton.setEnabled(false);
            confirmElectivesButton.setEnabled(false);
            electiveDisciplineList.setEnabled(false);
            appendOutput("Вибір дисциплін завершено. Подальші зміни неможливі.\n");
        } else {
            appendOutput("Ви повинні обрати рівно " + EnrollmentSystem.MAX_ELECTIVES_PER_STUDENT + " вибіркові дисципліни, щоб завершити вибір. Обрано: " + currentStudent.getElectiveCount() + ".\n");
        }
    }

    private void appendOutput(String text) {
        outputArea.append(text);
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EnrollmentSystemGUI::new);
    }
}