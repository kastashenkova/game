package mainstage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class TileManager {

    private GameBoard gameBoard;
    BufferedImage backgroundImage;

    BufferedImage carImg = ImageIO.read(getClass().getResourceAsStream("/cars/car.png"));
    public Car car;

    public TileManager(GameBoard gameBoard) throws IOException {
        this.gameBoard = gameBoard;
        car = new Car(0, 0, carImg);
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        try {
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/maps/map.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int x = -gameBoard.player.worldX + gameBoard.player.screenX;
        int y = -gameBoard.player.worldY + gameBoard.player.screenY;

        int maxX = 0;
        int maxY = 0;
        int minX = gameBoard.screenWidth - backgroundImage.getWidth();
        int minY = gameBoard.screenHeight - backgroundImage.getHeight();


        if (x > maxX) x = maxX;
        if (y > maxY) y = maxY;
        if (x < minX) x = minX;
        if (y < minY) y = minY;

        g2d.drawImage(backgroundImage, x, y, backgroundImage.getWidth(),backgroundImage.getHeight(), null);

        int carScreenX = getCarScreenX();
        int carScreenY = getCarScreenY();
        g2d.drawImage(car.image, carScreenX, carScreenY, car.width, car.height, null);


    }
    public int getCarScreenX() {
        int screenX = car.worldX - gameBoard.player.worldX + gameBoard.player.screenX;
        if (gameBoard.player.worldX < gameBoard.player.screenX) {
            screenX = car.worldX;

        } else if (gameBoard.player.worldX > gameBoard.worldWidth - (gameBoard.screenWidth - gameBoard.player.screenX)) {
            screenX = car.worldX - (gameBoard.worldWidth - gameBoard.screenWidth);

        }

        return screenX;
    }

    public int getCarScreenY() {
        int screenY = car.worldY - gameBoard.player.worldY + gameBoard.player.screenY;

        if (gameBoard.player.worldY < gameBoard.player.screenY) {
            screenY = car.worldY;
        } else if (gameBoard.player.worldY > gameBoard.worldHeight - (gameBoard.screenHeight - gameBoard.player.screenY)) {
            screenY = car.worldY - (gameBoard.worldHeight - gameBoard.screenHeight);
        }

        return screenY;
    }

}
class Car{
    public int worldX, worldY;
    public int width = 64, height = 64;
    public int speed = 2;
    public boolean movingDown = true;
    public BufferedImage image;

    public Car(int x, int y, BufferedImage image) {
        this.worldX = x;
        this.worldY = y;
        this.image = image;
    }

    public void update() {
        if (movingDown) {
            worldY += speed;
            if (worldY > 270) {
                movingDown = false;
            }
        } else {
            try {
                image = ImageIO.read(getClass().getResourceAsStream("/cars/car2.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            worldX -= speed;
            if(worldX < -64) {
                movingDown = true;
                worldY = 0;
                worldX = 0;
                try {
                    image = ImageIO.read(getClass().getResourceAsStream("/cars/car.png"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


}

