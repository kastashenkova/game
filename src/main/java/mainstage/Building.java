package mainstage;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Building {

    public int worldX, worldY;
    public BufferedImage image;
    public String name;
    public boolean collision = false;
    public int width, height;
    public boolean isReal = true;

    public Rectangle solidArea = new Rectangle(0, 0, 20, 20);
    public Rectangle getBounds() {
        return new Rectangle(worldX, worldY, width, height);
    }
    public void draw(Graphics2D g2d, Player player, GameBoard gameBoard) {
        int screenX = worldX - player.worldX + player.screenX;
        int screenY = worldY - player.worldY + player.screenY;
        g2d.drawImage(image, screenX, screenY, width, height, null);
    }

}
