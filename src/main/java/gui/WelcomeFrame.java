package gui;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.MusicPlayer;
import org.example.StartWindow;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.net.URL;

/**
 * The WelcomeFrame class represents the main welcome window of the Sims NaUKMA application.
 * It displays a main menu with options to start a new game, access settings, or exit the application.
 * It also includes background music, animated GIFs, and custom button styling.
 */
public class WelcomeFrame extends JFrame implements ActionListener {
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem newGameItem, exitItem;
    JButton startButton;
    JButton optionsButton;
    JButton quitButton;
    BufferedImage icon;

    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);

    /**
     * Constructs a new WelcomeFrame.
     * Initializes the frame properties, sets up the menu bar,
     * adds background music, animated GIFs, and the main action buttons.
     */
    public WelcomeFrame() {
        // Set custom text for OptionPane buttons
        UIManager.put("OptionPane.yesButtonText", "Так");
        UIManager.put("OptionPane.noButtonText", "Ні");
        UIManager.put("OptionPane.cancelButtonText", "Скасувати");

        setTitle("Sims NaUKMA");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null); // Center the frame on the screen
        setLayout(new BorderLayout());

        // Initialize and play background music
        MusicPlayer.getInstance().setMusicEnabled(true);
        MusicPlayer.getInstance().playMusic("/assets/Sounds/welcome.wav");

        // Set background panel with an image
        BackgroundPanel backgroundPanel = new BackgroundPanel(getClass().getResource("/backMain.png").getFile());
        setContentPane(backgroundPanel);

        // Set up the menu bar
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        newGameItem = new JMenuItem("Нова гра");
        exitItem = new JMenuItem("Вийти");

        fileMenu.add(newGameItem);
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        // Add action listeners for menu items
        newGameItem.addActionListener(this);
        exitItem.addActionListener(this);

        // Add animated GIFs to the background panel
        try {
            URL url = getClass().getResource("/sims.gif");
            ImageIcon icon = new ImageIcon(url);
            JLabel gifLabel = new JLabel(icon, SwingConstants.CENTER);
            gifLabel.setBounds(0, 500, 200, 184); // Manually set bounds for absolute positioning
            backgroundPanel.add(gifLabel);
        } catch (Exception e) {
            // Handle exception if GIF cannot be loaded
            System.err.println("Could not load /sims.gif: " + e.getMessage());
        }
        try {
            URL url = getClass().getResource("/study.gif");
            // Using HTML to control GIF size within JLabel
            String html = "<html><body><img src='" + url + "' width='150' height='120'></body></html>";
            JLabel gifLabel = new JLabel(html, SwingConstants.CENTER);
            gifLabel.setBounds(800, 0, 200, 184); // Manually set bounds for absolute positioning
            backgroundPanel.add(gifLabel);
        } catch (Exception e) {
            // Handle exception if GIF cannot be loaded
            System.err.println("Could not load /study.gif: " + e.getMessage());
        }


        // Set up the main panel for buttons
        JPanel mainPanel = new JPanel(new GridBagLayout());
        startButton = createButton("start");
        optionsButton = createButton("settings");
        quitButton = createButton("exit");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10); // Padding around buttons
        mainPanel.add(startButton, gbc);

        gbc.gridy = 1;
        mainPanel.add(optionsButton, gbc);

        gbc.gridy = 2;
        mainPanel.add(quitButton, gbc);
        mainPanel.setOpaque(false); // Make the panel transparent to show background

        add(mainPanel, BorderLayout.CENTER);

        // Add action listeners for the main buttons
        startButton.addActionListener(this);
        optionsButton.addActionListener(this);
        quitButton.addActionListener(this);

        // Load and set the frame icon
        try {
            icon = loadImage("/logo.png");
        } catch (IOException e) {
            System.err.println("Could not load /logo.png: " + e.getMessage());
        }
        setIconImage(icon);
        setVisible(true);
    }

    /**
     * Loads an image from the specified path.
     *
     * @param path The path to the image resource.
     * @return A BufferedImage object representing the loaded image.
     * @throws IOException If the image cannot be read.
     */
    private BufferedImage loadImage(String path) throws IOException {
        return ImageIO.read(getClass().getResourceAsStream(path));
    }

    /**
     * Handles action events generated by buttons and menu items.
     *
     * @param e The ActionEvent object.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            // Handle start button click: stop music, dispose current frame, show loading frame, then start new game
            SwingUtilities.invokeLater(() -> {
                MusicPlayer.getInstance().setMusicEnabled(false);
                dispose();
                LoadingFrame loading = new LoadingFrame();
                loading.startLoading(() -> new StartWindow().setVisible(true));
            });
        } else if (e.getSource() == quitButton || e.getSource() == exitItem) {
            // Handle quit button or exit menu item click: play sound and exit application
            MusicPlayer.getInstance().playButtonClick();
            System.exit(0);
        } else if (e.getSource() == optionsButton) {
            // Handle options button click: play sound and show options frame
            SwingUtilities.invokeLater(() -> {
                MusicPlayer.getInstance().playButtonClick();
                new OptionsFrame().setVisible(true);
            });
        } else if (e.getSource() == newGameItem) {
            // Handle new game menu item click (same as start button)
            SwingUtilities.invokeLater(() -> {
                MusicPlayer.getInstance().setMusicEnabled(false);
                dispose();
                LoadingFrame loading = new LoadingFrame();
                loading.startLoading(() -> new StartWindow().setVisible(true));
            });
        }
    }

    /**
     * Creates a formatted JButton with custom icons for normal, hover, and pressed states.
     * The button uses an image as its icon and is made transparent.
     *
     * @param text The base name for the button's image file (e.g., "start" for "start.png").
     * @return A configured JButton.
     */
    private JButton createButton(String text) {
        String filePath = "/button/" + text + ".png";
        JButton button = new JButton();
        ImageIcon icon = new ImageIcon(getClass().getResource(filePath));

        // Scale the image for the button
        Image scaledImage = icon.getImage().getScaledInstance(190, 50, Image.SCALE_SMOOTH);
        icon = new ImageIcon(scaledImage);

        // Create darkened versions for hover and pressed states
        ImageIcon hoverIcon = darkenIcon(icon, 0.85f); // Slightly darker
        ImageIcon pressedIcon = darkenIcon(icon, 0.65f); // Much darker

        button.setIcon(icon);
        button.setRolloverIcon(hoverIcon); // Icon when mouse hovers over
        button.setPressedIcon(pressedIcon); // Icon when button is pressed

        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Change cursor to hand on hover
        button.setContentAreaFilled(false); // Do not paint the content area (make it transparent)
        button.setBorderPainted(false); // Do not paint the border
        button.setFocusPainted(false); // Do not paint the focus border
        button.setOpaque(false); // Make the button itself transparent

        return button;
    }

    /**
     * Darkens an ImageIcon by applying a rescale operation to its RGB channels.
     *
     * @param originalIcon The original ImageIcon to darken.
     * @param darknessFactor A float value between 0.0f and 1.0f, where smaller values
     * result in more darkness (e.g., 0.85f for 85% brightness).
     * @return A new ImageIcon that is a darkened version of the original.
     */
    private ImageIcon darkenIcon(ImageIcon originalIcon, float darknessFactor) {
        // Create a BufferedImage from the ImageIcon
        BufferedImage original = new BufferedImage(
                originalIcon.getIconWidth(),
                originalIcon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = original.createGraphics();
        originalIcon.paintIcon(null, g, 0, 0);
        g.dispose();

        // Create a RescaleOp to darken the image
        // The array specifies scaling factors for R, G, B, and Alpha channels
        RescaleOp rescaleOp = new RescaleOp(
                new float[]{darknessFactor, darknessFactor, darknessFactor, 1f}, // Scale RGB, keep Alpha
                new float[4], null); // No offsets

        // Apply the filter to create the darkened image
        BufferedImage darkened = rescaleOp.filter(original, null);

        return new ImageIcon(darkened);
    }

    /**
     * The main method to start the application.
     * Sets the look and feel using FlatLaf and then creates and displays the WelcomeFrame.
     *
     * @param args Command line arguments (not used).
     * @throws UnsupportedLookAndFeelException If the FlatLaf look and feel is not supported.
     */
    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        // Set custom UI manager properties for Nimbus (though FlatLaf is used, these might affect some components)
        UIManager.put("nimbusBase", SIMS_MEDIUM_PINK);
        UIManager.put("nimbusBlueGrey", SIMS_LIGHT_BLUE);
        UIManager.put("control", SIMS_LIGHT_PINK);
        UIManager.put("textForeground", SIMS_DARK_TEXT);

        // Set the FlatLaf Light theme for a modern look
        UIManager.setLookAndFeel(new FlatLightLaf());

        // Create and show the WelcomeFrame on the Event Dispatch Thread
        SwingUtilities.invokeLater(WelcomeFrame::new);
    }
}