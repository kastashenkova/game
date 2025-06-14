package gui;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.GameFrame;
import org.example.MusicPlayer;
import org.example.StartWindow;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
        import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class WelcomeFrame extends JFrame implements ActionListener {
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem newGameItem, exitItem;
    JButton startButton; JButton optionsButton;  JButton quitButton;
    BufferedImage icon;
    MusicPlayer player;
    Border BorderFactory = new LineBorder(Color.BLACK);

    public WelcomeFrame() {
        setTitle("Sims NaUKMA");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        player = new MusicPlayer();
        player.playMusic("src/main/resources//assets/Sounds/welcome.wav");

        BackgroundPanel backgroundPanel = new BackgroundPanel(getClass().getResource("/backMain.png").getFile());
        setContentPane(backgroundPanel);

        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        newGameItem = new JMenuItem("New Game");
        exitItem = new JMenuItem("Exit");

        fileMenu.add(newGameItem);
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);


        newGameItem.addActionListener(this);
        exitItem.addActionListener(this);

        try {
            URL url = getClass().getResource("/sims.gif");
            ImageIcon icon = new ImageIcon(url);
            JLabel gifLabel = new JLabel(icon, SwingConstants.CENTER);
            gifLabel.setBounds(0, 400, 200, 184);
            backgroundPanel.add(gifLabel);
        } catch (Exception e) {
        }
        try {
            URL url = getClass().getResource("/study.gif");
            String html = "<html><body><img src='" + url + "' width='150' height='120'></body></html>";
            JLabel gifLabel = new JLabel(html, SwingConstants.CENTER);
            gifLabel.setBounds(600, 0, 200, 184);
            backgroundPanel.add(gifLabel);
        } catch (Exception e) {
        }


        JPanel mainPanel = new JPanel(new GridBagLayout());
         startButton = createButton("start");
         optionsButton = createButton("settings");
         quitButton = createButton("exit");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(startButton, gbc);

        gbc.gridy = 1;
        mainPanel.add(optionsButton, gbc);

        gbc.gridy = 2;
        mainPanel.add(quitButton, gbc);
        mainPanel.setOpaque(false);

        add(mainPanel, BorderLayout.CENTER);

        startButton.addActionListener(this);
        optionsButton.addActionListener(this);
        quitButton.addActionListener(this);

        try {
            icon = loadImage("/logo.png");
        } catch (IOException e){

        }
        setIconImage(icon);
        setVisible(true);
    }
    private BufferedImage loadImage(String path) throws IOException {
        return ImageIO.read(getClass().getResourceAsStream(path));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            SwingUtilities.invokeLater(() -> {
                player.stopMusic();
                dispose();
                LoadingFrame loading = new LoadingFrame();
                loading.startLoading(() -> new StartWindow().setVisible(true));
            });
        }
    }
    /**
     * returns a formated button
     * @param text text displayed on a button
     * @return button
     */
    private JButton createButton(String text) {

        String filePath =  "/button/" + text + ".png";

        JButton button = new JButton();
        ImageIcon icon = new ImageIcon(getClass().getResource(filePath));

       Image scaledImage = icon.getImage().getScaledInstance(190, 50, Image.SCALE_SMOOTH);
        icon = new ImageIcon(scaledImage);

        button.setIcon(icon);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);

        return button;
    }


    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new FlatLightLaf());
        SwingUtilities.invokeLater(WelcomeFrame::new);
    }
}
/**
 * class for a background picture
 */
class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    /**
     * constructor that gets the image from file by file path
     * @param filePath is used for getting an image
     */
    public BackgroundPanel(String filePath) {
        backgroundImage = new ImageIcon(filePath).getImage();
        setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
    }
}
