package org.example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Hero {
    private String name;
    private int energy;
    private int mood;
    private int hunger;

    public int x;
    public int initialY;
    public int y;

    private BufferedImage heroImage;
    private double scaleFactor;

    private double animationTimer;
    private final double SWAY_SPEED = 0.08;
    private final double SWAY_AMPLITUDE = 8.0;

    private boolean isSelected;

    private BufferedImage diamondImage;

    private String heroMessage = ""; // Повідомлення, що відображається над героєм
    private long messageDisplayEndTime = 0;
    private final long MESSAGE_DISPLAY_DURATION = 3000; // 3 секунди в мілісекундах

    private long lastHungerIncreaseTime;
    private final long HUNGER_INCREASE_INTERVAL = 5000; // Збільшувати голод кожні 5 секунд (5000 мс)
    private final int HUNGER_INCREASE_AMOUNT = 3; // Збільшувати голод на 3 одиниці за раз

    // --- Нове: Для стану низької енергії та таймера гри ---
    private boolean lowEnergyWarningActive;
    private long lowEnergyWarningStartTime;
    private final long GAME_OVER_TIME_LIMIT = 15000;
    private boolean isGameOverDueToEnergy;


    public Hero(String name, String imagePath, String diamondImagePath, int initialX, int intialY, double scaleFactor) {
        this.name = name;
        this.energy = 100;
        this.mood = 70;
        this.hunger = 0;
        this.x = initialX;
        this.initialY = intialY;
        this.y = intialY;
        this.animationTimer = 0;
        this.scaleFactor = scaleFactor;
        this.isSelected = false;

        this.lastHungerIncreaseTime = System.currentTimeMillis();

        this.lowEnergyWarningActive = false;
        this.lowEnergyWarningStartTime = 0;
        this.isGameOverDueToEnergy = false;

        try {
            this.heroImage = ImageIO.read(new File(imagePath));
            if (this.heroImage == null) {
                System.err.println("Помилка: Не вдалося завантажити зображення героя зі шляху: " + imagePath);
            }
            this.diamondImage = ImageIO.read(new File(diamondImagePath));
            if (this.diamondImage == null) {
                System.err.println("Помилка: Не вдалося завантажити зображення алмазу зі шляху: " + diamondImagePath);
            }
        } catch (IOException e) {
            System.err.println("Помилка: Під час читання файлу зображення: " + e.getMessage());
            e.printStackTrace();
            this.heroImage = null;
            this.diamondImage = null;
        }
    }

    public String getName() { return name; }
    public int getEnergy() { return energy; }
    public int getMood() { return mood; }
    public int getHunger() { return hunger; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getScaledWidth() { return heroImage != null ? (int)(heroImage.getWidth() * scaleFactor) : 0; }
    public int getScaledHeight() { return heroImage != null ? (int)(heroImage.getHeight() * scaleFactor) : 0; }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    // New method to check if game over condition for this hero is met
    public boolean isGameOverDueToEnergy() {
        return isGameOverDueToEnergy;
    }

    // New method to get the game over reason
    public String getGameOverReason() {
        if (isGameOverDueToEnergy) {
            return name + " вичерпав всю енергію і не відпочив вчасно! Гра закінчена.";
        }
        return null;
    }

    private void setMessage(String message) {
        this.heroMessage = message;
        this.messageDisplayEndTime = System.currentTimeMillis() + MESSAGE_DISPLAY_DURATION;
    }

    public void eat() {
        if (hunger <= 0) {
            setMessage(name + " не хоче їсти.");
            return;
        }
        hunger = Math.max(0, hunger - 30);
        mood = Math.min(100, mood + 15);
    }

    public void sleep() {
        // Reset low energy warning if hero sleeps
        if (energy < 100) {
            lowEnergyWarningActive = false;
            lowEnergyWarningStartTime = 0;
            isGameOverDueToEnergy = false; // Important: Reset game over state
            energy = Math.min(100, energy + 40);
            mood = Math.min(100, mood + 10);
        } else {
            setMessage(name + " не хоче спати.");
        }
    }

    public void study() {
        if (energy < 20) {
            setMessage(name + " не має енергії для навчання.");
            return;
        }
        energy -= 20;
        mood = Math.max(0, mood - 10);
    }

    public void relax() {
        if (mood >= 100 && energy >= 100) {
            setMessage(name + " не може більше відпочивати.");
            return;
        }
        mood = Math.min(100, mood + 20);
        energy = Math.min(100, energy + 5);
    }

    public void update() {
        long currentTime = System.currentTimeMillis();

        // Hunger increase logic
        if (currentTime - lastHungerIncreaseTime >= HUNGER_INCREASE_INTERVAL) {
            hunger = Math.min(100, hunger + HUNGER_INCREASE_AMOUNT);
            lastHungerIncreaseTime = currentTime;
        }

        // Mood decrease if energy is low (less than 20)
        if (energy < 20) {
            mood = Math.max(0, mood - 1);
        }

        if (energy <= 0) {
            if (!lowEnergyWarningActive) {
                lowEnergyWarningActive = true;
                lowEnergyWarningStartTime = currentTime;
                setMessage(name + " потребує сну! Залишилося 30 сек.");
            } else {
                if (currentTime - lowEnergyWarningStartTime >= GAME_OVER_TIME_LIMIT) {
                    isGameOverDueToEnergy = true;
                } else {
                    long timeLeft = (GAME_OVER_TIME_LIMIT - (currentTime - lowEnergyWarningStartTime)) / 1000;
                    setMessage(name + " не має сил! Залишилося " + timeLeft + " сек.");
                }
            }
        } else {
            if (lowEnergyWarningActive) {
                lowEnergyWarningActive = false;
                lowEnergyWarningStartTime = 0;
                setMessage(name + " почувається краще.");
            }
        }

        animationTimer += SWAY_SPEED;
        double swayOffset = SWAY_AMPLITUDE * Math.sin(animationTimer);
        this.y = initialY + (int) swayOffset;

        // Clear temporary hero message after duration
        if (currentTime > messageDisplayEndTime) {
            heroMessage = "";
        }
    }

    public void move(int deltaX, int deltaY) {
        this.x += deltaX;
        this.initialY += deltaY;
    }

    public void draw(Graphics g, int offsetX, int offsetY, double sceneScale) {
        if (heroImage == null) {
            g.setColor(java.awt.Color.RED);
            g.fillRect(x + offsetX, y + offsetY, 50, 50);
            g.setColor(java.awt.Color.BLACK);
            g.drawString("ПОМИЛКА", x + offsetX + 5, y + offsetY + 25);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        AffineTransform oldTransform = g2d.getTransform();

        AffineTransform sceneTransform = new AffineTransform();
        sceneTransform.translate(offsetX, offsetY);
        sceneTransform.scale(sceneScale, sceneScale);
        g2d.transform(sceneTransform);

        int currentDrawX = (int) (this.x);
        int currentDrawY = (int) (this.y);
        int currentDrawWidth = (int) (getScaledWidth());
        int currentDrawHeight = (int) (getScaledHeight());

        g2d.drawImage(heroImage, currentDrawX, currentDrawY, currentDrawWidth, currentDrawHeight, null);

        if (isSelected) {
            g2d.setColor(Color.GREEN);
            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(currentDrawX - 2, currentDrawY - 2, currentDrawWidth + 4, currentDrawHeight + 4);
            g2d.setStroke(oldStroke);
        }

        if (diamondImage != null) {
            int diamondWidth = (int) (diamondImage.getWidth() * 0.2);
            int diamondHeight = (int) (diamondImage.getHeight() * 0.2);
            int diamondX = currentDrawX + (currentDrawWidth - diamondWidth) / 2;
            int diamondY = currentDrawY - diamondHeight - 5;
            g2d.drawImage(diamondImage, diamondX, diamondY, diamondWidth, diamondHeight, null);
        }

        if (!heroMessage.isEmpty()) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(heroMessage);
            int textHeight = fm.getHeight();

            int messageX = currentDrawX + (currentDrawWidth - textWidth) / 2;
            int messageY = currentDrawY - textHeight - (diamondImage != null ? (int)(diamondImage.getHeight() * 0.2) + 5 : 5);

            g2d.fillRoundRect(messageX - 5, messageY - textHeight, textWidth + 10, textHeight + 5, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString(heroMessage, messageX, messageY - fm.getDescent());
        }

        g2d.setTransform(oldTransform);
    }
}