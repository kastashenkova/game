package org.example;

import javax.swing.*;

/**
 * The Reminder class provides a static method to display reminder messages to the user
 * using a JOptionPane warning message dialog.
 */
class Reminder {
    /**
     * Displays a reminder message in a warning dialog.
     *
     * @param parent The parent JFrame for the dialog, determining its position.
     * @param message The message string to be displayed in the reminder.
     */
    public static void showReminder(JFrame parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Нагадування", JOptionPane.WARNING_MESSAGE);
    }
}