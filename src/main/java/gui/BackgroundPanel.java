package gui;

import javax.swing.*;
import java.awt.*;

/**
 * class for a background picture
 */

public class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    /**
     * constructor that gets the image from file by file path
     * @param filePath is used for getting an image
     */
    public BackgroundPanel(String filePath) {
        backgroundImage = new ImageIcon(filePath).getImage();
        setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
    }
}
