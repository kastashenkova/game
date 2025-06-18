package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Represents the main window for the exam session, allowing the player to select an exam ticket.
 * This panel displays a list of available exam tickets as buttons, each allowing the user
 * to start an exam with the corresponding ticket.
 */
public class ExamMainWindow extends JPanel {
    private List<ExamTicket> tickets;
    private TicketSelectedListener listener;

    /**
     * An interface to be implemented by classes that want to be notified when an exam ticket is selected.
     */
    public interface TicketSelectedListener {
        /**
         * Called when an exam ticket has been selected by the user.
         *
         * @param ticket The {@link ExamTicket} that was selected.
         */
        void onTicketSelected(ExamTicket ticket);
    }

    /**
     * Sets the listener that will be notified when an exam ticket is selected.
     *
     * @param listener The {@link TicketSelectedListener} to be set.
     */
    public void setTicketSelectedListener(TicketSelectedListener listener) {
        this.listener = listener;
    }

    /**
     * Constructs a new {@code ExamMainWindow}.
     * Initializes the panel, sets its layout and background, and creates buttons for each available exam ticket.
     * Each button, when clicked, notifies the registered {@code TicketSelectedListener}.
     *
     * @param tickets A {@link List} of {@link ExamTicket} objects to be displayed for selection.
     */
    public ExamMainWindow(List<ExamTicket> tickets) {
        this.tickets = tickets;
        setLayout(new BorderLayout());
        setBackground(new Color(169, 225, 227)); // Sets a light blue background for the panel

        // Title Label
        JLabel title = new JLabel("Оберіть білет", SwingConstants.CENTER);
        title.setFont(new Font("Sans-Serif", Font.BOLD, 26));
        title.setForeground(new Color(30, 30, 30)); // Dark gray text color
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // Padding around the title
        add(title, BorderLayout.NORTH);

        // Panel for ticket buttons
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(0, 3, 30, 30)); // Grid layout with 3 columns and spacing
        buttonsPanel.setBackground(new Color(240, 244, 255)); // Light blue-gray background for button panel
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); // Padding around the button panel

        // Load and scale ticket icon
        ImageIcon ticketImg = new ImageIcon(getClass().getResource("/assets/Exam/ticket.png"));
        Image scaledImg = ticketImg.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaledImg);

        // Create and add buttons for each ticket
        for (int i = 0; i < tickets.size(); i++) {
            ExamTicket ticket = tickets.get(i);
            JButton ticketButton = new JButton("Білет " + (i + 1), icon);
            ticketButton.setHorizontalTextPosition(SwingConstants.CENTER);
            ticketButton.setVerticalTextPosition(SwingConstants.BOTTOM);
            ticketButton.setFont(new Font("Sans-Serif", Font.BOLD, 16));
            ticketButton.setBackground(new Color(74, 144, 226)); // Blue background for buttons
            ticketButton.setForeground(Color.WHITE); // White text color for buttons
            ticketButton.setFocusPainted(false); // Removes the focus border
            ticketButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding inside buttons

            // Add action listener to notify the listener when a button is clicked
            ticketButton.addActionListener(e -> {
                if (listener != null) {
                    listener.onTicketSelected(ticket);
                }
            });
            buttonsPanel.add(ticketButton);
        }
        add(buttonsPanel, BorderLayout.CENTER);
    }
}