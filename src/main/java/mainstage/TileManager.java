package mainstage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Manages the drawing of tiles and background elements on the game board.
 */
public class TileManager {

    private GameBoard gameBoard;
    BufferedImage backgroundImage;

    // Load car image immediately
    BufferedImage carImg = ImageIO.read(getClass().getResourceAsStream("/cars/car.png"));
    public Car car;

    /**
     * Constructs a {@code TileManager}.
     *
     * @param gameBoard The game board instance.
     * @throws IOException If there is an error loading car image.
     */
    public TileManager(GameBoard gameBoard) throws IOException {
        this.gameBoard = gameBoard;
        car = new Car(0, 0, carImg);
    }

    /**
     * Draws the background image and the car, adjusting their positions relative to the camera.
     *
     * @param g The {@code Graphics} object used for drawing.
     */
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        try {
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/maps/map.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calculate background position relative to the player's screen position
        int x = -gameBoard.player.worldX + gameBoard.player.screenX;
        int y = -gameBoard.player.worldY + gameBoard.player.screenY;

        // Clamp background position to prevent drawing outside world boundaries
        int maxX = 0;
        int maxY = 0;
        int minX = gameBoard.screenWidth - backgroundImage.getWidth();
        int minY = gameBoard.screenHeight - backgroundImage.getHeight();

        if (x > maxX) x = maxX;
        if (y > maxY) y = maxY;
        if (x < minX) x = minX;
        if (y < minY) y = minY;

        g2d.drawImage(backgroundImage, x, y, backgroundImage.getWidth(), backgroundImage.getHeight(), null);

        // Draw the car
        int carScreenX = getCarScreenX();
        int carScreenY = getCarScreenY();
        g2d.drawImage(car.image, carScreenX, carScreenY, car.width, car.height, null);
    }

    /**
     * Calculates the car's X coordinate relative to the screen, considering camera movement.
     *
     * @return The car's X coordinate on the screen.
     */
    public int getCarScreenX() {
        int screenX = car.worldX - gameBoard.player.worldX + gameBoard.player.screenX;

        // Adjust screenX if the player is near the world's horizontal boundaries
        if (gameBoard.player.worldX < gameBoard.player.screenX) {
            screenX = car.worldX;
        } else if (gameBoard.player.worldX > gameBoard.worldWidth - (gameBoard.screenWidth - gameBoard.player.screenX)) {
            screenX = car.worldX - (gameBoard.worldWidth - gameBoard.screenWidth);
        }
        return screenX;
    }

    /**
     * Calculates the car's Y coordinate relative to the screen, considering camera movement.
     *
     * @return The car's Y coordinate on the screen.
     */
    public int getCarScreenY() {
        int screenY = car.worldY - gameBoard.player.worldY + gameBoard.player.screenY;

        // Adjust screenY if the player is near the world's vertical boundaries
        if (gameBoard.player.worldY < gameBoard.player.screenY) {
            screenY = car.worldY;
        } else if (gameBoard.player.worldY > gameBoard.worldHeight - (gameBoard.screenHeight - gameBoard.player.screenY)) {
            screenY = car.worldY - (gameBoard.worldHeight - gameBoard.screenHeight);
        }
        return screenY;
    }
}

/**
 * Represents a car object moving on the game board.
 */
class Car {
    public int worldX, worldY;
    public int width = 64, height = 64;
    public int speed = 2;
    public boolean movingDown = true;
    public BufferedImage image;

    /**
     * Constructs a {@code Car} object.
     *
     * @param x     The initial X coordinate of the car in the world.
     * @param y     The initial Y coordinate of the car in the world.
     * @param image The image representing the car.
     */
    public Car(int x, int y, BufferedImage image) {
        this.worldX = x;
        this.worldY = y;
        this.image = image;
    }

    /**
     * Simulates the movement process of the car. The car moves down, then changes direction and moves left,
     * resetting its position after moving off-screen to the left.
     */
    public void update() {
        if (movingDown) {
            worldY += speed;
            if (worldY > 270) { // If car moves beyond a certain Y coordinate, change direction
                movingDown = false;
                try {
                    // Change car image for horizontal movement
                    image = ImageIO.read(getClass().getResourceAsStream("/cars/car2.png"));
                } catch (IOException e) {
                    throw new RuntimeException("Не вдалося завантажити зображення авто2: " + e.getMessage(), e);
                }
            }
        } else {
            worldX -= speed; // Move left
            if (worldX < -64) { // If car moves off-screen to the left, reset position and direction
                movingDown = true;
                worldY = 0;
                worldX = 0;
                try {
                    // Reset car image to its original (downward-facing)
                    image = ImageIO.read(getClass().getResourceAsStream("/cars/car.png"));
                } catch (IOException e) {
                    throw new RuntimeException("Не вдалося завантажити зображення авто: " + e.getMessage(), e);
                }
            }
        }
    }
}