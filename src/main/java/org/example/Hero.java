package org.example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Hero {
    private String name;
    private int health;
    private int energy;
    private int mood;
    private int hunger;
    private int experience;

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

    // --- Нове: Для передачі повідомлення про відмову в чат ---
    private String lastActionMessage = null;


    private long lastHungerIncreaseTime;
    private final long HUNGER_INCREASE_INTERVAL = 5000; // Збільшувати голод кожні 5 секунд (5000 мс)
    private final int HUNGER_INCREASE_AMOUNT = 3; // Збільшувати голод на 3 одиниці за раз


    public Hero(String name, String imagePath, String diamondImagePath, int initialX, int initialY, double scaleFactor) {
        this.name = name;
        this.health = 100;
        this.energy = 100;
        this.mood = 70;
        this.hunger = 0;
        this.experience = 0;
        this.x = initialX;
        this.initialY = initialY;
        this.y = initialY;
        this.animationTimer = 0;
        this.scaleFactor = scaleFactor;
        this.isSelected = false;

        this.lastHungerIncreaseTime = System.currentTimeMillis();

        try {
            this.heroImage = ImageIO.read(new File(imagePath));
            if (this.heroImage == null) {
                System.err.println("Помилка: Не вдалося завантажити зображення героя з шляху: " + imagePath);
            }
            this.diamondImage = ImageIO.read(new File(diamondImagePath));
            if (this.diamondImage == null) {
                System.err.println("Помилка: Не вдалося завантажити зображення алмазу з шляху: " + diamondImagePath);
            }
        } catch (IOException e) {
            System.err.println("Помилка при читанні файлу зображення: " + e.getMessage());
            e.printStackTrace();
            this.heroImage = null;
            this.diamondImage = null;
        }
    }

    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getEnergy() { return energy; }
    public int getMood() { return mood; }
    public int getHunger() { return hunger; }
    public int getExperience() { return experience; }
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

    public String getLastActionMessage() {
        String message = lastActionMessage;
        lastActionMessage = null; // Очищуємо після отримання, щоб не повторювати
        return message;
    }

    private void setMessage(String message) {
        this.heroMessage = message;
        this.lastActionMessage = message; // Зберігаємо повідомлення і для чату
        this.messageDisplayEndTime = System.currentTimeMillis() + MESSAGE_DISPLAY_DURATION;
    }

    public void eat() {
        if (hunger <= 0) {
            setMessage(name + " не хоче їсти");
            return;
        }
        hunger = Math.max(0, hunger - 30);
        mood = Math.min(100, mood + 15);
        lastActionMessage = null; // Очищуємо, якщо дія успішна, щоб GamePanel знав, що герой нічого не "сказав"
    }

    public void sleep() {
        if (energy >= 100) {
            setMessage(name + " не хоче спати.");
            return;
        }
        energy = Math.min(100, energy + 40);
        mood = Math.min(100, mood + 10);
        lastActionMessage = null;
    }

    public void study() {
        if (energy < 20) {
            setMessage(name + " не має енергії для навчання.");
            return;
        }
        energy -= 20;
        experience += 10;
        mood = Math.max(0, mood - 10);
        lastActionMessage = null;
    }

    public void relax() {
        if (mood >= 100 && energy >= 100) {
            setMessage(name + " не може більше відпочивати.");
            return;
        }
        mood = Math.min(100, mood + 20);
        energy = Math.min(100, energy + 5);
        lastActionMessage = null;
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHungerIncreaseTime >= HUNGER_INCREASE_INTERVAL) {
            hunger = Math.min(100, hunger + HUNGER_INCREASE_AMOUNT);
            lastHungerIncreaseTime = currentTime;
        }

        if (hunger >= 80) health = Math.max(0, health - 1);
        if (energy < 20) mood = Math.max(0, mood - 1);

        animationTimer += SWAY_SPEED;
        double swayOffset = SWAY_AMPLITUDE * Math.sin(animationTimer);
        this.y = initialY + (int) swayOffset;

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
            g.drawString("ERROR", x + offsetX + 5, y + offsetY + 25);
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
            g2d.setColor(Color.YELLOW);
            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(currentDrawX - 2, currentDrawY - 2, currentDrawWidth + 4, currentDrawHeight + 4);
            g2d.setStroke(oldStroke);
        }

        if (diamondImage != null) {
            int diamondWidth = (int) (diamondImage.getWidth() * 0.2);
            int diamondHeight = (int) (diamondImage.getHeight() * 0.2);
            int diamondX = currentDrawX + (currentDrawWidth - diamondWidth) / 2;
            int diamondY = currentDrawY - diamondHeight - 10;
            g2d.drawImage(diamondImage, diamondX, diamondY, diamondWidth, diamondHeight, null);
        }

        if (!heroMessage.isEmpty()) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(heroMessage);
            int textHeight = fm.getHeight();

            int messageX = currentDrawX + (currentDrawWidth - textWidth) / 2;
            int messageY = currentDrawY - textHeight - (diamondImage != null ? (int)(diamondImage.getHeight() * scaleFactor) + 20 : 10);

            g2d.fillRoundRect(messageX - 5, messageY - textHeight, textWidth + 10, textHeight + 5, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString(heroMessage, messageX, messageY - fm.getDescent());
        }

        g2d.setTransform(oldTransform);
    }
}