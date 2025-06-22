package mainstage;

import org.example.Hero;
import org.example.MusicPlayer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Represents the main Kontraktova Square simulation game board.
 * This class includes the game loop, drawing operations, and manages game elements
 * like the player, tiles, and buildings, with a "camera" that follows the player.
 */
public class GameBoard extends JPanel implements Runnable {

    private static final double FPS = 60;
    final int originalCharactersSize = 16;
    final int scale = 3;
    final int charactersSize = originalCharactersSize * scale;

    final int screenWidth = 600;
    final int screenHeight = 600;

    final int maxWorldCol = 30;
    final int maxWorldRow = 15;

    final int worldWidth = charactersSize * maxWorldCol;
    final int worldHeight = charactersSize * maxWorldRow;

    public MusicPlayer musicPlayer = new MusicPlayer();
    public Hero hero;
    int cameraX = 0, cameraY = 0;

    public Thread gameThread;

    public TileManager tileManager = new TileManager(this);
    public BuildingManager buildingManager = new BuildingManager(this);

    KeyEventHandler keyEventHandler = new KeyEventHandler();
    public Player player;
    public CollisionChecker collisionChecker = new CollisionChecker(this);

    /**
     * Constructs a {@code GameBoard}.
     *
     * @param mainFrame The main application frame, used to get the hero instance.
     * @throws IOException If there is an error loading resources.
     */
    public GameBoard(MainFrame mainFrame) throws IOException {
        hero = mainFrame.gameFrame.getHero();
        player = new Player(this, keyEventHandler);
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setDoubleBuffered(true);
        addKeyListener(keyEventHandler);
        setFocusable(true);
        buildingManager.setBuildings();
        requestFocusInWindow();
    }

    private boolean paused = false;

    /**
     * Sets the pause state of the game board.
     *
     * @param paused {@code true} to pause the game, {@code false} to unpause.
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * Checks if the game is currently paused.
     *
     * @return {@code true} if the game is paused, {@code false} otherwise.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Initiates the main game thread, starting the game loop and playing background music.
     */
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
        MusicPlayer.getInstance().setMusicEnabled(true);
        MusicPlayer.getInstance().playMusic("/assets/Sounds/theme1.wav");
    }

    /**
     * Draws the main game scene.
     *
     * @param g The {@code Graphics} object used for drawing.
     */
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

    /**
     * The main game loop that runs the simulation.
     */
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

    /**
     * Updates the game state, including player movement, camera position, and other game elements.
     */
    private void update() {
        if (paused) return;
        player.update();
        cameraX = player.worldX - screenWidth / 2 + charactersSize / 2;
        cameraY = player.worldY - screenHeight / 2 + charactersSize / 2;
        tileManager.car.update();
    }
}