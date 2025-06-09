package org.example;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class StartWindow extends JFrame {

    // Поля для зберігання вибраного персонажа
    private String selectedCharacterName;
    private String selectedCharacterResourcePath;

    public StartWindow() {
        setTitle("Оберіть персонажку");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(600, 400)); // Розмір вікна вибору

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 4, 10, 10)); // 1 ряд, 4 колонки, з відступами
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(220, 230, 250)); // Легкий синій фон

        String oksanaResourcePath = "assets/Models/Hero/Oksana.png";
        String oleksandraResourcePath = "assets/Models/Hero/Oleksandra.png";
        String gabrielResourcePath = "assets/Models/Hero/Gabriel.png";
        String sofiaResourcePath = "assets/Models/Hero/Sofia.png";

        // Передаємо ресурсні шляхи (String) до методу addCharacterPanel
        addCharacterPanel(mainPanel, "Оксана", oksanaResourcePath);
        addCharacterPanel(mainPanel, "Олександра", oleksandraResourcePath);
        addCharacterPanel(mainPanel, "Габріель", gabrielResourcePath);
        addCharacterPanel(mainPanel, "Софія", sofiaResourcePath);

        add(mainPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null); // Центрувати вікно на екрані
    }

    private void addCharacterPanel(JPanel parentPanel, String name, String imageResourcePath) {
        JPanel characterPanel = new JPanel();
        characterPanel.setLayout(new BorderLayout()); // Використовуємо BorderLayout для розміщення елементів
        characterPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2)); // Межа панелі
        characterPanel.setBackground(Color.WHITE); // Білий фон для панелі персонажа
        characterPanel.setOpaque(true);

        // Мітка для імені персонажа
        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER); // Центруємо текст
        nameLabel.setFont(new Font("Inter", Font.BOLD, 18)); // Сучасний шрифт
        nameLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Відступи зверху/знизу
        characterPanel.add(nameLabel, BorderLayout.NORTH); // Розміщуємо зверху

        ImageIcon characterIcon = null;
        URL imageUrl = getClass().getClassLoader().getResource(imageResourcePath);

        if (imageUrl != null) {
            characterIcon = new ImageIcon(imageUrl);
            // Масштабуємо зображення до потрібного розміру (60x165)
            Image image = characterIcon.getImage();
            Image scaledImage = image.getScaledInstance(60, 165, Image.SCALE_SMOOTH);
            characterIcon = new ImageIcon(scaledImage); // Створюємо новий ImageIcon зі масштабованим зображенням
        } else {
            System.err.println("Помилка: зображення '" + name + "' не знайдено в classpath за шляхом: " + imageResourcePath);
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
            selectedCharacterResourcePath = imageResourcePath;
            startGame();
        });

        characterPanel.add(selectButton, BorderLayout.SOUTH); // Розміщуємо знизу
        parentPanel.add(characterPanel);
    }

    private void startGame() {
        if (selectedCharacterName != null && selectedCharacterResourcePath != null) {
            this.dispose();
            SwingUtilities.invokeLater(() -> new GameFrame(selectedCharacterName, selectedCharacterResourcePath).setVisible(true)); // Передаємо ресурсний шлях
        } else {
            JOptionPane.showMessageDialog(this, "Будь ласка, оберіть персонажа!", "Помилка вибору", JOptionPane.WARNING_MESSAGE);
        }
    }

    public String getSelectedCharacterName() {
        return selectedCharacterName;
    }

    public String getSelectedCharacterResourcePath() { // Змінено назву
        return selectedCharacterResourcePath;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StartWindow().setVisible(true));
    }
}
