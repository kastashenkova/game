package gui;

import org.example.MusicPlayer;
import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

/**
 * simulates a process of loading to make a game more realistic
 */

public class LoadingFrame extends JFrame {

    private JProgressBar progressBar;
    private final JLabel textLabel = new JLabel("", SwingConstants.CENTER);
    private ArrayList<String> messages = new ArrayList<>();
    private final Timer messageTimer;
    private final Random random = new Random();

    /**
     * basic constructor
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
     * starts a loading process, plays a sound and shows the progress(1-100%)
     * @param onFinish - defines a unit of work to be executed by a thread
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
     * creates string-messages for fun
     */
    private void createRandomStrings() {
        messages.add("Завантаження Могилянки... дещо затягнулось, як і сесія 😅");
        messages.add("Завантаження може бути довгим..як пошук вільного місця в бібліо...");
        messages.add("Завантаження корпусів... 1, 2,3, КМЦ...");
        messages.add("Завантаження завершиться так само скоро,як ремонт 10 корпуса...");
        messages.add("За цю невеличку паузу можна встигнути випити каву...");
    }

    /**
     * updates a string-message
     */
    private void updateMessage() {
        int index = random.nextInt(messages.size());
        textLabel.setText(messages.get(index));
    }

}
