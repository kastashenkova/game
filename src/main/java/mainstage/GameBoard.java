package mainstage;

import org.example.Hero;
import org.example.MusicPlayer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;


public class GameBoard  extends JPanel implements Runnable {

    private static final double FPS = 60;
    final int originalCharactersSize = 16; // 16*16 default size
    final int scale = 3; // so it does not look small; also a standard scale
    final int charactersSize = originalCharactersSize * scale;

    final int screenWidth = 600;
    final int screenHeight = 600;

    final int maxWorldCol = 30;
    final int maxWorldRow = 15;

    final int worldWidth = charactersSize* maxWorldCol;
    final int worldHeight = charactersSize* maxWorldRow;

    public MusicPlayer musicPlayer = new MusicPlayer();
    public Hero hero;
    int cameraX = 0, cameraY = 0;

    public Thread gameThread;

    public TileManager tileManager = new TileManager(this);
    public BuildingManager buildingManager = new BuildingManager(this);

    KeyEventHandler keyEventHandler = new KeyEventHandler();
    public Player player ;
    public  CollisionChecker collisionChecker = new CollisionChecker(this);

    public GameBoard(MainFrame mainFrame) throws IOException {

        hero = mainFrame.gameFrame.getHero();
        player = new Player(this, keyEventHandler);
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        collisionChecker = new CollisionChecker(this);
        setDoubleBuffered(true);
        addKeyListener(keyEventHandler);
        setFocusable(true);
        buildingManager.setBuildings();
        requestFocusInWindow();

    }
    private boolean paused = false;

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
    public boolean isPaused() {
        return paused;
    }



    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
        MusicPlayer.getInstance().setMusicEnabled(true);
        MusicPlayer.getInstance().playMusic("/assets/Sounds/theme1.wav");

    }



    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        tileManager.draw(g2d);


        try {
            player.draw(g2d);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public void run() {

        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        if (paused) return;
        player.update();
        cameraX = player.worldX + screenWidth / 2 + charactersSize / 2;
        cameraY = player.worldY + screenHeight / 2 + charactersSize / 2;
        tileManager.car.update();
    }

}
