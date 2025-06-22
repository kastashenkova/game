package gui;
import mainstage.GameBoard;
import org.example.GameFrame;
import org.example.MusicPlayer;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;

/**
 * Class for a pause panel with different options such as:
 * returning to player panel, adjusting settings and continuing game.
 */
public class DeDialogPanel extends JPanel {

    // background color
    private static final Color BG = new Color(159, 131, 244);

    private GameBoard gameBoard;

    /**
     * Constructor for the frames with a game board.
     * Creates a pause panel with resume, player panel and settings options.
     *
     * @param gameBoard the game board instance to control pause/resume functionality
     */
    public DeDialogPanel(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        JLabel pausedLabel = new JLabel("ЗУПИНЕНО");
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
        add(new JButton(new PlayerPanelAction("ПАНЕЛЬ ПЕРСОНАЖКИ")));
        add(new JButton(new SettingsAction("НАЛАШТУВАННЯ")));
    }

    /**
     * Constructor for the frames without game board.
     * Creates a simplified pause panel with only settings option.
     */
    public DeDialogPanel() {

        JLabel pausedLabel = new JLabel("ЗУПИНЕНО");
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
     * Private class for resume action that resumes a game when triggered.
     */
    private class ResumeAction extends AbstractAction {
        /**
         * Constructor for ResumeAction.
         *
         * @param name the display name for the action
         */
        public ResumeAction(String name) {
            super(name);
        }

        /**
         * Performs the action to resume the game.
         *
         * @param e the action event that triggered this action
         */
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
     * Private class for the action that takes user to player's panel.
     */
    private class PlayerPanelAction extends AbstractAction {
        /**
         * Constructor for PlayerPanelAction.
         *
         * @param name the display name for the action
         */
        public PlayerPanelAction(String name) {
            super(name);
        }

        /**
         * Performs the action to navigate to the player panel.
         *
         * @param e the action event that triggered this action
         */
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
     * Private class for showing all possible adjustments a user can do.
     */
    private class SettingsAction extends AbstractAction {
        /**
         * Constructor for SettingsAction.
         *
         * @param name the display name for the action
         */
        public SettingsAction(String name) {
            super(name);
        }

        /**
         * Performs the action to open settings dialog.
         *
         * @param e the action event that triggered this action
         */
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