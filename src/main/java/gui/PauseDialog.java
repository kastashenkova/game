package gui;
import mainstage.GameBoard;
import org.example.GameFrame;
import org.example.MusicPlayer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;

public class PauseDialog {
    // path to example image used as "game" background
    private static final String IMG_PATH = "https://upload.wikimedia.org/"
            + "wikipedia/commons/7/76/Jump_%27n_Bump.png";



    public PauseDialog(){
        createAndShowGui();
    }

    private static void createAndShowGui() {
        // get the "game" background image, or exit if fail
        BufferedImage img = null;
        try {
            URL imgUrl = new URL(IMG_PATH);
            img = ImageIO.read(imgUrl);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }


        DeMainPanel mainPanel = new DeMainPanel(img);
        JFrame frame = new JFrame("Dialog Example");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(mainPanel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
}
// main JPanel
@SuppressWarnings("serial")
class DeMainPanel extends JPanel {
    private BufferedImage img; // background image



    public DeMainPanel(BufferedImage img) {
        super();
        this.img = img;

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (img != null) {
            g.drawImage(img, 0, 0, this);
        }
    }

    // size this JPanel to match the image's size
    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet() || img == null) {
            return super.getPreferredSize();
        }
        int width = img.getWidth();
        int height = img.getHeight();
        return new Dimension(width, height);
    }
}

class DeDialogPanel extends JPanel {
    private static final Color BG = new Color(159, 131, 244);

    private GameBoard gameBoard;
    private MusicPlayer musicPlayer = new MusicPlayer();

    public DeDialogPanel(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        JLabel pausedLabel = new JLabel("PAUSED");
        pausedLabel.setForeground(Color.BLACK);
        JPanel pausedPanel = new JPanel();
        pausedPanel.setOpaque(false);
        pausedPanel.add(pausedLabel);
        pausedPanel.setFont(new Font("Arial", Font.BOLD, 16));
        setBackground(BG);
        int eb = 15;
        setBorder(BorderFactory.createEmptyBorder(eb, eb, eb, eb));
        setLayout(new GridLayout(0, 1, 10, 10));
        add(pausedPanel);
        add(new JButton(new MainMenuAction("ПРОДОВЖИТИ")));
        add(new JButton(new PlayerPanelAction("ПАНЕЛЬ ПЕРСОНАЖА")));
        add(new JButton(new MainMenuAction("ГОЛОВНЕ МЕНЮ")));
        add(new JButton(new SettingsAction("НАЛАШТУВАННЯ")));
    }


    private class FooAction extends AbstractAction {
        public FooAction(String name) {
            super(name);
        }


        @Override
        public void actionPerformed(ActionEvent e) {

            musicPlayer.playButtonClick();
            Component comp = (Component) e.getSource();
            Window win = SwingUtilities.getWindowAncestor(comp);
            win.dispose();
        }
    }

    private class ResumeAction extends AbstractAction {
        public ResumeAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            musicPlayer.playButtonClick();
            gameBoard.setPaused(false);
            Window win = SwingUtilities.getWindowAncestor((Component) e.getSource());
            win.dispose();
        }
    }

    private class PlayerPanelAction extends AbstractAction {
        public PlayerPanelAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            musicPlayer.playButtonClick();
            Window win = SwingUtilities.getWindowAncestor((Component) e.getSource());
            win.dispose();
            gameBoard.gameThread.interrupt();
            gameBoard.musicPlayer.stopMusic();
            SwingUtilities.invokeLater(() -> {

                Window gameWindow = SwingUtilities.getWindowAncestor(gameBoard);
                if (gameWindow != null) {
                    gameWindow.dispose();
                }
                LoadingFrame loading = new LoadingFrame();
                loading.startLoading(() -> new GameFrame(gameBoard).setVisible(true));
            });
        }
    }
    private class MainMenuAction extends AbstractAction {
        public MainMenuAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            musicPlayer.playButtonClick();
            gameBoard.setPaused(false);
            Window win = SwingUtilities.getWindowAncestor((Component) e.getSource());
            win.dispose();
        }
    }
    private class SettingsAction extends AbstractAction {
        public SettingsAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            musicPlayer.playButtonClick();
            gameBoard.setPaused(false);
            Window win = SwingUtilities.getWindowAncestor((Component) e.getSource());
            win.dispose();
        }
    }
}


