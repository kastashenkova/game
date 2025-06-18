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

    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_ACCENT_COLOR = new Color(255, 179, 186);

    private StudyProgressGUI parentGUI;

    // Клас для зберігання питання та варіантів відповідей
    private static class ProfessorQuestion {
        String question;
        List<String> correctAnswers; // Список для можливих правильних відповідей
        List<String> partialAnswers; // Список для можливих частково правильних відповідей

        public ProfessorQuestion(String question, List<String> correctAnswers, List<String> partialAnswers) {
            this.question = question;
            this.correctAnswers = correctAnswers;
            this.partialAnswers = partialAnswers;
        }
    }

    // Hardcoded list of professors and their questions with answers
    private static final Map<String, ProfessorQuestion> professorsAndQuestions = new LinkedHashMap<>();
    static {
        // Приклад: "Яка максимальна швидкість світла, якщо рухатися перпендикулярно до нього?"
        // Правильна: "така ж", "незмінна"
        // Часткова: "мабуть така ж"
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

    private ImageIcon getRandomImage() {
        int index = (int) (Math.random() * examImages.length);
        return examImages[index];
    }

    public ExamWindow(Frame owner, Discipline discipline, Student student) {
        super(owner, "Екзамен", true);
        if (owner instanceof StudyProgressGUI) {
            this.parentGUI = (StudyProgressGUI) owner;
        } else {
            System.err.println("Помилка: CreditWindow повинен бути викликаний з StudyProgressGUI.");
        }



        this.discipline = discipline;
        this.student = student;
        this.currentAttempt = currentAttempt;

        setSize(800, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(owner);

        initComponents();
        applySimsStyle();
        updateUIBasedOnAttempts();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        examImages = new ImageIcon[] {
                new ImageIcon(getClass().getResource("/exam/img_1.png").getFile()),
                new ImageIcon(getClass().getResource("/exam/img_3.png").getFile()),
                new ImageIcon(getClass().getResource("/exam/img_4.png").getFile()),
                new ImageIcon(getClass().getResource("/exam/img_5.png").getFile()),
        };

        imageLabel = new JLabel();
        ImageIcon randomImage = getRandomImage();
        imageLabel.setIcon(randomImage);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(200, 200));

        add(imageLabel, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Отримання балів за екзамен з дисципліни «" + discipline.getName() + "»");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        attemptLabel = new JLabel();
        attemptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        attemptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(attemptLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        wheelPanel = new WheelPanel();
        wheelPanel.setPreferredSize(new Dimension(350, 350));
        wheelPanel.setMaximumSize(new Dimension(350, 350));
        wheelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(wheelPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        scoreLabel = new JLabel(" ");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        scoreLabel.setForeground(SIMS_DARK_TEXT);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(scoreLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        spinButton = new JButton("Крутити колесо!");
        spinButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        spinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        spinButton.addActionListener(e -> spinWheel());
        spinButton.setBackground(SIMS_LIGHT_BLUE);
        spinButton.setForeground(SIMS_DARK_TEXT);
        spinButton.setFocusPainted(false);
        spinButton.setBorderPainted(false);
        mainPanel.add(spinButton);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        add(mainPanel, BorderLayout.CENTER);
    }

    private void applySimsStyle() {
        getContentPane().setBackground(SIMS_LIGHT_PINK);

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

    private void styleComponent(Component comp) {
        if (comp instanceof JButton button) {
            button.setBackground(SIMS_MEDIUM_PINK);
            button.setForeground(SIMS_DARK_TEXT);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createLineBorder(SIMS_LIGHT_BLUE, 2));
        } else if (comp instanceof JLabel) {
            comp.setForeground(SIMS_DARK_TEXT);
        }
    }


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

        if (actualAttemptsMade < MAX_REGULAR_ATTEMPTS ||
                (actualAttemptsMade == MAX_REGULAR_ATTEMPTS && (currentTotalScore == null || currentTotalScore < PASSING_SCORE))) {
            spinButton.setEnabled(true);
            scoreLabel.setText("Поточний бал: " + (currentTotalScore != null ? currentTotalScore : "40"));
            if (actualAttemptsMade < MAX_REGULAR_ATTEMPTS) {
                spinButton.setText("Крутити колесо!");
                attemptLabel.setText("Спроба: " + (actualAttemptsMade + 1) + "/" + MAX_REGULAR_ATTEMPTS);
            } else {
                spinButton.setText("Крутити колесо (перездача)!");
                attemptLabel.setText("ПЕРЕЗДАЧА");
            }
        } else if (currentTotalScore != null && currentTotalScore >= PASSING_SCORE) {
            spinButton.setEnabled(false);
            spinButton.setText("Екзамен складено");
            scoreLabel.setText("Складено: " + currentTotalScore + " балів");
            attemptLabel.setText("Екзамен складено успішно!");
        } else {
            spinButton.setEnabled(false);
            spinButton.setText("Спроби вичерпано");
            scoreLabel.setText("Бал недостатній: " + (currentTotalScore != null ? currentTotalScore : "0") + "!");
            attemptLabel.setText("Всі спроби вичерпано!");
        }
    }

    private void spinWheel() {

        spinButton.setEnabled(false);
        MusicPlayer.getInstance().playSpin();

        Random random = new Random();
        int finalScoreFromWheel = random.nextInt(60) + 1; // Score from 1 to 60 for visual/professor selection
        int rotations = 5 + random.nextInt(5);
        double sectorSize = 360.0 / 60;
        double targetAngleInWheelCoordinates = (finalScoreFromWheel - 1) * sectorSize + sectorSize / 2;
        double rotationOffset = (360 - targetAngleInWheelCoordinates);
        double totalTargetRotation = rotations * 360 + rotationOffset;

        wheelPanel.startSpinning(totalTargetRotation, rotations, resultScore -> {
            int professorIndex = (resultScore - 1) % professorNames.size();
            String landedProfessorName = professorNames.get(professorIndex);
            ProfessorQuestion pq = professorsAndQuestions.get(landedProfessorName);

            String question = (pq != null) ? pq.question : "Цікаве питання від викладача!";

            String userAnswer = JOptionPane.showInputDialog(this,
                    "Питання від \"" + landedProfessorName + "\":\n\n" + question + "\n\nВаша відповідь:",
                    "Питання від викладача",
                    JOptionPane.QUESTION_MESSAGE);

            int pointsFromAnswer = 0;
            if (userAnswer != null) {
                userAnswer = userAnswer.toLowerCase().trim();
                if (pq != null) {
                    String finalUserAnswer = userAnswer;
                    if (pq.correctAnswers.stream().anyMatch(ans -> finalUserAnswer.contains(ans.toLowerCase()))) {
                        pointsFromAnswer = 40;
                        JOptionPane.showMessageDialog(this, "Ваша відповідь правильна! Отримано +40 балів.", "Відповідь вірна", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        String finalUserAnswer1 = userAnswer;
                        if (pq.partialAnswers.stream().anyMatch(ans -> finalUserAnswer1.contains(ans.toLowerCase()))) {
                            pointsFromAnswer = 20;
                            JOptionPane.showMessageDialog(this, "Ваша відповідь частково правильна! Отримано +20 балів.", "Відповідь часткова", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            pointsFromAnswer = 5;
                            JOptionPane.showMessageDialog(this, "Ваша відповідь неправильна. Отримано +5 балів за присутність.", "Відповідь невірна", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    pointsFromAnswer = 5;
                    JOptionPane.showMessageDialog(this, "Не вдалося знайти питання. Отримано +5 балів.", "Помилка питання", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                pointsFromAnswer = 5;
                JOptionPane.showMessageDialog(this, "Ви не відповіли. Отримано +5 балів за присутність.", "Без відповіді", JOptionPane.WARNING_MESSAGE);
            }

            currentSpunScore = pointsFromAnswer;
            scoreLabel.setText("Отримано: " + currentSpunScore + " балів");
            saveScoreAutomatically();
        });
    }

    private void saveScoreAutomatically() {
        if (currentSpunScore != null) {
            Integer existingScore = student.getTrimesterScore(discipline.getDisciplineId());
            int scoreToSave;
            int actualAttemptsMade = student.getZalikAttempts(discipline.getDisciplineId());

            if (actualAttemptsMade < MAX_REGULAR_ATTEMPTS) {
                int currentBaseScore = (existingScore != null) ? existingScore : 40;
                scoreToSave = currentBaseScore + currentSpunScore;
            } else {
                scoreToSave = 40 + currentSpunScore;
                if (existingScore != null) { // Якщо був попередній бал, беремо кращий (для перездачі)
                    scoreToSave = Math.max(existingScore, scoreToSave);
                }
            }

            if (scoreToSave > 100) {
                scoreToSave = 100;
            }

            student.setTrimesterScore(discipline.getDisciplineId(), scoreToSave);
            student.incrementZalikAttempts(discipline.getDisciplineId());

            currentAttempt = student.getZalikAttempts(discipline.getDisciplineId());

            JOptionPane.showMessageDialog(this,
                    "Додано " + currentSpunScore + " балів. Ваш поточний бал за екзамен: " + scoreToSave + "!",
                    "Бал збережено",
                    JOptionPane.INFORMATION_MESSAGE);

            if (scoreToSave >= PASSING_SCORE) {
                JOptionPane.showMessageDialog(this,
                        "Екзамен успішно складено! Бал: " + scoreToSave + ".",
                        "Успіх!",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else if (currentAttempt < MAX_TOTAL_ATTEMPTS) {
                currentSpunScore = null;
                scoreLabel.setText(" ");
                updateUIBasedOnAttempts();
            } else {
                JOptionPane.showMessageDialog(this,
                        "На жаль, Ви не набрали достатньо балів після " + MAX_TOTAL_ATTEMPTS + " спроб. Вас відраховано з університету!",
                        "Невдача",
                        JOptionPane.ERROR_MESSAGE);
                student.expel();
                dispose();
            }
            if (parentGUI != null) {
                parentGUI.updateProgressDisplay();
            }
        }
    }

    class WheelPanel extends JPanel {
        private double rotationAngle = 0;
        private Timer timer;
        private long startTime;
        private long duration = 3000;
        private double totalRotation = 0;
        private SpinCompletionListener completionListener;

        private final Color[] sectorColors = {
                SIMS_LIGHT_BLUE, SIMS_ACCENT_COLOR, SIMS_MEDIUM_PINK, SIMS_LIGHT_PINK
        };

        public WheelPanel() {
            setBackground(SIMS_LIGHT_PINK);
        }

        public void startSpinning(double finalTargetRotation, int rotations, SpinCompletionListener listener) {
            this.completionListener = listener;
            this.totalRotation = finalTargetRotation;
            this.rotationAngle = 0;
            this.startTime = System.currentTimeMillis();

            if (timer != null && timer.isRunning()) {
                timer.stop();
            }

            timer = new Timer(15, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    if (elapsed < duration) {
                        double progress = (double) elapsed / duration;
                        double easedProgress = 1 - Math.pow(1 - progress, 3);
                        rotationAngle = totalRotation * easedProgress;
                        repaint();
                    } else {
                        timer.stop();
                        rotationAngle = totalRotation;
                        repaint();
                        int resultScore = calculateScoreFromResultAngle(rotationAngle);
                        if (completionListener != null) {
                            completionListener.onSpinComplete(resultScore);
                        }
                    }
                }
            });
            timer.start();
        }

        private int calculateScoreFromResultAngle(double angle) {
            double normalizedAngle = (angle % 360 + 360) % 360;
            double angleAtArrow = (360 - normalizedAngle) % 360;

            int score = (int) (angleAtArrow / (360.0 / 60)) + 1;
            if (score == 61) score = 60;
            if (score == 0) score = 60;
            return score;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = Math.min(centerX, centerY) - 20;

            java.awt.geom.AffineTransform oldTransform = g2d.getTransform();

            g2d.rotate(Math.toRadians(rotationAngle), centerX, centerY);

            for (int i = 0; i < 60; i++) {
                double startAngle = i * (360.0 / 60);
                g2d.setColor(sectorColors[i % sectorColors.length]);
                g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2,
                        (int) startAngle, (int) (360.0 / 60));
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2,
                        (int) startAngle, (int) (360.0 / 60));
            }

            g2d.setColor(SIMS_DARK_TEXT);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 9)); // Smaller font for names
            FontMetrics fm = g2d.getFontMetrics();

            // Draw professor names instead of numbers
            for (int i = 0; i < 60; i++) {
                double angleInRadians = Math.toRadians(i * (360.0 / 60) + (360.0 / 60) / 2); // Center of the segment
                int textRadius = radius - fm.getHeight() / 2 - 5;

                int x = (int) (centerX + textRadius * Math.cos(angleInRadians));
                int y = (int) (centerY + textRadius * Math.sin(angleInRadians));

                // Get professor name, repeating if less than 60 unique names
                //    String text = professorNames.get(i % professorNames.size());

                    String text = " " + i;

                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();

                g2d.rotate(angleInRadians + Math.toRadians(90), x, y);
                g2d.drawString(text, x - textWidth / 2, y + textHeight / 4);
                g2d.rotate(-(angleInRadians + Math.toRadians(90)), x, y);
            }

            g2d.setTransform(oldTransform);

            g2d.setColor(Color.RED);
            int arrowBaseSize = 9;
            int arrowLength = radius - 136;

            int[] xPoints = {
                    centerX + radius,
                    centerX + radius,
                    centerX + radius - arrowLength
            };
            int[] yPoints = {
                    centerY - arrowBaseSize / 2,
                    centerY + arrowBaseSize / 2,
                    centerY
            };

            g2d.fillPolygon(xPoints, yPoints, 3);
            g2d.drawPolygon(xPoints, yPoints, 3);

            g2d.setColor(SIMS_DARK_TEXT);
            g2d.fillOval(centerX - 15, centerY - 15, 30, 30);
            g2d.setColor(Color.WHITE);
            g2d.drawOval(centerX - 15, centerY - 15, 30, 30);

            g2d.dispose();
        }
    }

    @FunctionalInterface
    interface SpinCompletionListener {
        void onSpinComplete(int resultScore);
    }
}