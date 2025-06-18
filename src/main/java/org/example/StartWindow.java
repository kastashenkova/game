package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import gui.LoadingFrame;
import mainstage.MainFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * The StartWindow class represents the initial window of the game where the player can
 * choose a character, enter their name, select a specialty, and choose their course year.
 */
public class StartWindow extends JFrame {

    // Fields for storing selected character information
    private String selectedCharacterName;
    private String selectedCharacterResourcePath;
    private JTextField nameField;
    private String enteredName = "";
    private JComboBox<String> specialtyBox;
    private JComboBox<String> courseBox;
    private String selectedSpecialty;
    private Specialty specialty;
    JLabel nameLabel; // This field seems to be shadowed by a local variable in addCharacterPanel
    String[] specialties = new String[]{
            "Інженерія програмного забезпечення", "Комп'ютерні науки",
            "Прикладна математика"};
    String[] courses = new String[]{
            "1", "2", "3" , "4", "5", "6" };
    int selectedCourse;

    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_TURQUOISE = new Color(64, 224, 208);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);
    private final Color SIMS_BUTTON_HOVER = new Color(255, 240, 245);
    private static final Color SIMS_GREEN_CORRECT = new Color(144, 238, 144);
    private static final Color SIMS_RED_INCORRECT = new Color(255, 99, 71);
    BufferedImage icon;

    /**
     * Loads an image from the specified resource path.
     * @param path The path to the image resource.
     * @return The loaded BufferedImage.
     * @throws IOException If the image cannot be read.
     */
    private BufferedImage loadImage(String path) throws IOException {
        return ImageIO.read(getClass().getResourceAsStream(path));
    }

    /**
     * Constructs the StartWindow, initializing UI components,
     * setting up layouts, and handling user input for character selection and details.
     */
    public StartWindow() {

        MusicPlayer.getInstance().setMusicEnabled(true);
        MusicPlayer.getInstance().playMusic("/assets/Sounds/Background.wav");
        setTitle("Оберіть персонажку");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(600, 400));
        try {
            icon = loadImage("/logo.png");
        } catch (IOException e){
            // Handle exception, e.g., log it or use a default icon
        }
        setIconImage(icon);
        JPanel topFormPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        topFormPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        topFormPanel.setBackground(SIMS_LIGHT_BLUE);


        Font labelFont = new Font("MONOSPACED", Font.BOLD, 14);
        Font fieldFont = new Font("Inter", Font.PLAIN, 12);


        JLabel nameLabel = new JLabel("Ім'я Сіма:", SwingConstants.CENTER);
        nameLabel.setFont(labelFont);
        topFormPanel.add(nameLabel);

        JLabel specialtyLabel = new JLabel("Спеціальність:", SwingConstants.CENTER);
        specialtyLabel.setFont(labelFont);
        topFormPanel.add(specialtyLabel);

        JLabel courseLabel = new JLabel("Курс:", SwingConstants.CENTER);
        courseLabel.setFont(labelFont);
        topFormPanel.add(courseLabel);

        nameField = new JTextField();
        nameField.setFont(fieldFont);
        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetter(c)) {
                    e.consume(); // ignores input
                }
            }
        });
        topFormPanel.add(nameField);

        specialtyBox = new JComboBox<>(specialties);
        specialtyBox.setFont(fieldFont);
        specialtyBox.setSelectedIndex(-1);
        specialtyBox.addActionListener(e -> {
            selectedSpecialty = (String) specialtyBox.getSelectedItem();
            int index = specialtyBox.getSelectedIndex();
            if (index == 0) specialty = Specialty.IPZ;
            if (index == 1) specialty = Specialty.KN;
            if (index == 2) specialty = Specialty.PM;
        });
        topFormPanel.add(specialtyBox);

        courseBox = new JComboBox<>(courses);
        courseBox.setFont(fieldFont);
        courseBox.setSelectedIndex(-1);
        courseBox.addActionListener(e -> {
            Object selected = courseBox.getSelectedItem();
            if (selected != null) {
                try {
                    selectedCourse = Integer.parseInt(selected.toString());
                } catch (NumberFormatException ex) {
                    selectedCourse = -1; // Indicate invalid selection
                }
            }
        });
        topFormPanel.add(courseBox);

        add(topFormPanel, BorderLayout.NORTH);


        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 4, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(220, 230, 250));


        addCharacterPanel(mainPanel,  "girl1",  "assets/Models/Hero/girl1.png");
        addCharacterPanel(mainPanel, "girl2",  "assets/Models/Hero/girl2.png");
        addCharacterPanel(mainPanel,  "girl3", "assets/Models/Hero/girl3.png");

        add(mainPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Adds a character selection panel to the main panel. Each character panel displays
     * a character image, a placeholder for the entered name, and a selection button.
     * @param parentPanel The JPanel to which this character panel will be added.
     * @param name The internal name of the character (e.g., "girl1").
     * @param imageResourcePath The resource path to the character's image.
     */
    private void addCharacterPanel(JPanel parentPanel,  String name, String imageResourcePath) {
        JPanel characterPanel = new JPanel();
        characterPanel.setLayout(new BorderLayout()); // Use BorderLayout to arrange elements
        characterPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2)); // Panel border
        characterPanel.setBackground(Color.WHITE); // White background for character panel
        characterPanel.setOpaque(true);

        // Label for character name (will display enteredName)
        JLabel nameLabel = new JLabel(enteredName, SwingConstants.CENTER); // Center the text
        nameLabel.setFont(new Font("Inter", Font.BOLD, 18)); // Modern font
        nameLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Top/bottom padding
        characterPanel.add(nameLabel, BorderLayout.NORTH); // Place at the top

        ImageIcon characterIcon = null;
        URL imageUrl = getClass().getClassLoader().getResource(imageResourcePath);

        if (imageUrl != null) {
            characterIcon = new ImageIcon(imageUrl);
            Image image = characterIcon.getImage();
            Image scaledImage = image.getScaledInstance(90, 155, Image.SCALE_SMOOTH);
            characterIcon = new ImageIcon(scaledImage); // Create a new ImageIcon with the scaled image
        } else {
            System.err.println("Error: image not found in classpath at path: " + imageResourcePath);
            // Add a placeholder text label if image not found
            JLabel errorLabel = new JLabel("No Image", SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            errorLabel.setFont(new Font("Inter", Font.PLAIN, 12));
            characterPanel.add(errorLabel, BorderLayout.CENTER);
        }

        // Add the image if it was successfully loaded
        if (characterIcon != null) {
            JLabel imageLabel = new JLabel(characterIcon, SwingConstants.CENTER); // Center the image
            characterPanel.add(imageLabel, BorderLayout.CENTER); // Place in the center
        }

        // "Select" button
        JButton selectButton = new JButton("Вибрати");
        selectButton.setForeground(Color.BLACK);
        selectButton.setFont(new Font("Inter", Font.PLAIN, 14));
        selectButton.setBackground(new Color(204, 223, 255)); // Button color: Cornflower Blue
        selectButton.setFocusPainted(false);
        selectButton.setBorder(BorderFactory.createLineBorder(new Color(80, 120, 200), 3)); // Darker border
        selectButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor on hover
        selectButton.addActionListener(e -> {
            selectedCharacterName = name;
            enteredName = nameField.getText().trim(); // Get name from text field
            selectedCharacterResourcePath = imageResourcePath;
            MusicPlayer.getInstance().playButtonClick();
            int result = JOptionPane.showConfirmDialog(
                    this, // Use 'this' for the parent component
                    "Готові до початку гри зі створеною персонажкою?",
                    "Підтвердження",
                    JOptionPane.YES_NO_CANCEL_OPTION
            );
            if (result == JOptionPane.YES_OPTION) {
                startGame();
            }
        });

        characterPanel.add(selectButton, BorderLayout.SOUTH); // Place at the bottom
        parentPanel.add(characterPanel);

    }

    /**
     * Starts the game after validating user inputs (name, specialty, course, character selection).
     * Creates a Hero object with the selected attributes and transitions to the GameFrame.
     */
    private void startGame() {
        if (enteredName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Будь ласка, введіть ім’я!", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedSpecialty == null) {
            JOptionPane.showMessageDialog(this, "Будь ласка, оберіть спеціальність!", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedCourse == 0) { // Assuming 0 is the default unselected value or an invalid course
            JOptionPane.showMessageDialog(this, "Будь ласка, оберіть курс!", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedCharacterName != null && selectedCharacterResourcePath != null) {
            this.dispose(); // Close the current window
            Hero hero = new Hero( ); // Create a new Hero instance
            hero.setName(selectedCharacterName);
            hero.setSelectedName(enteredName); // Set the name entered by the user
            hero.setHeroResourcePath(selectedCharacterResourcePath);
            hero.setSpecialty(specialty); // Set the chosen specialty
            hero.setBudget(150); // Initial budget
            hero.setEnergy(100); // Initial energy
            hero.setCourse(selectedCourse); // Set the chosen course

            MusicPlayer.getInstance().setMusicEnabled(false); // Stop background music
            SwingUtilities.invokeLater(() -> {
                LoadingFrame loading = new LoadingFrame(); // Show loading screen
                loading.startLoading(() -> {
                    new GameFrame(hero).setVisible(true); // Start the main game frame after loading
                });
            });
        }
    }

    /**
     * Gets the name of the selected character.
     * @return The internal name of the selected character.
     */
    public String getSelectedCharacterName() {
        return selectedCharacterName;
    }

    /**
     * Gets the resource path of the selected character's image.
     * @return The image resource path.
     */
    public String getSelectedCharacterResourcePath() { // Changed name
        return selectedCharacterResourcePath;
    }
}