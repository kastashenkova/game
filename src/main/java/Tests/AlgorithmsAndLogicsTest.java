package Tests;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AlgorithmsAndLogicsTest extends JFrame {

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

    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_TURQUOISE = new Color(64, 224, 208);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);
    private final Color SIMS_BUTTON_HOVER = new Color(255, 240, 245);
    private static final Color SIMS_GREEN_CORRECT = new Color(144, 238, 144);
    private static final Color SIMS_RED_INCORRECT = new Color(255, 99, 71);

    static class Question {
        String question;
        String[] options;
        int correctAnswer;

        public Question(String question, String[] options, int correctAnswer) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }

    public AlgorithmsAndLogicsTest() {
        setTitle("Тест із Теорії алгоритмів та математичної логіки");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Встановлюємо український текст для кнопок "OK" та "Скасувати"
        UIManager.put("OptionPane.okButtonText", "ОК");
        UIManager.put("OptionPane.cancelButtonText", "Скасувати");
        UIManager.put("OptionPane.yesButtonText", "Так");
        UIManager.put("OptionPane.noButtonText", "Ні");

        // Задаємо глобальні шрифти для JOptionPane та інших компонентів
        UIManager.put("OptionPane.messageFont", new Font("Arial", Font.PLAIN, 16));
        UIManager.put("OptionPane.buttonFont", new Font("Arial", Font.BOLD, 14));
        UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 16));
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 18));
        UIManager.put("TitledBorder.font", new Font("Arial", Font.BOLD, 18));


        initializeQuestions();
        userAnswers = new int[questions.length];
        for (int i = 0; i < userAnswers.length; i++) {
            userAnswers[i] = -1;
        }

        createUI();
        showInstructionsDialog();
        showStartAnimation();
        displayQuestion();
    }

    private void showStartAnimation() {
        JDialog animationDialog = new JDialog(this, true);
        animationDialog.setUndecorated(true);

        // Sims-like colors for animation
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

    private void initializeQuestions() {
        questions = new Question[] {
                new Question("1. Що таке алгоритм з точки зору теорії алгоритмів?",
                        new String[]{"Будь-яка послідовність дій", "Набір випадкових інструкцій", "Скінченна, однозначна, ефективна та результативна послідовність дій", "Математичне рівняння"}, 2),
                new Question("2. Яка модель обчислень вважається універсальною?",
                        new String[]{"Скінченний автомат", "Машина Тьюрінга", "Рекурсивна функція", "Лямбда-вираз"}, 1),
                new Question("3. Що означає «обчислювана функція»?",
                        new String[]{"Функція, яку можна описати у вигляді таблиці", "Функція, що виконується в браузері", "Функція, яку можна обчислити алгоритмом", "Графічна функція"}, 2),
                new Question("4. Яка з мов **не є** регулярною?",
                        new String[]{"Мова паліндромів", "Мова усіх слів над {a,b}", "Мова з парною кількістю символів a", "Мова, описана регулярним виразом"}, 0),
                new Question("5. Що таке висловлювальна логіка?",
                        new String[]{"Мова програмування", "Система рівнянь", "Математична логіка, що працює з істинними/хибними висловлюваннями", "Математична модель"}, 2),
                new Question("6. Який символ у логіці позначає імплікацію?",
                        new String[]{"∧", "∨", "¬", "→"}, 3),
                new Question("7. Коли висловлювання вважається тавтологією?",
                        new String[]{"Якщо воно істинне для певних значень", "Якщо істинне в одній ситуації", "Якщо істинне при всіх наборах значень змінних", "Якщо воно не містить ¬"}, 2),
                new Question("8. Що таке формальна мова?",
                        new String[]{"Мова програмування", "Мова без граматики", "Набір слів, побудованих за правилами граматики", "Мова, що містить тільки цифри"}, 2),
                new Question("9. Як називається множина функцій, що можуть бути обчислені машиною Тьюрінга?",
                        new String[]{"Множина рекурсивних функцій", "Множина інтегралів", "Гіпотетична множина", "Алгебраїчна система"}, 0),
                new Question("10. Який логічний квантор означає «для всіх»?",
                        new String[]{"∃", "∀", "⇒", "⇔"}, 1),
                new Question("11. Який клас задач позначається як **P**?",
                        new String[]{"Задачі, що вирішуються за експоненційний час", "Нерозв’язні задачі", "Задачі, що вирішуються за поліноміальний час", "Приблизні задачі"}, 2),
                new Question("12. Який логічний вираз є прикладом диз’юнкції?",
                        new String[]{"A ∧ B", "¬A", "A → B", "A ∨ B"}, 3),
                new Question("13. Який логічний висновок є коректним за правилом Modus Ponens?",
                        new String[]{"A, A ∧ B ⟹ B", "A → B, A ⟹ B", "A ∨ B, ¬A ⟹ B", "¬B, A ⟹ A → B"}, 1),
                new Question("14. Яке поняття означає нерозв’язність задачі?",
                        new String[]{"Її не можна вирішити за один крок", "Її не можна розв’язати жодним алгоритмом", "Потрібен потужний комп’ютер", "Вона потребує багато пам’яті"}, 1),
                new Question("15. Яка з мов є контекстно-вільною?",
                        new String[]{"Мова паліндромів", "Мова рівної кількості a і b", "Мова арифметичних виразів", "Мова зі змінною довжиною"}, 2)
        };
    }

    private void createUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(SIMS_MEDIUM_PINK);

        // Question Panel
        questionPanel = new JPanel();
        questionPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        questionPanel.setBackground(SIMS_TURQUOISE);
        questionPanel.setBorder(BorderFactory.createLineBorder(SIMS_LIGHT_BLUE, 5));
        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
        questionLabel.setForeground(SIMS_DARK_TEXT);
        questionPanel.add(questionLabel);
        mainPanel.add(questionPanel, BorderLayout.NORTH);

        // Options Panel
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridBagLayout());
        optionsPanel.setBackground(SIMS_MEDIUM_PINK);

        JPanel innerOptionsPanel = new JPanel();
        innerOptionsPanel.setLayout(new BoxLayout(innerOptionsPanel, BoxLayout.Y_AXIS));
        innerOptionsPanel.setBackground(SIMS_LIGHT_PINK);
        innerOptionsPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        optionButtons = new JRadioButton[4];
        optionButtonGroup = new ButtonGroup();
        String[] optionLetters = {"А", "Б", "В", "Г"};

        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font("Arial", Font.PLAIN, 20));
            optionButtons[i].setBackground(SIMS_LIGHT_PINK);
            optionButtons[i].setForeground(SIMS_DARK_TEXT);
            optionButtons[i].setFocusPainted(false);
            optionButtons[i].setHorizontalAlignment(SwingConstants.LEFT);
            optionButtons[i].setAlignmentX(Component.LEFT_ALIGNMENT);

            optionButtonGroup.add(optionButtons[i]);
            innerOptionsPanel.add(optionButtons[i]);
            innerOptionsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        optionsPanel.add(innerOptionsPanel, gbc);

        mainPanel.add(optionsPanel, BorderLayout.CENTER);

// Navigation Panel
        navigationPanel = new JPanel();
        navigationPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 15));
        navigationPanel.setBackground(SIMS_TURQUOISE);
        navigationPanel.setBorder(BorderFactory.createLineBorder(SIMS_LIGHT_BLUE, 5));

        nextButton = createSimsButton("Наступна");
        finishButton = createSimsButton("Завершити тест");

        navigationPanel.add(nextButton);
        navigationPanel.add(finishButton);
        mainPanel.add(navigationPanel, BorderLayout.SOUTH);

        add(mainPanel);

// Action Listeners
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentAnswer();
                if (currentQuestionIndex < questions.length - 1) {
                    currentQuestionIndex++;
                    displayQuestion();
                } else {
                    finishTest();
                }
            }
        });

        finishButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentAnswer();
                finishTest();
            }
        });

// Налаштування закриття вікна
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                int confirm = JOptionPane.showConfirmDialog(
                        AlgorithmsAndLogicsTest.this,
                        "Ви дійсно хочете вийти з тесту?",
                        "Підтвердження виходу",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    // Helper method to create Sims-style buttons
    private JButton createSimsButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(SIMS_LIGHT_BLUE);
        button.setForeground(SIMS_DARK_TEXT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(SIMS_BUTTON_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SIMS_LIGHT_BLUE);
            }
        });
        return button;
    }

    private void displayQuestion() {
        Question q = questions[currentQuestionIndex];
        questionLabel.setText(q.question);

        optionButtonGroup.clearSelection();
        String[] optionLetters = {"А", "Б", "В", "Г"};
        for (int i = 0; i < q.options.length; i++) {
            optionButtons[i].setText(optionLetters[i] + ". " + q.options[i]);
        }

        nextButton.setEnabled(currentQuestionIndex < questions.length - 1);
        finishButton.setEnabled(true);
    }

    private void saveCurrentAnswer() {
        for (int i = 0; i < optionButtons.length; i++) {
            if (optionButtons[i].isSelected()) {
                userAnswers[currentQuestionIndex] = i;
                return;
            }
        }
        userAnswers[currentQuestionIndex] = -1;
    }

    private void finishTest() {
        int score = 0;
        for (int i = 0; i < questions.length; i++) {
            if (userAnswers[i] == questions[i].correctAnswer) {
                score++;
            }
        }

        mainPanel.setVisible(false);

        boolean passed = (score >= 10);

        // Показуємо результат в JOptionPane
        String message = "Ваш результат: " + score + " з " + questions.length + " балів.";
        if (passed) {
            message += "\nВітаємо! Ви успішно пройшли тест із Теорії алгоритмів та математичної логіки.";
            showResultPanel(score);
        } else {
            message += "\nНа жаль, Ви набрали менше ніж 10 балів. Спробуйте ще раз!";
            int option = JOptionPane.showConfirmDialog(
                    this,
                    message,
                    "Результати тесту",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (option == JOptionPane.YES_OPTION) {
                resetTest();
            } else {
                System.exit(0); // Вийти
            }
        }
    }

    private void showResultPanel(int score) {
        resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout(10, 10));
        resultPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        resultPanel.setBackground(SIMS_MEDIUM_PINK);

        JLabel scoreLabel = new JLabel("Ваш результат: " + score + " з " + questions.length, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
        scoreLabel.setForeground(SIMS_DARK_TEXT);
        resultPanel.add(scoreLabel, BorderLayout.NORTH);

        JTextPane answerPane = new JTextPane();
        answerPane.setContentType("text/html");

        answerPane.setEditable(false);
        answerPane.setBackground(SIMS_LIGHT_PINK);
        answerPane.setBorder(new EmptyBorder(15, 15, 15, 15));

        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><body style='font-family: \"Arial\"; font-size: 14px; color: ")
                .append(toHex(SIMS_DARK_TEXT))
                .append(";'>");
        htmlContent.append("<h3 style='color: ").append(toHex(SIMS_TURQUOISE)).append(";'>Правильні відповіді</h3>");

        String[] optionLetters = {"А", "Б", "В", "Г"};
        for (int i = 0; i < questions.length; i++) {
            Question q = questions[i];
            int userAnswer = userAnswers[i];
            int correctAnswer = q.correctAnswer;

            htmlContent.append("<p>");
            htmlContent.append("<b>").append(q.question).append("</b><br>");

            for (int j = 0; j < q.options.length; j++) {
                String optionColor = toHex(SIMS_DARK_TEXT);

                if (j == correctAnswer) {
                    optionColor = toHex(SIMS_GREEN_CORRECT.darker());
                } else if (j == userAnswer && userAnswer != correctAnswer) {
                    optionColor = toHex(SIMS_RED_INCORRECT.darker());
                }

                htmlContent.append("<span style='color: ").append(optionColor).append(";'>")
                        .append(optionLetters[j]).append(". ").append(q.options[j])
                        .append("</span>");

                if (j == correctAnswer) {
                    htmlContent.append(" &nbsp;&#10003;");
                } else if (j == userAnswer && userAnswer != correctAnswer) {
                    htmlContent.append(" &nbsp;&#10007;");
                }
                htmlContent.append("<br>");
            }
            htmlContent.append("</p><hr style='border: 0.5px solid ").append(toHex(SIMS_LIGHT_BLUE)).append(";'>");
        }
        htmlContent.append("</body></html>");
        answerPane.setText(htmlContent.toString());

        JScrollPane scrollPane = new JScrollPane(answerPane);
        scrollPane.setPreferredSize(new Dimension(800, 500));

        resultPanel.add(scrollPane, BorderLayout.CENTER);

        JButton doneButton = createSimsButton("Готово"); // Змінено на "Готово"
        JPanel resultButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        resultButtonsPanel.setBackground(SIMS_TURQUOISE);
        resultButtonsPanel.add(doneButton);
        resultPanel.add(resultButtonsPanel, BorderLayout.SOUTH);

        add(resultPanel);
        revalidate();
        repaint();

        doneButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                    this,
                    "Вітаємо з успішним проходженням контрольної роботи з Теорії алгоритмів та математичної логіки!",
                    "Успіх!",
                    JOptionPane.INFORMATION_MESSAGE
            );
            System.exit(0);
        });
    }


    private void resetTest() {
        currentQuestionIndex = 0;
        for (int i = 0; i < userAnswers.length; i++) {
            userAnswers[i] = -1;
        }
        if (resultPanel != null) {
            resultPanel.setVisible(false);
            remove(resultPanel); // Remove the old result panel
        }
        mainPanel.setVisible(true); // Show the main test panel again
        displayQuestion();
        revalidate();
        repaint();
    }

    private void showInstructionsDialog() {
        String instructions = "<html>" +
                "<body style='font-family: \"Arial\"; font-size: 13px; color: " + toHex(SIMS_DARK_TEXT) + ";'>" +
                "<h1 style='color: " + toHex(Color.BLACK) + ";'>Інструкція до тесту з Теорії алгоритмів та математичної логіки</h1>" +
                "<p>Ласкаво просимо до тесту!</p>" +
                "<p>Будь ласка, дотримуйтеся цих простих кроків!</p>" +
                "<ol>" +
                "<li>Кожне запитання має <b>чотири варіанти відповіді</b> (А, Б, В, Г).</li>" +
                "<li><b>Оберіть лише одну</b> правильну, на Вашу думку, відповідь.</li>" +
                "<li>Використовуйте кнопку <b>«Наступна»</b> для переходу до наступного запитання.</li>" +
                "<li><b>Ви не зможете повернутися</b> до попередніх запитань.</li>" + // Оновлено
                "<li>Ваша відповідь на поточне запитання буде збережена під час переходу до наступного.</li>" +
                "<li>Після того, як Ви відповісте на всі запитання, натисніть <b>«Завершити тест»</b>.</li>" +
                "<li>Тест буде вважатися успішно пройденим, якщо Ви наберете <b>10 або більше балів</b>.</li>" +
                "<li>Якщо Ви наберете менше за 10 балів, Вам буде запропоновано <b>пройти тест ще раз</b>.</li>" +
                "<li>Якщо Ви успішно пройдете тест (10 або більше балів), Ви зможете переглянути <b>всі правильні відповіді</b>.</li>" + // Оновлено
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

    // Helper to convert Color to Hex string for HTML
    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("nimbusBase", SIMS_MEDIUM_PINK); // Основний колір для фонів
            UIManager.put("nimbusBlueGrey", SIMS_LIGHT_BLUE); // Колір для акцентів, рамок
            UIManager.put("control", SIMS_LIGHT_PINK); // Колір контролів (наприклад, фону кнопок)
            UIManager.put("textForeground", SIMS_DARK_TEXT); // Колір тексту

            UIManager.put("RadioButton.checkIcon", new ImageIcon() {
                @Override
                public int getIconWidth() { return 18; }
                @Override
                public int getIconHeight() { return 18; }
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2.setColor(SIMS_DARK_TEXT); // Темний колір для ободка
                    g2.fillOval(x + 2, y + 2, 14, 14);

                    g2.setColor(SIMS_LIGHT_PINK); // Колір фону радіокнопки
                    g2.fillOval(x + 3, y + 3, 12, 12);

                    if (((JRadioButton) c).isSelected()) {
                        g2.setColor(SIMS_TURQUOISE); // Бірюзовий колір для позначки
                        g2.fillOval(x + 6, y + 6, 6, 6); // Маленьке заповнене коло
                    }
                    g2.dispose();
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AlgorithmsAndLogicsTest().setVisible(true);
            }
        });
    }
}