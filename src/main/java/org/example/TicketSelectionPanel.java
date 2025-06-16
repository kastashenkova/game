package org.example;

import javax.swing.*;
import java.awt.*;

//клас для вибірки білету
public class TicketSelectionPanel extends JPanel {
    private TicketClickListener ticketClickListener;

    public interface TicketClickListener{
        void onTicketSelected(int ticketNumber);
    }
    public void setTicketClickListener(TicketClickListener listener) {
        this.ticketClickListener = listener;
    }
    public TicketSelectionPanel() {
        setLayout(new BorderLayout());
        //setBackground(new Color(245, 245, 255));
        JLabel title = new JLabel("Оберіть білет", SwingConstants.CENTER);
        title.setFont(new Font("Sans-Serif", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);
        JPanel ticketPanel = new JPanel();
        ticketPanel.setLayout(new GridLayout(2, 3, 30, 30));
        ticketPanel.setBackground(new Color(236, 176, 228));
        ticketPanel.setBorder(BorderFactory.createEmptyBorder(30, 60, 60, 60));

//        ImageIcon ticketImg = new ImageIcon("C:\\Users\\User\\IdeaProjects\\test\\ticket.png");
//        Image scaledImg = ticketImg.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH);
//        ImageIcon icon = new ImageIcon(scaledImg);
        for (int i = 1; i <= 6; i++) {
            JButton ticketButton = new JButton();
            //JButton ticketButton = new JButton(icon);
            ticketButton.setText("Білет " + i);
            ticketButton.setFont(new Font("Sans-Serif", Font.BOLD, 16));
            ticketButton.setHorizontalTextPosition(SwingConstants.CENTER);
            ticketButton.setVerticalTextPosition(SwingConstants.CENTER);
            ticketButton.setForeground(Color.WHITE);
            ticketButton.setContentAreaFilled(false);
            ticketButton.setBorderPainted(false);
            ticketButton.setFocusPainted(false);
            ticketButton.setToolTipText("Білет " + i);
            int ticketNumber = i;
            ticketButton.addActionListener(e -> {
                if (ticketClickListener != null) {
                    ticketClickListener.onTicketSelected(ticketNumber);}
            });
            ticketPanel.add(ticketButton);
        }
        add(ticketPanel, BorderLayout.CENTER);
    }
}
