package studies;

import org.example.Discipline;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.Random;

public class CreditWindow extends JDialog {
    private Discipline discipline;
    private Student student;
    private int currentAttempt;
    private JLabel scoreLabel;
    private JButton spinButton;
    private WheelPanel wheelPanel;
    private Integer currentSpunScore = null;
    private JLabel attemptLabel;

    private static final int MAX_REGULAR_ATTEMPTS = 2;
    private static final int MAX_TOTAL_ATTEMPTS = 3; // 2 регулярні + 1 перездача
    private static final int PASSING_SCORE = 60;

    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_ACCENT_COLOR = new Color(255, 179, 186);

    private StudyProgressGUI parentGUI;

    public CreditWindow(Frame owner, Discipline discipline, Student student, int currentAttempt) {
        super(owner, "Залік", true);
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

        JLabel titleLabel = new JLabel("Отримання балів за залік з дисципліни «" + discipline.getName() + "»");
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
        for (Component comp : ((JPanel) getContentPane().getComponent(0)).getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.setBackground(SIMS_MEDIUM_PINK);
                button.setForeground(SIMS_DARK_TEXT);
                button.setFocusPainted(false);
                button.setBorder(BorderFactory.createLineBorder(SIMS_LIGHT_BLUE, 2));
            } else if (comp instanceof JLabel) {
                comp.setForeground(SIMS_DARK_TEXT);
            }
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

        // Дозволяємо крутити колесо, якщо спроб менше MAX_REGULAR_ATTEMPTS,
        // АБО якщо спроб рівно MAX_REGULAR_ATTEMPTS (тобто це перездача)
        // АБО якщо поточний бал < PASSING_SCORE і спроб менше MAX_TOTAL_ATTEMPTS
        if (actualAttemptsMade < MAX_REGULAR_ATTEMPTS ||
                (actualAttemptsMade == MAX_REGULAR_ATTEMPTS && (currentTotalScore == null || currentTotalScore < PASSING_SCORE))) {
            spinButton.setEnabled(true);
            scoreLabel.setText("Поточний бал: " + (currentTotalScore != null ? currentTotalScore : "40"));
            if (actualAttemptsMade < MAX_REGULAR_ATTEMPTS) {
                spinButton.setText("Крутити колесо!");
                attemptLabel.setText("Спроба: " + (actualAttemptsMade + 1) + "/" + MAX_REGULAR_ATTEMPTS);
            } else { // actualAttemptsMade == MAX_REGULAR_ATTEMPTS, це буде перездача
                spinButton.setText("Крутити колесо (перездача)!");
                attemptLabel.setText("Спроба: Перездача (3/" + MAX_TOTAL_ATTEMPTS + ")");
            }
        } else if (currentTotalScore != null && currentTotalScore >= PASSING_SCORE) {
            spinButton.setEnabled(false);
            spinButton.setText("Залік складено");
            scoreLabel.setText("Складено: " + currentTotalScore + " балів");
            attemptLabel.setText("Залік складено успішно!");
        } else { // Всі спроби вичерпано і бал < PASSING_SCORE
            spinButton.setEnabled(false);
            spinButton.setText("Спроби вичерпано");
            scoreLabel.setText("Бал недостатній: " + (currentTotalScore != null ? currentTotalScore : "0") + "!");
            attemptLabel.setText("Всі спроби вичерпано!");
        }
    }


    private void spinWheel() {
        spinButton.setEnabled(false);

        Random random = new Random();
        // Бал від 1 до 60
        int finalScore = random.nextInt(60) + 1;
        int rotations = 5 + random.nextInt(5);
        double sectorSize = 360.0 / 60;
        double targetAngleInWheelCoordinates = (finalScore - 1) * sectorSize + sectorSize / 2;
        double rotationOffset = (360 - targetAngleInWheelCoordinates);
        double totalTargetRotation = rotations * 360 + rotationOffset;

        wheelPanel.startSpinning(totalTargetRotation, rotations, resultScore -> {
            currentSpunScore = resultScore;
            scoreLabel.setText("Отримано: " + currentSpunScore + " балів");
            saveScoreAutomatically();
        });
    }

    private void saveScoreAutomatically() {
        if (currentSpunScore != null) {
            Integer existingScore = student.getTrimesterScore(discipline.getDisciplineId());
            int scoreToSave;

            int baseScoreToAdd = 40; // Базовий бал, який студент має до колеса
            int previousSpunPoints = 0;

            if (existingScore != null) {
                // Якщо вже є бал, ми хочемо додати лише нові спінові бали до нього,
                // не перевищуючи 100 і не повторюючи базовий бал.
                // previousSpunPoints - це та частина балу, яка була отримана з колеса
                // (тобто існуючий бал мінус базові 40).
                previousSpunPoints = Math.max(0, existingScore - baseScoreToAdd);
            }

            // Новий загальний бал = Базовий бал (40) + Накопичені бали з попередніх спінів + Отримані бали
            // Це неправильна логіка. Нам потрібно або додати новий бал до попереднього,
            // АБО просто взяти максимальний з існуючого та нового (якщо ми хочемо, щоб кращий бал зберігався).
            // За умови "завжди прокрутити 2 рази", краще зберігати найкращий або просто останній.
            // Припустимо, ми хочемо, щоб кожен новий спін давав "новий" бал, додаючи його до 40.

            // Проста логіка: новий бал - це 40 + поточний_спін
            // Або: новий бал = Math.max(existingScore != null ? existingScore : 0, baseScoreToAdd + currentSpunScore);
            // Виходячи з попередньої реалізації, де 40 завжди додається, зберігаємо її, але з корекцією.

            // Правильна логіка для збереження:
            // Якщо це перша спроба, бал = 40 + currentSpunScore.
            // Якщо це наступна спроба, бал = 40 + MAX( попередній_спін_бал, поточний_спін_бал )
            // Або просто: новий бал - це кращий з усіх спроб, або просто останній результат.

            // Якщо ми хочемо, щоб бали "накопичувалися" або просто брали кращий результат,
            // а не сумувалися з попередніми спінами після 40, то логіка має бути такою:
            scoreToSave = baseScoreToAdd + currentSpunScore; // Поточний результат за спробу
            if (existingScore != null) {
                // Якщо вже є існуючий бал, беремо максимум між ним і новим scoreToSave
                scoreToSave = Math.max(existingScore, scoreToSave);
            }
            // Це забезпечить, що бал ніколи не зменшиться, але й не буде "накопичуватися" нескінченно.


            if (scoreToSave > 100) {
                scoreToSave = 100;
            }

            student.setTrimesterScore(discipline.getDisciplineId(), scoreToSave);
            student.incrementZalikAttempts(discipline.getDisciplineId());
            currentAttempt = student.getZalikAttempts(discipline.getDisciplineId()); // Оновлюємо currentAttempt з фактичної кількості

            JOptionPane.showMessageDialog(this,
                    "Додано " + currentSpunScore + " балів. Ваш поточний бал за залік: " + scoreToSave + "!",
                    "Бал збережено",
                    JOptionPane.INFORMATION_MESSAGE);

            // Логіка перевірки результату та доступності наступної спроби
            if (scoreToSave >= PASSING_SCORE && student.getZalikAttempts(discipline.getDisciplineId()) >= MAX_REGULAR_ATTEMPTS) {
                // Якщо бал прохідний і вже зроблено мінімум 2 спроби (або більше)
                // АБО якщо бал прохідний після перездачі
                JOptionPane.showMessageDialog(this,
                        "Залік успішно складено! Бал: " + scoreToSave + ".",
                        "Успіх!",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Закриваємо вікно
            } else if (student.getZalikAttempts(discipline.getDisciplineId()) < MAX_TOTAL_ATTEMPTS) {
                // Якщо спроб ще лишилось (навіть якщо бал прохідний, але спроб < MAX_REGULAR_ATTEMPTS)
                // АБО якщо бал не прохідний, але є ще спроби
                currentSpunScore = null;
                scoreLabel.setText(" ");
                updateUIBasedOnAttempts(); // Оновлюємо UI для наступної спроби
            } else {
                // Всі спроби вичерпано і бал < PASSING_SCORE
                JOptionPane.showMessageDialog(this,
                        "На жаль, Ви не набрали достатньо балів після " + MAX_TOTAL_ATTEMPTS + " спроб. Вас відраховано з університету!",
                        "Відрахування",
                        JOptionPane.ERROR_MESSAGE);
                student.expel();
                dispose(); // Закриваємо вікно
            }
            if (parentGUI != null) {
                parentGUI.updateProgressDisplay(); // Оновлюємо батьківське вікно
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
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 9));
            FontMetrics fm = g2d.getFontMetrics();

            for (int i = 1; i <= 60; i++) {
                double angleInRadians = Math.toRadians((i - 1) * (360.0 / 60) + (360.0 / 60) / 2);
                int textRadius = radius - fm.getHeight() / 2 - 5;

                int x = (int) (centerX + textRadius * Math.cos(angleInRadians));
                int y = (int) (centerY + textRadius * Math.sin(angleInRadians));

                String text = String.valueOf(i);
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