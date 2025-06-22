package mainstage;

import org.example.Hero;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Represents the player character in the game board simulation.
 * Manages player position, movement, animation, and collision.
 */
public class Player {

    public int worldX;
    public int worldY;
    public final int screenX;
    public final int screenY;
    public int speed;
    private GameBoard gameBoard;
    KeyEventHandler keyEventHandler;
    BufferedImage right1, right2, left1, left2, defaultPic;
    int spriteCounter = 0;
    int spriteNumber = 1;
    public Rectangle solidArea;
    public Direction direction;
    public boolean collisionOn;
    public Hero hero;

    /**
     * Constructs a {@code Player} object.
     *
     * @param gameBoard       The game board where the player exists.
     * @param keyEventHandler The key handler to process player input.
     */
    public Player(GameBoard gameBoard, KeyEventHandler keyEventHandler) {
        this.gameBoard = gameBoard;
        this.keyEventHandler = keyEventHandler;
        hero = gameBoard.hero;
        screenX = gameBoard.screenWidth / 2;
        screenY = gameBoard.screenHeight / 2;
        direction = Direction.right;
        solidArea = new Rectangle(8, 16, 32, 32);
        setDefaultValues();
        getBufferedImages();
    }

    /**
     * Sets the player's initial starting position and speed.
     */
    public void setDefaultValues() {
        worldX = gameBoard.charactersSize * 5;
        worldY = gameBoard.charactersSize * 10;
        speed = 3;
    }

    /**
     * Updates the player's position and animation based on keyboard input and checks for collisions.
     */
    public void update() {
        collisionOn = false;
        if (keyEventHandler.upPressed || keyEventHandler.downPressed ||
                keyEventHandler.leftPressed || keyEventHandler.rightPressed) {

            if (keyEventHandler.upPressed) {
                direction = Direction.up;
            }
            if (keyEventHandler.downPressed) {
                direction = Direction.down;
            }
            if (keyEventHandler.leftPressed) {
                direction = Direction.left;
            }
            if (keyEventHandler.rightPressed) {
                direction = Direction.right;
            }

            // Check for collisions with buildings before moving
            gameBoard.collisionChecker.checkCollisions(gameBoard.player, gameBoard.buildingManager.getBuildings());

            // If no collision, update player's world coordinates
            if (!collisionOn) {
                switch (direction) {
                    case up:
                        if (!(gameBoard.player.worldY < 5)) {
                            worldY -= speed;
                        }
                        break;
                    case down:
                        if (!(gameBoard.player.worldY >= gameBoard.tileManager.backgroundImage.getHeight() - 160)) {
                            worldY += speed;
                        }
                        break;
                    case left:
                        if (!(gameBoard.player.worldX < 5)) {
                            worldX -= speed;
                        }
                        break;
                    case right:
                        if (!(gameBoard.player.worldX >= gameBoard.tileManager.backgroundImage.getWidth() - 2 * gameBoard.charactersSize)) {
                            worldX += speed;
                        }
                        break;
                }
            }

            // Update sprite for animation
            spriteCounter++;
            if (spriteCounter == 14) {
                if (spriteNumber == 1) {
                    spriteNumber = 2;
                } else if (spriteNumber == 2) {
                    spriteNumber = 1;
                }
                spriteCounter = 0;
            }
        }
    }

    /**
     * Loads the buffered images for player animations based on the selected character.
     */
    private void getBufferedImages() {
        String selectedCharacter = hero.getName();
        String basePath = "/player/" + selectedCharacter + "/";

        try {
            defaultPic = loadImage(basePath + "default.png");
            right1 = loadImage(basePath + "Walk.png");
            right2 = loadImage(basePath + "walk2.png");
            left1 = loadImage(basePath + "left1.png");
            left2 = loadImage(basePath + "left2.png");
        } catch (IOException e) {
            System.out.println("Помилка при завантаженні зображень персонажа: " + selectedCharacter);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to load an image from the given path.
     *
     * @param path The path to the image resource.
     * @return The loaded {@link BufferedImage}.
     * @throws IOException If the image cannot be read.
     */
    private BufferedImage loadImage(String path) throws IOException {
        return ImageIO.read(getClass().getResourceAsStream(path));
    }

    /**
     * Draws the player character on the screen, updating the image based on direction and animation frame.
     * Also draws a diamond icon above the player.
     *
     * @param g The {@link Graphics2D} object used for drawing.
     * @throws IOException If there is an error loading the diamond image.
     */
    public void draw(Graphics2D g) throws IOException {
        BufferedImage image = defaultPic; // Default image when stationary

        // Select the correct animation frame based on direction
        if (keyEventHandler.rightPressed) {
            if (spriteNumber == 1) {
                image = right1;
            } else if (spriteNumber == 2) {
                image = right2;
            }
        } else if (keyEventHandler.leftPressed) {
            if (spriteNumber == 1) {
                image = left1;
            } else if (spriteNumber == 2) {
                image = left2;
            }
        } else if (keyEventHandler.upPressed) {
            // Use right-facing sprites for up movement
            if (spriteNumber == 1) {
                image = right1;
            } else if (spriteNumber == 2) {
                image = right2;
            }
        } else if (keyEventHandler.downPressed) {
            // Use right-facing sprites for down movement
            if (spriteNumber == 1) {
                image = right1;
            } else if (spriteNumber == 2) {
                image = right2;
            }
        }

        // Draw the player image
        g.drawImage(image, getScreenX(), getScreenY(), gameBoard.charactersSize, gameBoard.charactersSize, null);

        // Draw a diamond icon above the player
        int centerX = getScreenX() + 10;
        int centerY = getScreenY() - 30;
        int size = 20;
        BufferedImage im = loadImage("/player/diam.png");
        g.drawImage(im, centerX, centerY, size, size + 5, null);
    }

    /**
     * Calculates the player's X coordinate relative to the screen, adjusting for camera movement.
     *
     * @return The player's X coordinate on the screen.
     */
    public int getScreenX() {
        int screenX = this.screenX;

        // Adjust screenX if the player is near the left edge of the world
        if (worldX < screenX) {
            screenX = worldX;
        }

        // Adjust screenX if the player is near the right edge of the world
        int rightOffset = gameBoard.screenWidth - this.screenX;
        if (worldX > gameBoard.worldWidth - rightOffset) {
            screenX = gameBoard.screenWidth - (gameBoard.worldWidth - worldX);
        }
        return screenX;
    }

    /**
     * Calculates the player's Y coordinate relative to the screen, adjusting for camera movement.
     *
     * @return The player's Y coordinate on the screen.
     */
    public int getScreenY() {
        int screenY = this.screenY;

        // Adjust screenY if the player is near the top edge of the world
        if (worldY < screenY) {
            screenY = worldY;
        }

        // Adjust screenY if the player is near the bottom edge of the world
        int bottomOffset = gameBoard.screenHeight - this.screenY;
        if (worldY > gameBoard.worldHeight - bottomOffset) {
            screenY = gameBoard.screenHeight - (gameBoard.worldHeight - worldY);
        }
        return screenY;
    }
}