package mainstage;

import gui.PauseAction;
import org.example.GameFrame;
import org.example.MusicPlayer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Represents the main frame for the game board.
 */
public class MainFrame extends JFrame {

    public GameFrame gameFrame;

    /**
     * Constructs a {@code MainFrame} and initializes its user interface.
     *
     * @param gameFrame The main game frame of the application.
     */
    public MainFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        setLocationRelativeTo(null);
        try {
            initUI();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize UI: " + e.getMessage(), e);
        }
    }

    /**
     * Initializes the main user interface, adds the game board,
     * and sets up buttons for pausing and resuming the game.
     *
     * @throws IOException If there is an error loading button images.
     */
    private void initUI() throws IOException {
        GameBoard gameBoard = new GameBoard(this);
        add(gameBoard);
        gameBoard.requestFocusInWindow();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Setup Pause Button
        PauseAction pauseAction = new PauseAction("", gameBoard);
        JButton pauseButton = new JButton(pauseAction);
        ImageIcon iconBtn = new ImageIcon(getClass().getResource("/button/pause.png"));
        Image scaledImage = iconBtn.getImage().getScaledInstance(140, 30, Image.SCALE_SMOOTH);
        iconBtn = new ImageIcon(scaledImage);
        pauseButton.setIcon(iconBtn);
        pauseButton.setContentAreaFilled(false);
        pauseButton.setBorderPainted(false);
        pauseButton.setFocusPainted(false);
        pauseButton.setOpaque(false);

        // Setup Resume Button
        JButton resumeButton = new JButton();
        ImageIcon iconBtn1 = new ImageIcon(getClass().getResource("/button/start.png"));
        Image scaledImage1 = iconBtn1.getImage().getScaledInstance(140, 30, Image.SCALE_SMOOTH);
        iconBtn1 = new ImageIcon(scaledImage1);
        resumeButton.setIcon(iconBtn1);
        resumeButton.setContentAreaFilled(false);
        resumeButton.setBorderPainted(false);
        resumeButton.setFocusPainted(false);
        resumeButton.setOpaque(false);
        resumeButton.addActionListener(e -> {
            MusicPlayer.getInstance().playButtonClick();
            if (gameBoard.isPaused()) {
                gameBoard.setPaused(false);
                gameBoard.requestFocus();
            }
        });

        // Setup Top Bar Panel
        JPanel topBar = new JPanel();
        topBar.setOpaque(false);
        topBar.add(resumeButton);
        topBar.add(pauseButton);

        add(topBar, BorderLayout.NORTH);
        gameBoard.startGameThread(); // Start the game loop
        pack(); // Pack the frame to its preferred size
        setLocationRelativeTo(null); // Center the frame on the screen

        // Set frame icon
        BufferedImage icon = ImageIO.read(getClass().getResourceAsStream("/logo.png"));
        setIconImage(icon);

        // Ensure game board has focus for key events
        resumeButton.setFocusable(false); // Make resume button non-focusable to keep focus on game board
        gameBoard.requestFocus();
    }
}