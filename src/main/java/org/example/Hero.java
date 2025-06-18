package org.example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * The Hero class represents the main character in the game.
 * It manages the hero's attributes like energy, mood, hunger, knowledge, level, and budget.
 * It also handles the hero's visual representation, movement, and interaction messages.
 */
public class Hero {
    private String name;


    private int energy;
    private int mood;
    private int hunger;


    private int knowledge; // Hero's knowledge attribute, ranges from 0 to 100


    private int level = 1; // Current level of the hero (1 or 2)
    private boolean canLevelUp; // Indicates if the hero can level up
    private int budget;
    private int course;

    private String selectedName;


    private Student student; // Creates a virtual version of the student for registration and subsequent activity on university services

    private Specialty specialty;

    public int x;
    public int initialY;
    public int y;


    public String heroResourcePath;


    private BufferedImage heroImage; // Original size hero image
    private double scaleFactor; // Scaling factor for drawing

    private BufferedImage diamondImage; // Original size diamond image

    private double animationTimer;
    private final double SWAY_SPEED = 0.08;
    private final double SWAY_AMPLITUDE = 8.0;

    private boolean isSelected;

    private String heroMessage = ""; // Message displayed above the hero
    private long messageDisplayEndTime = 0;
    private final long MESSAGE_DISPLAY_DURATION = 3000; // 3 seconds in milliseconds

    private long lastHungerIncreaseTime;
    private final long HUNGER_INCREASE_INTERVAL = 5000; // Increase hunger every 5 seconds (5000 ms)
    private final int HUNGER_INCREASE_AMOUNT = 3; // Increase hunger by 3 units at a time

    private boolean lowEnergyWarningActive;
    private long lowEnergyWarningStartTime;
    private final long GAME_OVER_TIME_LIMIT = 15000; // 15 seconds until game over
    private boolean isGameOverDueToEnergy;

    /**
     * Default constructor for the Hero class.
     */
    public Hero() {

    }

    /**
     * Constructs a new Hero with specified properties and loads images.
     *
     * @param name The initial name of the hero.
     * @param heroResourcePath The path to the hero's image resource.
     * @param diamondResourcePath The path to the diamond image resource, or null for default.
     * @param initialX The initial X-coordinate of the hero.
     * @param initialY The initial Y-coordinate of the hero.
     * @param scaleFactor The scaling factor for the hero's image.
     */
    public Hero(String name, String heroResourcePath, String diamondResourcePath, int initialX, int initialY, double scaleFactor) {
        this.name = name;
        this.energy = 100;
        this.mood = 70;
        this.hunger = 30;
        this.knowledge = 0; // Initialization of knowledge
        this.level = 1; // Start from level 1
        this.canLevelUp = false; // By default, the hero cannot level up

        this.x = initialX;
        this.initialY = initialY;
        this.y = initialY;
        this.animationTimer = 0;
        this.scaleFactor = scaleFactor;
        this.isSelected = false;

        this.lastHungerIncreaseTime = System.currentTimeMillis();

        this.lowEnergyWarningActive = false;
        this.lowEnergyWarningStartTime = 0;
        this.isGameOverDueToEnergy = false;

        this.heroResourcePath = heroResourcePath;

        // Path to the diamond image. If it is passed as a parameter, use it.
        // Otherwise, use the default path.
        if (diamondResourcePath == null || diamondResourcePath.isEmpty()) {
            diamondResourcePath = "assets/Models/Hero/diamond.png";
        }

        try {
            URL heroImageUrl = getClass().getClassLoader().getResource(heroResourcePath);
            if (heroImageUrl != null) {
                this.heroImage = ImageIO.read(heroImageUrl);
            } else {
                System.err.println("Hero: Error: Hero image resource not found at path: " + heroResourcePath);
            }
        } catch (IOException e) {
            System.err.println("Hero: Error reading hero image file: " + e.getMessage());
            e.printStackTrace();
            this.heroImage = null; // Ensure null if loading failed
        }

        // Loading diamond image as a resource from the classpath
        try {
            URL diamondImageUrl = getClass().getClassLoader().getResource(diamondResourcePath);
            if (diamondImageUrl != null) {
                this.diamondImage = ImageIO.read(diamondImageUrl);
            } else {
                System.err.println("Hero: Error: Diamond image resource not found at path: " + diamondResourcePath);
            }
        } catch (IOException e) {
            System.err.println("Hero: Error reading diamond image file: " + e.getMessage());
            e.printStackTrace();
            this.diamondImage = null; // Ensure null if loading failed
        }
    }

    /**
     * Gets the hero's image.
     * @return The BufferedImage of the hero.
     */
    public BufferedImage getHeroImage() {
        return heroImage;
    }

    /**
     * Sets the hero's image.
     * @param heroImage The BufferedImage to set as the hero's image.
     */
    public void setHeroImage(BufferedImage heroImage) {
        this.heroImage = heroImage;
    }

    /**
     * Gets the hero's name.
     * @return The name of the hero.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the hero's current energy level.
     * @return The energy level (0-100).
     */
    public int getEnergy() {
        return energy;
    }

    /**
     * Gets the hero's current mood level.
     * @return The mood level (0-100).
     */
    public int getMood() {
        return mood;
    }

    /**
     * Gets the hero's current hunger level.
     * @return The hunger level (0-100).
     */
    public int getHunger() {
        return hunger;
    }


    /**
     * Gets the hero's current knowledge level.
     * @return The knowledge level (0-100).
     */
    public int getKnowledge() {
        return knowledge;
    } // Getter for knowledge

    /**
     * Gets the hero's current level.
     * @return The hero's level (e.g., 1 or 2).
     */
    public int getLevel() {
        return level;
    } // Getter for level

    /**
     * Gets the hero's X-coordinate.
     * @return The X-coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the hero's current Y-coordinate (which might be influenced by animation).
     * @return The Y-coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * These methods return scaled dimensions based on the original image and scaleFactor.
     * @return The scaled width of the hero's image.
     */
    public int getScaledWidth() {
        return heroImage != null ? (int) (heroImage.getWidth() * scaleFactor) : 0;
    }

    /**
     * Returns the scaled height of the hero's image based on the current scale factor.
     * @return The scaled height of the hero's image.
     */
    public int getScaledHeight() {
        return heroImage != null ? (int) (heroImage.getHeight() * scaleFactor) : 0;
    }

    /**
     * Checks if the hero is currently selected.
     * @return true if selected, false otherwise.
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Sets the selection status of the hero.
     * @param selected true to select the hero, false to deselect.
     */
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    /**
     * New method to check if game over condition for this hero is met.
     * @return true if game over due to energy, false otherwise.
     */
    public boolean isGameOverDueToEnergy() {
        return isGameOverDueToEnergy;
    }

    /**
     * New method to get the game over reason.
     * @return A string describing the game over reason, or null if not game over due to energy.
     */
    public String getGameOverReason() {
        if (isGameOverDueToEnergy) {
            return name + " не має енергії! Гра завершена.";
        }
        return null;
    }


    /**
     * Sets a message to be displayed above the hero for a limited duration.
     * @param message The message to display.
     */
    private void setMessage(String message) {
        this.heroMessage = message;
        this.messageDisplayEndTime = System.currentTimeMillis() + MESSAGE_DISPLAY_DURATION;
    }
    /**
     * Sets the hero's knowledge level.
     * @param knowledge The new knowledge level.
     */
    public void setKnowledge(int knowledge) {
        this.knowledge = knowledge;
    }


    /**
     * Method for the hero to advance to the next level.
     * Currently supports transitioning from Level 1 to Level 2 (session).
     */
    public void levelUp() {
        if (canLevelUp && level == 1) { // Can only transition from level 1 to level 2 if allowed
            level = 2; // Transition to Level 2 (session)
            knowledge = 0; // Reset knowledge after leveling up
            canLevelUp = false; // Reset level-up capability
            energy = Math.min(100, energy + 20); // A bit of energy after leveling up
            mood = Math.min(100, mood + 10); // Improve mood
            setMessage(selectedName + " успішно перейшла на рівень 2 (сесія)!");
        } else if (level >= 2) {
            setMessage(selectedName + " вже на рівні сесії.");
        } else {
            setMessage(selectedName + " ще не готова до сесії (потрібно більше знань).");
        }
    }

    /**
     * Updates the hero's state, including hunger, energy, mood, and animation.
     */
    public void update() {
        long currentTime = System.currentTimeMillis();

        // Hunger increase logic
        if (currentTime - lastHungerIncreaseTime >= HUNGER_INCREASE_INTERVAL) {
            hunger = Math.min(100, hunger + HUNGER_INCREASE_AMOUNT);
            lastHungerIncreaseTime = currentTime;
            // If hunger is high, mood may drop
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
                // Do not set a message here to avoid overriding other more important messages
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

    /**
     * Moves the hero by the specified delta values.
     * @param deltaX The change in the X-coordinate.
     * @param deltaY The change in the Y-coordinate.
     */
    public void move(int deltaX, int deltaY) {
        this.x += deltaX;
        this.initialY += deltaY; // Update initialY for correct sway
        this.y = initialY; // Reset y so that sway starts from the new position
    }

    /**
     * Draws the hero on the screen, including its image, selection border, diamond icon,
     * and any temporary messages. Also displays the hero's stats (energy, mood, hunger,
     * knowledge, budget) and current level in the top right corner.
     *
     * @param g The Graphics context to draw on.
     * @param offsetX The X-offset for drawing the scene.
     * @param offsetY The Y-offset for drawing the scene.
     * @param sceneScale The scaling factor for the entire scene.
     */
    public void draw(Graphics g, int offsetX, int offsetY, double sceneScale) {
        // If the hero image is not loaded, draw a placeholder rectangle
        if (heroImage == null) {
            g.setColor(java.awt.Color.RED);
            g.fillRect(x + offsetX, y + offsetY, 50, 50);
            g.setColor(java.awt.Color.BLACK);
            g.drawString("ПОМИЛКА ЗОБРАЖЕННЯ", x + offsetX + 5, y + offsetY + 25);
            // Important: restore the transform even if the image is not loaded
            // to avoid affecting subsequent drawings
            Graphics2D g2d_for_reset = (Graphics2D) g;
            g2d_for_reset.setTransform(new AffineTransform()); // Reset to identity matrix
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        AffineTransform oldTransform = g2d.getTransform();

        // Apply scene transformation (offset and scaling of the entire scene)
        AffineTransform sceneTransform = new AffineTransform();
        sceneTransform.translate(offsetX, offsetY);
        sceneTransform.scale(sceneScale, sceneScale);
        g2d.transform(sceneTransform);

        // Position and size of the hero in the scaled scene space
        int currentDrawX = this.x; // x, y are already hero coordinates
        int currentDrawY = this.y;
        int currentDrawWidth = getScaledWidth();  // Use the scaled width of the hero
        int currentDrawHeight = getScaledHeight(); // Use the scaled height of the hero

        // Draw the hero image
        g2d.drawImage(heroImage, currentDrawX, currentDrawY, currentDrawWidth, currentDrawHeight, null);

        // If the hero is selected, draw a green border
        if (isSelected) {
            g2d.setColor(Color.GREEN);
            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(currentDrawX - 2, currentDrawY - 2, currentDrawWidth + 4, currentDrawHeight + 4);
            g2d.setStroke(oldStroke);
        }

        // Draw the diamond image if it's loaded
        if (diamondImage != null) {
            int diamondWidth = (int) (diamondImage.getWidth() * 0.2); // Scale 0.2 for the diamond
            int diamondHeight = (int) (diamondImage.getHeight() * 0.2);
            int diamondX = currentDrawX + (currentDrawWidth - diamondWidth) / 2; // Center the diamond above the hero
            int diamondY = currentDrawY - diamondHeight - 5; // Slightly above the hero
            g2d.drawImage(diamondImage, diamondX, diamondY, diamondWidth, diamondHeight, null);
        }

        // Display message above the hero (for eat, sleep, study, relax)
        if (!heroMessage.isEmpty()) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12)); // Font for the message
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(heroMessage);
            int textHeight = fm.getHeight();

            // Position for the message
            int messageX = currentDrawX + (currentDrawWidth - textWidth) / 2;
            int messageY = currentDrawY - textHeight - (diamondImage != null ? (int) (diamondImage.getHeight() * 0.2) + 5 : 5);
            if (diamondImage == null) messageY -= 15; // If no diamond, lift the message higher

            // Draw the background frame for the message
            g2d.fillRoundRect(messageX - 5, messageY - textHeight, textWidth + 10, textHeight + 5, 10, 10);
            g2d.setColor(Color.BLACK); // Text color for the message
            g2d.drawString(heroMessage, messageX, messageY - fm.getDescent());
        }

        // Restore the original transform of the graphics context,
        // to draw UI elements in fixed positions on the panel
        g2d.setTransform(oldTransform);


        // --- Display static panels (bars) in the top right corner ---
        int panelWidth = g.getClipBounds().width; // Get the actual width of the panel
        int statsBarWidth = 100;
        int statsBarHeight = 10;
        int statsPadding = 5;
        int initialTopRightX = panelWidth - statsBarWidth - 20; // 20px offset from the right edge
        int currentTopRightY = 20; // 20px offset from the top edge

        // Energy Bar
        g2d.setColor(Color.RED);
        g2d.fillRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.setColor(Color.GREEN);
        g2d.fillRect(initialTopRightX, currentTopRightY, (int) (statsBarWidth * (energy / 100.0)), statsBarHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("Енергія: " + this.energy + "%", initialTopRightX, currentTopRightY - 5);


        // Mood Bar
        currentTopRightY += (statsBarHeight + statsPadding + 10); // Additional offset for convenience
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.setColor(Color.ORANGE);
        g2d.fillRect(initialTopRightX, currentTopRightY, (int) (statsBarWidth * (mood / 100.0)), statsBarHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.drawString("Настрій: " + mood + "%", initialTopRightX, currentTopRightY - 5);


        // Hunger Bar
        currentTopRightY += (statsBarHeight + statsPadding + 10);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.setColor(new Color(139, 69, 19)); // Brown for hunger
        g2d.fillRect(initialTopRightX, currentTopRightY, (int) (statsBarWidth * (hunger / 100.0)), statsBarHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.drawString("Голод: " + this.hunger + "%", initialTopRightX, currentTopRightY - 5);


        // Knowledge Bar
        currentTopRightY += (statsBarHeight + statsPadding + 10);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.setColor(new Color(0, 150, 250)); // Blue for knowledge
        g2d.fillRect(initialTopRightX, currentTopRightY, (int) (statsBarWidth * (knowledge / 100.0)), statsBarHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.drawString("Знання: " + this.knowledge + "%", initialTopRightX, currentTopRightY - 5);

        currentTopRightY += (statsBarHeight + statsPadding + 10);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.setColor(new Color(200, 50, 150));
        g2d.fillRect(initialTopRightX, currentTopRightY, (int) (statsBarWidth * (budget / 1000.0)), statsBarHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(initialTopRightX, currentTopRightY, statsBarWidth, statsBarHeight);
        g2d.drawString("Бюджет: " + this.budget + "₴", initialTopRightX, currentTopRightY - 5);

        // Display current level
        currentTopRightY += (statsBarHeight + statsPadding + 10);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Рівень: " + this.level, initialTopRightX, currentTopRightY);

        // Message about the possibility of leveling up
        if (canLevelUp) {
            currentTopRightY += (statsBarHeight + statsPadding + 5);
            g2d.setColor(Color.BLUE.darker());
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            String levelUpMsg = "Готова до сесії!";
            int msgWidth = g2d.getFontMetrics().stringWidth(levelUpMsg);
            g2d.drawString(levelUpMsg, initialTopRightX + (statsBarWidth - msgWidth) / 2, currentTopRightY);
        }
    }

    /**
     * Sets the name of the hero.
     * @param name The new name for the hero.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Decreases the hero's hunger level by a specified amount of nutrition.
     * Ensures hunger does not go below 0.
     * @param nutrition The amount by which to decrease hunger.
     */
    public void decreaseHunger(int nutrition) {

        this.hunger -= nutrition;
        if (hunger <= 0) this.hunger = 0;
    }

    public int getBudget() {
        return budget;
    }

    /**
     * Sets the hero's budget.
     * @param budget The new budget value.
     */
    public void setBudget(int budget) {
        this.budget = budget;
    }

    /**
     * Decreases the hero's budget by a specified amount.
     * @param decrease The amount to decrease the budget by.
     */
    public void decreaseBudget(int decrease) {
        this.budget -= decrease;
    }

    /**
     * Decreases the hero's energy by a specified amount.
     * @param energy The amount to decrease energy by.
     */
    public void decreaseEnergy(int energy){
        this.energy -= energy;
    }

    /**
     * Increases the hero's energy by a specified amount, capping at 100.
     * @param energy The amount to increase energy by.
     */
    public void increaseEnergy(int energy){
        int result =    this.energy + energy;
        if(result>=100){
            this.energy = 100;
            return;
        }
        this.energy = result;
    }
    /**
     * Decreases the hero's mood by a specified amount.
     * @param mood The amount to decrease mood by.
     */
    public void decreaseMood(int mood){
        this.mood -= mood;
    }

    /**
     * Increases the hero's mood by a specified amount, capping at 100.
     * @param mood The amount to increase mood by.
     */
    public void increaseMood(int mood){
        int result =    this.mood + mood;
        if(result>=100){
            this.mood = 100;
            return;
        }
        this.mood = result;
    }

    /**
     * Gets the hero's specialty.
     * @return The hero's Specialty object.
     */
    public Specialty getSpecialty() {
        return specialty;
    }

    /**
     * Sets the hero's specialty.
     * @param specialty The Specialty object to set.
     */
    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
    }

    /**
     * Gets the hero's associated Student object.
     * @return The Student object.
     */
    public Student getStudent() {
        return student;
    }

    /**
     * Sets the hero's associated Student object.
     * @param student The Student object to set.
     */
    public void setStudent(Student student) {
        this.student = student;
    }

    /**
     * Gets the path to the hero's resource image.
     * @return The string path to the hero's image.
     */
    public String getHeroResourcePath() {
        return heroResourcePath;
    }

    /**
     * Sets the path to the hero's resource image.
     * @param heroResourcePath The new string path for the hero's image.
     */
    public void setHeroResourcePath(String heroResourcePath) {
        this.heroResourcePath = heroResourcePath;
    }

    /**
     * Gets the hero's current course year.
     * @return The current course year.
     */
    public int getCourse() {
        return course;
    }

    /**
     * Sets the hero's current course year.
     * @param course The new course year.
     */
    public void setCourse(int course) {
        this.course = course;
    }

    /**
     * Gets the currently selected name for the hero.
     * @return The selected name string.
     */
    public String getSelectedName() {
        return selectedName;
    }

    /**
     * Sets the currently selected name for the hero.
     * @param selectedName The new selected name string.
     */
    public void setSelectedName(String selectedName) {
        this.selectedName = selectedName;
    }

    /**
     * Sets the hero's level.
     * @param level The new level for the hero.
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Sets the hero's energy level directly.
     * @param energy The new energy level.
     */
    public void setEnergy(int energy) {
        this.energy = energy;
    }
}