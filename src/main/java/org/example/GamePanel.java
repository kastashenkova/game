package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

    private long lastMessageTime;
    private final long MESSAGE_INTERVAL_MIN = 2000;
    private final long MESSAGE_INTERVAL_MAX = 6000;
    private long nextMessageInterval;
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

    private enum GameState {
        PLAYING, GAME_OVER
    }
    private GameState currentGameState;

    // Default hero properties are no longer needed here as Hero object is passed directly.
    // The responsibility for creating new hero moved to GameFrame for restart.


    public GamePanel(Hero hero, GameFrame parentFrame) { // Removed initial hero properties from constructor
        this.hero = hero;
        this.parentFrame = parentFrame;
        setPreferredSize(new Dimension(1000, 800));
        setBackground(Color.LIGHT_GRAY);

        currentGameState = GameState.PLAYING;

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
        heroActionsPanel.setVisible(false);

        JButton eatButton = new JButton("Їсти");
        eatButton.setFont(new Font("Arial", Font.PLAIN, 10));
        eatButton.addActionListener(e -> {
            if (currentGameState == GameState.PLAYING) {
                hero.eat();
                updateStatsDisplay();
            }
        });
        heroActionsPanel.add(eatButton);

        JButton sleepButton = new JButton("Спати");
        sleepButton.setFont(new Font("Arial", Font.PLAIN, 10));
        sleepButton.addActionListener(e -> {
            if (currentGameState == GameState.PLAYING) {
                hero.sleep();
                updateStatsDisplay();
            }
        });
        heroActionsPanel.add(sleepButton);

        JButton studyButton = new JButton("Навчатися");
        studyButton.setFont(new Font("Arial", Font.PLAIN, 10));
        studyButton.addActionListener(e -> {
            if (currentGameState == GameState.PLAYING) {
                hero.study();
                updateStatsDisplay();
            }
        });
        heroActionsPanel.add(studyButton);

        JButton relaxButton = new JButton("Відпочивати");
        relaxButton.setFont(new Font("Arial", Font.PLAIN, 10));
        relaxButton.addActionListener(e -> {
            if (currentGameState == GameState.PLAYING) {
                hero.relax();
                updateStatsDisplay();
            }
        });
        heroActionsPanel.add(relaxButton);

        add(heroActionsPanel);

        gameTimer = new Timer(1000 / 60, this);
        gameTimer.start();

        lastMessageTime = System.currentTimeMillis();
        nextMessageInterval = generateRandomMessageInterval();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentGameState != GameState.PLAYING) return;

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
                if (currentGameState != GameState.PLAYING) return;

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
                if (currentGameState != GameState.PLAYING) return;
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
                if (currentGameState != GameState.PLAYING) return;

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

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (currentGameState == GameState.GAME_OVER && e.getKeyCode() == KeyEvent.VK_R) {
                    // Trigger game over event in parent frame
                    fireGameOverEvent(hero.getGameOverReason());
                }
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

        if (currentGameState == GameState.GAME_OVER) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String gameOverText = "ГРА ЗАКІНЧЕНА!";
            FontMetrics fm = g.getFontMetrics(g.getFont());
            int textWidth = fm.stringWidth(gameOverText);
            int textHeight = fm.getHeight();
            g.drawString(gameOverText, (getWidth() - textWidth) / 2, getHeight() / 2 - textHeight);

            g.setFont(new Font("Arial", Font.PLAIN, 18));
            String reasonText = hero.getGameOverReason();
            fm = g.getFontMetrics(g.getFont());
            textWidth = fm.stringWidth(reasonText);
            g.drawString(reasonText, (getWidth() - textWidth) / 2, getHeight() / 2 + 20);

            g.setFont(new Font("Arial", Font.BOLD, 24));
            String restartText = "Натисніть 'R', щоб почати спочатку.";
            fm = g.getFontMetrics(g.getFont());
            textWidth = fm.stringWidth(restartText);
            g.drawString(restartText, (getWidth() - textWidth) / 2, getHeight() / 2 + 80);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentGameState == GameState.PLAYING) {
            hero.update();
            updateStatsDisplay();

            if (hero.isGameOverDueToEnergy()) {
                currentGameState = GameState.GAME_OVER;
                heroActionsPanel.setVisible(false);
                gameTimer.stop();
                parentFrame.appendToChat("Система", hero.getGameOverReason());
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMessageTime >= nextMessageInterval) {
                generateRandomChatMessages();
                lastMessageTime = currentTime;
                nextMessageInterval = generateRandomMessageInterval();
            }

            if (hero.isSelected()) {
                updateHeroActionsPanelLocation();
            }
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

        int panelWidth = 200;
        int panelHeight = 80;

        heroActionsPanel.setSize(panelWidth, panelHeight);

        int panelX = heroScreenX + (int)(hero.getScaledWidth() * sceneScale) / 2 - panelWidth / 2;
        int panelY = heroScreenY - panelHeight - 10;

        panelX = Math.max(0, Math.min(panelX, getWidth() - panelWidth));
        panelY = Math.max(0, Math.min(panelY, getHeight() - panelHeight));
        panelY = Math.max(0, panelY);

        heroActionsPanel.setLocation(panelX, panelY);
    }

    private long generateRandomMessageInterval() {
        return (long) (random.nextDouble() * (MESSAGE_INTERVAL_MAX - MESSAGE_INTERVAL_MIN + 1)) + MESSAGE_INTERVAL_MIN;
    }

    private void generateRandomChatMessages() {
        String randomPlayer = studentNames[random.nextInt(studentNames.length)];
        String randomMsg = kmaMessages[random.nextInt(kmaMessages.length)];
        parentFrame.appendToChat(randomPlayer, randomMsg);
    }

    // New method to fire the game over event to the parent frame
    public void fireGameOverEvent(String reason) {
        // Stop the game timer definitively
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
        // Notify the GameFrame that the game is over and it should handle the restart
        parentFrame.handleGameOver(reason);
    }

    // Method to set a new hero (used for restarting from GameFrame)
    public void setHero(Hero newHero) {
        this.hero = newHero;
        currentGameState = GameState.PLAYING; // Reset game state to playing
        isHeroDragging = false;
        isSceneDragging = false;
        sceneOffsetX = 0;
        sceneOffsetY = 0;
        sceneScale = 1.0;
        hideHeroActionsPanel();
        updateStatsDisplay();
        gameTimer.start(); // Restart the timer for the new game
        lastMessageTime = System.currentTimeMillis();
        nextMessageInterval = generateRandomMessageInterval();
        repaint();
    }
}