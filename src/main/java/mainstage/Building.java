package mainstage;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Represents a building object with its basic properties within the game world.
 */
public class Building {

    public int worldX, worldY; // The X and Y coordinates of the building in the game world
    public BufferedImage image; // The image representing the building
    public String name; // The name of the building
    public boolean collision = false; // Indicates if the building has collision properties
    public int width, height; // The width and height of the building

    /**
     * Returns the bounding rectangle of the building for collision detection.
     *
     * @return A {@link Rectangle} object representing the building's bounds.
     */
    public Rectangle getBounds() {
        return new Rectangle(worldX, worldY, width, height);
    }
}