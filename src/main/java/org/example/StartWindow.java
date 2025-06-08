package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class StartWindow extends JFrame {

    private String selectedCharacterName;
    private String selectedCharacterImagePath;

    public StartWindow() {
        setTitle("Виберіть персонажа");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(600, 400)); // Розмір вікна вибору

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 4, 10, 10)); // 1 ряд, 4 колонки, з відступами
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(220, 230, 250)); // Легкий синій фон

        // Шляхи до зображень персонажів
        String oksanaPath = "C:\\Users\\Acer\\IdeaProjects\\game\\assets\\Models\\Hero\\Oksana.png";
        String oleksandraPath = "C:\\Users\\Acer\\IdeaProjects\\game\\assets\\Models\\Hero\\Oleksandra.png"; // Припустимо, у вас є ці файли
        String gabrielPath = "C:\\Users\\Acer\\IdeaProjects\\game\\assets\\Models\\Hero\\Gabriel.png";
        String sofiaPath = "C:\\Users\\Acer\\IdeaProjects\\game\\assets\\Models\\Hero\\Sofia.png";

        // Перевірка існування файлів зображень (допоможе уникнути помилок)
        if (!new File(oksanaPath).exists()) {
            System.err.println("Помилка: Файл Oksana.png не знайдено за шляхом: " + oksanaPath);
        }

        addCharacterPanel(mainPanel, "Оксана", oksanaPath);
        addCharacterPanel(mainPanel, "Олександра", oleksandraPath);
        addCharacterPanel(mainPanel, "Габріель", gabrielPath);
        addCharacterPanel(mainPanel, "Софія", sofiaPath);

        add(mainPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null); // Центрувати вікно на екрані
    }

    private void addCharacterPanel(JPanel parentPanel, String name, String imagePath) {
        JPanel charPanel = new JPanel();
        charPanel.setLayout(new BorderLayout());
        charPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        charPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        charPanel.add(nameLabel, BorderLayout.NORTH);

        ImageIcon icon = new ImageIcon(imagePath);
        Image image = icon.getImage();
        Image scaledImage = image.getScaledInstance(50, 150, Image.SCALE_SMOOTH); // Розмір зображення
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage), SwingConstants.CENTER);
        charPanel.add(imageLabel, BorderLayout.CENTER);

        JButton selectButton = new JButton("Обрати");
        selectButton.setFont(new Font("Arial", Font.BOLD, 14));
        selectButton.addActionListener(e -> {
            selectedCharacterName = name;
            selectedCharacterImagePath = imagePath;
            startGame();
        });
        charPanel.add(selectButton, BorderLayout.SOUTH);

        parentPanel.add(charPanel);
    }

    private void startGame() {
        if (selectedCharacterName != null && selectedCharacterImagePath != null) {
            // Закриваємо вікно вибору персонажа
            this.dispose();
            // Запускаємо гру з обраним персонажем
            SwingUtilities.invokeLater(() -> new GameFrame(selectedCharacterName, selectedCharacterImagePath));
        } else {
            JOptionPane.showMessageDialog(this, "Будь ласка, оберіть персонажа!", "Помилка вибору", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Методи для отримання обраного персонажа (не потрібні, якщо гра запускається безпосередньо)
    public String getSelectedCharacterName() {
        return selectedCharacterName;
    }

    public String getSelectedCharacterImagePath() {
        return selectedCharacterImagePath;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StartWindow().setVisible(true));
    }
}