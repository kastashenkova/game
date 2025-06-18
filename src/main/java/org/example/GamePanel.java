package org.example;

import gui.GoodbyeWindow;
import gui.HintPanel;
import gui.LoadingFrame;
import mainstage.MainFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * GamePanel is the main panel where the game world is rendered and user interactions are handled.
 * It displays the hero, manages game state, handles user input for hero movement and actions,
 * and displays in-game messages.
 */
public class GamePanel extends JPanel implements ActionListener {
    private Hero hero;
    private JLabel statsLabel;
    private Timer gameTimer;
    private GameFrame parentFrame;


    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);
    private final Color SIMS_BUTTON_HOVER = new Color(255, 240, 245);

    HintPanel hintPanel;

    private Point lastMousePos;
    private boolean isHeroDragging = false;
    private boolean isSceneDragging = false;

    private int sceneOffsetX = 0;
    private int sceneOffsetY = 0;
    private double sceneScale = 1.0;

    private final double SCALE_SPEED = 0.1;

    private JPanel heroActionsPanel;

    private JButton goToWorldButton;

    private long lastMessageTime;
    private final long MESSAGE_INTERVAL_MIN = 500;
    private final long MESSAGE_INTERVAL_MAX = 2000;
    private long nextMessageInterval;
    private Random random = new Random();

    // --- For floating chat messages ---
    private List<FloatingMessage> floatingMessages;
    private final int FLOATING_MESSAGE_DISPLAY_DURATION = 3300;
    private final int MESSAGE_OFFSET_Y = 5; // Offset between messages
    private final int MESSAGE_MARGIN_X = 20; // Offset from the right edge
    private final int MESSAGE_BOTTOM_START_Y = 50; // Offset from the bottom edge for the first message

    private JTextField userMessageInputField;
    private JButton sendUserMessageButton;


    private String[] studentNames = {"Ксенія", "Катя", "Петро", "Женя", "Ольга", "Тарас", "Стас", "Дмитро"};
    private String[] kmaMessages = {
            "Коли там вже результати модуля з вишки?",
            "Хтось розуміє, що робити з цим есе з філософії?",
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
            "Що робити, якщо я вже всьо?..",
            "Хеееееелп",
            "Чому цей персонаж так швидко знову хоче їсти???",
            "Нє, ну мій герой прям найкращий, ахахахах",
            "ПХХАХАХ, кринж",
            "Топова гра, канєшна",
            "Ля-ля-ля",
            "Туць-туць-туць",
            "i want to break free",
            "Можна не спамити тут???",
            "Чому на 3 курсі кожна пара — як фінальний босс?",
            "Невже хтось уже написав курсову з ML? Поділіться натхненням!",
            "Як це «використайте API», якщо я ще чайник у ньому?",
            "Чи є в когось старі лаби з системного програмування?",
            "Мені здається, що інтеліджі мене ненавидить...",
            "У когось є гайд по нормальному сну під час сесії?",
            "Які кафешки працюють після 9 вечора біля кампусу?",
            "Чому компілюється, але не працює?!",
            "Що брати з собою на ІТ-хакатон? Крім паніки.",
            "Хто хоче зібратись і разом подебажити алгоритми перед заліком?",
            "Скільки ще можна мучити ту джаву...",
            "Коли дедлайн — це твій особистий ворог номер один...",
            "Хтось іде сьогодні на англійську?",
            "Нащо мені це все, якщо я просто хочу писати гемблінг?",
            "Можна, будь ласка, ще один тиждень перед сесією...",
            "Є хто з бакалаврату, хто пережив лаби з ОС? Поділіться секретами.",
            "Це вже навіть не баг, це фіча життя, пхпхахх",
            "А можна просто скласти сесію, нічого не вивчивши, і жити далі?",
            "Моя мотивація зараз десь між «поспати» і «поплакати»",
            "Гуглю, як здати лабу, якщо ти програміст тільки в серці",
            "Тільки я один починаю курсову в день дедлайну, чи це вже факультетська традиція?",
            "Моя клавіатура вже сама друкує 'git commit -m 'panic fix'",
            "Чому з кожним семестром кави треба все більше, а віри — все менше?",
            "Знову прослухав всю пару, думаючи, чи то я тупий, чи то тема складна",
            "Я тут, я читаю, я нічого не розумію, але я тут"
    };

    private enum GameState {
        PLAYING, GAME_OVER, GAME_PAUSED
    }
    private GameState currentGameState;

    /**
     * Inner class to represent a floating message displayed on the screen.
     */
    private static class FloatingMessage {
        String sender;
        String message;
        long startTime;
        int currentY;
        int height;
        int width;

        /**
         * Constructs a new FloatingMessage.
         *
         * @param sender The sender of the message.
         * @param message The content of the message.
         * @param startTime The time when the message started displaying.
         * @param initialY The initial Y-coordinate for the message.
         * @param width The width of the message bubble.
         * @param height The height of the message bubble.
         */
        FloatingMessage(String sender, String message, long startTime, int initialY, int width, int height) {
            this.sender = sender;
            this.message = message;
            this.startTime = startTime;
            this.currentY = initialY;
            this.width = width;
            this.height = height;
        }

        /**
         * Updates the Y-coordinate of the message.
         * @param newY The new Y-coordinate.
         */
        public void updateY(int newY) {
            this.currentY = newY;
        }
    }


    /**
     * Constructs a new GamePanel.
     *
     * @param hero The Hero object representing the player character.
     * @param parentFrame The parent GameFrame that contains this panel.
     */
    public GamePanel(Hero hero, GameFrame parentFrame) {
        this.hero = hero;
        this.parentFrame = parentFrame;
        setPreferredSize(new Dimension(1200, 800));
        setBackground(new Color(252, 234, 249));
        currentGameState = GameState.PLAYING;
        setLayout(null);

        MusicPlayer.getInstance().setMusicEnabled(true);
        MusicPlayer.getInstance().playMusic("/assets/Sounds/Background.wav");

        hintPanel = new HintPanel();
        hintPanel.setHint(hero.getLevel());
        hintPanel.setBounds(900, 350, 280, 220);
        add(hintPanel);

        JButton sleepButton = createSimsButton("Спати");
        sleepButton.addActionListener(e -> {
            MusicPlayer.getInstance().playButtonClick();
            currentGameState = GameState.GAME_PAUSED;

            Component comp = (Component) e.getSource();
            if (comp == null) return;

            // Create a semi-transparent panel for darkening
            JPanel glassPane = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(new Color(0, 0, 0, 150)); // semi-transparent black
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            glassPane.setOpaque(false);
            glassPane.setLayout(null);

            RootPaneContainer win = (RootPaneContainer) SwingUtilities.getWindowAncestor(comp);
            win.setGlassPane(glassPane);


            JLabel messageLabel = new JLabel("Герой спить...", SwingConstants.CENTER);
            messageLabel.setFont(new Font("MONOSPACED", Font.BOLD, 22));
            messageLabel.setForeground(Color.WHITE);
            messageLabel.setBounds(400, 80, 400, 50); // position and size can be adjusted
            glassPane.add(messageLabel);

            JLabel countdownLabel = new JLabel("10", SwingConstants.CENTER);
            countdownLabel.setFont(new Font("MONOSPACED", Font.BOLD, 56));
            countdownLabel.setForeground(Color.WHITE);
            countdownLabel.setBounds(500, 350, 100, 100);
            glassPane.add(countdownLabel);

            glassPane.setVisible(true);
            glassPane.repaint();

            JLabel z1 = createZLabel("Z", 24, 540, 200);
            JLabel z2 = createZLabel("Z", 36, 580, 180);
            JLabel z3 = createZLabel("Z", 48, 620, 160);

            glassPane.add(z1);
            glassPane.repaint();

            //timer for zzz
            Timer zAppearTimer = new Timer(500, null);
            final int[] step = {1};
            Timer[] pulseTimerHolder = new Timer[1];


            zAppearTimer.addActionListener(eevt -> {
                if (step[0] == 1) {
                    glassPane.add(z2);
                    glassPane.repaint();
                } else if (step[0] == 2) {
                    glassPane.add(z3);
                    glassPane.repaint();
                    zAppearTimer.stop();


                    Timer pulseTimer = new Timer(500, null);
                    pulseTimerHolder[0] = pulseTimer; // store reference

                    final int[] pulseStep = {0};
                    pulseTimer.addActionListener(ev -> {
                        switch (pulseStep[0]) {
                            case 0 -> z1.setVisible(true);
                            case 1 -> z2.setVisible(true);
                            case 2 -> z3.setVisible(true);
                            case 3 -> z1.setVisible(false);
                            case 4 -> z2.setVisible(false);
                            case 5 -> z3.setVisible(false);
                        }
                        pulseStep[0] = (pulseStep[0] + 1) % 6;
                    });

                    pulseTimer.start();
                }
                step[0]++;
            });

            zAppearTimer.start();

            //general timer
            Timer timer = new Timer(1000, null);
            final int[] secondsPassed = {10};

            String[] sleepMessages = {
                    "Герой спить...",
                    "Набираємося сил...",
                    "Сон - важлива справа!",
                    "Заряджаємось енергією..."
            };


            timer.addActionListener(event -> {
                if (secondsPassed[0] > 0) {
                    int timeLeft = secondsPassed[0];
                    countdownLabel.setText(String.valueOf(secondsPassed[0]));
                    hero.increaseEnergy(10);
                    MusicPlayer.getInstance().setMusicEnabled(false);
                    MusicPlayer.getInstance().playTick();
                    int randomIndex = (int)(Math.random() * sleepMessages.length);
                    String msg = sleepMessages[randomIndex];
                    messageLabel.setText(msg);
                    secondsPassed[0]--;
                } else {
                    timer.stop();
                    countdownLabel.setText("0");
                    if (pulseTimerHolder[0] != null) {
                        pulseTimerHolder[0].stop();
                    }
                    currentGameState = GameState.PLAYING;
                    MusicPlayer.getInstance().setMusicEnabled(true);
                    MusicPlayer.getInstance().playMusic("/assets/Sounds/Background.wav");
                    glassPane.setVisible(false);
                    JOptionPane.showMessageDialog(this, "Ваш сім відновив енергію і готовий до нових звершень!");
                }
            });

            timer.start();
        });
        sleepButton.setBounds(1020, 250, 100, 50);
        add(sleepButton);

        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statsLabel.setForeground(Color.BLACK);
        statsLabel.setBounds(10, 10, 200, 100);
        add(statsLabel);

        floatingMessages = new ArrayList<>();

        heroActionsPanel = new JPanel();
        heroActionsPanel.setLayout(new GridLayout(2, 2, 5, 5));
        heroActionsPanel.setBackground(new Color(200, 200, 255, 180));
        heroActionsPanel.setVisible(false);

        if(hero.getLevel()==3){
            JButton schedule = createSimsButton("Розклад сесії");
            schedule.addActionListener(e-> {
                MusicPlayer.getInstance().playButtonClick();
                SwingUtilities.invokeLater(() -> {
                    ExamSessionWindow examSessionWindow = new ExamSessionWindow();
                    examSessionWindow.setVisible(true);
                });
            });
            schedule.setBounds(800, 10, 150, 50);
            add(schedule);
        }
        if(hero.getLevel()==4){
            JButton fin = createSimsButton("Фінальне вікно");
            fin.addActionListener(e-> {
                Window gameWindow = SwingUtilities.getWindowAncestor(this);
                if (gameWindow != null) {
                    gameWindow.dispose();
                }
                MusicPlayer.getInstance().playButtonClick();
                SwingUtilities.invokeLater(() -> {
                    new GoodbyeWindow().setVisible(true);
                });
            });
            fin.setBounds(800, 10, 150, 50);
            add(fin);
        }

        gameTimer = new Timer(1000 / 60, this);
        gameTimer.start();

        lastMessageTime = System.currentTimeMillis();
        nextMessageInterval = generateRandomMessageInterval();

        // --- Initialize and add input field and send button ---
        userMessageInputField = new JTextField();
        userMessageInputField.setFont(new Font("Arial", Font.PLAIN, 12));
        userMessageInputField.setToolTipText("Введіть ваше повідомлення...");
        add(userMessageInputField);

        sendUserMessageButton = new JButton("Надіслати");
        sendUserMessageButton.setFont(new Font("Arial", Font.BOLD, 12));
        add(sendUserMessageButton);

        // Add action listener for the button and input field
        ActionListener sendMessageAction = e -> {
            String message = userMessageInputField.getText().trim();
            if (!message.isEmpty()) {
                showFloatingMessage("Ви", message); // Display the message
                MusicPlayer.getInstance().setMusicEnabled(true);
                MusicPlayer.getInstance().playEffect("/assets/Sounds/message_send.wav");
                userMessageInputField.setText(""); // Clear the field
            }
        };
        sendUserMessageButton.addActionListener(sendMessageAction);
        userMessageInputField.addActionListener(sendMessageAction); // Send on Enter


        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentGameState != GameState.PLAYING) return;

                // If the user clicked on the input field, don't drag the scene/hero
                if (e.getSource() == userMessageInputField || e.getSource() == sendUserMessageButton) {
                    return;
                }

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

                // If the user double-clicked on the input field, don't toggle hero selection
                if (e.getSource() == userMessageInputField || e.getSource() == sendUserMessageButton) {
                    return;
                }

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

                // If dragging occurs on input elements, ignore
                if (e.getSource() == userMessageInputField || e.getSource() == sendUserMessageButton) {
                    return;
                }

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
                positionUserChatElements(); // Update input field position on scale
                repaint();
            }
        });

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (currentGameState == GameState.GAME_OVER && e.getKeyCode() == KeyEvent.VK_R) {
                    fireGameOverEvent(hero.getGameOverReason());
                }
            }
        });

        positionUserChatElements();
        goToWorldButton = getGoToWorldButton();
        goToWorldButton.setBounds(10, 600, 180, 70);
        add(goToWorldButton);

        JPanel panel = heroInfoPanel(hero);
        panel.setBounds(10, 10, 230, 150);
        add(panel);

        JPanel panel1 = infoPanel();
        panel1.setBounds(10, 180, 230, 250);
        add(panel1);
    }
    /**
     * Creates a JLabel with "Z" text for the sleeping animation.
     * @param text The text for the label, usually "Z".
     * @param size The font size of the "Z".
     * @param x The x-coordinate for the label's position.
     * @param y The y-coordinate for the label's position.
     * @return A JLabel configured with "Z" text.
     */
    private JLabel createZLabel(String text, int size, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Serif", Font.BOLD, size));
        label.setForeground(Color.GRAY);
        label.setBounds(x, y, 100, 100);
        return label;
    }

    /**
     * Overrides the paintComponent method to render the game elements.
     * This includes the hero, floating messages, and the game over screen.
     * @param g The Graphics object for drawing.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        hero.draw(g, sceneOffsetX, sceneOffsetY, sceneScale);

        long currentTime = System.currentTimeMillis();
        Font messageFont = new Font("Segoe UI", Font.BOLD, 10);
        g.setFont(messageFont);
        FontMetrics fm = g.getFontMetrics(messageFont);

        // Position messages from bottom up, considering the input field
        int currentY = getHeight() - MESSAGE_BOTTOM_START_Y - (userMessageInputField.isVisible() ? userMessageInputField.getHeight() + 10 : 0);
        List<FloatingMessage> messagesToRemove = new ArrayList<>();

        for (int i = floatingMessages.size() - 1; i >= 0; i--) {
            FloatingMessage msg = floatingMessages.get(i);
            if (currentTime - msg.startTime < FLOATING_MESSAGE_DISPLAY_DURATION) {
                // Use HTML for richer text formatting in JLabel for accurate preferred size calculation
                String text = "<html><b>" + msg.sender + ":</b> " + msg.message + "</html>";
                JLabel tempLabel = new JLabel(text);
                tempLabel.setFont(messageFont);
                // Set max size to ensure preferred size is calculated correctly for wrapping text
                tempLabel.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
                Dimension prefSize = tempLabel.getPreferredSize();

                int messageWidth = prefSize.width + 30; // Add padding
                int messageHeight = prefSize.height + 10; // Add padding

                int messageX = getWidth() - messageWidth - MESSAGE_MARGIN_X;
                msg.currentY = currentY;

                // Draw message bubble background
                g.setColor(new Color(255, 255, 220, 200)); // Light yellow with transparency
                g.fillRoundRect(messageX, msg.currentY, messageWidth, messageHeight, 10, 10);
                // Draw message bubble border
                g.setColor(Color.GRAY);
                g.drawRoundRect(messageX, msg.currentY, messageWidth, messageHeight, 10, 10);

                // Draw message text
                String displayText = msg.sender + ": " + msg.message;
                g.setColor(Color.BLUE.darker());
                g.drawString(displayText, messageX + 10, msg.currentY + fm.getAscent() + 5);

                currentY -= (messageHeight + MESSAGE_OFFSET_Y); // Move Y up for the next message

            } else {
                messagesToRemove.add(msg); // Mark message for removal if its display duration has passed
            }
        }
        floatingMessages.removeAll(messagesToRemove); // Remove expired messages

        // Handle Game Over state rendering
        if (currentGameState == GameState.GAME_OVER) {
            // Darken the screen
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());

            // Draw "GAME OVER!" text
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String gameOverText = "ГРА ЗАВЕРШЕНА!";
            fm = g.getFontMetrics(g.getFont());
            int textWidth = fm.stringWidth(gameOverText);
            int textHeight = fm.getHeight();
            g.drawString(gameOverText, (getWidth() - textWidth) / 2, getHeight() / 2 - textHeight);

            // Draw game over reason
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            String reasonText = hero.getGameOverReason();
            fm = g.getFontMetrics(g.getFont());
            textWidth = fm.stringWidth(reasonText);
            g.drawString(reasonText, (getWidth() - textWidth) / 2, getHeight() / 2 + 20);

            // Draw restart instruction
            g.setFont(new Font("Arial", Font.BOLD, 24));
            String restartText = "Натисніть 'R', щоб почати спочатку.";
            fm = g.getFontMetrics(g.getFont());
            textWidth = fm.stringWidth(restartText);
            g.drawString(restartText, (getWidth() - textWidth) / 2, getHeight() / 2 + 80);

            // Hide the user chat input and send button
            userMessageInputField.setVisible(false);
            sendUserMessageButton.setVisible(false);
        } else {
            // Show the user chat input and send button when the game is active
            userMessageInputField.setVisible(true);
            sendUserMessageButton.setVisible(true);
            positionUserChatElements(); // Ensure they are positioned correctly
        }
    }

    /**
     * This method is called repeatedly by the game timer. It updates the game state,
     * checks for game over conditions, and generates random chat messages.
     * @param e The ActionEvent from the timer.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentGameState == GameState.PLAYING) {
            hero.update(); // Update hero's internal state (e.g., energy, needs)

            if (hero.isGameOverDueToEnergy()) {
                currentGameState = GameState.GAME_OVER; // Change game state to GAME_OVER
                heroActionsPanel.setVisible(false); // Hide hero action panel
                gameTimer.stop(); // Stop the game timer
                showFloatingMessage("Система", hero.getGameOverReason()); // Display game over message
            }

            long currentTime = System.currentTimeMillis();
            // Check if it's time to generate a random chat message
            if (currentTime - lastMessageTime >= nextMessageInterval) {
                generateRandomChatMessages(); // Generate and display a random message

                MusicPlayer.getInstance().setMusicEnabled(true);
                MusicPlayer.getInstance().playEffect("/assets/Sounds/message_received.wav"); // Play message received sound
                lastMessageTime = currentTime; // Reset last message time
                nextMessageInterval = generateRandomMessageInterval(); // Set a new random interval for the next message
            }

            // If the hero is selected, update the position of their action panel
            if (hero.isSelected()) {
                updateHeroActionsPanelLocation();
            }
        }
        repaint(); // Redraw the panel
    }

    /**
     * Makes the hero actions panel visible.
     */
    private void showHeroActionsPanel() {
        heroActionsPanel.setVisible(true);
        updateHeroActionsPanelLocation(); // Ensure it's correctly positioned
    }

    /**
     * Hides the hero actions panel.
     */
    private void hideHeroActionsPanel() {
        heroActionsPanel.setVisible(false);
    }

    /**
     * Updates the location of the hero actions panel based on the hero's position and scene scale/offset.
     * This ensures the panel stays anchored to the hero even when the scene is scaled or dragged.
     */
    private void updateHeroActionsPanelLocation() {
        // Calculate hero's screen coordinates after applying scene scale and offset
        int heroScreenX = (int) (hero.getX() * sceneScale + sceneOffsetX);
        int heroScreenY = (int) (hero.getY() * sceneScale + sceneOffsetY);

        int panelWidth = 200;
        int panelHeight = 80;

        heroActionsPanel.setSize(panelWidth, panelHeight);

        // Position the panel above the hero, centered horizontally
        int panelX = heroScreenX + (int)(hero.getScaledWidth() * sceneScale) / 2 - panelWidth / 2;
        int panelY = heroScreenY - panelHeight - 10; // 10 pixels above the hero

        // Ensure the panel stays within the bounds of the GamePanel
        panelX = Math.max(0, Math.min(panelX, getWidth() - panelWidth));
        panelY = Math.max(0, Math.min(panelY, getHeight() - panelHeight));
        panelY = Math.max(0, panelY); // Prevent panel from going above the top edge

        heroActionsPanel.setLocation(panelX, panelY);
    }

    /**
     * Positions the user chat input field and send button at the bottom right of the panel.
     * This method is called during painting and whenever the panel size or scale changes.
     */
    private void positionUserChatElements() {
        int inputFieldWidth = 150;
        int inputFieldHeight = 25;
        int buttonWidth = 100;
        int buttonHeight = 25;
        int padding = 10;

        // Calculate total width of chat elements to align them from the right
        int totalChatInputWidth = inputFieldWidth + buttonWidth + padding;
        // X-coordinate for right alignment, with a margin from the right edge
        int inputX = getWidth() - totalChatInputWidth - MESSAGE_MARGIN_X;
        // Y-coordinate remains at the bottom, with padding from the bottom edge
        int inputY = getHeight() - inputFieldHeight - padding;

        userMessageInputField.setBounds(inputX, inputY, inputFieldWidth, inputFieldHeight);

        // Position the send button to the right of the input field
        int buttonX = inputX + inputFieldWidth + padding;
        int buttonY = inputY;

        sendUserMessageButton.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
    }

    /**
     * Generates a random time interval for displaying a new chat message.
     * The interval will be between MESSAGE_INTERVAL_MIN and MESSAGE_INTERVAL_MAX.
     * @return A random long value representing the interval in milliseconds.
     */
    private long generateRandomMessageInterval() {
        return (long) (random.nextDouble() * (MESSAGE_INTERVAL_MAX - MESSAGE_INTERVAL_MIN + 1)) + MESSAGE_INTERVAL_MIN;
    }

    /**
     * Generates a random chat message from the predefined lists of student names and messages,
     * and displays it as a floating message on the panel.
     */
    private void generateRandomChatMessages() {
        String randomPlayer = studentNames[random.nextInt(studentNames.length)]; // Pick a random student name
        String randomMsg = kmaMessages[random.nextInt(kmaMessages.length)]; // Pick a random message
        showFloatingMessage(randomPlayer, randomMsg); // Display the message
    }

    /**
     * Displays a floating message on the game panel. Messages are stacked from the bottom right.
     * @param sender The sender of the message.
     * @param message The content of the message.
     */
    public void showFloatingMessage(String sender, String message) {
        Font messageFont = new Font("Segoe UI", Font.BOLD, 10);
        // Create a temporary JLabel to calculate the preferred size of the message text
        JLabel tempLabel = new JLabel("<html><b>" + sender + ":</b> " + message + "</html>");
        tempLabel.setFont(messageFont);
        tempLabel.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE); // Allow JLabel to calculate width for wrapping
        Dimension prefSize = tempLabel.getPreferredSize();

        int messageWidth = prefSize.width + 20; // Add horizontal padding to the message bubble
        int messageHeight = prefSize.height + 10; // Add vertical padding to the message bubble

        // Calculate the initial Y-position for a new message at the bottom,
        // taking into account the user message input field if it's visible.
        int startY = getHeight() - MESSAGE_BOTTOM_START_Y - (userMessageInputField.isVisible() ? userMessageInputField.getHeight() + 10 : 0);

        // Shift all existing floating messages upwards to make space for the new one.
        for (FloatingMessage msg : floatingMessages) {
            msg.updateY(msg.currentY - (messageHeight + MESSAGE_OFFSET_Y));
        }

        final int MAX_DISPLAY_MESSAGES = 3;
        if (floatingMessages.size() >= MAX_DISPLAY_MESSAGES) {
            floatingMessages.remove(0);
        }

        // Create and add the new floating message to the list.
        FloatingMessage newMessage = new FloatingMessage(sender, message, System.currentTimeMillis(), startY, messageWidth, messageHeight);
        floatingMessages.add(newMessage);

        repaint(); // Request a repaint to display the new message
    }

    /**
     * Triggers the game over event, stopping the game timer and notifying the parent frame
     * to handle the game over state (e.g., transition to a new window).
     * @param reason The reason for the game over (e.g., "Hero ran out of energy").
     */
    public void fireGameOverEvent(String reason) {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop(); // Stop the main game loop
        }
        parentFrame.handleGameOver(reason); // Delegate game over handling to the parent frame
    }

    /**
     * Sets a new hero for the game panel, effectively resetting the game state
     * for a new or loaded hero.
     * @param newHero The new Hero object to be used in the game.
     */
    public void setHero(Hero newHero) {
        this.hero = newHero;
        currentGameState = GameState.PLAYING; // Reset game state to playing
        isHeroDragging = false; // Reset dragging flags
        isSceneDragging = false;
        sceneOffsetX = 0; // Reset scene offset and scale
        sceneOffsetY = 0;
        sceneScale = 1.0;
        hideHeroActionsPanel(); // Hide the hero action panel
        gameTimer.start(); // Restart the game timer
        lastMessageTime = System.currentTimeMillis(); // Reset message timer
        nextMessageInterval = generateRandomMessageInterval(); // Generate new message interval
        floatingMessages.clear(); // Clear any existing floating messages
        repaint(); // Redraw the panel with the new hero and state
    }

    /**
     * Creates and returns a JPanel that displays information about the hero.
     * This panel typically shows the hero's name, course, and specialty.
     * @param hero The Hero object whose information is to be displayed.
     * @return A JPanel containing hero information.
     */
    private JPanel heroInfoPanel(Hero hero) {
        String name = hero.getSelectedName();
        int course = hero.getCourse();
        Specialty specialty = hero.getSpecialty();
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 250, 255)); // Light blue-ish background
        panel.setBorder(BorderFactory.createTitledBorder( // Titled border for a neat look
                BorderFactory.createLineBorder(new Color(120, 150, 200), 2), // Blue border
                "Інформація про героя", // Title text
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Inter", Font.BOLD, 14), // Title font
                new Color(60, 90, 150) // Title color
        ));
        panel.setLayout(new GridLayout(3, 1, 5, 5)); // Grid layout for labels

        JLabel nameLabel = new JLabel("Ім’я: " + name);
        nameLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        nameLabel.setForeground(new Color(30, 30, 30)); // Dark gray text

        JLabel courseLabel = new JLabel("Курс: " + course);
        courseLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        courseLabel.setForeground(new Color(30, 30, 30));

        // Using HTML for specialty label to allow text wrapping if needed
        JLabel specialtyLabel = new JLabel("<html><body style='width: 150px'>Спеціальність: " + specialty.toString() + "</body></html>");
        specialtyLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        specialtyLabel.setForeground(new Color(30, 30, 30));

        panel.add(nameLabel);
        panel.add(courseLabel);
        panel.add(specialtyLabel);

        return panel;
    }

    /**
     * Creates and returns a JPanel that provides brief game instructions to the player.
     * This panel offers hints on how to play the game and interact with the world.
     * @return A JPanel containing game instructions.
     */
    private JPanel infoPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(250, 250, 155)); // Light yellow background
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(120, 150, 250), 2), // Blue border
                "Коротка інструкція", // Title text
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Inter", Font.BOLD, 14),
                new Color(60, 90, 150)
        ));
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel("<html><body style='width: 150px'>Виходьте у світ!</body></html>");
        label.setFont(new Font("MONOSPACED", Font.BOLD, 16));
        label.setForeground(new Color(30, 30, 30));
        panel.add(label, BorderLayout.NORTH);

        JLabel label1 = new JLabel("<html><body style='width: 150px'>Для накопичення знань та проходження рівнів, шукайте головний корпус Могилянки та сміливо заходьте!</body></html>");
        label1.setFont(new Font("Inter", Font.ITALIC, 12));
        label1.setForeground(new Color(30, 30, 30));
        panel.add(label1, BorderLayout.CENTER);

        JLabel label2 = new JLabel("<html><body style='width: 150px'>Для підтримки життєздатности не забувайте підкріплюватись у маркеті «SimsPo» або кафе швидкого приготування!</body></html>");
        label2.setFont(new Font("Inter", Font.ITALIC, 12));
        label2.setForeground(new Color(30, 30, 30));
        panel.add(label2, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates and returns a "Go to World" button, which facilitates the transition
     * from the current panel (presumably a character creation/home screen) to the
     * main game world.
     * @return A JButton configured as "Вийти у світ".
     */
    private JButton getGoToWorldButton(){
        goToWorldButton =  new JButton("Вийти у світ");
        goToWorldButton.setBackground(SIMS_LIGHT_PINK); // Custom Sims-like light pink background
        goToWorldButton.setForeground(Color.GRAY); // Gray text
        goToWorldButton.setFont(new Font("MONOSPACED", Font.BOLD, 16));
        goToWorldButton.setFocusPainted(false); // No focus border
        goToWorldButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding
        goToWorldButton.setBorder(new LineBorder(Color.WHITE, 2, true)); // White border with rounded corners
        goToWorldButton.addActionListener(e ->{
            MusicPlayer.getInstance().playButtonClick(); // Play sound effect
            MusicPlayer.getInstance().setMusicEnabled(false); // Disable current music
            parentFrame.dispose(); // Close the current game frame
            currentGameState=GameState.GAME_PAUSED; // Pause game state
            SwingUtilities.invokeLater(() -> {
                Window gameWindow = SwingUtilities.getWindowAncestor(this);
                if (gameWindow != null) {
                    gameWindow.dispose(); // Dispose of the current game window
                }
                LoadingFrame loading = new LoadingFrame(); // Show a loading screen
                loading.startLoading(() -> {
                    MainFrame mainFrame = new MainFrame(this.parentFrame); // Create the main game world frame
                    mainFrame.setVisible(true); // Make the main game world visible
                });
            });

        });
        return goToWorldButton;
    }

    /**
     * Returns the hint panel associated with this game panel.
     * @return The HintPanel instance.
     */
    public HintPanel getHintPanel() {
        return hintPanel;
    }

    /**
     * Sets the hint panel for this game panel.
     * @param hintPanel The HintPanel instance to set.
     */
    public void setHintPanel(HintPanel hintPanel) {
        this.hintPanel = hintPanel;
    }

    /**
     * Creates a JButton with a custom Sims-like visual style.
     * It includes a light blue background, dark text, and hover effects.
     * @param text The text to display on the button.
     * @return A styled JButton.
     */
    private JButton createSimsButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(SIMS_LIGHT_BLUE); // Custom light blue background
        button.setForeground(SIMS_DARK_TEXT); // Custom dark text color
        button.setFocusPainted(false); // No focus border
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor on hover
        // Add mouse listener for hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(SIMS_BUTTON_HOVER); // Change background on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SIMS_LIGHT_BLUE); // Revert background when mouse exits
            }
        });
        return button;
    }
}