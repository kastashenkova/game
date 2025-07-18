package mainstage;

import Tests.TestManager;
import gui.LoadingFrame;
import org.example.EnrollmentSystemGUI;
import org.example.Hero;
import org.example.MusicPlayer;
import org.example.StudyProgressGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

/**
 * class to check the collision on the game board
 */
public class CollisionChecker {
    GameBoard gameBoard;


    public CollisionChecker(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    /**
     * analyzes collisions
     * @param player - player of the game
     * @param objects - analyse throw
     */
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

    /**
     * checks a collision  with a specific building and
     * sets the logic according to occurred collision
     * @param obj - a building to analyse
     * @param player  - player of the game
     */
        private void checkSpecificCollisions(Building obj, Player player) {

            if(!gameEnded && obj.name.equals("NaUKMA")) {
                gameEnded = true;
                gameBoard.gameThread.interrupt();
                gameBoard.musicPlayer.stopMusic();
                MusicPlayer.getInstance().playButtonClick();
                Hero hero = gameBoard.hero;
                System.out.println(hero.getBudget());
                int level = player.hero.getLevel();
                SwingUtilities.invokeLater(() -> {

                    Window gameWindow = SwingUtilities.getWindowAncestor(gameBoard);
                    if (gameWindow != null) {
                        gameWindow.dispose();
                    }
                    if (level == 1) {
                        hero.levelUp();
                        LoadingFrame loading = new LoadingFrame();
                        loading.startLoading(() -> {
                            EnrollmentSystemGUI enrollmentSystemGUI = new EnrollmentSystemGUI(hero);
                            enrollmentSystemGUI.setVisible(true);
                        });
                    } else if (level == 2) {
                        hero.setLevel(3);
                        TestManager testManager = new TestManager(hero);
                        testManager.startTest();
                    } else if (level == 3) {

                        LoadingFrame loading = new LoadingFrame();
                        loading.startLoading(() -> {

                            new StudyProgressGUI(hero).setVisible(true);

                        });
                    }
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
                gameBoard.player.direction = Direction.defaulted;

                gameBoard.player.keyEventHandler.clearAllKeys();
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
                        new Product("Картопля фрі", 60, 20, "/food/french.png"),
                        new Product("Гамбургер", 65, 30, "/food/hamb.png"),
                        new Product("Чизбургер", 80, 35, "/food/cheese.png"),
                        new Product("Морозиво", 50, 20, "/food/ice.png"),
                        new Product("Тако", 70, 30, "/food/taco.png"),
                        new Product("Сендвіч", 60, 30, "/food/sand.png"),
                        new Product("Креветки", 75, 25, "/food/shrimps.png"));

                        player.worldY -= 40;
                        player.worldX +=50;
                gameBoard.setPaused(true);
                gameBoard.player.direction = Direction.defaulted;
                gameBoard.player.keyEventHandler.clearAllKeys();
                SwingUtilities.invokeLater(() -> {
                    ShopFrame shopFrame;
                    try {
                        shopFrame = new ShopFrame(gameBoard, products);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    shopFrame.setVisible(true);

                });
            } else if((obj.name.equals("wheel"))){
                player.worldY += 40;
                player.worldX -=100;
                gameBoard.setPaused(true);

                try {
                    WheelStage wheelStage = new WheelStage(gameBoard);
                    wheelStage.setVisible(true);
                    wheelStage.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                           JOptionPane.showMessageDialog(null, "Ви ощасливили свого сіма та підняли йому " +
                                   "настрій!");
                            MusicPlayer.getInstance().setMusicEnabled(true);
                            MusicPlayer.getInstance().playMusic("/assets/Sounds/theme1.wav");

                            gameBoard.player.keyEventHandler.clearAllKeys();
                               }
                    });
                } catch (IOException e){

                }

            }
    }
}