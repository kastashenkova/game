package gui;
import mainstage.GameBoard;
import studies.GameFrame;
import studies.MusicPlayer;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;


public class DeDialogPanel extends JPanel {
    private static final Color BG = new Color(159, 131, 244);

    private GameBoard gameBoard;


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
        add(new JButton(new MainMenuAction("ГОЛОВНЕ МЕНЮ")));
        add(new JButton(new SettingsAction("НАЛАШТУВАННЯ")));
    }


    private class ResumeAction extends AbstractAction {
        public ResumeAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MusicPlayer.getInstance().playButtonClick();
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
    private class MainMenuAction extends AbstractAction {
        public MainMenuAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MusicPlayer.getInstance().playButtonClick();
            Window win = SwingUtilities.getWindowAncestor((Component) e.getSource());
            win.dispose();
            gameBoard.gameThread.interrupt();
            gameBoard.setPaused(true);
            MusicPlayer.getInstance().setMusicEnabled(false);
            SwingUtilities.invokeLater(() -> {
                Window gameWindow = SwingUtilities.getWindowAncestor(gameBoard);
                if (gameWindow != null) {
                    gameWindow.dispose();
                }
                LoadingFrame loading = new LoadingFrame();
                loading.startLoading(() -> new WelcomeFrame().setVisible(true));
            });
        }

    }
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


