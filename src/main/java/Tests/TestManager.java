package Tests;

import gui.LoadingFrame;
import org.example.Discipline;
import org.example.GameFrame;
import org.example.Hero;
import org.example.Student;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestManager {

    private Hero hero;
    private Student student;

    private List<Discipline> examDisciplines;

    public TestManager(Hero hero) {

        this.hero = hero;
        this.student = hero.getStudent();
        this.examDisciplines = student.getEnrolledDisciplines();
        System.out.println(examDisciplines.size());

    }

    public void generateForAll(){
        for (Discipline discipline : examDisciplines) {
            generateQuestions(discipline);
        }
    }
    public void startTest() {
        List<Discipline> selected = getThreeRandomDisciplines();
        generateQuestions(selected.get(0));
        generateQuestions(selected.get(1));
        showNextTestWithLoading(selected, 0);
    }

    private List<Discipline> getThreeRandomDisciplines() {
        List<Discipline> copy = new ArrayList<>(examDisciplines);
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(3, copy.size()));
    }

    private void showNextTestWithLoading(List<Discipline> selected, int index) {
        if (index >= selected.size()) {
            JOptionPane.showMessageDialog(null, "Ви пройшли всі тести!", "Успіх", JOptionPane.INFORMATION_MESSAGE);
            SwingUtilities.invokeLater(() -> {
                LoadingFrame loading = new LoadingFrame();
                loading.startLoading(() -> {
                    hero.levelUp();
                    hero.setLevel(3);
                    hero.setStudent(student);
                    hero.decreaseEnergy(40);
                    hero.decreaseHunger(-30);
                    GameFrame gameFrame =  new GameFrame(hero);
                    String nextHint = "Вітаємо з прозодженням 2-го рівня!\n" +
                            "- Твій сім втомився - відпочинь\n" +
                            "- Підкріпись чимось\n" +
                            "- Гарно підготуйся перед останнім ривком!\n" +
                            "- Вір у свою удачу";
                    gameFrame.getGamePanel().getHintPanel().setText(nextHint);
                    gameFrame.setVisible(true);
                });
            });
            return;
        }

        Discipline current = selected.get(index);
        generateQuestions(current);
        LoadingFrame loading = new LoadingFrame();
        loading.startLoading(() -> {
            SwingUtilities.invokeLater(() -> {
                MainTestFrame testFrame = new MainTestFrame(current, () -> {
                    while (true) {
                        int result = JOptionPane.showOptionDialog(
                                null,
                                "Час переходити до наступного тесту!\nНатисніть ТАК, коли будете готові.",
                                "Наступний тест",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                new Object[]{"Так"}, // тільки одна кнопка
                                "Так"
                        );
                        if (result == 0) {
                            break; // вийти з циклу, якщо натиснуто "Так"
                        }
                    }
                    showNextTestWithLoading(selected, index + 1);
                });
                testFrame.setVisible(true);
            });
        });
    }


    public void generateQuestions(Discipline discipline) {
            if (discipline.getName().equals("Забезпечення якості доступу програмних продуктів"))
                discipline.questions = new Question[]{
                        new Question("1. Що таке забезпечення якості (QA) у розробці ПЗ?",
                                new String[]{"Написання коду", "Встановлення драйверів", "Сукупність процесів для гарантування якості продукту", "Розгортання сайту"}, 2),
                        new Question("2. Що таке тестування програмного забезпечення?",
                                new String[]{"Встановлення оновлень", "Процес перевірки відповідності вимогам", "Написання документації", "Архівування даних"}, 1),
                        new Question("3. Який тип тестування перевіряє функціональність програми згідно з вимогами?",
                                new String[]{"Тестування продуктивності", "Інтеграційне тестування", "Функціональне тестування", "Ручне тестування"}, 2),
                        new Question("4. Що таке accessibility (доступність) у ПЗ?",
                                new String[]{"Підключення до Wi-Fi", "Можливість доступу до коду", "Пристосування інтерфейсу до потреб користувачів з інвалідністю", "Обмін файлами"}, 2),
                        new Question("5. Який стандарт часто використовується для забезпечення якості ПЗ?",
                                new String[]{"IEEE 802.11", "ISO 9001", "HTML5", "CSS3"}, 1),
                        new Question("6. Що таке тест-кейс?",
                                new String[]{"Клас для створення даних", "Файл з кодом", "Опис вхідних даних, дій і очікуваного результату", "Список тестерів"}, 2),
                        new Question("7. Яке тестування виконується без знання внутрішньої структури коду?",
                                new String[]{"Біле тестування", "Ручне тестування", "Чорне тестування", "Функціональне"}, 2),
                        new Question("8. Яка мета юзабіліті-тестування?",
                                new String[]{"Перевірити безпеку", "Оцінити зручність використання", "Знайти помилки в коді", "Виявити віруси"}, 1),
                        new Question("9. Що таке баг (bug)?",
                                new String[]{"Файл зі стилями", "Помилка в програмі", "Тестувальник", "Метод шифрування"}, 1),
                        new Question("10. Що таке CI/CD у тестуванні?",
                                new String[]{"Мова програмування", "Інтерфейс командного рядка", "Безперервна інтеграція та розгортання", "Ручне оновлення драйверів"}, 2),
                        new Question("11. Який із принципів доступності говорить про підтримку клавіатурної навігації?",
                                new String[]{"Розбірливість", "Керованість", "Надійність", "Гнучкість"}, 1),
                        new Question("12. Який тип тестування оцінює стабільність ПЗ при тривалому використанні?",
                                new String[]{"Регресійне тестування", "Юзабіліті", "Навантажувальне (стабільності)", "Інтеграційне"}, 2),
                        new Question("13. Що таке регресійне тестування?",
                                new String[]{"Перевірка нового функціоналу", "Перевірка, що старі функції не зламалися після змін", "Тестування дизайну", "Сканування портів"}, 1),
                        new Question("14. Яка абревіатура відповідає базовим принципам доступності WCAG?",
                                new String[]{"CRUD", "ARIA", "POUR", "SOAP"}, 2),
                        new Question("15. Хто відповідає за забезпечення якості в команді?",
                                new String[]{"Лише тестувальник", "Менеджер проєкту", "Вся команда розробки", "Дизайнер"}, 2)
                };
            else {
                discipline.questions = new Question[]{
                        new Question("1. Що таке штучний інтелект (ШІ)?",
                                new String[]{"Програма для обробки відео", "Система, що імітує інтелектуальну поведінку людини", "Технологія збереження даних", "Середовище для програмування"}, 1),
                        new Question("2. Що таке інтелектуальний агент?",
                                new String[]{"Формула в логіці", "Сутність, що сприймає середовище та діє у ньому", "Сховище інформації", "Нейрон штучної мережі"}, 1),
                        new Question("3. Який із алгоритмів є прикладом пошуку в просторі станів?",
                                new String[]{"K-means", "A*", "SVM", "RANSAC"}, 1),
                        new Question("4. Що таке евристика у ШІ?",
                                new String[]{"Формула для сортування", "Статистичне правило", "Оцінка, яка допомагає пришвидшити пошук рішення", "База знань"}, 2),
                        new Question("5. Що таке експертна система?",
                                new String[]{"Програма для обробки зображень", "Система правил і фактів, яка імітує розв’язування задач експертом", "Бібліотека в Python", "Система для обчислення похідних"}, 1),
                        new Question("6. Який компонент відповідає за логічне виведення в експертній системі?",
                                new String[]{"База даних", "Мотор виводу (Inference Engine)", "Користувацький інтерфейс", "Регулятор"}, 1),
                        new Question("7. Яка модель використовується у навчанні з учителем?",
                                new String[]{"K-means", "Decision Tree", "Apriori", "DBSCAN"}, 1),
                        new Question("8. Яка функція нейрону в штучній нейронній мережі?",
                                new String[]{"Аналіз тексту", "Передача сигналу без обробки", "Обчислення зваженої суми входів і передача її через функцію активації", "Зберігання даних"}, 2),
                        new Question("9. Який з перелічених є прикладом нечіткого ШІ?",
                                new String[]{"Логічне виведення", "Чітке сортування", "Fuzzy Logic", "Масив даних"}, 2),
                        new Question("10. Що таке машинне навчання?",
                                new String[]{"Процес ручного програмування усіх правил", "Модуль управління пам’яттю", "Метод автоматичного покращення алгоритмів через досвід", "Система баз даних"}, 2),
                        new Question("11. Яка функція використовується для активації у нейромережах?",
                                new String[]{"log(x)", "sigmoid", "sqrt(x)", "tan(x)"}, 1),
                        new Question("12. У чому полягає суть алгоритму K-ближчих сусідів (KNN)?",
                                new String[]{"Пошук у глибину", "Класифікація об’єкта за найближчими прикладами у просторі", "Створення дерева рішень", "Побудова функції втрат"}, 1),
                        new Question("13. Що таке overfitting (перенавчання)?",
                                new String[]{"Модель погано запам’ятала дані", "Модель занадто точно відображає тренувальні дані, але не узагальнює", "Недостатньо даних", "Зміщення вхідного простору"}, 1),
                        new Question("14. Який із методів є без учителя?",
                                new String[]{"Logistic Regression", "K-means", "Decision Tree", "Random Forest"}, 1),
                        new Question("15. Яка цільова функція використовується в класифікації?",
                                new String[]{"Mean Squared Error", "Log Loss", "Euclidean Distance", "Fuzzy membership"}, 1)
                };
            }

    }
}