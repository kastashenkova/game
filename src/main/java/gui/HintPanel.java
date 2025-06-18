package gui;

import javax.swing.*;
import java.awt.*;

public class HintPanel extends JPanel {

    private JTextArea textArea;
    private String text;

    public HintPanel() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(218, 244, 252));
        setBorder(BorderFactory.createLineBorder(Color.BLUE));

        JLabel label = new JLabel("HINT MESSAGE!");
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

    public void setText(String text){
        this.text = text;
        this.textArea.setText(text);
    }

    public void setHint(int level){
        String nextHint1 = "Поточний етап: запис на дисципліни\n" +
                "- Запасайся терпінням...\n" +
                "- Готуй запасні дисципліни на випадок, якщо не встигнеш\n" +
                "- Дій швидко!\n" +
                "- Пауза, на жаль, тут не працює...";
        if(level==1) {
            setText(nextHint1);  return;
        } else if(level==2){
            String nextHint2 = "Вітаємо з прозодженням 1-го рівня!\n" +
                    "- Твій сім втомився - відпочинь\n" +
                    "- Підкріпись чимось\n" +
                    "- Почитай конспекти перед наступним рівнем\n" +
                    "- Постарайся набрати бали і облегчити сесію";
            setText(nextHint2);  return;
        } else if(level==3){
            String nextHint3 = "Вітаємо з прозодженням 2-го рівня!\n" +
                    "- Твій сім втомився - відпочинь\n" +
                    "- Підкріпись чимось\n" +
                    "- Гарно підготуйся перед останнім ривком!\n" +
                    "- Розважся для підняття настрою";
            setText(nextHint3);  return;
        }
        else {
            String nextHint4 = "Вітаємо з проходженням 3-го рівня!\n" +
                    "- Наразі наступних рівнів не передбачено\n" +
                    "- Для виходу на фінальне вікно натисніть відповідну кнопку \n" +
                    "- Незалежно від результатів, ми вас вітаємо з проходженням гри!\n" ;
            setText(nextHint4);  return;
        }

    }


}
