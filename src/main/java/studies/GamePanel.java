package studies;

import gui.LoadingFrame;
import mainstage.MainFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;
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

    private JButton goToWorldButton;

    private long lastMessageTime;
    private final long MESSAGE_INTERVAL_MIN = 500;
    private final long MESSAGE_INTERVAL_MAX = 2000;
    private long nextMessageInterval;
    private Random random = new Random();

    // --- Для спливаючих повідомлень чату ---
    private List<FloatingMessage> floatingMessages;
    private final int FLOATING_MESSAGE_DISPLAY_DURATION = 3300;
    private final int MESSAGE_OFFSET_Y = 5; // Відступ між повідомленнями
    private final int MESSAGE_MARGIN_X = 20; // Відступ від правого краю
    private final int MESSAGE_BOTTOM_START_Y = 50; // Відступ від нижнього краю для першого повідомлення

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

    // Внутрішній клас для представлення спливаючого повідомлення
    private static class FloatingMessage {
        String sender;
        String message;
        long startTime;
        int currentY;
        int height;
        int width;

        FloatingMessage(String sender, String message, long startTime, int initialY, int width, int height) {
            this.sender = sender;
            this.message = message;
            this.startTime = startTime;
            this.currentY = initialY;
            this.width = width;
            this.height = height;
        }

        public void updateY(int newY) {
            this.currentY = newY;
        }
    }


    public GamePanel(Hero hero, GameFrame parentFrame) {
        this.hero = hero;
        this.parentFrame = parentFrame;
        setPreferredSize(new Dimension(1200, 800));
        setBackground(Color.LIGHT_GRAY);
        currentGameState = GameState.PLAYING;
        setLayout(null);

        // Initialize MusicPlayer (assuming default sound path or specific path if needed)
        MusicPlayer.getInstance().setMusicEnabled(true);
        MusicPlayer.getInstance().playMusic("/assets/Sounds/Background.wav");

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

        JButton eatButton = new JButton("Їсти");
        eatButton.setFont(new Font("Arial", Font.PLAIN, 10));
        eatButton.addActionListener(e -> {
            if (currentGameState == GameState.PLAYING) {
                hero.eat();
            }
        });
        heroActionsPanel.add(eatButton);

        JButton sleepButton = new JButton("Спати");
        sleepButton.setFont(new Font("Arial", Font.PLAIN, 10));
        sleepButton.addActionListener(e -> {
            if (currentGameState == GameState.PLAYING) {
                hero.sleep();
            }
        });
        heroActionsPanel.add(sleepButton);

        JButton studyButton = new JButton("Навчатися");
        studyButton.setFont(new Font("Arial", Font.PLAIN, 10));
        studyButton.addActionListener(e -> {
            if (currentGameState == GameState.PLAYING) {
                hero.study();
            }
        });
        heroActionsPanel.add(studyButton);

        JButton relaxButton = new JButton("Відпочивати");
        relaxButton.setFont(new Font("Arial", Font.PLAIN, 10));
        relaxButton.addActionListener(e -> {
            if (currentGameState == GameState.PLAYING) {
                hero.relax();
            }
        });
        heroActionsPanel.add(relaxButton);

        add(heroActionsPanel);

        gameTimer = new Timer(1000 / 60, this);
        gameTimer.start();

        lastMessageTime = System.currentTimeMillis();
        nextMessageInterval = generateRandomMessageInterval();

        // --- Ініціалізація та додавання поля вводу та кнопки надсилання ---
        userMessageInputField = new JTextField();
        userMessageInputField.setFont(new Font("Arial", Font.PLAIN, 12));
        userMessageInputField.setToolTipText("Введіть ваше повідомлення...");
        add(userMessageInputField);

        sendUserMessageButton = new JButton("Надіслати");
        sendUserMessageButton.setFont(new Font("Arial", Font.BOLD, 12));
        add(sendUserMessageButton);

        // Додаємо слухача подій для кнопки та поля вводу
        ActionListener sendMessageAction = e -> {
            String message = userMessageInputField.getText().trim();
            if (!message.isEmpty()) {
                MusicPlayer.getInstance().setMusicEnabled(true);
                MusicPlayer.getInstance().playEffect("/assets/Sounds/message_send.wav");
                userMessageInputField.setText(""); // Очищаємо поле
            }
        };
        sendUserMessageButton.addActionListener(sendMessageAction);
        userMessageInputField.addActionListener(sendMessageAction); // Відправлення по Enter

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentGameState != GameState.PLAYING) return;

                // Якщо користувач натиснув на поле вводу, не перетягуємо сцену/героя
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

                // Якщо користувач подвійним кліком на поле вводу, не перемикаємо виділення героя
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

                // Якщо перетягування відбувається на елементах введення, ігноруємо
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
                positionUserChatElements(); // Оновлюємо позицію поля вводу при масштабуванні
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
       goToWorldButton.setBounds(10, 600, 100, 70);
        add(goToWorldButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        hero.draw(g, sceneOffsetX, sceneOffsetY, sceneScale);

        long currentTime = System.currentTimeMillis();
        Font messageFont = new Font("Segoe UI", Font.BOLD, 10);
        g.setFont(messageFont);
        FontMetrics fm = g.getFontMetrics(messageFont);

        // Позиціонування повідомлень знизу вгору, з урахуванням поля вводу
        int currentY = getHeight() - MESSAGE_BOTTOM_START_Y - (userMessageInputField.isVisible() ? userMessageInputField.getHeight() + 10 : 0);
        List<FloatingMessage> messagesToRemove = new ArrayList<>();

        for (int i = floatingMessages.size() - 1; i >= 0; i--) {
            FloatingMessage msg = floatingMessages.get(i);
            if (currentTime - msg.startTime < FLOATING_MESSAGE_DISPLAY_DURATION) {
                String text = "<html><b>" + msg.sender + ":</b> " + msg.message + "</html>";
                JLabel tempLabel = new JLabel(text);
                tempLabel.setFont(messageFont);
                tempLabel.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
                Dimension prefSize = tempLabel.getPreferredSize();

                int messageWidth = prefSize.width + 30;
                int messageHeight = prefSize.height + 10;

                int messageX = getWidth() - messageWidth - MESSAGE_MARGIN_X;
                msg.currentY = currentY;

                g.setColor(new Color(255, 255, 220, 200));
                g.fillRoundRect(messageX, msg.currentY, messageWidth, messageHeight, 10, 10);
                g.setColor(Color.GRAY);
                g.drawRoundRect(messageX, msg.currentY, messageWidth, messageHeight, 10, 10);

                String displayText = msg.sender + ": " + msg.message;
                g.setColor(Color.BLUE.darker());
                g.drawString(displayText, messageX + 10, msg.currentY + fm.getAscent() + 5);

                currentY -= (messageHeight + MESSAGE_OFFSET_Y);

            } else {
                messagesToRemove.add(msg);
            }
        }
        floatingMessages.removeAll(messagesToRemove);

        if (currentGameState == GameState.GAME_OVER) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String gameOverText = "ГРА ЗАВЕРШЕНА!";
            fm = g.getFontMetrics(g.getFont());
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

            // Приховуємо поле вводу та кнопку, коли гра завершена
            userMessageInputField.setVisible(false);
            sendUserMessageButton.setVisible(false);
        } else {
            // Показуємо поле вводу та кнопку, коли гра активна
            userMessageInputField.setVisible(true);
            sendUserMessageButton.setVisible(true);
            positionUserChatElements(); // Забезпечуємо правильне позиціонування
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentGameState == GameState.PLAYING) {
            hero.update();

            if (hero.isGameOverDueToEnergy()) {
                currentGameState = GameState.GAME_OVER;
                heroActionsPanel.setVisible(false);
                gameTimer.stop();
                showFloatingMessage("Система", hero.getGameOverReason());
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMessageTime >= nextMessageInterval) {
                generateRandomChatMessages();

                MusicPlayer.getInstance().setMusicEnabled(true);
                MusicPlayer.getInstance().playEffect("/assets/Sounds/message_received.wav");
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

    private void positionUserChatElements() {
        int inputFieldWidth = 150;
        int inputFieldHeight = 25;
        int buttonWidth = 100;
        int buttonHeight = 25;
        int padding = 10;

        // Загальна ширина елементів чату
        int totalChatInputWidth = inputFieldWidth + buttonWidth + padding;
        // X-координата для вирівнювання по правому краю
        int inputX = getWidth() - totalChatInputWidth - MESSAGE_MARGIN_X;
        // Y-координата залишається знизу
        int inputY = getHeight() - inputFieldHeight - padding;

        userMessageInputField.setBounds(inputX, inputY, inputFieldWidth, inputFieldHeight);

        // Розміщуємо кнопку надсилання праворуч від поля вводу
        int buttonX = inputX + inputFieldWidth + padding;
        int buttonY = inputY;

        sendUserMessageButton.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
    }


    private long generateRandomMessageInterval() {
        return (long) (random.nextDouble() * (MESSAGE_INTERVAL_MAX - MESSAGE_INTERVAL_MIN + 1)) + MESSAGE_INTERVAL_MIN;
    }

    private void generateRandomChatMessages() {
        String randomPlayer = studentNames[random.nextInt(studentNames.length)];
        String randomMsg = kmaMessages[random.nextInt(kmaMessages.length)];
        showFloatingMessage(randomPlayer, randomMsg);
    }

    public void showFloatingMessage(String sender, String message) {
        Font messageFont = new Font("Segoe UI", Font.BOLD, 10);
        JLabel tempLabel = new JLabel("<html><b>" + sender + ":</b> " + message + "</html>");
        tempLabel.setFont(messageFont);
        tempLabel.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        Dimension prefSize = tempLabel.getPreferredSize();

        int messageWidth = prefSize.width + 20;
        int messageHeight = prefSize.height + 10;

        // Обчислюємо початкову позицію для нового повідомлення (нижній правий кут),
        // враховуючи наявність поля вводу повідомлень
        int startY = getHeight() - MESSAGE_BOTTOM_START_Y;

        // Зміщуємо всі наявні повідомлення вгору
        for (FloatingMessage msg : floatingMessages) {
            msg.updateY(msg.currentY - (messageHeight + MESSAGE_OFFSET_Y));
        }

        FloatingMessage newMessage = new FloatingMessage(sender, message, System.currentTimeMillis(), startY, messageWidth, messageHeight);
        floatingMessages.add(newMessage);

        repaint();
    }


    public void fireGameOverEvent(String reason) {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
        parentFrame.handleGameOver(reason);
    }

    public void setHero(Hero newHero) {
        this.hero = newHero;
        currentGameState = GameState.PLAYING;
        isHeroDragging = false;
        isSceneDragging = false;
        sceneOffsetX = 0;
        sceneOffsetY = 0;
        sceneScale = 1.0;
        hideHeroActionsPanel();
        gameTimer.start();
        lastMessageTime = System.currentTimeMillis();
        nextMessageInterval = generateRandomMessageInterval();
        floatingMessages.clear();
        repaint();
    }

    private JButton getGoToWorldButton(){
        goToWorldButton = new JButton("Вийти у світ");
        goToWorldButton.setBackground(new Color(78, 90, 205));
        goToWorldButton.setForeground(Color.WHITE);
        goToWorldButton.setFont(new Font("Arial", Font.BOLD, 16));
        goToWorldButton.setFocusPainted(false);
        goToWorldButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        goToWorldButton.setBorder(new LineBorder(Color.WHITE, 2, true)); // true = округлі кути
        goToWorldButton.addActionListener(e ->{
                  MusicPlayer.getInstance().playButtonClick();
                  MusicPlayer.getInstance().setMusicEnabled(false);
                     parentFrame.dispose();
                    currentGameState=GameState.GAME_PAUSED;
                    SwingUtilities.invokeLater(() -> {
                        Window gameWindow = SwingUtilities.getWindowAncestor(this);
                        if (gameWindow != null) {
                            gameWindow.dispose();
                        }
                        LoadingFrame loading = new LoadingFrame();
                        loading.startLoading(() -> {
                            MainFrame mainFrame = new MainFrame(this.parentFrame);
                            mainFrame.setVisible(true);
                        });
                    });

        });
        return goToWorldButton;
    }
}