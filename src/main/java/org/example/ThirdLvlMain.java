package org.example;

import javax.swing.*;
import java.util.List;
//тут через мейн поки запускається вікно з самими білетами та питаннями, треба зв'язати з першим вікном exam session
public class ThirdLvlMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Білети до екзамену");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            List<ExamTicket> tickets = ExamTicket.generateSampleTickets();
            ExamMainWindow window = new ExamMainWindow(tickets);
            window.setTicketSelectedListener(ticket -> {
                frame.setContentPane(new Exam(ticket, frame));
                frame.revalidate();
            });
            frame.setContentPane(window);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

