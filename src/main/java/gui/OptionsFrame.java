package gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.example.MusicPlayer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class OptionsFrame extends JFrame {
    private JSlider volumeSlider;
    private JButton themeButton;
    private JButton instructionButton;
    boolean isDark = false;

    private int currentVolume = 50;
        public OptionsFrame() {
            setTitle("Налаштування гри");
            setSize(400, 300);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            panel.setBackground(new Color(241, 227, 253));

            JLabel volumeLabel = new JLabel("Гучність музики:");
            volumeSlider = new JSlider(0, 100, currentVolume);
            volumeSlider.setUI(new CustomSlider(volumeSlider));
            volumeSlider.setPaintTicks(true);
            volumeSlider.setMajorTickSpacing(25);
            volumeSlider.setMinorTickSpacing(5);
            volumeSlider.setPaintLabels(true);
            volumeSlider.setFont(new Font("Arial", Font.PLAIN, 10));

            volumeSlider.addChangeListener(e -> {

                    currentVolume = volumeSlider.getValue();
                    MusicPlayer.getInstance().setVolume(currentVolume);

            });

            themeButton = createButton("Змінити тему");
            themeButton.addActionListener(e -> {
                MusicPlayer.getInstance().playButtonClick();
                if(!isDark) {
                    try {
                        UIManager.setLookAndFeel(new FlatDarkLaf());
                        isDark = true;
                    } catch (UnsupportedLookAndFeelException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    try {
                        UIManager.setLookAndFeel(new FlatLightLaf());
                        isDark = false;
                    } catch (UnsupportedLookAndFeelException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                SwingUtilities.updateComponentTreeUI(this);
                JOptionPane.showMessageDialog(this,
                        isDark ? "Темна тема активована!" : "Світла тема активована!", "THEME UPDATED", JOptionPane.INFORMATION_MESSAGE);

            });


            panel.add(volumeLabel);
            panel.add(volumeSlider, BorderLayout.CENTER);
            panel.add(themeButton, BorderLayout.NORTH);

            instructionButton = getInstructionButton();
            panel.add(instructionButton, BorderLayout.PAGE_END);
            add(panel);

        }
    private JButton createButton(String text) {

        JButton button = new JButton();
        button.setBackground(new Color(164, 183, 253));
        button.setText(text);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }
    private JButton getInstructionButton(){
        instructionButton = new JButton("Інструкції та поради");
        instructionButton.setBackground(new Color(78, 90, 205));
        instructionButton.setForeground(Color.WHITE);
        instructionButton.setFont(new Font("Arial", Font.BOLD, 12));
        instructionButton.setFocusPainted(false);
        instructionButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        instructionButton.setBorder(new LineBorder(Color.WHITE, 2, true)); // true = округлі кути
        instructionButton.addActionListener(e -> {

            SwingUtilities.invokeLater(() -> {

                MusicPlayer.getInstance().playButtonClick();
                InstructionDialog dialog = new InstructionDialog(this);
                dialog.setVisible(true);

            });
        });

        return instructionButton;

}

}

class InstructionDialog extends JDialog {

    public InstructionDialog(JFrame parent) {
        super(parent, "Як вижити в НаУКМА", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JTextArea instructions = new JTextArea();
        instructions.setBackground(new Color(104, 183, 213));
        instructions.setEditable(false);
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setFont(new Font("MONOSPACED", Font.BOLD, 14));
        instructions.setText(
                """
                🎓 Вітаємо в «Сімс НаУКМА»!

                Тут ти управлятимеш сімом, якому треба:
                - вижити між парами, дедлайнами і лекціями;
                - боротися із САЗом, зібрати кредити, не втративши розум;
                - встигнути все, окрім сну.
                
                📋 Основні поради:
                • Слідкуй за потребами: їсти, спати, і хоча б іноді відвідувати пари.
                • Не витрачай усі гроші одразу(по можливості), стипендію ще треба заробити.
                • Бали - це важливо. Бо так можна і до третього рівня не дійти.
                • Для підняття настрою шукай колесо огляду!
                
                ☕ Лайфхаки:
                •Тут можна ставити все на паузу. Крім САЗу. САЗ пауз не пробачає.).
                •Краще попотіти на другому рівні, аніж складати усі іспити на сесії.
                •І хоча тобі може так не здаватися, але сон - це важливо!
               
                Успіхів та натхнення!
                """
        );

        JScrollPane scrollPane = new JScrollPane(instructions);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton closeButton = new JButton("Зрозуміло");

        closeButton.addActionListener(e -> {
            MusicPlayer.getInstance().playButtonClick();
            dispose();
        });
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
class CustomSlider extends BasicSliderUI {
    Image im;

    public CustomSlider(JSlider js) {
        super(js);
    }

    @Override
    public void paintThumb(Graphics g) {
        try {
            if (im == null) {
                URL url = getClass().getResource("/button/slider.png");
                if (url != null) {
                    im = ImageIO.read(url);
                } else {
                    System.err.println("Не знайдено ресурс /button/slider.png");
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Rectangle thumb = thumbRect;
        int width = thumb.width;
        int height = thumb.height;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(im, thumb.x, thumb.y, width, height, null);
        g2.dispose();
    }
}
