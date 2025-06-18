package mainstage;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Building {

    public int worldX, worldY;
    public BufferedImage image;
    public String name;
    public boolean collision = false;
    public int width, height;

    public Rectangle getBounds() {
        return new Rectangle(worldX, worldY, width, height);
    }

}
