package org.example;

import mainstage.GameBoard;
import org.example.Student;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * The main game frame for the Sims NaUKMA application. This frame hosts the game panel
 * and manages the main game logic and hero/student data.
 */
public class GameFrame extends JFrame {

    private Hero hero;
    Hero newHero;

    private GamePanel gamePanel;
    private Student student;

    private final String initialHeroName;
    private final String initialHeroImagePath;
    private final String initialDiamondImagePath;
    private final int initialHeroX;
    private final int initialHeroY;
    private final double initialScaleFactor;

    /**
     * Constructs a GameFrame with a new Hero instance based on the provided Hero object.
     * This constructor is typically used when starting a new game or carrying over basic hero data.
     *
     * @param hero The Hero object containing initial data for the new game.
     */
    public GameFrame(Hero hero) {
        setTitle("Sims NaUKMA");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        this.initialHeroName = hero.getName();
        this.initialHeroImagePath = hero.getHeroResourcePath();
        this.initialDiamondImagePath = "assets/Models/Hero/diamond.png";
        this.initialHeroX = 350;
        this.initialHeroY = 150;
        this.initialScaleFactor = 0.4;

        // Create a new hero with the provided resource paths
        newHero = new Hero(initialHeroName, initialHeroImagePath, initialDiamondImagePath, initialHeroX, initialHeroY, initialScaleFactor);
        student = new Student(initialHeroName, hero.getCourse(), hero.getSpecialty().toString());

        newHero.setStudent(student);
        newHero.setBudget(hero.getBudget());
        newHero.setSpecialty(hero.getSpecialty());
        if(hero.getStudent()!=null) {
            student.setExamDisciplines(hero.getStudent().getExamDisciplines());
            student.setEnrolledDisciplines(hero.getStudent().getEnrolledDisciplines());
        }
        newHero.setCourse(hero.getCourse());
        newHero.setSelectedName(hero.getSelectedName());
        newHero.setLevel(hero.getLevel());
        newHero.setEnergy(hero.getEnergy());

        gamePanel = new GamePanel(newHero, this);
        add(gamePanel, BorderLayout.CENTER);

        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();

        // The frame size now depends only on GamePanel
        setPreferredSize(new Dimension(gamePanel.getPreferredSize().width, gamePanel.getPreferredSize().height));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Initial system message will now be displayed as a floating message via GamePanel
        gamePanel.showFloatingMessage("Система", "Ласкаво просимо до віртуальної Могилянки!");
    }

    /**
     * Constructs a GameFrame from an existing GameBoard. This constructor is used
     * to restore a game state, taking hero and student data directly from the GameBoard.
     *
     * @param gameBoard The GameBoard instance containing the current game state.
     */
    public GameFrame(GameBoard gameBoard){
        setTitle("NaUKMA Sims");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        this.initialHeroName = gameBoard.player.hero.getName();
        this.initialHeroImagePath = gameBoard.player.hero.getHeroImage().toString();
        this.initialDiamondImagePath = "assets/Models/Hero/diamond.png";
        this.initialHeroX = 350;
        this.initialHeroY = 150;
        this.initialScaleFactor = 0.4;

        if(gameBoard.player.hero!=null){
            newHero = gameBoard.player.hero;
        }

        gamePanel = new GamePanel(newHero, this);
        add(gamePanel, BorderLayout.CENTER);

        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();

        // The frame size now depends only on GamePanel
        setPreferredSize(new Dimension(gamePanel.getPreferredSize().width, gamePanel.getPreferredSize().height));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Initial system message will now be displayed as a floating message via GamePanel
        gamePanel.showFloatingMessage("Система", "Ласкаво просимо до віртуальної Могилянки!");

    }

    /**
     * Handles the game over condition, disposing of the current frame and
     * opening a new StartWindow.
     *
     * @param reason A string explaining why the game is over (e.g., "Student expelled").
     */
    public void handleGameOver(String reason) {
        dispose();
        SwingUtilities.invokeLater(() -> new StartWindow().setVisible(true));
    }

    /**
     * Generates a random ID string. (Note: This method is currently unused in the provided code,
     * but remains for potential future use).
     *
     * @return A randomly generated ID string.
     */
    private String generateRandomID(){
        RandomGenerator randomGenerator = new Random();
        int in = randomGenerator.nextInt(120000, 990000);
        return "RH" + in;
    }

    /**
     * Returns the current Hero object managed by this game frame.
     *
     * @return The Hero object.
     */
    public Hero getHero() {
        return newHero;
    }

    /**
     * Sets the Hero object for this game frame.
     *
     * @param hero The Hero object to set.
     */
    public void setHero(Hero hero) {
        this.hero = hero;
    }

    /**
     * Returns the GamePanel instance associated with this frame.
     *
     * @return The GamePanel.
     */
    public GamePanel getGamePanel() {
        return gamePanel;
    }

    /**
     * Sets the GamePanel instance for this frame.
     *
     * @param gamePanel The GamePanel to set.
     */
    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }
}