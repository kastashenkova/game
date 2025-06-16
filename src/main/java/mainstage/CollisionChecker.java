package mainstage;

import gui.LoadingFrame;
import org.example.EnrollmentSystemGUI;
import org.example.Hero;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class CollisionChecker {

    GameBoard gameBoard;
    Hero hero;

    public CollisionChecker(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    public void checkCollisions(Player player, Building[] objects) {
        player.collisionOn = false;

        Rectangle futureBounds = new Rectangle(
                player.worldX + player.solidArea.x,
                player.worldY + player.solidArea.y,
                player.solidArea.width,
                player.solidArea.height
        );

        switch (player.direction) {
            case up:
                futureBounds.y -= player.speed;
                break;
            case down:
                futureBounds.y += player.speed;
                break;
            case left:
                futureBounds.x -= player.speed;
                break;
            case right:
                futureBounds.x += player.speed;
                break;
        }

        for (Building obj : objects) {
            if (obj != null && obj.collision) {
                if (futureBounds.intersects(obj.getBounds())) {
                    checkSpecificCollisions(obj, player);
                    player.collisionOn = true;
                    return;
                }
            }
        }
    }
    private boolean gameEnded = false;

        private void checkSpecificCollisions(Building obj, Player player) {

            if(!gameEnded && obj.name.equals("NaUKMA")){
                gameEnded = true;
                gameBoard.gameThread.interrupt();
                gameBoard.musicPlayer.stopMusic();
                Hero hero = gameBoard.hero;
                int level = player.hero.getLevel();
                SwingUtilities.invokeLater(() -> {

                    Window gameWindow = SwingUtilities.getWindowAncestor(gameBoard);
                    if (gameWindow != null) {
                        gameWindow.dispose();
                    }
                    LoadingFrame loading = new LoadingFrame();
                    loading.startLoading(() -> {
                        if(level==1){
                            hero.levelUp();
                            EnrollmentSystemGUI enrollmentSystemGUI = new EnrollmentSystemGUI(hero);
                            enrollmentSystemGUI.setVisible(true);
                        } else if(level==2){

                        }
                    });
                });
            }
            else if(obj.name.equals("shop")){
                java.util.List<Product> products = List.of(
                        new Product("Яблуко", 30, 10, "/food/apple.png"),
                        new Product("Банан", 25, 10, "/food/banana.png"),
                        new Product("Хліб", 50, 25, "/food/bread.png"),
                        new Product("Піца", 60, 20, "/food/pizza.png"),
                        new Product("Салат", 60, 20, "/food/salad.png"),
                        new Product("Шоколад", 60, 20, "/food/chocolate.png"),
                        new Product("Йогурт", 35, 15, "/food/yoghurt.png")

                );
                player.worldY -= 40;
                player.worldX -=50;
                gameBoard.setPaused(true);
                SwingUtilities.invokeLater(() -> {
                    ShopFrame shopFrame;
                    try {
                        shopFrame = new ShopFrame(gameBoard, products);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    shopFrame.setVisible(true);

                });

        }
            else  if(obj.name.equals("cafe")){
                java.util.List<Product> products = List.of(
                        new Product("French Fries", 60, 20, "/food/french.png"),
                        new Product("Hamburger", 65, 30, "/food/hamb.png"),
                        new Product("Cheeseburger", 80, 35, "/food/cheese.png"),
                        new Product("Ice Cream", 50, 20, "/food/ice.png"),
                        new Product("Tacos", 70, 30, "/food/taco.png"),
                        new Product("Sandwich", 60, 30, "/food/sand.png"),
                        new Product("Shrimps", 75, 25, "/food/shrimps.png"));

                        player.worldY -= 40;
                        player.worldX +=50;
                gameBoard.setPaused(true);
                SwingUtilities.invokeLater(() -> {
                    ShopFrame shopFrame;
                    try {
                        shopFrame = new ShopFrame(gameBoard, products);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    shopFrame.setVisible(true);

                });
            }
    }
}