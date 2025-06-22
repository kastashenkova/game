package gui;

import mainstage.GameBoard;
import org.example.MusicPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Handles the action of pausing the game.
 */
public class PauseAction extends AbstractAction {
    private static final int ALPHA = 175; // Transparency level for the dimmed background (0 to 255)
    private static final Color GP_BG = new Color(0, 0, 0, ALPHA); // Color for the dimmed background
    private GameBoard gameBoard;
    private DeDialogPanel deDialogPanel;

    /**
     * Constructs a {@code PauseAction} with the specified name and game board.
     *
     * @param name The name of the action.
     * @param gameBoard The game board instance to be paused.
     */
    public PauseAction(String name, GameBoard gameBoard) {
        super(name);
        this.gameBoard = gameBoard;
        this.deDialogPanel = new DeDialogPanel(gameBoard);
    }

    /**
     * Constructs a {@code PauseAction} with the specified name.
     *
     * @param name The name of the action.
     */
    public PauseAction(String name) {
        super(name);
        this.deDialogPanel = new DeDialogPanel();
    }

    /**
     * Sets the game on pause and displays a darkened, paused screen effect with a dialog.
     *
     * @param e The event to be processed.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        MusicPlayer.getInstance().playButtonClick();
        if (gameBoard != null) {
            gameBoard.setPaused(true);
        }

        Component comp = (Component) e.getSource();
        if (comp == null) {
            return;
        }

        JPanel glassPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };

        glassPane.setOpaque(false);
        glassPane.setBackground(GP_BG);

        RootPaneContainer win = (RootPaneContainer) SwingUtilities.getWindowAncestor(comp);
        win.setGlassPane(glassPane);
        glassPane.setVisible(true);

        JDialog dialog = new JDialog((Window) win, "", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.getContentPane().add(deDialogPanel);
        dialog.setUndecorated(true);
        dialog.pack();
        dialog.setLocationRelativeTo((Window) win);
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        glassPane.setVisible(false);
    }
}