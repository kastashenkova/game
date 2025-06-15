package mainstage;

import gui.LoadingFrame;
import studies.StartWindow;

import javax.swing.*;
import java.awt.*;

public class CollisionChecker {

    GameBoard gameBoard;

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

        // Зсуваємо її у напрямку
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
                    checkSpecificCollisions(obj);
                    player.collisionOn = true;
                    return;
                }
            }
        }
    }
    private boolean gameEnded = false;

        private void checkSpecificCollisions(Building obj) {

            if(!gameEnded && obj.name.equals("NaUKMA")){
                gameEnded = true;
                gameBoard.gameThread.interrupt();
                gameBoard.musicPlayer.stopMusic();
                SwingUtilities.invokeLater(() -> {

                    Window gameWindow = SwingUtilities.getWindowAncestor(gameBoard);
                    if (gameWindow != null) {
                        gameWindow.dispose();
                    }
                    LoadingFrame loading = new LoadingFrame();
                    loading.startLoading(() -> {
                        StartWindow startWindow = new StartWindow();
                        startWindow.setVisible(true);
                    });
                });
            }
            else if(obj.name.equals("cafe")){
                System.out.println(obj.name);

        }
            else  if(obj.name.equals("cafe")){

            }
    }
}