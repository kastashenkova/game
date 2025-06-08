package org.example;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class GameFrame extends JFrame {
    private Hero hero;
    private GamePanel gamePanel;
    private JTextArea chatArea;
    private JTextField messageInputField;
    private JPanel chatPanel;
    private JPanel chatControlPanel;
    private int originalChatPanelWidth;
    private JButton toggleChatButton;
    private boolean chatExpanded = true;

    private final String initialHeroName;
    private final String initialHeroImagePath;
    private final String initialDiamondImagePath;
    private final int initialHeroX;
    private final int initialHeroY;
    private final double initialScaleFactor;

    public GameFrame(String heroName, String heroImagePath) {
        setTitle("NaUKMA Sims");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        this.initialHeroName = heroName;
        this.initialHeroImagePath = heroImagePath;
        this.initialDiamondImagePath = "C:\\Users\\Acer\\IdeaProjects\\game\\assets\\Models\\Hero\\diamond.png";
        this.initialHeroX = 350;
        this.initialHeroY = 250;
        this.initialScaleFactor = 0.4;

        hero = new Hero(initialHeroName, initialHeroImagePath, initialDiamondImagePath, initialHeroX, initialHeroY, initialScaleFactor);

        gamePanel = new GamePanel(hero, this);
        add(gamePanel, BorderLayout.CENTER);

        chatPanel = new JPanel(new BorderLayout());
        originalChatPanelWidth = 250;
        chatPanel.setPreferredSize(new Dimension(originalChatPanelWidth, 0));
        chatPanel.setBorder(BorderFactory.createTitledBorder("Чат КМА"));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        chatArea.setBackground(new Color(240, 240, 240));
        chatArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        messageInputField = new JTextField();
        messageInputField.setFont(new Font("Arial", Font.PLAIN, 12));
        inputPanel.add(messageInputField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Надіслати");
        sendButton.setFont(new Font("Arial", Font.BOLD, 12));
        sendButton.addActionListener(e -> sendMessage());
        inputPanel.add(sendButton, BorderLayout.EAST);

        messageInputField.addActionListener(e -> sendMessage());

        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        toggleChatButton = new JButton("<< Чат");
        toggleChatButton.setFont(new Font("Arial", Font.BOLD, 10));
        toggleChatButton.setMargin(new Insets(2, 2, 2, 2));
        toggleChatButton.addActionListener(e -> toggleChatPanel());

        chatControlPanel = new JPanel(new BorderLayout());
        chatControlPanel.add(toggleChatButton, BorderLayout.NORTH);
        chatControlPanel.add(chatPanel, BorderLayout.CENTER);

        chatControlPanel.setPreferredSize(new Dimension(originalChatPanelWidth, gamePanel.getPreferredSize().height));

        add(chatControlPanel, BorderLayout.EAST);

        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();

        setPreferredSize(new Dimension(gamePanel.getPreferredSize().width + originalChatPanelWidth, gamePanel.getPreferredSize().height));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        appendToChat("Система", "Ласкаво просимо до віртуальної Могилянки!");
    }

    public void appendToChat(String sender, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        chatArea.append("[" + sdf.format(new Date()) + "] " + sender + ": " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void clearChat() {
        chatArea.setText("");
        appendToChat("Система", "Гра перезапущена.");
    }

    private void sendMessage() {
        String message = messageInputField.getText().trim();
        if (!message.isEmpty()) {
            appendToChat("Ви", message);
            messageInputField.setText("");
        }
    }

    private void toggleChatPanel() {
        if (chatExpanded) {
            chatControlPanel.setPreferredSize(new Dimension(toggleChatButton.getPreferredSize().width, chatControlPanel.getHeight()));
            toggleChatButton.setText("Чат >>");
            chatPanel.setVisible(false);
            toggleChatButton.setToolTipText("Розгорнути чат");
        } else {
            chatControlPanel.setPreferredSize(new Dimension(originalChatPanelWidth, chatControlPanel.getHeight()));
            toggleChatButton.setText("<< Чат");
            chatPanel.setVisible(true);
            toggleChatButton.setToolTipText("Згорнути чат");
        }
        chatExpanded = !chatExpanded;
        revalidate();
        repaint();
    }

    public void handleGameOver(String reason) {
        dispose();
        SwingUtilities.invokeLater(() -> new StartWindow().setVisible(true));
    }
}