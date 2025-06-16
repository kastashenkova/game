package gui;

import javax.swing.*;
import java.awt.*;

public class HintPanel extends JPanel {

    private JTextArea textArea;
    private String text;

    public HintPanel(String hintText) {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(218, 244, 252));
        setBorder(BorderFactory.createLineBorder(Color.BLUE));

        JLabel label = new JLabel("HINT MESSAGE!");
        label.setFont(new Font("MONOSPACED", Font.BOLD, 16));
        add(label, BorderLayout.NORTH);

        textArea = new JTextArea(hintText);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("MONOSPACED", Font.PLAIN, 14));
        textArea.setOpaque(false);

        add(textArea, BorderLayout.CENTER);
    }

    public void setText(String text){
        this.text = text;
        this.textArea.setText(text);
    }
}
