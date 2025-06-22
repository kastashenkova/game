package mainstage;

import org.example.Hero;
import org.example.MusicPlayer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a simulation of a shop or a cafe frame where the player can buy food.
 */
public class ShopFrame extends JFrame {

    private final List<Product> inventory = new ArrayList<>();
    private Hero hero;

    /**
     * Constructs a {@code ShopFrame} for buying products.
     *
     * @param gameBoard The game board instance, used to access the hero.
     * @param products  A list of {@link Product} objects available for purchase.
     * @throws IOException If there is an error loading the frame icon.
     */
    public ShopFrame(GameBoard gameBoard, List<Product> products) throws IOException {
        setTitle("Продуктова станція");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(600, 400));

        BufferedImage icon = ImageIO.read(getClass().getResourceAsStream("/logo.png"));
        setIconImage(icon);

        hero = gameBoard.hero;

        JPanel productList = new JPanel();
        productList.setLayout(new BoxLayout(productList, BoxLayout.Y_AXIS));
        productList.setBackground(new Color(240, 250, 255));

        for (Product p : products) {
            productList.add(addProductPanel(p));
            productList.add(Box.createVerticalStrut(10)); // Add spacing between product panels
        }

        JScrollPane scrollPane = new JScrollPane(productList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Adjust scroll speed
        scrollPane.setBorder(null); // Remove default scroll pane border
        add(scrollPane, BorderLayout.CENTER);

        pack(); // Size the frame to fit its contents
        setLocationRelativeTo(null); // Center the frame on the screen
    }

    /**
     * Creates and returns a JPanel for a single product, including its image, name, price, nutrition info, and a buy button.
     *
     * @param product The {@link Product} for which to create the panel.
     * @return A {@link JPanel} representing the product.
     */
    private JPanel addProductPanel(Product product) {
        JPanel productPanel = new JPanel();
        productPanel.setLayout(new BorderLayout());
        productPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        productPanel.setBackground(Color.white);
        productPanel.setOpaque(true);

        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("Inter", Font.BOLD, 18));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel imageLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(product.getImagePath()));
            imageLabel.setIcon(new ImageIcon(icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            imageLabel.setText("❌"); // Display an 'X' if image fails to load
            System.err.println("Помилка під час завантаження зображення продукту " + product.getName() + ": " + e.getMessage());
        }
        JLabel infoLabel = new JLabel("<html>Ціна: " + product.getPrice() + " ₴<br>Поживність: +" + product.getNutrition() + " енергії</html>");
        infoLabel.setFont(new Font("MONOSPACED", Font.BOLD, 14));

        JButton selectButton = new JButton("Купити");
        selectButton.setBackground(new Color(238, 252, 252));
        selectButton.setBorder(new LineBorder(new Color(0, 107, 107), 3));
        selectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        selectButton.setFocusPainted(false);
        selectButton.addActionListener(e -> {
            MusicPlayer.getInstance().playButtonClick();
            if (hero.getBudget() >= product.getPrice()) {
                inventory.add(product);
                hero.decreaseHunger(product.getNutrition());
                hero.decreaseBudget(product.getPrice());
                JOptionPane.showMessageDialog(this, "Ви купили " + product.getName() + "!" + "\n" + "Голод: " + hero.getHunger() +
                        ". Поточний бюджет: " + hero.getBudget(), "УСПІХ", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Недостатньо коштів для покупки продукту " + product.getName() + "\n" + "! Поточний бюджет: "
                        + hero.getBudget(), "ВІДМОВА", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Arrange components within the product panel
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(imageLabel, BorderLayout.CENTER);

        JPanel center = new JPanel(new GridLayout(2, 1));
        center.setOpaque(false);
        center.add(nameLabel);
        center.add(infoLabel);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.add(selectButton);

        productPanel.add(left, BorderLayout.WEST);
        productPanel.add(center, BorderLayout.CENTER);
        productPanel.add(right, BorderLayout.EAST);

        return productPanel;
    }
}