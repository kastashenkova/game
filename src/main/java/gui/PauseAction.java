package gui;

import mainstage.GameBoard;
import org.example.MusicPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * class for a pause action
 */
public class PauseAction extends AbstractAction {
    private static final int ALPHA = 175; // how much see-thru. 0 to 255
    private static final Color GP_BG = new Color(0, 0, 0, ALPHA);
    private GameBoard gameBoard;
    private DeDialogPanel deDialogPanel = new DeDialogPanel(gameBoard);  // jpanel shown in JDialog

    /**
     *
     * @param name - name of a frame
     * @param gameBoard - game board of the game
     */
    public PauseAction(String name, GameBoard gameBoard) {

            super(name);
            this.gameBoard = gameBoard;
            this.deDialogPanel = new DeDialogPanel(gameBoard); // передаємо далі
        }

    /**
     *
     * @param name - name of a frame
     */
    public PauseAction(String name) {
        super(name);

        this.deDialogPanel = new DeDialogPanel(); // передаємо далі
    }

    /**
     * Sets a game on pause and creates an effect of a darkened paused screen
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        MusicPlayer.getInstance().playButtonClick();
        if (gameBoard!=null) {
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


        JDialog dialog = new JDialog((Window)win, "", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.getContentPane().add(deDialogPanel);
        dialog.setUndecorated(true);
        dialog.pack();
        dialog.setLocationRelativeTo((Window) win);
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        glassPane.setVisible(false);

    }
}