package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    private Hero hero;
    private JLabel statsLabel;
    private Timer gameTimer;
    private GameFrame parentFrame;

    private Point lastMousePos;
    private boolean isHeroDragging = false;
    private boolean isSceneDragging = false;

    private int sceneOffsetX = 0;
    private int sceneOffsetY = 0;
    private double sceneScale = 1.0;

    private final double SCALE_SPEED = 0.1;

    private JPanel heroActionsPanel;

    private long lastAiMessageTime;
    private final long AI_MESSAGE_INTERVAL_MIN = 2000; // Мінімум 2 секунди
    private final long AI_MESSAGE_INTERVAL_MAX = 6000; // Максимум 6 секунд
    private long nextAiMessageInterval;
    private Random random = new Random();

    private String[] studentNames = {"Ксенія", "Катя", "Петро", "Женя", "Ольга", "Тарас", "Стас", "Дмитро"};
    private String[] kmaMessages = {
            "Коли там вже результати модуля з вишки?",
            "Хтось розуміє, що робити з цим есе з філософії?.",
            "Треба здати дедлайн з проєктного менеджменту!",
            "Де найкраща кава біля корпусу?",
            "Порадьте, як вижити на сесії в Могилянці!!!",
            "Завтра пара з Пєчкуровою, хто готовий?",
            "Хочу до Глибовця на лекцію!",
            "У когось є конспект з історії України?",
            "Не забудьте зареєструватися на вибіркові!",
            "Мій сім вже п'яту годину сидить над курсовою...",
            "Хтось піде сьогодні на КМЦ?",
            "Як же хочеться спати, але ж лекція з мікроекономіки!",
            "Шукаю одногрупників для роботи над спільним проєктом.",
            "Чи є в когось вільний комп'ютер у бібліотеці?",
            "Ого, мій сім щойно отримав 95 балів за колоквіум!",
            "Цей семінар з логіки просто вибух мозку.",
            "Коли наступний могилянський пікнік?",
            "Хтось вже бачив розклад на наступний семестр?",
            "Ура, здали всі лаби з матаналізу!",
            "Думаю про те, як поєднати навчання і роботу.",
            "Що не так із САЗом???",
            "Вже скучив за бурсою...",
            "Що робити, якщо я вже всьо?..",
            "Хеееееелп",
            "Чому цей персонаж так швидко знову хоче їсти???",
            "Нє, ну мій герой прям найкращий, ахахахах",
            "ПХХАХАХ, кринж",
            "Топова гра, канєшна",
            "Ля-ля-ля",
            "Туць-туць-туць",
            "i want to break free",
            "Можна не спамити тут???"
    };


    public GamePanel(Hero hero, GameFrame parentFrame) {
        this.hero = hero;
        this.parentFrame = parentFrame;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.LIGHT_GRAY);

        setLayout(null);

        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statsLabel.setForeground(Color.BLACK);
        updateStatsDisplay();
        statsLabel.setBounds(10, 10, 200, 100);
        add(statsLabel);

        heroActionsPanel = new JPanel();
        heroActionsPanel.setLayout(new GridLayout(2, 2, 5, 5));
        heroActionsPanel.setBackground(new Color(200, 200, 255, 180));
        heroActionsPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        heroActionsPanel.setVisible(false);

        JButton eatButton = new JButton("Їсти");
        eatButton.setFont(new Font("Arial", Font.PLAIN, 10));
        eatButton.addActionListener(e -> {
            hero.eat();
            updateStatsDisplay();
        });
        heroActionsPanel.add(eatButton);

        JButton sleepButton = new JButton("Спати");
        sleepButton.setFont(new Font("Arial", Font.PLAIN, 10));
        sleepButton.addActionListener(e -> {
            hero.sleep();
            updateStatsDisplay();
        });
        heroActionsPanel.add(sleepButton);

        JButton studyButton = new JButton("Навчатися");
        studyButton.setFont(new Font("Arial", Font.PLAIN, 10));
        studyButton.addActionListener(e -> {
            hero.study();
            updateStatsDisplay();
        });
        heroActionsPanel.add(studyButton);

        JButton relaxButton = new JButton("Відпочивати");
        relaxButton.setFont(new Font("Arial", Font.PLAIN, 10));
        relaxButton.addActionListener(e -> {
            hero.relax();
            updateStatsDisplay();
        });
        heroActionsPanel.add(relaxButton);

        add(heroActionsPanel);

        gameTimer = new Timer(1000 / 60, this);
        gameTimer.start();

        lastAiMessageTime = System.currentTimeMillis();
        nextAiMessageInterval = generateRandomAiMessageInterval();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int scaledMouseX = (int) ((e.getX() - sceneOffsetX) / sceneScale);
                int scaledMouseY = (int) ((e.getY() - sceneOffsetY) / sceneScale);

                if (scaledMouseX >= hero.getX() && scaledMouseX <= hero.getX() + hero.getScaledWidth() &&
                        scaledMouseY >= hero.getY() && scaledMouseY <= hero.getY() + hero.getScaledHeight()) {
                    isHeroDragging = true;
                    lastMousePos = new Point(scaledMouseX - hero.getX(), scaledMouseY - hero.getY());
                } else {
                    isSceneDragging = true;
                    lastMousePos = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isHeroDragging = false;
                isSceneDragging = false;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int scaledMouseX = (int) ((e.getX() - sceneOffsetX) / sceneScale);
                    int scaledMouseY = (int) ((e.getY() - sceneOffsetY) / sceneScale);

                    if (scaledMouseX >= hero.getX() && scaledMouseX <= hero.getX() + hero.getScaledWidth() &&
                            scaledMouseY >= hero.getY() && scaledMouseY <= hero.getY() + hero.getScaledHeight()) {
                        hero.setSelected(!hero.isSelected());
                        if (hero.isSelected()) {
                            showHeroActionsPanel();
                        } else {
                            hideHeroActionsPanel();
                        }
                    } else {
                        hero.setSelected(false);
                        hideHeroActionsPanel();
                    }
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos == null) return;

                if (isHeroDragging && hero.isSelected()) {
                    int newHeroX = (int) ((e.getX() - sceneOffsetX) / sceneScale - lastMousePos.x);
                    int newHeroY = (int) ((e.getY() - sceneOffsetY) / sceneScale - lastMousePos.y);

                    hero.x = newHeroX;
                    hero.initialY = newHeroY;

                    updateHeroActionsPanelLocation();

                } else if (isSceneDragging) {
                    int dx = e.getX() - lastMousePos.x;
                    int dy = e.getY() - lastMousePos.y;

                    sceneOffsetX += dx;
                    sceneOffsetY += dy;

                    updateHeroActionsPanelLocation();

                    lastMousePos = e.getPoint();
                }
                repaint();
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double oldScale = sceneScale;
                double scaleChange = e.getWheelRotation() * -SCALE_SPEED;
                sceneScale += scaleChange;
                sceneScale = Math.max(0.1, sceneScale);
                sceneScale = Math.min(5.0, sceneScale);

                int mouseX = e.getX();
                int mouseY = e.getY();

                double scaleRatio = sceneScale / oldScale;

                sceneOffsetX = (int) (mouseX - ((mouseX - sceneOffsetX) * scaleRatio));
                sceneOffsetY = (int) (mouseY - ((mouseY - sceneOffsetY) * scaleRatio));

                updateHeroActionsPanelLocation();

                repaint();
            }
        });
    }

    private void updateStatsDisplay() {
        String stats = String.format(
                "<html><b>%s</b><br>" +
                        "Енергія: %d<br>" +
                        "Настрій: %d<br>" +
                        "Голод: %d</html>",
                hero.getName(),
                hero.getEnergy(),
                hero.getMood(), hero.getHunger()
        );
        statsLabel.setText(stats);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        hero.draw(g, sceneOffsetX, sceneOffsetY, sceneScale);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        hero.update();
        updateStatsDisplay();

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAiMessageTime >= nextAiMessageInterval) {
            generateRandomChatMessages();
            lastAiMessageTime = currentTime;
            nextAiMessageInterval = generateRandomAiMessageInterval();
        }

        if (hero.isSelected()) {
            updateHeroActionsPanelLocation();
        }
        repaint();
    }

    private void showHeroActionsPanel() {
        heroActionsPanel.setVisible(true);
        updateHeroActionsPanelLocation();
    }

    private void hideHeroActionsPanel() {
        heroActionsPanel.setVisible(false);
    }

    private void updateHeroActionsPanelLocation() {
        int heroScreenX = (int) (hero.getX() * sceneScale + sceneOffsetX);
        int heroScreenY = (int) (hero.getY() * sceneScale + sceneOffsetY);
        int heroScreenHeight = (int) (hero.getScaledHeight() * sceneScale);

        int panelWidth = 200;
        int panelHeight = 80;

        heroActionsPanel.setSize(panelWidth, panelHeight);

        int panelX = heroScreenX + hero.getScaledWidth() / 2 - panelWidth / 2;
        int panelY = heroScreenY - panelHeight - 10;

        panelX = Math.max(0, Math.min(panelX, getWidth() - panelWidth));
        panelY = Math.max(0, Math.min(panelY, getHeight() - panelHeight));
        panelY = Math.max(0, panelY);

        heroActionsPanel.setLocation(panelX, panelY);
    }

    private long generateRandomAiMessageInterval() {
        return (long) (random.nextDouble() * (AI_MESSAGE_INTERVAL_MAX - AI_MESSAGE_INTERVAL_MIN + 1)) + AI_MESSAGE_INTERVAL_MIN;
    }

    private void generateRandomChatMessages() {
        String randomPlayer = studentNames[random.nextInt(studentNames.length)];
        String randomMsg = kmaMessages[random.nextInt(kmaMessages.length)];
        parentFrame.appendToChat(randomPlayer, randomMsg);
    }
}