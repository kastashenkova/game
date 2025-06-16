package org.example;

import javax.swing.*;

//reminder of the exam day
class Reminder {
    public static void showReminder(JFrame parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Нагадування", JOptionPane.WARNING_MESSAGE);
    }
}