package org.example;

import javax.swing.*;
import java.awt.*;

/**
 * The `TicketSelectionPanel` class provides a graphical user interface component
 * for selecting a "ticket" (e.g., for a test or exam). It displays a grid of buttons,
 * each representing a different ticket number, and allows a listener to be notified
 * when a ticket is selected.
 */
public class TicketSelectionPanel extends JPanel {
    private TicketClickListener ticketClickListener;

    /**
     * An interface to be implemented by classes that want to be notified when a ticket is selected.
     */
    public interface TicketClickListener {
        /**
         * Called when a ticket button is clicked.
         *
         * @param ticketNumber The number of the selected ticket.
         */
        void onTicketSelected(int ticketNumber);
    }

    /**
     * Sets the listener for ticket selection events.
     *
     * @param listener The object that implements the {@link TicketClickListener} interface.
     */
    public void setTicketClickListener(TicketClickListener listener) {
        this.ticketClickListener = listener;
    }

    /**
     * Constructs a new `TicketSelectionPanel`.
     * Initializes the panel with a title and a grid of six ticket selection buttons.
     * Each button, when clicked, notifies the registered {@link TicketClickListener}.
     */
    public TicketSelectionPanel() {
        // Set the layout for the main panel to BorderLayout
        setLayout(new BorderLayout());
        // setBackground(new Color(245, 245, 255)); // Commented out background setting

        // Create and configure the title label
        JLabel title = new JLabel("Оберіть білет", SwingConstants.CENTER);
        title.setFont(new Font("Sans-Serif", Font.BOLD, 28)); // Set font and size
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // Add padding
        add(title, BorderLayout.NORTH); // Add title to the top of the panel

        // Create a panel to hold the ticket buttons in a grid layout
        JPanel ticketPanel = new JPanel();
        // GridLayout with 2 rows, 3 columns, and specified horizontal/vertical gaps
        ticketPanel.setLayout(new GridLayout(2, 3, 30, 30));
        ticketPanel.setBackground(new Color(236, 176, 228)); // Set background color for the ticket grid
        ticketPanel.setBorder(BorderFactory.createEmptyBorder(30, 60, 60, 60)); // Add padding around the grid

        // Loop to create and add 6 ticket buttons
        for (int i = 1; i <= 6; i++) {
            JButton ticketButton = new JButton();
            // JButton ticketButton = new JButton(icon); // Example of using an icon, currently commented out
            ticketButton.setText("Білет " + i); // Set button text
            ticketButton.setFont(new Font("Sans-Serif", Font.BOLD, 16)); // Set font
            ticketButton.setHorizontalTextPosition(SwingConstants.CENTER); // Center text horizontally
            ticketButton.setVerticalTextPosition(SwingConstants.CENTER); // Center text vertically
            ticketButton.setForeground(Color.WHITE); // Set text color to white
            ticketButton.setContentAreaFilled(false); // Make button area transparent
            ticketButton.setBorderPainted(false); // Do not paint button border
            ticketButton.setFocusPainted(false); // Do not paint focus border
            ticketButton.setToolTipText("Білет " + i); // Add tooltip for accessibility

            final int ticketNumber = i; // Store ticket number for the action listener
            ticketButton.addActionListener(e -> {
                // If a listener is registered, notify it about the selected ticket number
                if (ticketClickListener != null) {
                    ticketClickListener.onTicketSelected(ticketNumber);
                }
            });
            ticketPanel.add(ticketButton); // Add the button to the ticket grid panel
        }
        add(ticketPanel, BorderLayout.CENTER); // Add the ticket grid panel to the center of the main panel
    }
}