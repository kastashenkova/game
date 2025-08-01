package gui;

import org.example.MusicPlayer;
import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

/**
 * Simulates a process of loading to make the game more realistic.
 * Displays animated loading screen with progress bar and rotating messages.
 */
public class LoadingFrame extends JFrame {

    private JProgressBar progressBar;
    private final JLabel textLabel = new JLabel("", SwingConstants.CENTER);
    private ArrayList<String> messages = new ArrayList<>();
    private final Timer messageTimer;
    private final Random random = new Random();

    /**
     * Default constructor that initializes the loading frame with all UI components.
     * Sets up the window, progress bar, animated GIF, and message rotation system.
     */
    public LoadingFrame() {
        setTitle("Завантаження...");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        try {
            URL url = getClass().getResource("/load1.gif");
            ImageIcon icon = new ImageIcon(url);
            JLabel gifLabel = new JLabel(icon, SwingConstants.CENTER);
            contentPanel.add(gifLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel fallback = new JLabel("Завантаження...", SwingConstants.CENTER);
            contentPanel.add(fallback, BorderLayout.CENTER);
        }
        createRandomStrings();
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setForeground(Color.BLUE);
        progressBar.setBackground(Color.LIGHT_GRAY);
        progressBar.setFont(new Font("Arial", Font.BOLD, 14));
        bottomPanel.add(textLabel, BorderLayout.NORTH);
        bottomPanel.add(progressBar, BorderLayout.SOUTH);

        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(contentPanel);
        updateMessage();
        messageTimer = new Timer(3000, e -> updateMessage());
        requestFocusInWindow();
    }

    /**
     * Starts the loading process, plays background music and shows progress from 0 to 100%.
     * Executes the provided task after loading completion.
     *
     * @param onFinish the Runnable task to be executed after loading completes
     */
    public void startLoading(Runnable onFinish) {
        setVisible(true);
        MusicPlayer.getInstance().setMusicEnabled(true);
        MusicPlayer.getInstance().playMusic("/assets/Sounds/theme2.wav");
        new Thread(() -> {
            for (int i = 0; i <= 100; i++) {
                final int progress = i;
                SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            SwingUtilities.invokeLater(() -> {
                messageTimer.stop();
                dispose();
                MusicPlayer.getInstance().setMusicEnabled(false);
                onFinish.run();
            });
        }).start();
    }

    /**
     * Creates humorous loading messages related to university life.
     * Populates the messages list with funny references to university experiences.
     */
    private void createRandomStrings() {
        messages.add("Завантаження Могилянки... дещо затягнулося, як і сесія 😅");
        messages.add("Завантаження може бути тривалим.. як і пошук вільного місця в бібліо...");
        messages.add("Завантаження корпусів... 1, 2, 3, КМЦ...");
        messages.add("Завантаження завершиться так само скоро, як і ремонт 10 корпусу...");
        messages.add("За цю невеличку паузу можна встигнути випити кави...");
    }

    /**
     * Updates the displayed message by randomly selecting one from the messages list.
     * Called periodically by the message timer to rotate loading messages.
     */
    private void updateMessage() {
        int index = random.nextInt(messages.size());
        textLabel.setText(messages.get(index));
    }
}