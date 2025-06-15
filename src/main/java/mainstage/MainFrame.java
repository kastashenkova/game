package mainstage;

import gui.PauseAction;
import org.example.GameFrame;
import org.example.MusicPlayer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MainFrame extends JFrame {

    GameFrame gameFrame;
public MainFrame(GameFrame gameFrame) {
    this.gameFrame = gameFrame;
    setLocationRelativeTo(null);
        try {
            initUI();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
}

private void initUI() throws IOException {
    GameBoard gameBoard = new GameBoard(this);
    add(gameBoard);
    gameBoard.requestFocusInWindow();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    PauseAction pauseAction = new PauseAction("", gameBoard);
    JButton pauseButton = new JButton(pauseAction);
    ImageIcon iconBtn = new ImageIcon(getClass().getResource( "/button/pause.png"));
    Image scaledImage = iconBtn.getImage().getScaledInstance(140, 30, Image.SCALE_SMOOTH);
    iconBtn = new ImageIcon(scaledImage);
    pauseButton.setIcon(iconBtn);
    pauseButton.setContentAreaFilled(false);
    pauseButton.setBorderPainted(false);
    pauseButton.setFocusPainted(false);
    pauseButton.setOpaque(false);

    JButton resumeButton = new JButton();
    ImageIcon iconBtn1 = new ImageIcon(getClass().getResource( "/button/start.png"));
    Image scaledImage1 = iconBtn1.getImage().getScaledInstance(140, 30, Image.SCALE_SMOOTH);
    iconBtn1 = new ImageIcon(scaledImage1);
    resumeButton.setIcon(iconBtn1);
    resumeButton.setContentAreaFilled(false);
    resumeButton.setBorderPainted(false);
    resumeButton.setFocusPainted(false);
    resumeButton.setOpaque(false);
    resumeButton.addActionListener(e->{
        MusicPlayer.getInstance().playButtonClick();
        if(gameBoard.isPaused()){
            gameBoard.setPaused(false);
            gameBoard.requestFocus();
        }
    });


    JPanel topBar = new JPanel();
    topBar.setOpaque(false);

    topBar.add(resumeButton);
    topBar.add(pauseButton);

    add(topBar, BorderLayout.NORTH);
    gameBoard.startGameThread();
    pack();
    setLocationRelativeTo(null);
    BufferedImage icon = ImageIO.read(getClass().getResourceAsStream("/logo.png"));
    setIconImage(icon);
    resumeButton.setFocusable(false);
    gameBoard.requestFocus();


}


}


