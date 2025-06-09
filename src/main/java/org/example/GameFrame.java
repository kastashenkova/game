package org.example;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    private Hero hero;
    private GamePanel gamePanel;

    private final String initialHeroName;
    private final String initialHeroImagePath;
    private final String initialDiamondImagePath;
    private final int initialHeroX;
    private final int initialHeroY;
    private final double initialScaleFactor;

    public GameFrame(String heroName, String heroImagePath) {
        setTitle("NaUKMA Sims");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        this.initialHeroName = heroName;
        this.initialHeroImagePath = heroImagePath;
        this.initialDiamondImagePath = "assets/Models/Hero/diamond.png";
        this.initialHeroX = 350;
        this.initialHeroY = 250;
        this.initialScaleFactor = 0.4;

        // Створюємо героя з переданими шляхами до ресурсів
        hero = new Hero(initialHeroName, initialHeroImagePath, initialDiamondImagePath, initialHeroX, initialHeroY, initialScaleFactor);

        gamePanel = new GamePanel(hero, this);
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
}