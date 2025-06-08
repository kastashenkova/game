package org.example;

import org.example.StartWindow;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class GameFrame extends JFrame {
    private Hero hero;
    private GamePanel gamePanel; // Оголошуємо тут
    private JTextArea chatArea;
    private JTextField messageInputField;
    private JPanel chatPanel;
    private JPanel chatControlPanel;
    private int originalChatPanelWidth;
    private JButton toggleChatButton;
    private boolean chatExpanded = true;

    public GameFrame(String heroName, String heroImagePath) {
        setTitle("2D Hero Game (Swing)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        String diamondImagePath = "C:\\Users\\Acer\\IdeaProjects\\game\\assets\\Models\\Hero\\diamond.png";

        double scaleFactor = 0.4;

        hero = new Hero(heroName, heroImagePath, diamondImagePath, 350, 250, scaleFactor);

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

        toggleChatButton = new JButton("<<");
        toggleChatButton.setFont(new Font("Arial", Font.BOLD, 10));
        toggleChatButton.setMargin(new Insets(2, 2, 2, 2));
        toggleChatButton.addActionListener(e -> toggleChatPanel());

        chatControlPanel = new JPanel(new BorderLayout());
        chatControlPanel.add(toggleChatButton, BorderLayout.NORTH);
        chatControlPanel.add(chatPanel, BorderLayout.CENTER);

        chatControlPanel.setPreferredSize(new Dimension(originalChatPanelWidth, gamePanel.getPreferredSize().height));
        chatControlPanel.setMinimumSize(new Dimension(0, 0));
        chatControlPanel.setMaximumSize(new Dimension(originalChatPanelWidth, Short.MAX_VALUE));

        add(chatControlPanel, BorderLayout.EAST);

        // Встановлюємо початковий розмір фрейму
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
            chatControlPanel.setMinimumSize(new Dimension(toggleChatButton.getPreferredSize().width, chatControlPanel.getHeight()));
            chatControlPanel.setMaximumSize(new Dimension(toggleChatButton.getPreferredSize().width, Short.MAX_VALUE));
            toggleChatButton.setText("Чат >>");
            chatPanel.setVisible(false);
            toggleChatButton.setToolTipText("Розгорнути чат");
        } else {
            chatControlPanel.setPreferredSize(new Dimension(originalChatPanelWidth, chatControlPanel.getHeight()));
            chatControlPanel.setMinimumSize(new Dimension(originalChatPanelWidth, chatControlPanel.getHeight()));
            chatControlPanel.setMaximumSize(new Dimension(originalChatPanelWidth, Short.MAX_VALUE));
            toggleChatButton.setText("<< Чат");
            chatPanel.setVisible(true);
            toggleChatButton.setToolTipText("Згорнути чат");
        }
        chatExpanded = !chatExpanded;
        SwingUtilities.getWindowAncestor(chatControlPanel).revalidate();
        SwingUtilities.getWindowAncestor(chatControlPanel).repaint();
    }
}