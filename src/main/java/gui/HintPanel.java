package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Class for the small hints throughout the game.
 * Displays different hints depending on the player's level.
 */
public class HintPanel extends JPanel {

    private JTextArea textArea;
    private String text;

    /**
     * Default constructor that initializes the hint panel with UI components.
     * Sets up the layout, styling, and text area for displaying hints.
     */
    public HintPanel() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(218, 244, 252));
        setBorder(BorderFactory.createLineBorder(Color.BLUE));

        JLabel label = new JLabel("Повідомлення з натяками");
        label.setFont(new Font("MONOSPACED", Font.BOLD, 16));
        add(label, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("MONOSPACED", Font.PLAIN, 14));
        textArea.setOpaque(false);

        add(textArea, BorderLayout.CENTER);
    }

    /**
     * Sets a custom text for the hint panel.
     *
     * @param text the text to display in the hint panel
     */
    public void setText(String text){
        this.text = text;
        this.textArea.setText(text);
    }

    /**
     * Sets a predefined hint text based on the player's current level.
     * Each level has its own specific hint message with gameplay advice.
     *
     * @param level the current level of the player (1-4), determines which hint to display
     */
    public void setHint(int level){
        String nextHint1 = "Поточний етап: запис на дисципліни\n" +
                "— Запасайся терпінням...\n" +
                "— Готуй запасні дисципліни на випадок, якщо не встигнеш.\n" +
                "— Дій швидко!\n" +
                "— Павза, на жаль, тут не працює...";
        if(level==1) {
            setText(nextHint1);  return;
        } else if(level==2){
            String nextHint2 = "Вітаємо з проходженням 1-го рівня!\n" +
                    "— Твій сім втомився — відпочинь.\n" +
                    "— Підкріпись чимось.\n" +
                    "— Почитай конспекти перед наступним рівнем.\n" +
                    "— Постарайся набрати бали та полегшити сесію.";
            setText(nextHint2);  return;
        } else if(level==3){
            String nextHint3 = "Вітаємо з проходженням 2-го рівня!\n" +
                    "— Твій сім втомився — відпочинь.\n" +
                    "— Підкріпись чимось.\n" +
                    "— Гарно підготуйся перед останнім ривком!\n" +
                    "— Розважся для піднесення настрою.";
            setText(nextHint3);  return;
        }
        else {
            String nextHint4 = "Вітаємо з проходженням 3-го рівня!\n" +
                    "— Наразі наступних рівнів не передбачено.\n" +
                    "— Для виходу на фінальне вікно натисніть відповідну кнопку.\n" +
                    "— Незалежно від результатів, ми вас вітаємо з проходженням гри!\n" ;
            setText(nextHint4);  return;
        }
    }
}