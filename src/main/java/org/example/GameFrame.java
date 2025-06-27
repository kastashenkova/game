package org.example;

import mainstage.GameBoard;
import org.example.Student;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.random.RandomGenerator;

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

        // Створюємо героя з переданими шляхами до ресурсів
        newHero = new Hero(initialHeroName, initialHeroImagePath, initialDiamondImagePath, initialHeroX, initialHeroY, initialScaleFactor);
        student = new Student( initialHeroName, hero.getCourse(), hero.getSpecialty().toString());

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

        // Розмір фрейму тепер залежить тільки від GamePanel
        setPreferredSize(new Dimension(gamePanel.getPreferredSize().width, gamePanel.getPreferredSize().height));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Початкове системне повідомлення тепер відображатиметься як спливаюче повідомлення через GamePanel
        gamePanel.showFloatingMessage("Система", "Ласкаво просимо до віртуальної Могилянки!");
    }

    public GameFrame(GameBoard gameBoard){
        setTitle("Sims NaUKMA");
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

        // Розмір фрейму тепер залежить тільки від GamePanel
        setPreferredSize(new Dimension(gamePanel.getPreferredSize().width, gamePanel.getPreferredSize().height));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Початкове системне повідомлення тепер відображатиметься як спливаюче повідомлення через GamePanel
        gamePanel.showFloatingMessage("Система", "Ласкаво просимо до віртуальної Могилянки!");

    }

    public void handleGameOver(String reason) {
        dispose();
        SwingUtilities.invokeLater(() -> new StartWindow().setVisible(true));
    }

    private String generateRandomID(){
        RandomGenerator randomGenerator = new Random();
        int in = randomGenerator.nextInt(120000, 990000);
        return "RH" + in;
    }
    public Hero getHero() {
        return newHero;
    }

    public void setHero(Hero hero) {
        this.hero = hero;
    }
    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }
}