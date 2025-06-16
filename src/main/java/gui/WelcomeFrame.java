package gui;

import com.formdev.flatlaf.FlatLightLaf;
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
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.net.URL;

public class WelcomeFrame extends JFrame implements ActionListener {
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem newGameItem, exitItem;
    JButton startButton; JButton optionsButton;  JButton quitButton;
    BufferedImage icon;

    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);

    public WelcomeFrame() {
        UIManager.put("OptionPane.yesButtonText", "Так");
        UIManager.put("OptionPane.noButtonText", "Ні");
        UIManager.put("OptionPane.cancelButtonText", "Скасувати");

        setTitle("Sims NaUKMA");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        MusicPlayer.getInstance().setMusicEnabled(true);
        MusicPlayer.getInstance().playMusic("/assets/Sounds/welcome.wav");

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
            gifLabel.setBounds(0, 500, 200, 184);
            backgroundPanel.add(gifLabel);
        } catch (Exception e) {
        }
        try {
            URL url = getClass().getResource("/study.gif");
            String html = "<html><body><img src='" + url + "' width='150' height='120'></body></html>";
            JLabel gifLabel = new JLabel(html, SwingConstants.CENTER);
            gifLabel.setBounds(800, 0, 200, 184);
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
                MusicPlayer.getInstance().setMusicEnabled(false);
                dispose();
                LoadingFrame loading = new LoadingFrame();
                loading.startLoading(() -> new StartWindow().setVisible(true));
            });
        } else if (e.getSource() == quitButton) {
            MusicPlayer.getInstance().playButtonClick();
            System.exit(0);
        } else if(e.getSource() == optionsButton) {
            SwingUtilities.invokeLater(() -> {
                MusicPlayer.getInstance().playButtonClick();
                new OptionsFrame().setVisible(true);
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

        ImageIcon hoverIcon = darkenIcon(icon, 0.85f);
        ImageIcon pressedIcon = darkenIcon(icon, 0.65f);

        button.setIcon(icon);
        button.setRolloverIcon(hoverIcon);
        button.setPressedIcon(pressedIcon);

        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);

        return button;
    }
    private ImageIcon darkenIcon(ImageIcon originalIcon, float darknessFactor) {
        BufferedImage original = new BufferedImage(
                originalIcon.getIconWidth(),
                originalIcon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = original.createGraphics();
        originalIcon.paintIcon(null, g, 0, 0);
        g.dispose();


        RescaleOp rescaleOp = new RescaleOp(
                new float[]{darknessFactor, darknessFactor, darknessFactor, 1f},
                new float[4], null);
        BufferedImage darkened = rescaleOp.filter(original, null);

        return new ImageIcon(darkened);
    }



    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.put("nimbusBase", SIMS_MEDIUM_PINK);
        UIManager.put("nimbusBlueGrey", SIMS_LIGHT_BLUE);
        UIManager.put("control", SIMS_LIGHT_PINK);
        UIManager.put("textForeground", SIMS_DARK_TEXT);

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
