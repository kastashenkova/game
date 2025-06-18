package gui;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.MusicPlayer;
import org.example.StartWindow;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.net.URL;

public class GoodbyeWindow extends JFrame implements ActionListener {
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem newGameItem, exitItem;
    JButton startButton; JButton optionsButton;  JButton quitButton;
    BufferedImage icon;

    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);

    public GoodbyeWindow() {
        UIManager.put("OptionPane.yesButtonText", "Так");
        UIManager.put("OptionPane.noButtonText", "Ні");
        UIManager.put("OptionPane.cancelButtonText", "Скасувати");

        setTitle("End of Game!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        MusicPlayer.getInstance().setMusicEnabled(true);
        MusicPlayer.getInstance().playMusic("/assets/Sounds/theme1.wav");

       setBackground(SIMS_LIGHT_BLUE);

        startButton = createButton("start");
        startButton.addActionListener(this);

        String instructions = "<html>" +
                "<body style='font-family: \"Arial\"; font-size: 13px; color: #00000;'>" +
                "<h1 style='color: #00000;'>Дякуємо за вашу увагу до гри Сімс НаУКМА!</h1>" +
                "<p>Сподіваємось, вона хоч трохи дала вам відчути справжню атмосферу навчання в Могилянці</p>" +
                "<p>До нових зустрічей та оновлень!</p>" +
                "<ol>" +
                "<li>Для того, щоб почати нову гру - натисніть кнопку <b> PLAY</b> </li>" +
                "<li>У разі виникнення питань та пропозицій - звертайтеся до розробників(так, вони на фото).</li>" +
                "</ol>" +
                "<p><b>Бажаємо успіхів!</b></p>" +
                "</body></html>";

        JEditorPane editorPane = new JEditorPane("text/html", instructions);
        editorPane.setEditable(false);
        editorPane.setBackground(SIMS_LIGHT_PINK);
        editorPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(200, 220));


        add(startButton, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.NORTH);

        try {
            icon = loadImage("/logo.png");
        } catch (IOException e){
        }
        setIconImage(icon);

        ImageIcon icon1 = new ImageIcon(getClass().getResource("/mimi.png"));
        JLabel label = new JLabel(icon1);
        label.setBounds(800, 0, 200, 160);
        add(label);

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
                loading.startLoading(() -> new WelcomeFrame().setVisible(true));
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


}
