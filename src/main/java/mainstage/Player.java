package mainstage;

import org.example.Hero;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Player {

    int worldX;
    int worldY;
    public final int screenX;
    public final int screenY;
    public int  speed;
    private GameBoard gameBoard;
    private KeyEventHandler keyEventHandler;
    BufferedImage right1, right2, left1, left2, defaultPic;
    int spriteCounter = 0;
    int spriteNumber = 1;
    public Rectangle solidArea;
    public Direction direction;
    public boolean collisionOn;
    public Hero hero;

    public Player(GameBoard gameBoard, KeyEventHandler keyEventHandler) {
        this.gameBoard = gameBoard;
        this.keyEventHandler = keyEventHandler;
        hero = gameBoard.hero;
        screenX = gameBoard.screenWidth/2;
        screenY =gameBoard.screenHeight/2;
        direction = Direction.right;
        solidArea = new Rectangle(8, 16, 32, 32);
        setDefaultValues();
        getBufferedImages();

    }
    public void setDefaultValues(){
        worldX = gameBoard.charactersSize*5;
        worldY = gameBoard.charactersSize*10;
        speed = 3;
    }

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
            collisionOn = false;
            gameBoard.collisionChecker.checkCollisions(gameBoard.player, gameBoard.buildingManager.getBuildings());
            if (collisionOn == false) {
                switch (direction) {
                    case up:
                        if(!(gameBoard.player.worldY < 5)){
                        worldY -= speed;}
                        break;
                    case down:
                        if(!(gameBoard.player.worldY >= gameBoard.tileManager.backgroundImage.getHeight() - 160)){
                        worldY += speed;}
                        break;
                    case left:
                        if(!(gameBoard.player.worldX < 5)){
                            worldX -= speed;}
                        break;
                    case right:
                        if (!(gameBoard.player.worldX >= gameBoard.tileManager.backgroundImage.getWidth() - 2*gameBoard.charactersSize)){
                        worldX += speed;}
                        break;
                }

            }
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


    private void getBufferedImages() {
        String selectedCharacter = hero.getName();
        String basePath = "/player/" + selectedCharacter + "/";

        try {
            defaultPic = loadImage(basePath + "default.png");
            right1     = loadImage(basePath + "Walk.png");
            right2     = loadImage(basePath + "walk2.png");
            left1      = loadImage(basePath + "left1.png");
            left2      = loadImage(basePath + "left2.png");
        } catch (IOException e) {
            System.out.println("Помилка при завантаженні зображень персонажа: " + selectedCharacter);
            e.printStackTrace();
        }
    }

    private BufferedImage loadImage(String path) throws IOException {
        return ImageIO.read(getClass().getResourceAsStream(path));
    }

    public void draw(Graphics2D g) throws IOException {

        g.setColor(Color.BLACK);
        BufferedImage image = defaultPic;
        if(keyEventHandler.rightPressed){
            if(spriteNumber == 1){
                image = right1;
            } else if(spriteNumber == 2){
                image = right2;
            }
        }else if(keyEventHandler.leftPressed){
            if(spriteNumber == 1){
                image = left1;
            }else if(spriteNumber == 2){
                image = left2;
            }
        } else if(keyEventHandler.upPressed){
            if(spriteNumber== 1){
                image = right1;
            } else if(spriteNumber == 2){
                image = right2;
            }
        } else if(keyEventHandler.downPressed){
            if(spriteNumber == 1){
                image = right1;
            } else if(spriteNumber == 2){
                image = right2;
            }
        }
        g.drawImage(image, getScreenX(), getScreenY(), gameBoard.charactersSize, gameBoard.charactersSize, null);
        int centerX = getScreenX() + 10;
        int centerY = getScreenY() - 30;

        int size = 20;

        BufferedImage im = loadImage("/player/diam.png");
        g.drawImage(im, centerX, centerY, size, size+5, null);


    }
    public int getScreenX() {
        int screenX = this.screenX;

        if (worldX < screenX) {
            screenX = worldX;
        }

        int rightOffset = gameBoard.screenWidth - this.screenX;
        if (worldX > gameBoard.worldWidth - rightOffset) {
            screenX = gameBoard.screenWidth - (gameBoard.worldWidth - worldX);
        }
        return screenX;
    }

    public int getScreenY() {
        int screenY = this.screenY;

        if (worldY < screenY) {
            screenY = worldY;
        }

        int bottomOffset = gameBoard.screenHeight - this.screenY;
        if (worldY > gameBoard.worldHeight - bottomOffset) {
            screenY = gameBoard.screenHeight - (gameBoard.worldHeight - worldY);
        }

        return screenY;
    }


}
