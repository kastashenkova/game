package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ExamMainWindow extends JPanel {
    private List<ExamTicket> tickets;
    private TicketSelectedListener listener;

    public interface TicketSelectedListener {
        void onTicketSelected(ExamTicket ticket);
    }
    public void setTicketSelectedListener(TicketSelectedListener listener) {
        this.listener = listener;
    }
    public ExamMainWindow(List<ExamTicket> tickets) {
        this.tickets = tickets;
        setLayout(new BorderLayout());
        setBackground(new Color(169, 225, 227));
        JLabel title = new JLabel("Оберіть білет", SwingConstants.CENTER);
        title.setFont(new Font("Sans-Serif", Font.BOLD, 26));
        title.setForeground(new Color(30, 30, 30));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(0, 3, 30, 30));
        buttonsPanel.setBackground(new Color(240, 244, 255));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        ImageIcon ticketImg = new ImageIcon(getClass().getResource("/assets/Exam/ticket.png"));
        Image scaledImg = ticketImg.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaledImg);

        for (int i = 0; i < tickets.size(); i++) {
            ExamTicket ticket = tickets.get(i);
            JButton ticketButton = new JButton("Білет " + (i + 1), icon);
            ticketButton.setHorizontalTextPosition(SwingConstants.CENTER);
            ticketButton.setVerticalTextPosition(SwingConstants.BOTTOM);
            ticketButton.setFont(new Font("Sans-Serif", Font.BOLD, 16));
            ticketButton.setBackground(new Color(74, 144, 226));
            ticketButton.setForeground(Color.WHITE);
            ticketButton.setFocusPainted(false);
            ticketButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
