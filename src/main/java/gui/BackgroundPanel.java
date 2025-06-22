package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Class for a background picture panel that extends JPanel to display an image as background.
 */
public class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    /**
     * Constructor that gets the image from file by file path.
     *
     * @param filePath the file path used for getting an image
     */
    public BackgroundPanel(String filePath) {
        backgroundImage = new ImageIcon(filePath).getImage();
        setLayout(new BorderLayout());
    }

    /**
     * Paints the component by drawing the background image scaled to fill the entire panel.
     *
     * @param g the Graphics context in which to paint
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
    }
}