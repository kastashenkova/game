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

public class ShopFrame extends JFrame {


    private final List<Product> inventory = new ArrayList<>();
    private Hero hero;


    public ShopFrame(GameBoard gameBoard, List<Product> products) throws IOException {

        setTitle("Food Station");
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
            productList.add(Box.createVerticalStrut(10));
        }

        JScrollPane scrollPane = new JScrollPane(productList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        pack();

        setLocationRelativeTo(null);
    }

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
            imageLabel.setText("❌");
        }
        JLabel infoLabel = new JLabel("<html>Ціна: " + product.getPrice() + " ₴<br>Поживність: +" + product.getNutrition() + " енергії</html>");
        infoLabel.setFont(new Font("MONOSPACED", Font.BOLD, 14));


        JButton selectButton = new JButton("Купити");
        selectButton.setBackground(new Color(238, 252, 252));
        selectButton.setBorder(new LineBorder(new Color(0, 107, 107), 3));
        selectButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Курсор-рука при наведенні
        selectButton.setFocusPainted(false);
        selectButton.addActionListener(e -> {
            MusicPlayer.getInstance().playButtonClick();
            if(hero.getBudget() >= product.getPrice()) {
                inventory.add(product);
                hero.decreaseHunger(product.getNutrition());
                hero.decreaseBudget(product.getPrice());
                JOptionPane.showMessageDialog(this, "Ви купили " + product.getName() + "!" + "\n" +"Голод: " + hero.getHunger() +
                        ". Поточний бюджет: " + hero.getBudget(), "SUCCESS", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Недостатньо коштів для покупки продукту " + product.getName() + "\n" + "! Поточний бюджет: "
                                + hero.getBudget(), "DECLINE", JOptionPane.WARNING_MESSAGE);

            }

        });
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
