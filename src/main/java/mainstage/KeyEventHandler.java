package mainstage;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Handles keyboard input for controlling player actions within the game.
 */
public class KeyEventHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed;

    /**
     * Not used in this implementation.
     * @param e The {@link KeyEvent} generated by a key typed event.
     */
    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    /**
     * Clears all movement flags, stopping any continuous player movement.
     * This is typically called when the game state changes (e.g., pausing).
     */
    public void clearAllKeys() {
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
    }

    /**
     * Called when a key is pressed. Sets the corresponding movement flag to {@code true}.
     * @param e The {@link KeyEvent} generated by a key press.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_UP) {
            upPressed = true;
        }
        if (code == KeyEvent.VK_DOWN) {
            downPressed = true;
        }
        if (code == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }
        if (code == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }
    }

    /**
     * Called when a key is released. Sets the corresponding movement flag to {@code false}.
     * @param e The {@link KeyEvent} generated by a key release.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_UP) {
            upPressed = false;
        }
        if (code == KeyEvent.VK_DOWN) {
            downPressed = false;
        }
        if (code == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (code == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }
}