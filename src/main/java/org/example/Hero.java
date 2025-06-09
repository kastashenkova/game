package org.example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException; // Імпортуємо тільки IOException
import java.net.URL; // Імпортуємо URL
import javax.imageio.ImageIO;

public class Hero {
    private String name;
    private int energy;
    private int mood;
    private int hunger;

    public int x;
    public int initialY;
    public int y;

    private BufferedImage heroImage; // Зображення героя (оригінального розміру)
    private double scaleFactor; // Коефіцієнт масштабування для малювання

    private BufferedImage diamondImage; // Зображення алмазу (оригінального розміру)

    private double animationTimer;
    private final double SWAY_SPEED = 0.08;
    private final double SWAY_AMPLITUDE = 8.0;

    private boolean isSelected;

    private String heroMessage = ""; // Повідомлення, що відображається над героєм
    private long messageDisplayEndTime = 0;
    private final long MESSAGE_DISPLAY_DURATION = 3000; // 3 секунди в мілісекундах

    private long lastHungerIncreaseTime;
    private final long HUNGER_INCREASE_INTERVAL = 5000; // Збільшувати голод кожні 5 секунд (5000 мс)
    private final int HUNGER_INCREASE_AMOUNT = 3; // Збільшувати голод на 3 одиниці за раз

    private boolean lowEnergyWarningActive;
    private long lowEnergyWarningStartTime;
    private final long GAME_OVER_TIME_LIMIT = 15000; // 15 секунд до завершення гри
    private boolean isGameOverDueToEnergy;

    public Hero(String name, String heroResourcePath, String diamondResourcePath, int initialX, int intialY, double scaleFactor) {
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

        diamondResourcePath = "assets/Models/Hero/diamond.png";

        try {
            URL heroImageUrl = getClass().getClassLoader().getResource(heroResourcePath);
            if (heroImageUrl != null) {
                this.heroImage = ImageIO.read(heroImageUrl);
                System.out.println("Hero: Зображення героя '" + name + "' завантажено з URL: " + heroImageUrl);
            } else {
                System.err.println("Hero: Помилка: Ресурс зображення героя не знайдено за шляхом: " + heroResourcePath);
            }
        } catch (IOException e) {
            System.err.println("Hero: Помилка під час читання файлу зображення героя: " + e.getMessage());
            e.printStackTrace();
            this.heroImage = null; // Забезпечити null, якщо завантаження не вдалося
        }

        // Завантаження зображення діаманта як ресурсу з classpath
        try {


            URL diamondImageUrl = getClass().getClassLoader().getResource(diamondResourcePath);
            if (diamondImageUrl != null) {
                this.diamondImage = ImageIO.read(diamondImageUrl);
                System.out.println("Hero: Зображення алмазу завантажено з URL: " + diamondImageUrl);
            } else {
                System.err.println("Hero: Помилка: Ресурс зображення алмазу не знайдено за шляхом: " + diamondResourcePath);
            }
        } catch (IOException e) {
            System.err.println("Hero: Помилка під час читання файлу зображення алмазу: " + e.getMessage());
            e.printStackTrace();
            this.diamondImage = null; // Забезпечити null, якщо завантаження не вдалося
        }
    }

    public String getName() { return name; }
    public int getEnergy() { return energy; }
    public int getMood() { return mood; }
    public int getHunger() { return hunger; }
    public int getX() { return x; }
    public int getY() { return y; }
    // Ці методи повертають масштабовані розміри на основі оригінального зображення та scaleFactor
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
            return name + " не має енергії! Гра завершена.";
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
        setMessage(name + " поїв."); // Додано повідомлення
    }

    public void sleep() {
        // Reset low energy warning if hero sleeps
        if (energy < 100) {
            lowEnergyWarningActive = false;
            lowEnergyWarningStartTime = 0;
            isGameOverDueToEnergy = false; // Important: Reset game over state
            energy = Math.min(100, energy + 40);
            mood = Math.min(100, mood + 10);
            setMessage(name + " поспав."); // Додано повідомлення
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
        setMessage(name + " повчився."); // Додано повідомлення
    }

    public void relax() {
        if (mood >= 100 && energy >= 100) {
            setMessage(name + " не може більше відпочивати.");
            return;
        }
        mood = Math.min(100, mood + 20);
        energy = Math.min(100, energy + 5);
        setMessage(name + " відпочив."); // Додано повідомлення
    }

    public void update() {
        long currentTime = System.currentTimeMillis();

        // Hunger increase logic
        if (currentTime - lastHungerIncreaseTime >= HUNGER_INCREASE_INTERVAL) {
            hunger = Math.min(100, hunger + HUNGER_INCREASE_AMOUNT);
            lastHungerIncreaseTime = currentTime;
        }

        if (energy < 20) {
            mood = Math.max(0, mood - 1);
        }

        if (energy <= 0) {
            if (!lowEnergyWarningActive) {
                lowEnergyWarningActive = true;
                lowEnergyWarningStartTime = currentTime;
                setMessage(name + " потребує сну! Залишилося 30 сек."); // Змінено на 30 сек.
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

        if (currentTime > messageDisplayEndTime) {
            heroMessage = "";
        }
    }

    public void move(int deltaX, int deltaY) {
        this.x += deltaX;
        this.initialY += deltaY; // Оновлюємо initialY для коректного sway
        this.y = initialY; // Скидаємо y, щоб sway почався з нової позиції
    }

    public void draw(Graphics g, int offsetX, int offsetY, double sceneScale) {
        // Якщо зображення героя не завантажено, малюємо прямокутник-заповнювач
        if (heroImage == null) {
            g.setColor(java.awt.Color.RED);
            g.fillRect(x + offsetX, y + offsetY, 50, 50);
            g.setColor(java.awt.Color.BLACK);
            g.drawString("ПОМИЛКА ЗОБРАЖЕННЯ", x + offsetX + 5, y + offsetY + 25);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        AffineTransform oldTransform = g2d.getTransform();

        // Застосовуємо трансформацію для сцени (зміщення та масштабування всієї сцени)
        AffineTransform sceneTransform = new AffineTransform();
        sceneTransform.translate(offsetX, offsetY);
        sceneTransform.scale(sceneScale, sceneScale);
        g2d.transform(sceneTransform);

        // Позиція та розмір героя в масштабованому просторі сцени
        int currentDrawX = this.x; // x, y вже є координатами героя
        int currentDrawY = this.y;
        int currentDrawWidth = getScaledWidth();  // Використовуємо масштабовану ширину героя
        int currentDrawHeight = getScaledHeight(); // Використовуємо масштабовану висоту героя

        // Малюємо зображення героя
        g2d.drawImage(heroImage, currentDrawX, currentDrawY, currentDrawWidth, currentDrawHeight, null);

        // Якщо герой вибраний, малюємо зелену рамку
        if (isSelected) {
            g2d.setColor(Color.GREEN);
            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(currentDrawX - 2, currentDrawY - 2, currentDrawWidth + 4, currentDrawHeight + 4);
            g2d.setStroke(oldStroke);
        }

        // Малюємо зображення діаманта, якщо воно завантажено
        if (diamondImage != null) {
            int diamondWidth = (int) (diamondImage.getWidth() * 0.2); // Масштаб 0.2 для діаманта
            int diamondHeight = (int) (diamondImage.getHeight() * 0.2);
            int diamondX = currentDrawX + (currentDrawWidth - diamondWidth) / 2; // Центруємо діамант над героєм
            int diamondY = currentDrawY - diamondHeight - 5; // Трохи вище героя
            g2d.drawImage(diamondImage, diamondX, diamondY, diamondWidth, diamondHeight, null);
        }

        // Відображення повідомлення над героєм
        if (!heroMessage.isEmpty()) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12)); // Шрифт для повідомлення
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(heroMessage);
            int textHeight = fm.getHeight();

            // Позиція для повідомлення
            int messageX = currentDrawX + (currentDrawWidth - textWidth) / 2;
            int messageY = currentDrawY - textHeight - (diamondImage != null ? (int)(diamondImage.getHeight() * 0.2) + 5 : 5);
            if (diamondImage == null) messageY -= 15; // Якщо немає діаманта, підняти повідомлення вище

            // Малюємо фонову рамку для повідомлення
            g2d.fillRoundRect(messageX - 5, messageY - textHeight, textWidth + 10, textHeight + 5, 10, 10);
            g2d.setColor(Color.BLACK); // Колір тексту повідомлення
            g2d.drawString(heroMessage, messageX, messageY - fm.getDescent());
        }

        // Відновлюємо оригінальну трансформацію графічного контексту
        g2d.setTransform(oldTransform);
    }
}