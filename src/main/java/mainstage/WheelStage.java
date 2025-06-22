package mainstage;

import org.example.Hero;
import org.example.MusicPlayer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * A frame to simulate a ferris wheel experience.
 */
public class WheelStage extends JFrame {

    Hero hero;

    /**
     * Constructs a new {@code WheelStage}.
     *
     * @param gameBoard The game board instance, used to access the hero.
     * @throws IOException If there is an error loading resources like the frame icon or GIF.
     */
    public WheelStage(GameBoard gameBoard) throws IOException {

        MusicPlayer.getInstance().setMusicEnabled(true);
        MusicPlayer.getInstance().playMusic("/assets/Sounds/fun.wav");

        setTitle("Колесо огляду");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        setSize(600, 600);
        setLocationRelativeTo(null);
        setBackground(new Color(118, 244, 255));

        BufferedImage icon = ImageIO.read(getClass().getResourceAsStream("/logo.png"));
        setIconImage(icon);

        URL url = WheelStage.class.getResource("/wheel.gif");
        if (url != null) {
            ImageIcon gifIcon = new ImageIcon(url);
            JLabel gifLabel = new JLabel(gifIcon);
            add(gifLabel);
        } else {
            System.out.println("Гіфка не знайдена");
        }

        setVisible(true);

        hero = gameBoard.hero;
        hero.increaseEnergy(30);
    }
}