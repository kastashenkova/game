package gui;
import mainstage.GameBoard;
import org.example.GameFrame;
import org.example.MusicPlayer;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;

/**
 * class for a pause panel with different options such as:
 * returning to player panel, adjust settings and continue game
 */
public class DeDialogPanel extends JPanel {

    // background color
    private static final Color BG = new Color(159, 131, 244);

    private GameBoard gameBoard;

    /**
     * constructor for the frames with a game board
     */
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
        add(new JButton(new ResumeAction("ПРОДОВЖИТИ")));
        add(new JButton(new PlayerPanelAction("ПАНЕЛЬ ПЕРСОНАЖА")));
        add(new JButton(new SettingsAction("НАЛАШТУВАННЯ")));
    }

    /**
     * constructor for the frames without game board
     */
    public DeDialogPanel() {

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

        add(new JButton(new SettingsAction("НАЛАШТУВАННЯ")));
    }

    /**
     * private class for resume action that resumes a game action when triggered
     */
    private class ResumeAction extends AbstractAction {
        public ResumeAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MusicPlayer.getInstance().playButtonClick();
            gameBoard.setPaused(false);
            gameBoard.requestFocus();
            Window win = SwingUtilities.getWindowAncestor((Component) e.getSource());
            win.dispose();
        }
    }
    /**
     * private class for the action that takes user to player's panel
     */
    private class PlayerPanelAction extends AbstractAction {
        public PlayerPanelAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MusicPlayer.getInstance().playButtonClick();
            Window win = SwingUtilities.getWindowAncestor((Component) e.getSource());
            win.dispose();

                gameBoard.gameThread.interrupt();

                MusicPlayer.getInstance().setMusicEnabled(false);
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
    /**
     * private class for showing all possible adjustments a user can do
     */
    private class SettingsAction extends AbstractAction {
        public SettingsAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MusicPlayer.getInstance().playButtonClick();
            Window win = SwingUtilities.getWindowAncestor((Component) e.getSource());
            win.dispose();
            OptionsFrame optionsFrame = new OptionsFrame();
            optionsFrame.setVisible(true);
    }
}
}


