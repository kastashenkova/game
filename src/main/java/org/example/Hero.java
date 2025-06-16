package org.example;

import org.example.Student;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class Hero {
    private String name;
    private int energy;
    private int mood;
    private int hunger;
    private int knowledge; // Новий атрибут: Знання, від 0 до 100


    private int level = 1; // Новий атрибут: Поточний рівень героя (1 або 2)
    private boolean canLevelUp; // Новий атрибут: Чи може герой перейти на новий рівень
    private int budget;
    private int course;

    private String selectedName;


    private Student student; //створюємо віртуальну версію студента для можливості запису на сазі та подальшої активності на університетських сервісах

    private Specialty specialty;

    public int x;
    public int initialY;
    public int y;


    public String heroResourcePath;


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

    public Hero() {

    }

    public Hero(String name, String heroResourcePath, String diamondResourcePath, int initialX, int intialY, double scaleFactor) {
        this.name = name;
        this.energy = 100;
        this.mood = 70;
        this.hunger = 30;
        this.knowledge = 0; // Ініціалізація знань
        this.level = 1; // Починаємо з рівня 1
        this.canLevelUp = false; // За замовчуванням герой не може перейти на новий рівень

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

        this.heroResourcePath = heroResourcePath;

        // Шлях до зображення діаманта. Якщо він передається як параметр, використовуємо його.
        // Якщо ні, використовуємо шлях за замовчуванням.
        if (diamondResourcePath == null || diamondResourcePath.isEmpty()) {
            diamondResourcePath = "assets/Models/Hero/diamond.png";
        }

        try {
            URL heroImageUrl = getClass().getClassLoader().getResource(heroResourcePath);
            if (heroImageUrl != null) {
                this.heroImage = ImageIO.read(heroImageUrl);
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
            } else {
                System.err.println("Hero: Помилка: Ресурс зображення алмазу не знайдено за шляхом: " + diamondResourcePath);
            }
        } catch (IOException e) {
            System.err.println("Hero: Помилка під час читання файлу зображення алмазу: " + e.getMessage());
            e.printStackTrace();
            this.diamondImage = null; // Забезпечити null, якщо завантаження не вдалося
        }
    }

    public BufferedImage getHeroImage() {
        return heroImage;
    }

    public void setHeroImage(BufferedImage heroImage) {
        this.heroImage = heroImage;
    }

    public String getName() {
        return name;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMood() {
        return mood;
    }

    public int getHunger() {
        return hunger;
    }

    public int getKnowledge() {
        return knowledge;
    } // Геттер для знань

    public int getLevel() {
        return level;
    } // Геттер для рівня

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // Ці методи повертають масштабовані розміри на основі оригінального зображення та scaleFactor
    public int getScaledWidth() {
        return heroImage != null ? (int) (heroImage.getWidth() * scaleFactor) : 0;
    }

    public int getScaledHeight() {
        return heroImage != null ? (int) (heroImage.getHeight() * scaleFactor) : 0;
    }

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

    // Новий метод для перевірки, чи може герой перейти на наступний рівень
    public boolean canLevelUp() {
        return canLevelUp;
    }

    private void setMessage(String message) {
        this.heroMessage = message;
        this.messageDisplayEndTime = System.currentTimeMillis() + MESSAGE_DISPLAY_DURATION;
    }

    public void eat() {
        if (hunger <= 0) {
            setMessage(selectedName + " не хоче їсти.");
            return;
        }
        hunger = Math.max(0, hunger - 30);
        mood = Math.min(100, mood + 15);
        setMessage(selectedName + " поїла."); // Додано повідомлення
    }

    public void sleep() {
        // Reset low energy warning if hero sleeps
        if (energy < 100) {
            lowEnergyWarningActive = false;
            lowEnergyWarningStartTime = 0;
            isGameOverDueToEnergy = false; // Important: Reset game over state
            energy = Math.min(100, energy + 40);
            mood = Math.min(100, mood + 10);
            setMessage(selectedName + " поспала."); // Додано повідомлення
        } else {
            setMessage(selectedName + " не хоче спати.");
        }
    }

    public void study() {
        if (energy < 20) {
            setMessage(selectedName + " не має енергії для навчання.");
            return;
        }
        // Якщо герой вже на рівні сесії і знання повні, не дозволяємо вчитися далі
        if (level >= 2 && knowledge >= 100) {
            setMessage(selectedName + " вже готова до сесії!");
            return;
        }

        energy = Math.max(0, energy - 20);
        mood = Math.max(0, mood - 10);
        knowledge = Math.min(100, knowledge + 10); // Знання повільно наповнюються (+10 за навчання)

        // Перевіряємо, чи може герой перейти на новий рівень
        if (knowledge >= 100 && level == 1) { // Тільки якщо знання досягли 100 і ми на 1 рівні
            canLevelUp = true;
            setMessage(selectedName + " готова до сесії! Натисніть 'Підвищити рівень'."); // Повідомлення про готовність до сесії
        } else {
            setMessage(selectedName + " повчилася. Знання: " + knowledge + "%.");
        }
    }

    public void relax() {
        if (mood >= 100 && energy >= 100) {
            setMessage(selectedName + " не може більше відпочивати.");
            return;
        }
        mood = Math.min(100, mood + 20);
        energy = Math.min(100, energy + 5);
        setMessage(name + " відпочила.");
    }

    /**
     * Метод для переходу героя на наступний рівень.
     * Наразі підтримує перехід з рівня 1 на рівень 2 (сесія).
     */
    public void levelUp() {
        if (canLevelUp && level == 1) { // Можна перейти тільки з 1 на 2 рівень, якщо дозволено
            level = 2; // Переходимо на рівень 2 (сесія)
            knowledge = 0; // Скидаємо знання після переходу на новий рівень
            canLevelUp = false; // Скидаємо можливість переходу
            energy = Math.min(100, energy + 20); // Трохи енергії після переходу
            mood = Math.min(100, mood + 10); // Покращуємо настрій
            setMessage(selectedName + " успішно перейшла на рівень 2 (сесія)!");
        } else if (level >= 2) {
            setMessage(selectedName + " вже на рівні сесії.");
        } else {
            setMessage(selectedName + " ще не готова до сесії (потрібно більше знань).");
        }
    }

    public void update() {
        long currentTime = System.currentTimeMillis();

        // Hunger increase logic
        if (currentTime - lastHungerIncreaseTime >= HUNGER_INCREASE_INTERVAL) {
            hunger = Math.min(100, hunger + HUNGER_INCREASE_AMOUNT);
            lastHungerIncreaseTime = currentTime;
            // Якщо голод високий, настрій може падати
            if (hunger > 70) {
                mood = Math.max(0, mood - 2);
            }
        }

        // Energy and Mood decay when low energy
        if (energy < 20) {
            mood = Math.max(0, mood - 1);
        }

        // Game Over logic for energy
        if (energy <= 0) {
            if (!lowEnergyWarningActive) {
                lowEnergyWarningActive = true;
                lowEnergyWarningStartTime = currentTime;
                setMessage(selectedName + " потребує сну! Залишилося " + (GAME_OVER_TIME_LIMIT / 1000) + " сек.");
            } else {
                if (currentTime - lowEnergyWarningStartTime >= GAME_OVER_TIME_LIMIT) {
                    isGameOverDueToEnergy = true;
                } else {
                    long timeLeft = (GAME_OVER_TIME_LIMIT - (currentTime - lowEnergyWarningStartTime)) / 1000;
                    setMessage(selectedName + " не має сил! Залишилося " + timeLeft + " сек.");
                }
            }
        } else {
            if (lowEnergyWarningActive) {
                lowEnergyWarningActive = false;
                lowEnergyWarningStartTime = 0;
                // Не встановлюємо повідомлення тут, щоб не перебивати інші важливіші повідомлення
            }
        }

        // Sway animation
        animationTimer += SWAY_SPEED;
        double swayOffset = SWAY_AMPLITUDE * Math.sin(animationTimer);
        this.y = initialY + (int) swayOffset;

        // Clear message after duration
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
            // Важливо: відновити трансформацію, навіть якщо зображення не завантажено
            // Щоб уникнути впливу на подальші малювання
            Graphics2D g2d_for_reset = (Graphics2D) g;
            g2d_for_reset.setTransform(new AffineTransform()); // Скидаємо до одиничної матриці
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

        // Відображення повідомлення над героєм (для eat, sleep, study, relax)
        if (!heroMessage.isEmpty()) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12)); // Шрифт для повідомлення
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(heroMessage);
            int textHeight = fm.getHeight();

            // Позиція для повідомлення
            int messageX = currentDrawX + (currentDrawWidth - textWidth) / 2;
            int messageY = currentDrawY - textHeight - (diamondImage != null ? (int) (diamondImage.getHeight() * 0.2) + 5 : 5);
            if (diamondImage == null) messageY -= 15; // Якщо немає діаманта, підняти повідомлення вище

            // Малюємо фонову рамку для повідомлення
            g2d.fillRoundRect(messageX - 5, messageY - textHeight, textWidth + 10, textHeight + 5, 10, 10);
            g2d.setColor(Color.BLACK); // Колір тексту повідомлення
            g2d.drawString(heroMessage, messageX, messageY - fm.getDescent());
        }

        // Відновлюємо оригінальну трансформацію графічного контексту,
        // щоб малювати UI елементи у фіксованих позиціях на панелі
        g2d.setTransform(oldTransform);


        // --- Відображення статичних панелей (шкал) у верхньому правому куті ---
        int panelWidth = g.getClipBounds().width; // Отримуємо фактичну ширину панелі
        int statsBarWidth = 100;
        int statsBarHeight = 10;
        int statsPadding = 5;
        int initialTopRightX = panelWidth - statsBarWidth - 20; // 20px відступ від правого краю
        int currentTopRightY = 20; // 20px відступ від верхнього краю

        // Панель енергії
        g2d.setColor(Color.RED);
        g2d.fillRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.setColor(Color.GREEN);
        g2d.fillRect(initialTopRightX, currentTopRightY, (int) (statsBarWidth * (energy / 100.0)), statsBarHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("Енергія: " + energy + "%", initialTopRightX, currentTopRightY - 5);


        // Панель настрою
        currentTopRightY += (statsBarHeight + statsPadding + 10); // Додатковий відступ для зручності
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.setColor(Color.ORANGE);
        g2d.fillRect(initialTopRightX, currentTopRightY, (int) (statsBarWidth * (mood / 100.0)), statsBarHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.drawString("Настрій: " + mood + "%", initialTopRightX, currentTopRightY - 5);


        // Панель голоду
        currentTopRightY += (statsBarHeight + statsPadding + 10);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.setColor(new Color(139, 69, 19)); // Коричневий для голоду
        g2d.fillRect(initialTopRightX, currentTopRightY, (int) (statsBarWidth * (hunger / 100.0)), statsBarHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.drawString("Голод: " + hunger + "%", initialTopRightX, currentTopRightY - 5);


        // Панель знань
        currentTopRightY += (statsBarHeight + statsPadding + 10);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.setColor(new Color(0, 150, 250)); // Синій для знань
        g2d.fillRect(initialTopRightX, currentTopRightY, (int) (statsBarWidth * (knowledge / 100.0)), statsBarHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.drawString("Знання: " + knowledge + "%", initialTopRightX, currentTopRightY - 5);

        currentTopRightY += (statsBarHeight + statsPadding + 10);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.setColor(new Color(200, 50, 150));
        g2d.fillRect(initialTopRightX, currentTopRightY, (int) (statsBarWidth * (budget / 1000.0)), statsBarHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.drawString("Бюджет: " + budget + "₴", initialTopRightX, currentTopRightY - 5);

        //
        // Відображення поточного рівня
        currentTopRightY += (statsBarHeight + statsPadding + 10);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Рівень: " + this.level, initialTopRightX, currentTopRightY);

        // Повідомлення про можливість переходу на новий рівень
        if (canLevelUp) {
            currentTopRightY += (statsBarHeight + statsPadding + 5);
            g2d.setColor(Color.BLUE.darker());
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            String levelUpMsg = "Готова до сесії!";
            int msgWidth = g2d.getFontMetrics().stringWidth(levelUpMsg);
            g2d.drawString(levelUpMsg, initialTopRightX + (statsBarWidth - msgWidth) / 2, currentTopRightY);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void decreaseHunger(int nutrition) {

        this.hunger -= nutrition;
        if (hunger <= 0) this.hunger = 0;
    }


    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public void decreaseBudget(int decrease) {
        this.budget -= decrease;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getHeroResourcePath() {
        return heroResourcePath;
    }

    public void setHeroResourcePath(String heroResourcePath) {
        this.heroResourcePath = heroResourcePath;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
    }

    public String getSelectedName() {
        return selectedName;
    }

    public void setSelectedName(String selectedName) {
        this.selectedName = selectedName;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}