package mainstage;

import gui.BackgroundPanel;
import org.example.GameFrame;
import org.example.Hero;
import org.example.MusicPlayer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class WheelStage extends JFrame {

    Hero hero;

    public WheelStage(GameBoard gameBoard) throws IOException {

        MusicPlayer.getInstance().setMusicEnabled(true);
        MusicPlayer.getInstance().playMusic("/assets/Sounds/fun.wav");

        setTitle("Ferris Wheel!");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        setSize(600, 600);
        setLocationRelativeTo(null);

        BufferedImage icon = ImageIO.read(getClass().getResourceAsStream("/logo.png"));
        setIconImage(icon);

        BackgroundPanel backgroundPanel = new BackgroundPanel(getClass().getResource("/backWheel.jpg").getFile());
        setContentPane(backgroundPanel);

        try {
            URL url = getClass().getResource("/wheel.gif");
            ImageIcon icon1 = new ImageIcon(url);
            JLabel ferrisLabel = new JLabel(icon1);
            add(ferrisLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        hero = gameBoard.hero;
        hero.increaseEnergy(30);

    }
}
