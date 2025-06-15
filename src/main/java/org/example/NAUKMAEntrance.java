package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class NAUKMAEntrance extends JPanel {

    private static final Color SIMS_LIGHT_PINK = new Color(255, 233, 243);
    private static final Color SIMS_MEDIUM_PINK = new Color(255, 212, 222);
    private static final Color SIMS_TURQUOISE = new Color(64, 224, 208);
    private static final Color SIMS_LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SIMS_DARK_TEXT = new Color(50, 50, 50);
    private final Color SIMS_BUTTON_HOVER = new Color(255, 240, 245);
    private static final Color SIMS_GREEN_CORRECT = new Color(144, 238, 144);
    private static final Color SIMS_RED_INCORRECT = new Color(255, 99, 71);

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Увімкнення згладжування для кращої якості зображення
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // Визначаємо висоту підлоги від нижнього краю панелі
        int floorLevelY = panelHeight - 250; // Збільшено висоту підлоги, щоб "горизонт" був вищим

        // --- Фон кімнати: Стіни та підлога ---
        // Стіни (світло-кремовий з легким градієнтом)
        GradientPaint wallGradient = new GradientPaint(0, 0, new Color(245, 245, 220),
                0, floorLevelY, new Color(230, 230, 210));
        g2d.setPaint(wallGradient);
        g2d.fillRect(0, 0, panelWidth, floorLevelY); // Стіни до рівня підлоги

        // Підлога (світло-бежевий з невеликим градієнтом та текстурою)
        GradientPaint floorGradient = new GradientPaint(0, floorLevelY, new Color(220, 220, 200),
                0, panelHeight, new Color(190, 190, 170));
        g2d.setPaint(floorGradient);
        g2d.fillRect(0, floorLevelY, panelWidth, panelHeight - floorLevelY); // Підлога від її рівня до низу панелі

        // Додаємо імітацію плитки на підлозі
        g2d.setColor(new Color(180, 180, 160, 80)); // Прозорий сіро-коричневий
        int tileSize = 30;
        for (int i = 0; i < panelWidth; i += tileSize) {
            g2d.drawLine(i, floorLevelY, i, panelHeight);
        }
        for (int i = floorLevelY; i < panelHeight; i += tileSize) {
            g2d.drawLine(0, i, panelWidth, i);
        }

        // --- Стеля ---
        g2d.setColor(new Color(220, 220, 200));
        g2d.fillRect(0, 0, panelWidth, 50); // Стеля

        // Центральний світильник (простий)
        g2d.setColor(new Color(255, 255, 200, 150)); // Світло-жовтий напівпрозорий
        g2d.fillOval(panelWidth / 2 - 50, 10, 100, 30);
        g2d.setColor(new Color(100, 100, 80));
        g2d.drawOval(panelWidth / 2 - 50, 10, 100, 30);

        // --- Вхід до НАУКМА ---
        int entranceWidth = 250; // Збільшена ширина дверей
        int entranceHeight = 350; // Збільшена висота дверей
        int entranceX = panelWidth / 2 - entranceWidth / 2 - 80; // Змінено: посунуто лівіше на 80 пікселів
        int entranceY = floorLevelY - entranceHeight; // Позиція відносно нового рівня підлоги

        // Зовнішня стіна входу
        g2d.setColor(new Color(80, 80, 80)); // Темніший сірий
        g2d.fill(new RoundRectangle2D.Double(entranceX - 15, entranceY - 15, entranceWidth + 30, entranceHeight + 30, 20, 20));

        // Рамка дверей
        g2d.setColor(new Color(120, 120, 120)); // Сірий
        g2d.fill(new RoundRectangle2D.Double(entranceX, entranceY, entranceWidth, entranceHeight, 15, 15));

        // Дерев'яні панелі (двері)
        Color woodColor1 = new Color(139, 69, 19); // Темно-коричневий
        Color woodColor2 = new Color(160, 82, 45); // Середньо-коричневий

        // Ліва дверна панель
        GradientPaint woodGradient1 = new GradientPaint(entranceX + 15, entranceY + 10, woodColor1,
                entranceX + entranceWidth / 2 - 5, entranceY + entranceHeight - 10, woodColor2);
        g2d.setPaint(woodGradient1);
        g2d.fill(new RoundRectangle2D.Double(entranceX + 15, entranceY + 10, entranceWidth / 2 - 20, entranceHeight - 20, 10, 10));

        // Права дверна панель
        GradientPaint woodGradient2 = new GradientPaint(entranceX + entranceWidth / 2 + 5, entranceY + 10, woodColor1,
                entranceX + entranceWidth - 15, entranceY + entranceHeight - 10, woodColor2);
        g2d.setPaint(woodGradient2);
        g2d.fill(new RoundRectangle2D.Double(entranceX + entranceWidth / 2 + 5, entranceY + 10, entranceWidth / 2 - 20, entranceHeight - 20, 10, 10));

        // Ручки дверей
        g2d.setColor(new Color(180, 180, 180));
        g2d.fill(new RoundRectangle2D.Double(entranceX + entranceWidth / 4 - 5, entranceY + entranceHeight / 2 - 30, 15, 60, 8, 8));
        g2d.fill(new RoundRectangle2D.Double(entranceX + entranceWidth * 3 / 4 - 10, entranceY + entranceHeight / 2 - 30, 15, 60, 8, 8));

        // Напис "НАУКМА" над входом
        g2d.setColor(new Color(30, 30, 30)); // Темніший колір
        g2d.setFont(new Font("Arial", Font.BOLD, 32)); // Більший шрифт
        FontMetrics fm = g2d.getFontMetrics();
        String naukmaText = "НАУКМА";
        int textWidth = fm.stringWidth(naukmaText);
        g2d.drawString(naukmaText, entranceX + entranceWidth / 2 - textWidth / 2, entranceY - 25);

        // --- Стіл охоронця ---
        int deskWidth = 400; // Ще більша ширина
        int deskHeight = 150; // Ще більша висота
        int deskDepth = 100; // Ще більша глибина для імітації 3D
        int deskX = panelWidth - deskWidth - 80; // Змінено позицію для більшого столу
        int deskY = floorLevelY - deskHeight; // Позиція відносно нового рівня підлоги

        // Тінь від столу
        g2d.setColor(new Color(0, 0, 0, 80)); // Більш помітна тінь
        g2d.fillOval(deskX + 40, floorLevelY - 30, deskWidth - 80, 50);

        // Задня частина столу (темніше)
        g2d.setColor(new Color(100, 50, 10)); // Темно-коричневий
        g2d.fillRect(deskX + deskDepth / 2, deskY, deskWidth - deskDepth, deskHeight);

        // Верхня поверхня столу
        GradientPaint deskTopGradient = new GradientPaint(deskX, deskY, new Color(160, 82, 45),
                deskX + deskWidth, deskY + deskDepth, new Color(120, 60, 20));
        g2d.setPaint(deskTopGradient);
        g2d.fill(new Polygon(new int[]{deskX, deskX + deskDepth, deskX + deskWidth, deskX + deskWidth - deskDepth},
                new int[]{deskY, deskY, deskY + deskHeight, deskY + deskHeight}, 4));

        // Передня частина столу
        g2d.setColor(new Color(139, 69, 19)); // Темно-коричневий
        g2d.fillRect(deskX, deskY + deskDepth, deskWidth, deskHeight - deskDepth);


        // --- Об'єкти на столі ---
        // Монітор
        g2d.setColor(new Color(50, 50, 50)); // Темно-сірий
        g2d.fill(new RoundRectangle2D.Double(deskX + 60, deskY - 60, 120, 90, 20, 20)); // Більший монітор
        g2d.setColor(new Color(100, 100, 100)); // Ніжка монітора
        g2d.fillRect(deskX + 115, deskY + 30, 10, 40);
        g2d.setColor(new Color(0, 100, 200)); // Екран (синій)
        g2d.fill(new RoundRectangle2D.Double(deskX + 65, deskY - 55, 110, 80, 15, 15));

        // Клавіатура
        g2d.setColor(new Color(80, 80, 80));
        g2d.fill(new RoundRectangle2D.Double(deskX + 200, deskY + 40, 120, 45, 10, 10)); // Більша клавіатура

        // Мишка
        g2d.setColor(new Color(80, 80, 80));
        g2d.fillOval(deskX + 330, deskY + 45, 25, 25); // Більша мишка

        // --- Крісло охоронця (пусте) ---
        int chairWidth = 120; // Збільшена ширина крісла
        int chairHeight = 160; // Збільшена висота крісла
        int chairX = deskX + 100; // Позиція відносно нового столу
        int chairY = floorLevelY - chairHeight + 30; // Позиція відносно нового рівня підлоги

        // Тінь від крісла
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillOval(chairX + 20, floorLevelY - 20, chairWidth - 40, 30);

        // Спинка крісла
        g2d.setColor(new Color(90, 90, 90)); // Темно-сірий
        g2d.fill(new RoundRectangle2D.Double(chairX, chairY, chairWidth, chairHeight - 60, 30, 30));

        // Сидіння крісла
        g2d.setColor(new Color(120, 120, 120)); // Світліший сірий
        g2d.fill(new Ellipse2D.Double(chairX, chairY + chairHeight - 80, chairWidth, 70)); // Більше сидіння

        // Ніжки крісла
        g2d.setColor(new Color(70, 70, 70));
        g2d.fillRect(chairX + 25, chairY + chairHeight - 30, 10, 35);
        g2d.fillRect(chairX + chairWidth - 35, chairY + chairHeight - 30, 10, 35);


        // --- Декорації на стінах ---
        // Проста картина/плакат
        g2d.setColor(new Color(180, 180, 180)); // Світла рамка
        g2d.fill(new RoundRectangle2D.Double(50, 80, 150, 100, 15, 15)); // Більша картина
        g2d.setColor(new Color(100, 150, 200)); // "Малюнок"
        g2d.fill(new RoundRectangle2D.Double(55, 85, 140, 90, 10, 10));

        // Годинник
        g2d.setColor(new Color(100, 100, 100)); // Рамка годинника
        g2d.fillOval(panelWidth - 180, 80, 80, 80); // Більший годинник
        g2d.setColor(Color.WHITE); // Циферблат
        g2d.fillOval(panelWidth - 175, 85, 70, 70);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(panelWidth - 140, 100, 2, 30); // Годинникова стрілка
        g2d.fillRect(panelWidth - 140, 100, 2, 40); // Хвилинна стрілка (трохи довша)
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        UIManager.put("nimbusBase", SIMS_MEDIUM_PINK);
        UIManager.put("nimbusBlueGrey", SIMS_LIGHT_BLUE);
        UIManager.put("control", SIMS_LIGHT_PINK);
        UIManager.put("textForeground", SIMS_DARK_TEXT);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Вхід до НаУКМА");
            NAUKMAEntrance panel = new NAUKMAEntrance();
            frame.add(panel);
            frame.setSize(1200, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null); // По центру екрана
            frame.setVisible(true);
        });
    }
}