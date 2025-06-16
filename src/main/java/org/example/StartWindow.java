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

public class StartWindow extends JFrame {

    // Поля для зберігання вибраного персонажа
    private String selectedCharacterName;
    private String selectedCharacterResourcePath;
    private JTextField nameField;
    private String enteredName = "";
    private JComboBox<String> specialtyBox;
    private JComboBox<String> courseBox;
    private String selectedSpecialty;
    private Specialty specialty;
    JLabel nameLabel;
    String[] specialties = new String[]{
        "Інженерія програмного забезпечення", "Комп'ютерні науки",
            "Прикладна математика"};
    String[] courses = new String[]{
         "БП-1", "БП-2", "БП-3" , "БП-4", "МП-1", "МП-2" };
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

    private BufferedImage loadImage(String path) throws IOException {
        return ImageIO.read(getClass().getResourceAsStream(path));
    }

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
                    e.consume(); // ігнорує введення
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
                    selectedCourse = -1;
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

    private void addCharacterPanel(JPanel parentPanel,  String name, String imageResourcePath) {
        JPanel characterPanel = new JPanel();
        characterPanel.setLayout(new BorderLayout()); // Використовуємо BorderLayout для розміщення елементів
        characterPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2)); // Межа панелі
        characterPanel.setBackground(Color.WHITE); // Білий фон для панелі персонажа
        characterPanel.setOpaque(true);

        // Мітка для імені персонажа
        nameLabel = new JLabel(enteredName, SwingConstants.CENTER); // Центруємо текст
        nameLabel.setFont(new Font("Inter", Font.BOLD, 18)); // Сучасний шрифт
        nameLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Відступи зверху/знизу
        characterPanel.add(nameLabel, BorderLayout.NORTH); // Розміщуємо зверху

        ImageIcon characterIcon = null;
        URL imageUrl = getClass().getClassLoader().getResource(imageResourcePath);

        if (imageUrl != null) {
            characterIcon = new ImageIcon(imageUrl);
            Image image = characterIcon.getImage();
            Image scaledImage = image.getScaledInstance(110, 155, Image.SCALE_SMOOTH);
            characterIcon = new ImageIcon(scaledImage); // Створюємо новий ImageIcon зі масштабованим зображенням
        } else {
            System.err.println("Помилка: зображення не знайдено в classpath за шляхом: " + imageResourcePath);
            // Додаємо текстову мітку-заглушку, якщо зображення не знайдено
            JLabel errorLabel = new JLabel("Немає зображення", SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            errorLabel.setFont(new Font("Inter", Font.PLAIN, 12));
            characterPanel.add(errorLabel, BorderLayout.CENTER);
        }

        // Додаємо зображення, якщо воно було успішно завантажено
        if (characterIcon != null) {
            JLabel imageLabel = new JLabel(characterIcon, SwingConstants.CENTER); // Центруємо зображення
            characterPanel.add(imageLabel, BorderLayout.CENTER); // Розміщуємо в центрі
        }

        // Кнопка "Вибрати"
        JButton selectButton = new JButton("Вибрати");
        selectButton.setForeground(Color.BLACK);
        selectButton.setFont(new Font("Inter", Font.PLAIN, 14));
        selectButton.setBackground(new Color(204, 223, 255)); // Колір кнопки: Cornflower Blue
        selectButton.setFocusPainted(false);
        selectButton.setBorder(BorderFactory.createLineBorder(new Color(80, 120, 200), 3)); // Темніша рамка
        selectButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Курсор-рука при наведенні
        selectButton.addActionListener(e -> {
            selectedCharacterName = name;
            enteredName = nameField.getText().trim();
            selectedCharacterResourcePath = imageResourcePath;
            MusicPlayer.getInstance().playButtonClick();
            int result = JOptionPane.showConfirmDialog(
                    null,
                    "Готові до початку гри зі створеною персонажкою?",
                    "Підтвердження",
                    JOptionPane.YES_NO_CANCEL_OPTION
            );
            if (result == JOptionPane.YES_OPTION) {
                startGame();
            }
        });

        characterPanel.add(selectButton, BorderLayout.SOUTH); // Розміщуємо знизу
        parentPanel.add(characterPanel);

    }

    private void startGame() {
        if (enteredName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Будь ласка, введіть ім’я!", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedSpecialty == null) {
            JOptionPane.showMessageDialog(this, "Будь ласка, оберіть спеціальність!", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedCharacterName != null && selectedCharacterResourcePath != null) {
            this.dispose();
            Hero hero = new Hero( );
            hero.setName(selectedCharacterName);
            hero.setSelectedName(enteredName);
            hero.setHeroResourcePath(selectedCharacterResourcePath);
            hero.setSpecialty(specialty);
            System.out.println(specialty.toString());
            hero.setCourse(selectedCourse);

            MusicPlayer.getInstance().setMusicEnabled(false);
            SwingUtilities.invokeLater(() -> {
                LoadingFrame loading = new LoadingFrame();
                loading.startLoading(() -> {
                    new GameFrame(hero).setVisible(true);
                });
            });
        }
    }

    public String getSelectedCharacterName() {
        return selectedCharacterName;
    }

    public String getSelectedCharacterResourcePath() { // Змінено назву
        return selectedCharacterResourcePath;
    }


}
