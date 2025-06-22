package Tests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gui.LoadingFrame;
import org.example.Discipline;
import org.example.GameFrame;
import org.example.Hero;
import org.example.Student;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the test-taking process for the student (hero) in the game.
 * It selects disciplines for testing, generates questions, presents tests to the user,
 * and handles post-test logic including saving game data and updating hero stats.
 */
public class TestManager {

    private Hero hero;
    private Student student;
    private List<Discipline> examDisciplines;

    private static final String DATA_FILE = "enrollment_data.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Constructs a new TestManager.
     * Initializes with the current hero, extracts the student and their exam disciplines.
     * Sets the hero's initial knowledge level.
     *
     * @param hero The {@link Hero} object representing the player.
     */
    public TestManager(Hero hero) {
        this.hero = hero;
        this.student = hero.getStudent();
        this.examDisciplines = student.getExamDisciplines();
        hero.setKnowledge(80); // Sets an initial knowledge level for the hero.
    }

    /**
     * Starts the test process.
     * Selects three random exam disciplines and initiates the display of the first test.
     */
    public void startTest() {
        List<Discipline> selected = getThreeRandomDisciplines();
        if (!selected.isEmpty()) {
            generateQuestions(selected.get(0));
            if (selected.size() > 1) {
                generateQuestions(selected.get(1));
            }
        }
        showNextTestWithLoading(selected, 0);
    }

    /**
     * Selects up to three random disciplines from the student's list of exam disciplines.
     *
     * @return A {@code List} of up to three randomly selected {@link Discipline} objects.
     */
    private List<Discipline> getThreeRandomDisciplines() {
        List<Discipline> copy = new ArrayList<>(examDisciplines);
        Collections.shuffle(copy); // Randomize the order of disciplines
        return copy.subList(0, Math.min(3, copy.size())); // Return up to 3 disciplines
    }

    /**
     * Recursively displays the next test in the sequence with a loading screen.
     * After all tests are completed, it updates hero stats, saves data, and returns to the game frame.
     *
     * @param selected A list of disciplines chosen for testing.
     * @param index The current index of the test to be displayed from the {@code selected} list.
     */
    private void showNextTestWithLoading(List<Discipline> selected, int index) {
        if (index >= selected.size()) {
            // All tests completed
            JOptionPane.showMessageDialog(null, "Ви пройшли всі тести!", "Успіх", JOptionPane.INFORMATION_MESSAGE);
            SwingUtilities.invokeLater(() -> {
                LoadingFrame loading = new LoadingFrame();
                loading.startLoading(() -> {
                    hero.levelUp(); // Assuming this increases the hero's level
                    hero.setLevel(3); // Explicitly setting level to 3
                    hero.setStudent(student); // Ensure student object is updated in hero
                    hero.decreaseEnergy(40); // Decrease energy (penalty/cost for testing)
                    hero.decreaseHunger(-30); // Decrease hunger (positive effect, less hungry)
                    hero.decreaseMood(30); // Decrease mood (positive effect, happier)
                    saveDataJson(); // Save game data
                    GameFrame gameFrame = new GameFrame(hero);
                    gameFrame.getGamePanel().getHintPanel().setHint(3); // Set a specific hint
                    gameFrame.setVisible(true); // Show the main game frame
                });
            });
            return;
        }

        Discipline current = selected.get(index);
        generateQuestions(current); // Generate questions for the current discipline

        LoadingFrame loading = new LoadingFrame();
        loading.startLoading(() -> {
            SwingUtilities.invokeLater(() -> {
                // Create and display the test frame
                MainTestFrame testFrame = new MainTestFrame(hero, current, () -> {
                    // This Runnable is executed after the current test in MainTestFrame is finished
                    while (true) {
                        int result = JOptionPane.showOptionDialog(
                                null,
                                "Час переходити до наступного тесту!\nНатисніть ТАК, коли будете готові.",
                                "Наступний тест",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                new Object[]{"Так"}, // Only one button "Так"
                                "Так"
                        );
                        if (result == 0) {
                            break; // Exit loop if "Так" is pressed
                        }
                    }
                    showNextTestWithLoading(selected, index + 1); // Proceed to the next test
                });
                testFrame.setVisible(true);
            });
        });
    }

    /**
     * Saves the current student's data to a JSON file.
     * This method is used to persist the game state.
     */
    private void saveDataJson() {
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            gson.toJson(student, writer);
        } catch (IOException e) {
            System.err.println("Помилка під час збереження даних у " + DATA_FILE + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generates and assigns a set of predefined questions to the given discipline
     * based on its name. If the discipline name doesn't match any known set,
     * no questions will be assigned.
     *
     * @param discipline The {@link Discipline} object for which to generate questions.
     */
    public void generateQuestions(Discipline discipline) {
        if (discipline.getName().equals("Забезпечення якости доступу програмних продуктів"))
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
        else if (discipline.getName().equals("Основи штучного інтелекту")) {
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
        } else if (discipline.getName().equals("Алгебра та геометрія")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке вектор у просторі?",
                            new String[]{"Число", "Множина", "Спрямований відрізок із напрямом і довжиною", "Кут між прямими"}, 2),
                    new Question("2. Як знайти довжину вектора \\( \\vec{a} = (3, 4) \\)?",
                            new String[]{"3 + 4", "3 × 4", "√(3² + 4²)", "3² – 4²"}, 2),
                    new Question("3. Чим є векторний добуток двох колінеарних векторів \\( \\vec{a} \\) та \\( \\vec{b} \\)?",
                            new String[]{"Ненульовий вектор", "Нульовий вектор", "Скалярна величина", "Одиничний вектор"}, 1),
                    new Question("4. Яка з властивостей характерна для матриць?",
                            new String[]{"Комутативність множення", "Наявність визначника", "Всі квадратні матриці — симетричні", "Можливість поділу матриць"}, 1),
                    new Question("5. Як обчислити визначник матриці 2×2?",
                            new String[]{"ad – bc", "ab + cd", "a + b + c + d", "a × b × c × d"}, 0),
                    new Question("6. Яка умова сумісності системи лінійних рівнянь?",
                            new String[]{"Наявність хоча б одного розв’язку", "Тільки одне рівняння", "Рівність кількості невідомих і рівнянь", "Наявність нульової матриці"}, 0),
                    new Question("7. Що таке ранг матриці?",
                            new String[]{"Сума її діагональних елементів", "Максимальна кількість лінійно незалежних рядків або стовпців", "Кількість нульових елементів", "Розмір матриці"}, 1),
                    new Question("8. Як знайти кут між векторами?",
                            new String[]{"За їх довжинами", "За їх добутком", "За формулою скалярного добутку і косинуса", "Через визначник"}, 2),
                    new Question("9. Який вигляд має рівняння прямої на площині?",
                            new String[]{"x² + y² = r²", "y = kx + b", "x + y + z = 0", "ab + cd = 0"}, 1),
                    new Question("10. Що таке площина у тривимірному просторі?",
                            new String[]{"Пряма, яка проходить через початок координат", "Множина точок, що задовольняє рівняння ax + by + cz = d", "Рівняння кола", "Система векторів"}, 1),
                    new Question("11. Яка умова ортогональності векторів?",
                            new String[]{"Їх скалярний добуток дорівнює 0", "Їх довжини рівні", "Їх сума — нуль", "Вектори лежать на одній прямій"}, 0),
                    new Question("12. Який метод використовується для розв’язання СЛАР?",
                            new String[]{"Інтегрування", "Теорема Піфагора", "Метод Гаусса", "Скалярне множення"}, 2),
                    new Question("13. Що таке лінійна комбінація векторів?",
                            new String[]{"Добуток двох векторів", "Сума векторів з числовими коефіцієнтами", "Множина нульових векторів", "Кут між векторами"}, 1),
                    new Question("14. Яка фігура має усі сторони та кути рівні?",
                            new String[]{"Ромб", "Паралелограм", "Квадрат", "Трапеція"}, 2),
                    new Question("15. Що таке ортонормований базис?",
                            new String[]{"Будь-який базис", "Система лінійно залежних векторів", "Ортогональна система векторів з довжиною 1", "Базис, що містить нульовий вектор"}, 2)
            };
        } else if (discipline.getName().equals("Теорія алгоритмів та математичної логіки") || discipline.getName().equals("Математична логіка та теорія алгоритмів")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке алгоритм з точки зору теорії алгоритмів?",
                            new String[]{"Будь-яка послідовність дій", "Набір випадкових інструкцій", "Скінченна, однозначна, ефективна та результативна послідовність дій", "Математичне рівняння"}, 2),
                    new Question("2. Яка модель обчислень вважається універсальною?",
                            new String[]{"Скінченний автомат", "Машина Тьюрінга", "Рекурсивна функція", "Лямбда-вираз"}, 1),
                    new Question("3. Що означає «обчислювана функція»?",
                            new String[]{"Функція, яку можна описати у вигляді таблиці", "Функція, що виконується в браузері", "Функція, яку можна обчислити алгоритмом", "Графічна функція"}, 2),
                    new Question("4. Яка з мов **не є** регулярною?",
                            new String[]{"Мова паліндромів", "Мова усіх слів над {a,b}", "Мова з парною кількістю символів a", "Мова, описана регулярним виразом"}, 0),
                    new Question("5. Що таке висловлювальна логіка?",
                            new String[]{"Мова програмування", "Система рівнянь", "Математична логіка, що працює з істинними/хибними висловлюваннями", "Математична модель"}, 2),
                    new Question("6. Який символ у логіці позначає імплікацію?",
                            new String[]{"∧", "∨", "¬", "→"}, 3),
                    new Question("7. Коли висловлювання вважається тавтологією?",
                            new String[]{"Якщо воно істинне для певних значень", "Якщо істинне в одній ситуації", "Якщо істинне при всіх наборах значень змінних", "Якщо воно не містить ¬"}, 2),
                    new Question("8. Що таке формальна мова?",
                            new String[]{"Мова програмування", "Мова без граматики", "Набір слів, побудованих за правилами граматики", "Мова, що містить тільки цифри"}, 2),
                    new Question("9. Як називається множина функцій, що можуть бути обчислені машиною Тьюрінга?",
                            new String[]{"Множина рекурсивних функцій", "Множина інтегралів", "Гіпотетична множина", "Алгебраїчна система"}, 0),
                    new Question("10. Який логічний квантор означає «для всіх»?",
                            new String[]{"∃", "∀", "⇒", "⇔"}, 1),
                    new Question("11. Який клас задач позначається як **P**?",
                            new String[]{"Задачі, що вирішуються за експоненційний час", "Нерозв’язні задачі", "Задачі, що вирішуються за поліноміальний час", "Приблизні задачі"}, 2),
                    new Question("12. Який логічний вираз є прикладом диз’юнкції?",
                            new String[]{"A ∧ B", "¬A", "A → B", "A ∨ B"}, 3),
                    new Question("13. Який логічний висновок є коректним за правилом Modus Ponens?",
                            new String[]{"A, A ∧ B ⟹ B", "A → B, A ⟹ B", "A ∨ B, ¬A ⟹ B", "¬B, A ⟹ A → B"}, 1),
                    new Question("14. Яке поняття означає нерозв’язність задачі?",
                            new String[]{"Її не можна вирішити за один крок", "Її не можна розв’язати жодним алгоритмом", "Потрібен потужний комп’ютер", "Вона потребує багато пам’яті"}, 1),
                    new Question("15. Яка з мов є контекстно-вільною?",
                            new String[]{"Мова паліндромів", "Мова рівної кількості a і b", "Мова арифметичних виразів", "Мова зі змінною довжиною"}, 2)
            };

        } else if (discipline.getName().equals("Алгоритми і структури даних") || discipline.getName().equals("Програмування")) {
            discipline.questions = new Question[]{
                    new Question("1. Яка основна функція компілятора?",
                            new String[]{"Виконання програми", "Перетворення вихідного коду у машинозрозумілий код", "Відлагодження програми", "Управління пам'яттю"}, 1),
                    new Question("2. Що таке змінна в програмуванні?",
                            new String[]{"Постійне значення", "Ім'я для комірки пам'яті, що зберігає дані", "Тип даних", "Оператор порівняння"}, 1),
                    new Question("3. Який тип циклу використовується, коли кількість ітерацій відома заздалегідь?",
                            new String[]{"While", "Do-While", "For", "ForEach"}, 2),
                    new Question("4. Що таке синтаксична помилка?",
                            new String[]{
                                    "Помилка в логіці програми",
                                    "Помилка, яка виникає під час виконання програми",
                                    "Помилка, що порушує правила написання коду мови програмування",
                                    "Помилка, пов'язана з неправильним використанням пам'яті"}, 2),
                    new Question("5. Який оператор використовується для перевірки рівності двох значень у більшості мов?",
                            new String[]{"=", "==", ":=", "==="}, 1),
                    new Question("6. Що таке функція (метод) у програмуванні?",
                            new String[]{"Змінна для зберігання тексту", "Блок коду, призначений для виконання певного завдання", "Тип даних", "Структура даних"}, 1),
                    new Question("7. Яка структура даних є LIFO (Last-In, First-Out)?",
                            new String[]{"Черга (Queue)", "Стек (Stack)", "Список (List)", "Масив (Array)"}, 1),
                    new Question("8. Яка концепція ООП дозволяє створювати нові класи на основі наявних?",
                            new String[]{"Інкапсуляція", "Поліморфізм", "Наслідування", "Абстракція"}, 2),
                    new Question("9. Для чого призначений оператор 'if-else'?",
                            new String[]{"Для повторення блоку коду", "Для виконання коду на основі умови", "Для оголошення змінних", "Для створення об'єктів"}, 1),
                    new Question("10. Що таке 'алгоритм'?",
                            new String[]{"Мова програмування", "Набір інструкцій для розв'язання задачі", "Тип комп'ютера", "Програма для малювання"}, 1),
                    new Question("11. Який оператор логічного 'І' (AND) використовується в Java/C++/C#?",
                            new String[]{"||", "!", "&&", "|"}, 2),
                    new Question("12. Що таке 'масив'?",
                            new String[]{"Змінна для одного значення", "Колекція однотипних елементів, що зберігаються послідовно", "Функція", "Оператор"}, 1),
                    new Question("13. Який з цих термінів НЕ є основним компонентом ООП?",
                            new String[]{
                                    "Інкапсуляція",
                                    "Наслідування",
                                    "Компіляція",
                                    "Поліморфізм"}, 2),
                    new Question("14. Що повертає булева змінна?",
                            new String[]{"Ціле число", "Рядок тексту", "Правда або хибність (true/false)", "Список значень"}, 2),
                    new Question("15. Який алгоритм сортування є найпростішим для розуміння і реалізації, але не найефективнішим?",
                            new String[]{
                                    "Швидке сортування (QuickSort)",
                                    "Сортування злиттям (MergeSort)",
                                    "Бульбашкове сортування (Bubble Sort)",
                                    "Сортування вибором (Selection Sort)"}, 2)
            };
        } else if (discipline.getName().equals("Патерни проєктування та дизайн АРІ")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке патерн проєктування (design pattern)?",
                            new String[]{"Мова програмування", "Конкретна реалізація", "Узагальнене рішення типових задач проєктування ПЗ", "Система команд"}, 2),
                    new Question("2. Який патерн належить до порожніх (creational) патернів?",
                            new String[]{"Strategy", "Decorator", "Factory Method", "Observer"}, 2),
                    new Question("3. Який патерн забезпечує створення лише одного екземпляра класу?",
                            new String[]{"Builder", "Adapter", "Singleton", "Mediator"}, 2),
                    new Question("4. Що таке REST API?",
                            new String[]{"Формат бази даних", "Протокол HTTP/2", "Архітектурний стиль для створення веб-сервісів", "Вебфреймворк"}, 2),
                    new Question("5. Який HTTP-метод зазвичай використовується для створення ресурсу в REST?",
                            new String[]{"GET", "POST", "PUT", "DELETE"}, 1),
                    new Question("6. Що таке ідіоматичний підхід у дизайні API?",
                            new String[]{"Патерн кодування алгоритмів", "Використання звичних структур і практик для даної мови чи платформи", "Формула кешування", "Опис транзакцій"}, 1),
                    new Question("7. Який з GoF-патернів належить до поведінкових (behavioral)?",
                            new String[]{"Bridge", "Proxy", "Observer", "Composite"}, 2),
                    new Question("8. Що таке версіонування API?",
                            new String[]{"Зміна ключів доступу", "Опис інтерфейсів у Swagger", "Керування змінами API без порушення існуючих інтеграцій", "Формат логування"}, 2),
                    new Question("9. Який формат зазвичай використовується для опису REST API?",
                            new String[]{"CSV", "XLSX", "OpenAPI (Swagger)", "HTML"}, 2),
                    new Question("10. Що таке idempotent метод у REST?",
                            new String[]{"Метод, що повертає однакову відповідь при повторному виклику", "Метод, що кешується", "Метод з авторизацією", "Метод тільки для читання"}, 0),
                    new Question("11. Що означає статус HTTP 201?",
                            new String[]{"Помилка авторизації", "Успішне створення ресурсу", "Дані не знайдено", "Заборонено"}, 1),
                    new Question("12. Який патерн корисний для реалізації UI, що оновлюється при зміні даних?",
                            new String[]{"Adapter", "Observer", "Facade", "Flyweight"}, 1),
                    new Question("13. Який механізм використовують для авторизації доступу до REST API?",
                            new String[]{"OAuth 2.0", "HTML", "FTP", "SOAP"}, 0),
                    new Question("14. Який патерн дозволяє змінювати поведінку об'єкта без зміни коду класу?",
                            new String[]{"Strategy", "Prototype", "Factory", "Memento"}, 0),
                    new Question("15. Що є антипатерном при проєктуванні API?",
                            new String[]{"Чітке розділення маршрутів", "Використання status-кодів", "Нестабільна структура відповіді", "Фільтрація параметрами"}, 2)
            };
        } else if (discipline.getName().equals("Архітектура інформаційних систем")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке архітектура інформаційної системи?",
                            new String[]{"Інструкція користувача", "Фізична схема мережі", "Структурне представлення компонентів ІС та їх взаємодії", "Алгоритм авторизації"}, 2),
                    new Question("2. Яка з моделей описує поділ системи на клієнт і сервер?",
                            new String[]{"Peer-to-peer", "Однорівнева", "Клієнт-серверна", "Багатопоточна"}, 2),
                    new Question("3. Які основні рівні має типова трирівнева архітектура ІС?",
                            new String[]{"Мережевий, візуальний, аналітичний", "Фронтенд, бекенд, кеш", "Презентаційний, логічний, рівень даних", "Фізичний, логічний, концептуальний"}, 2),
                    new Question("4. Яка роль СУБД в інформаційній системі?",
                            new String[]{"Генерація звітів", "Зберігання і доступ до структурованих даних", "Шифрування повідомлень", "Відображення інтерфейсу"}, 1),
                    new Question("5. Що таке middleware у контексті ІС?",
                            new String[]{"Фреймворк для інтерфейсу", "Проміжне програмне забезпечення для зв’язку між компонентами", "Бібліотека для візуалізації", "Тип бази даних"}, 1),
                    new Question("6. Який стандарт описує архітектуру підприємства в TOGAF?",
                            new String[]{"SAFe", "DMN", "ADM (Architecture Development Method)", "SDLC"}, 2),
                    new Question("7. Що таке SOA (Service-Oriented Architecture)?",
                            new String[]{"Архітектура з графічним інтерфейсом", "Підхід, де компоненти реалізовані як сервіси з чіткими інтерфейсами", "Мережева топологія", "Модель даних"}, 1),
                    new Question("8. Який тип архітектури найкраще підходить для масштабування мікросервісів?",
                            new String[]{"Монолітна", "Трирівнева", "Клієнт-сервер", "Контейнеризована"}, 3),
                    new Question("9. Що таке ERP-система в контексті корпоративних ІС?",
                            new String[]{"Система для електронного документообігу", "Система для планування ресурсів підприємства", "Файлова система", "Графічна утиліта"}, 1),
                    new Question("10. Яка технологія часто використовується для обміну повідомленнями в SOA?",
                            new String[]{"FTP", "MQ (Message Queue)", "SOAP-тунелювання", "HTTP"}, 1),
                    new Question("11. Що таке API в інформаційній системі?",
                            new String[]{"Мережевий протокол", "Інтерфейс для взаємодії програмних компонентів", "Таблиця даних", "Протокол передачі ключів"}, 1),
                    new Question("12. Який формат обміну даними найчастіше використовується у веб-сервісах?",
                            new String[]{"DOCX", "CSV", "JSON", "EXE"}, 2),
                    new Question("13. Яка мета використання UML в архітектурі ІС?",
                            new String[]{"Розробка алгоритмів шифрування", "Моделювання структури та поведінки системи", "Збереження логів", "Побудова файлової ієрархії"}, 1),
                    new Question("14. Яка архітектура передбачає незалежне розгортання окремих компонентів?",
                            new String[]{"Монолітна", "Мікросервісна", "Пірамідальна", "Реляційна"}, 1),
                    new Question("15. Що таке інтероперабельність у контексті ІС?",
                            new String[]{"Захист даних", "Можливість систем працювати разом та обмінюватися даними", "Зменшення розміру БД", "Компресія об’єктів"}, 1)
            };
        } else if (discipline.getName().equals("Основи комп'ютерних алгоритмів")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке алгоритм?",
                            new String[]{"Мова програмування", "Покрокове розв’язання задачі", "Вид змінної", "Формат файлу"}, 1),
                    new Question("2. Яка характеристика описує ефективність алгоритму?",
                            new String[]{"Складність алгоритму", "Його мова", "Тип змінних", "Довжина назви"}, 0),
                    new Question("3. Яка з наведених складностей є найгіршою за продуктивністю?",
                            new String[]{"O(1)", "O(log n)", "O(n)", "O(n²)"}, 3),
                    new Question("4. Який алгоритм сортування працює за принципом «розділяй і володарюй»?",
                            new String[]{"Сортування вставками", "Пірамідальне сортування", "Швидке сортування", "Сортування вибором"}, 2),
                    new Question("5. Що таке рекурсія?",
                            new String[]{"Алгоритм, що повторюється циклом", "Функція, яка викликає сама себе", "Використання умовних операторів", "Зміна змінних"}, 1),
                    new Question("6. Яка структура даних працює за принципом LIFO?",
                            new String[]{"Черга", "Масив", "Список", "Стек"}, 3),
                    new Question("7. Який алгоритм пошуку найшвидший у відсортованому масиві?",
                            new String[]{"Лінійний пошук", "Бінарний пошук", "Ітеративний пошук", "Пошук перебором"}, 1),
                    new Question("8. Що таке цикл у програмуванні?",
                            new String[]{"Умова", "Інструкція повторення", "Клас об'єкта", "Компілятор"}, 1),
                    new Question("9. Яка з операцій є основною для хеш-таблиці?",
                            new String[]{"Додавання у кінець", "Лінійний пошук", "Використання хеш-функції", "Сортування"}, 2),
                    new Question("10. Що таке граф у теорії алгоритмів?",
                            new String[]{"Математична функція", "Статистична діаграма", "Структура з вузлів і ребер", "Компілятор"}, 2),
                    new Question("11. Яка з наведених структур є динамічною?",
                            new String[]{"Масив", "Стек", "Зв’язаний список", "Кортеж"}, 2),
                    new Question("12. Що таке жадібний алгоритм?",
                            new String[]{"Алгоритм, який оптимально вирішує всі задачі", "Алгоритм, що вибирає найкраще рішення на кожному кроці", "Складний алгоритм", "Алгоритм з рекурсією"}, 1),
                    new Question("13. Яка структура найкраще підходить для реалізації черги?",
                            new String[]{"Стек", "Масив", "Зв’язаний список", "Черга (Queue)"}, 3),
                    new Question("14. Який алгоритм використовується для знаходження найкоротшого шляху у графі?",
                            new String[]{"Бінарний пошук", "DFS", "Сортування злиттям", "Алгоритм Дейкстри"}, 3),
                    new Question("15. Що таке Big-O нотація?",
                            new String[]{"Опис пам’яті", "Позначення типів даних", "Оцінка складності алгоритму", "Формула розмітки"}, 2)
            };
        } else if (discipline.getName().equals("Аналіз великих даних (Big Data)") || discipline.getName().equals("Аналіз даних великого розміру (Big Data)")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке великі дані (Big Data)?",
                            new String[]{"Будь-які числові дані", "Дані, що не піддаються обробці традиційними методами через обсяг, швидкість або різноманіття", "Файли великих розмірів", "Тільки відео і аудіо файли"}, 1),
                    new Question("2. Які основні характеристики визначають Big Data? (модель 3V)",
                            new String[]{"Volume, Velocity, Variety", "Volume, Version, Value", "Value, Visibility, Volume", "Visualization, Volume, Validity"}, 0),
                    new Question("3. Яка платформа є однією з найпоширеніших у обробці великих даних?",
                            new String[]{"TensorFlow", "MySQL", "Hadoop", "Jupyter"}, 2),
                    new Question("4. Що таке HDFS у Hadoop?",
                            new String[]{"Формат даних", "Графічний інтерфейс", "Розподілена файлова система", "Тип БД"}, 2),
                    new Question("5. Яка з систем баз даних належить до NoSQL?",
                            new String[]{"PostgreSQL", "MongoDB", "Oracle", "MySQL"}, 1),
                    new Question("6. Що таке MapReduce?",
                            new String[]{"Алгоритм стиснення", "Модель обробки даних у два етапи: Map і Reduce", "Тип машинного навчання", "Формат JSON"}, 1),
                    new Question("7. Який фреймворк є розширенням Hadoop для обчислень у пам’яті?",
                            new String[]{"Hive", "Pig", "Spark", "Storm"}, 2),
                    new Question("8. Який компонент Spark використовується для роботи з даними SQL-подібного типу?",
                            new String[]{"Spark Core", "Spark SQL", "Spark Streaming", "Spark MLlib"}, 1),
                    new Question("9. Яка задача добре вирішується за допомогою кластеризації в Big Data?",
                            new String[]{"Пошук шкідливого ПЗ", "Групування клієнтів за поведінкою", "Аналіз графіків функцій", "Перетворення форматів файлів"}, 1),
                    new Question("10. Який тип даних є прикладом потокових даних?",
                            new String[]{"Архівовані файли", "Логи в реальному часі", "CSV-файл", "SQL-запити"}, 1),
                    new Question("11. Який з наведених інструментів використовують для обробки потокових даних?",
                            new String[]{"Spark SQL", "Apache Kafka", "MapReduce", "HDFS"}, 1),
                    new Question("12. Що таке шардінг (sharding) у базах даних?",
                            new String[]{"Метод обробки SQL-запитів", "Розподіл даних на окремі частини між вузлами", "Компресія даних", "Тип пам’яті"}, 1),
                    new Question("13. Який формат даних є найбільш придатним для обробки в Hadoop?",
                            new String[]{"XLSX", "CSV", "Parquet", "DOCX"}, 2),
                    new Question("14. У чому перевага розподілених обчислень у Big Data?",
                            new String[]{"Зменшення пам’яті", "Використання лише одного процесора", "Паралельна обробка великих обсягів даних", "Ручне керування"}, 2),
                    new Question("15. Який з інструментів дозволяє будувати пайплайни машинного навчання на великих даних?",
                            new String[]{"NumPy", "Spark MLlib", "Excel", "Pandas"}, 1)
            };
        } else if (discipline.getName().equals("Комп'ютерна графіка")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке комп’ютерна графіка?",
                            new String[]{"Розділ програмування з базами даних", "Система для обчислення", "Методи створення, зберігання, обробки й виводу зображень за допомогою комп’ютера", "Система захисту інформації"}, 2),
                    new Question("2. Що таке піксель у растровій графіці?",
                            new String[]{"Формула кольору", "Біт інформації", "Мінімальний елемент зображення, який має колір", "Об'єкт у векторному редакторі"}, 2),
                    new Question("3. Який формат зображення є растровим?",
                            new String[]{"SVG", "EPS", "BMP", "PDF"}, 2),
                    new Question("4. Що таке векторна графіка?",
                            new String[]{"Зображення, які складаються з пікселів", "Зображення, що описуються математичними формулами", "Ручні малюнки", "Тривимірні моделі"}, 1),
                    new Question("5. Яка модель описує колір на екрані?",
                            new String[]{"CMYK", "HSV", "RGB", "XYZ"}, 2),
                    new Question("6. У якому форматі краще зберігати фотографію для вебу без втрати якості?",
                            new String[]{"JPEG", "BMP", "TIFF", "PNG"}, 3),
                    new Question("7. Який із алгоритмів використовують для малювання прямої на піксельному екрані?",
                            new String[]{"Флойд-Стейнберг", "Bresenham", "Dijkstra", "A*"}, 1),
                    new Question("8. Яке перетворення змінює розмір зображення без зміни пропорцій?",
                            new String[]{"Зсув", "Масштабування", "Поворот", "Віддзеркалення"}, 1),
                    new Question("9. Яка з трансформацій змінює лише положення об'єкта без зміни його розміру або форми?",
                            new String[]{"Зсув (translation)", "Масштабування", "Проєкція", "Рендеринг"}, 0),
                    new Question("10. Яка з операцій використовується для побудови тіней у 3D-графіці?",
                            new String[]{"Ray tracing", "Clipping", "Wireframe", "Flood fill"}, 0),
                    new Question("11. Що таке z-buffer у 3D-графіці?",
                            new String[]{"Масив текстур", "Метод визначення глибини об’єктів відносно камери", "Буфер кольору", "Інтерфейс GPU"}, 1),
                    new Question("12. Який API використовується для програмування графіки у веббраузерах?",
                            new String[]{"OpenGL", "DirectX", "WebGL", "Vulkan"}, 2),
                    new Question("13. Яке поняття описує частоту оновлення зображення на екрані?",
                            new String[]{"Роздільна здатність", "Фреймрейт (FPS)", "Глибина кольору", "Антиаліасинг"}, 1),
                    new Question("14. Що таке сплайн?",
                            new String[]{"Тип текстури", "Полігональна сітка", "Гладка крива, що задається контрольними точками", "Піксельна маска"}, 2),
                    new Question("15. Що таке рендеринг?",
                            new String[]{"Процес створення сценарію", "Процес відображення 3D-сцени в 2D-зображення", "Компіляція графічного коду", "Моделювання освітлення"}, 1)
            };
        } else if (discipline.getName().equals("Комп'ютерний зір / Computer Vision (англ. мовою)")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке комп’ютерний зір?",
                            new String[]{"Галузь штучного інтелекту, що вивчає людський зір", "Технологія для оптичного зберігання", "Метод дозволу зображень", "Галузь ІТ, що навчає машини аналізувати й інтерпретувати зображення та відео"}, 3),
                    new Question("2. Який інструмент найчастіше використовують для комп’ютерного зору у Python?",
                            new String[]{"NumPy", "OpenCV", "Matplotlib", "TensorBoard"}, 1),
                    new Question("3. Який колірний простір краще підходить для виявлення об’єктів за тоном?",
                            new String[]{"RGB", "BGR", "HSV", "CMYK"}, 2),
                    new Question("4. Який з фільтрів використовується для згладжування зображень?",
                            new String[]{"Собель", "Лаплас", "Гаус", "Кенні"}, 2),
                    new Question("5. Що таке згортка (convolution) у CNN?",
                            new String[]{"Об'єднання зображень", "Масштабування", "Операція фільтрації із застосуванням ядра до зображення", "Інверсія кольору"}, 2),
                    new Question("6. Що таке ядро (kernel) в контексті згортки?",
                            new String[]{"Область ROI", "Параметр регуляризації", "Матриця коефіцієнтів, що накладається на зображення", "Масив сегментів"}, 2),
                    new Question("7. Який метод використовують для виявлення країв на зображенні?",
                            new String[]{"Підвищення яскравості", "Скалярне множення", "Алгоритм Кенні", "Поворот фону"}, 2),
                    new Question("8. Яка архітектура нейромереж найчастіше використовується в задачах комп’ютерного зору?",
                            new String[]{"RNN", "Transformer", "CNN", "GAN"}, 2),
                    new Question("9. Який із рівнів CNN відповідає за зменшення розмірності карти ознак?",
                            new String[]{"Input layer", "Dense layer", "Pooling layer", "ReLU layer"}, 2),
                    new Question("10. Який алгоритм застосовують для розпізнавання облич у зображенні?",
                            new String[]{"YOLO", "RANSAC", "KNN", "FFT"}, 0),
                    new Question("11. Що таке сегментація зображення?",
                            new String[]{"Обрізання країв", "Розподіл зображення на області, що мають однакові властивості", "Перетворення кольору", "Розмиття"}, 1),
                    new Question("12. Який формат зображення краще зберігає дані без втрат?",
                            new String[]{"JPEG", "BMP", "PNG", "GIF"}, 2),
                    new Question("13. Для чого використовується функція `cv2.findContours()` в OpenCV?",
                            new String[]{"Для зміни кольору", "Для пошуку обрисів об'єктів", "Для згортки", "Для розпізнавання тексту"}, 1),
                    new Question("14. Який підхід використовують для сегментації з глибоким навчанням?",
                            new String[]{"ResNet", "YOLO", "U-Net", "VGG"}, 2),
                    new Question("15. Що таке data augmentation у комп’ютерному зорі?",
                            new String[]{"Стиснення зображення", "Створення синтетичних даних на основі модифікацій оригінальних", "Побудова дерев рішень", "Пошук контурів"}, 1)
            };
        } else if (discipline.getName().equals("Аналіз даних")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке аналіз даних?",
                            new String[]{"Процес збору даних", "Процес створення вебінтерфейсу", "Процес обробки, візуалізації та інтерпретації даних", "Процес кодування у Python"}, 2),
                    new Question("2. Яке з наведених є прикладом кількісної змінної?",
                            new String[]{"Колір авто", "Тип пристрою", "Температура", "Категорія продукту"}, 2),
                    new Question("3. Яке значення середнього арифметичного для набору {2, 4, 6, 8}?",
                            new String[]{"4", "5", "6", "20"}, 1),
                    new Question("4. Який графік доцільно використовувати для візуалізації розподілу частот?",
                            new String[]{"Діаграма розсіювання", "Стовпчикова діаграма", "Гістограма", "Кругова діаграма"}, 2),
                    new Question("5. Що таке кореляція?",
                            new String[]{"Причинно-наслідковий зв’язок", "Символічне представлення даних", "Залежність між двома змінними", "Вид класифікації"}, 2),
                    new Question("6. Яке значення коефіцієнта кореляції свідчить про сильний зв’язок?",
                            new String[]{"0.1", "0.4", "0.7", "0"}, 2),
                    new Question("7. Що таке дисперсія?",
                            new String[]{"Міра центральної тенденції", "Міра розсіювання значень відносно середнього", "Сума всіх значень", "Кількість категорій"}, 1),
                    new Question("8. Яка мета кластеризації?",
                            new String[]{"Передбачити значення", "Змінити розмірність", "Розбити дані на групи за схожістю", "Оцінити модель"}, 2),
                    new Question("9. Який алгоритм кластеризації є найвідомішим?",
                            new String[]{"KNN", "Linear Regression", "K-means", "Naive Bayes"}, 2),
                    new Question("10. Що таке регресія?",
                            new String[]{"Машинне кодування", "Метод передбачення числового значення на основі вхідних змінних", "Тип бази даних", "Сортування даних"}, 1),
                    new Question("11. Яка з метрик використовується для оцінки якості регресії?",
                            new String[]{"Accuracy", "Recall", "R² (коефіцієнт детермінації)", "F1-score"}, 2),
                    new Question("12. Що таке надмірне навчання (overfitting)?",
                            new String[]{"Коли модель не здатна навчатися", "Коли модель погано запам’ятовує", "Коли модель запам’ятала шум даних, але не узагальнює", "Коли відсутні вхідні дані"}, 2),
                    new Question("13. Яка бібліотека Python найчастіше використовується для аналізу даних?",
                            new String[]{"NumPy", "Matplotlib", "Pandas", "Scikit-learn"}, 2),
                    new Question("14. Що таке boxplot?",
                            new String[]{"Таблиця даних", "Діаграма розсіювання", "Графік, що показує медіану, квартилі та викиди", "Графік розподілу частот"}, 2),
                    new Question("15. Що таке нормалізація даних?",
                            new String[]{"Перетворення в числовий формат", "Видалення рядків", "Приведення значень до одного масштабу", "Випадкове змішування"}, 2)
            };
        } else if (discipline.getName().equals("Бази даних") || discipline.getName().equals("Бази даних та інформаційні системи")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке база даних?",
                            new String[]{"Сукупність вебсторінок", "Формат файлу для зображень", "Організована сукупність даних", "Мова програмування"}, 2),
                    new Question("2. Яка основна функція СУБД?",
                            new String[]{"Оптимізація мережі", "Керування файлами", "Управління даними у базі", "Аналіз зображень"}, 2),
                    new Question("3. Що таке SQL?",
                            new String[]{"Мова веброзробки", "Мова стилів", "Мова запитів до баз даних", "Формат таблиці"}, 2),
                    new Question("4. Яка команда SQL використовується для вибірки даних з таблиці?",
                            new String[]{"SELECT", "INSERT", "DELETE", "UPDATE"}, 0),
                    new Question("5. Що таке первинний ключ (primary key)?",
                            new String[]{"Поле з дублікатами", "Унікальний ідентифікатор запису", "Назва таблиці", "Тип змінної"}, 1),
                    new Question("6. Що означає нормалізація бази даних?",
                            new String[]{"Оптимізація коду", "Збереження паролів", "Процес усунення надмірності", "Встановлення зображень"}, 2),
                    new Question("7. Яка команда SQL використовується для додавання нового запису?",
                            new String[]{"SELECT", "DELETE", "INSERT", "DROP"}, 2),
                    new Question("8. Що таке зовнішній ключ (foreign key)?",
                            new String[]{"Ключ шифрування", "Зовнішній доступ до бази", "Поле, що зв'язує дві таблиці", "Резервне поле"}, 2),
                    new Question("9. Який тип зв'язку описує співвідношення 'один до багатьох'?",
                            new String[]{"1:1", "1:N", "N:M", "N:0"}, 1),
                    new Question("10. Яке розширення зазвичай мають файли баз даних SQLite?",
                            new String[]{".db", ".sql", ".html", ".exe"}, 0),
                    new Question("11. Що таке транзакція в СУБД?",
                            new String[]{"Процес шифрування", "Звіт про помилки", "Послідовність дій як єдине ціле", "Мережева команда"}, 2),
                    new Question("12. Яка команда SQL використовується для зміни структури таблиці?",
                            new String[]{"CHANGE", "UPDATE", "ALTER", "MODIFY"}, 2),
                    new Question("13. Яка функція використовується для підрахунку кількості записів у SQL?",
                            new String[]{"SUM()", "AVG()", "COUNT()", "MAX()"}, 2),
                    new Question("14. Який тип даних у SQL використовується для зберігання тексту?",
                            new String[]{"INT", "VARCHAR", "DATE", "FLOAT"}, 1),
                    new Question("15. Яка команда використовується для видалення таблиці?",
                            new String[]{"DELETE TABLE", "REMOVE", "DROP TABLE", "TRUNCATE"}, 2)
            };
        } else if (discipline.getName().equals("Диференціальні рівняння")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке диференціальне рівняння?",
                            new String[]{"Рівняння, що містить похідну невідомої функції", "Рівняння з модулями", "Рівняння з логарифмами", "Система рівнянь без похідних"}, 0),
                    new Question("2. Який порядок має рівняння y'' + y = 0?",
                            new String[]{"Перший", "Другий", "Третій", "Нульовий"}, 1),
                    new Question("3. Що називається загальним розв’язком диференціального рівняння?",
                            new String[]{"Конкретне значення y", "Розв’язок, що включає константу інтегрування", "Похідна функції", "Межа функції"}, 1),
                    new Question("4. Яке з рівнянь є рівнянням з відокремлюваними змінними?",
                            new String[]{"y' = y² + x", "y' = x/y", "y' + y = x", "y'' + y = 0"}, 1),
                    new Question("5. Як називається метод інтегрування першого порядку з відокремлюваними змінними?",
                            new String[]{"Інтегрування частинами", "Метод заміни", "Метод відокремлення змінних", "Граничний метод"}, 2),
                    new Question("6. Який із методів використовується для розв’язання лінійного рівняння y' + p(x)y = q(x)?",
                            new String[]{"Метод інтегруючого множника", "Метод Ейлера", "Метод заміни змінної", "Метод Гауса"}, 0),
                    new Question("7. Рівняння y'' + ω²y = 0 описує:",
                            new String[]{"Зростаючу експоненту", "Демпфований коливальний процес", "Гармонічні коливання", "Розпад радіоактивності"}, 2),
                    new Question("8. Що таке початкова умова в задачі Коші?",
                            new String[]{"Границя функції", "Конкретне значення y або y' при x = x₀", "Інтеграл функції", "Проміжок визначення"}, 1),
                    new Question("9. Яке з наведених рівнянь є нелінійним?",
                            new String[]{"y' + 2y = 0", "y'' + y = 0", "y' = y²", "y'' + 3y' + y = 0"}, 2),
                    new Question("10. Що таке однорідне лінійне диференціальне рівняння?",
                            new String[]{"Рівняння без правої частини (q(x) = 0)", "Рівняння, де всі коефіцієнти рівні", "Рівняння з дробами", "Рівняння без похідних"}, 0),
                    new Question("11. Як записується загальний розв’язок однорідного рівняння y' = ky?",
                            new String[]{"y = kx", "y = Ce^(kx)", "y = Cx^k", "y = k^x"}, 1),
                    new Question("12. Для чого використовують характеристичне рівняння?",
                            new String[]{"Для знаходження меж", "Для розв’язання лінійних рівнянь другого порядку з постійними коефіцієнтами", "Для похідних", "Для інтегрування"}, 1),
                    new Question("13. Рівняння y'' + 4y' + 4y = 0 має корені характеристичного рівняння:",
                            new String[]{"Різні дійсні", "Комплексно спряжені", "Збігаються", "Немає коренів"}, 2),
                    new Question("14. Що таке частинний розв’язок неоднорідного рівняння?",
                            new String[]{"Розв’язок без похідної", "Будь-яке рівняння, що не містить y", "Розв’язок, який задовольняє праву частину рівняння", "Постійна функція"}, 2),
                    new Question("15. Як отримати загальний розв’язок неоднорідного рівняння?",
                            new String[]{"Скласти загальний розв’язок однорідного та частинний неоднорідного", "Взяти інтеграл правої частини", "Поділити на y", "Обчислити межу"}, 0)
            };
        } else if (discipline.getName().equals("Дискретна математика")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке множина в дискретній математиці?",
                            new String[]{"Рядок тексту", "Набір впорядкованих елементів", "Набір об'єктів, які розглядаються як єдине ціле", "Математична функція"}, 2),
                    new Question("2. Який символ позначає перетин множин?",
                            new String[]{"∪", "∩", "\\", "⊆"}, 1),
                    new Question("3. Що означає логічне висловлювання?",
                            new String[]{"Формула з невідомими", "Речення, яке може бути істинним або хибним", "Гіпотеза", "Множина чисел"}, 1),
                    new Question("4. Який результат має висловлювання: true AND false?",
                            new String[]{"true", "false", "не визначено", "1"}, 1),
                    new Question("5. Яке з наведених є тавтологією?",
                            new String[]{"A ∧ ¬A", "A ∨ ¬A", "¬(A ∨ B)", "A ∧ B"}, 1),
                    new Question("6. Що таке істиннісна таблиця?",
                            new String[]{"Таблиця множин", "Граф логічної функції", "Таблиця, що відображає значення логічного виразу для всіх комбінацій змінних", "Розклад пари"}, 2),
                    new Question("7. Який символ означає заперечення (NOT)?",
                            new String[]{"∨", "∧", "→", "¬"}, 3),
                    new Question("8. Що таке відношення в дискретній математиці?",
                            new String[]{"Залежність між двома об’єктами", "Множина чисел", "Булева функція", "Формула"}, 0),
                    new Question("9. Яка властивість притаманна рефлексивному відношенню?",
                            new String[]{"Для всіх x: (x, x) ∈ R", "Для всіх x ≠ y: (x, y) ∈ R", "Для всіх x: (x, x) ∉ R", "R складається лише з пар (x, y), де x > y"}, 0),
                    new Question("10. Що таке граф у теорії графів?",
                            new String[]{"Малюнок", "Сукупність вершин та ребер", "Рівняння", "Множина функцій"}, 1),
                    new Question("11. Який граф називається орієнтованим?",
                            new String[]{"Граф без вершин", "Граф, у якому всі ребра мають напрямок", "Граф без циклів", "Граф, де всі вершини сполучені"}, 1),
                    new Question("12. Який тип графа не має петель і кратних ребер?",
                            new String[]{"Орієнтований граф", "Мультиграф", "Простий граф", "Повний граф"}, 2),
                    new Question("13. Що таке бінарне відношення?",
                            new String[]{"Відношення з одним елементом", "Відношення між двома множинами", "Математичне правило", "Графік"}, 1),
                    new Question("14. Яка логічна операція відповідає слову «або»?",
                            new String[]{"¬", "∧", "∨", "→"}, 2),
                    new Question("15. Для яких значень A і B висловлювання A → B хибне?",
                            new String[]{"A = false, B = true", "A = true, B = true", "A = false, B = false", "A = true, B = false"}, 3)
            };
        } else if (discipline.getName().equals("Стохастична фінансова математика / Stochastic Financial Mathematics (англ.мовою)")) {
            discipline.questions = new Question[]{
                    new Question("1. Що вивчає стохастична фінансова математика?",
                            new String[]{"Оптимізацію бізнес-процесів", "Випадкові процеси, які використовуються в моделюванні фінансових ринків", "Економіку підприємств", "Детерміновані рівняння"}, 1),
                    new Question("2. Який процес лежить в основі моделі Брауна?",
                            new String[]{"Детермінований рух", "Синусоїда", "Випадковий процес із незалежними приростами", "Експоненціальний розподіл"}, 2),
                    new Question("3. Що таке геометричний броунівський рух?",
                            new String[]{"Модель, яка не змінюється в часі", "Стохастичний процес для моделювання ціни активу", "Рівняння для облікової ставки", "Детермінована оцінка"}, 1),
                    new Question("4. Що описує рівняння Блека–Шоулза?",
                            new String[]{"Ціну акції", "Процентну ставку", "Ціноутворення європейських опціонів", "Інфляційний ризик"}, 2),
                    new Question("5. Яке припущення лежить в основі моделі Блека–Шоулза?",
                            new String[]{"Ціни зростають лінійно", "Ринок є неефективним", "Рух ціни — геометричний броунівський", "Всі трейдери діють ірраціонально"}, 2),
                    new Question("6. Що таке Ітô-лема?",
                            new String[]{"Рівняння теплопровідності", "Правило інтегрування функцій від стохастичних процесів", "Алгоритм регресії", "Формула похибки оцінки"}, 1),
                    new Question("7. Який тип інтеграла використовується в стохастичному численні?",
                            new String[]{"Рімана", "Лебега", "Ітô-інтеграл", "Фур’є"}, 2),
                    new Question("8. Що таке хеджування у фінансовій математиці?",
                            new String[]{"Інвестиція в ризиковий актив", "Уникнення податків", "Зменшення фінансового ризику за допомогою похідних інструментів", "Фіксація процентної ставки"}, 2),
                    new Question("9. Що таке варіація процесу?",
                            new String[]{"Значення функції", "Сума похідних", "Підсумок квадратів приростів процесу", "Глибина спаду"}, 2),
                    new Question("10. Що означає «нейтральна до ризику міра»?",
                            new String[]{"Міра, яка ігнорує волатильність", "Імовірнісна міра, при якій очікувана дохідність дорівнює безризиковій ставці", "Міра довіри до інвестора", "Коефіцієнт ліквідності"}, 1),
                    new Question("11. Яке стохастичне рівняння моделює процес доходності в моделі Васичека?",
                            new String[]{"R(t) = R(0)e^(σt)", "dR = a(b − R)dt + σdW", "dS/S = μdt + σdW", "dX = λX dt"}, 1),
                    new Question("12. Що є основним джерелом випадковості у фінансових моделях?",
                            new String[]{"Фіксовані коефіцієнти", "Стохастичні рівняння", "Емпіричні дані", "Змінна інфляція"}, 1),
                    new Question("13. Для чого використовується Monte Carlo simulation у фінансових задачах?",
                            new String[]{"Створення графіків", "Оцінка ймовірностей для складних випадкових процесів", "Прогноз погоди", "Збирання даних"}, 1),
                    new Question("14. Яка модель враховує стрибки (jumps) в цінах активів?",
                            new String[]{"Блек–Шоулз", "Модель Васичека", "Модель Мертона з стрибками", "Модель Каплана–Майєра"}, 2),
                    new Question("15. Який з фінансових інструментів є похідним (деривативом)?",
                            new String[]{"Акція", "Облігація", "Опціон", "Готівка"}, 2)
            };
        } else if (discipline.getName().equals("Функціональний аналіз")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке нормований простір?",
                            new String[]{"Множина з внутрішнім добутком", "Множина з визначеним інтегралом", "Лінійний простір з нормою, що визначає довжину векторів", "Система координат"}, 2),
                    new Question("2. Як називається операція, що перетворює вектор у скаляр, обчислюючи його довжину або величину?",
                            new String[]{"Векторний добуток", "Норма вектора", "Скалярний добуток", "Проєкція вектора"}, 1),
                    new Question("3. Що таке метричний простір?",
                            new String[]{"Простір з нормою", "Простір, у якому задано поняття відстані (метрика)", "Простір з базисом", "Сукупність неперервних функцій"}, 1),
                    new Question("4. Простір називається повним, якщо...",
                            new String[]{"У ньому існує скалярний добуток", "Усі обмежені послідовності збіжні", "Будь-яка фундаментальна послідовність збігається", "Немає нульових елементів"}, 2),
                    new Question("5. Що таке простір Банаха?",
                            new String[]{"Метричний простір", "Лінійний нормований повний простір", "Будь-який топологічний простір", "Функціональний підпростір"}, 1),
                    new Question("6. Що є ознакою простору Гільберта?",
                            new String[]{"Задана лише метрика", "Наявність внутрішнього добутку і повнота", "Відсутність лінійності", "Нескінченність базису"}, 1),
                    new Question("7. Який з виразів є внутрішнім добутком у \\( \\mathbb{R}^n \\)?",
                            new String[]{"Сума квадратів координат", "Сума добутків відповідних координат", "Різниця координат", "Векторна норма"}, 1),
                    new Question("8. Що означає неперервність лінійного оператора?",
                            new String[]{"Він зберігає напрямок векторів", "Має обмежену норму", "Обмеженість у точці рівнозначна загальній обмеженості", "Він інтегровний"}, 2),
                    new Question("9. Яка з умов гарантує компактність оператора у гільбертовому просторі?",
                            new String[]{"Збереження норми", "Зображення обмеженої множини має компактне замикання", "Лінійність", "Самоспряженість"}, 1),
                    new Question("10. Теорема Хана–Банаха стверджує, що...",
                            new String[]{"Будь-який функціонал можна продовжити зі збереженням норми", "Будь-яка функція неперервна", "Вектор можна розкласти в базис", "Усі простори скінченновимірні"}, 0),
                    new Question("11. Що таке ортонормована система в просторі Гільберта?",
                            new String[]{"Система з однаковими векторами", "Система, де всі вектори ортогональні і мають норму 1", "Сукупність базисів", "Неперервна функція"}, 1),
                    new Question("12. Що таке спектр оператора?",
                            new String[]{"Множина всіх значень норми", "Множина λ, для яких оператор (T – λI) не має оберненого", "Множина власних векторів", "Область збіжності ряду"}, 1),
                    new Question("13. Коли функціонал вважається слабко-* неперервним?",
                            new String[]{"Коли він диференційовний", "Коли він зберігає внутрішній добуток", "Коли збіжність функціоналів відображає точкову збіжність", "Коли значення не змінюється"}, 2),
                    new Question("14. Що таке лінійний функціонал?",
                            new String[]{"Функція, що має похідну", "Функція з двома змінними", "Лінійне відображення з простору у поле скалярів", "Векторна функція"}, 2),
                    new Question("15. Яка з теорем гарантує наявність ортонормованого базису у просторі Гільберта?",
                            new String[]{"Теорема Больцано", "Теорема Рісса", "Теорема Гільберта", "Теорема про ортогоналізацію Грама-Шмідта"}, 3)
            };
        } else if (discipline.getName().equals("Інтелектуальні системи")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке інтелектуальна система?",
                            new String[]{"Система автоматичного керування", "Система з можливістю приймати рішення, подібно до людини", "Система для створення графіки", "Операційна система"}, 1),
                    new Question("2. Яка з наведених галузей є частиною інтелектуальних систем?",
                            new String[]{"Мережева безпека", "Системне програмування", "Штучний інтелект", "Технічне креслення"}, 2),
                    new Question("3. Що таке експертна система?",
                            new String[]{"Програма для дизайну", "Інтелектуальна система, що імітує роботу експерта", "База даних користувачів", "Файл з інструкціями"}, 1),
                    new Question("4. Який компонент містить правила у продукційній експертній системі?",
                            new String[]{"Мотор виводу", "База знань", "Інтерфейс користувача", "Сенсорна мережа"}, 1),
                    new Question("5. Що таке штучна нейронна мережа?",
                            new String[]{"Мережа з оптичних волокон", "Математична модель, що імітує роботу нейронів мозку", "Операційна система", "Графічний редактор"}, 1),
                    new Question("6. Що таке навчання з учителем у машинному навчанні?",
                            new String[]{"Процес без міток", "Навчання з відгуком від користувача", "Навчання на основі пар вхід-вихід", "Навчання з випадковими даними"}, 2),
                    new Question("7. Що таке нечітка логіка?",
                            new String[]{"Програмування на низькому рівні", "Логіка з чіткими правилами", "Логіка, що дозволяє невизначеність і градації істинності", "Ігрова логіка"}, 2),
                    new Question("8. Який алгоритм найчастіше використовується для побудови дерева рішень?",
                            new String[]{"K-Means", "ID3", "Backpropagation", "Gradient Descent"}, 1),
                    new Question("9. Що таке машинне навчання?",
                            new String[]{"Навчання операційній системі", "Підбір дизайну програм", "Метод побудови моделей, що навчаються на даних", "Запуск комп’ютера в мережі"}, 2),
                    new Question("10. Яка мета системи рекомендацій?",
                            new String[]{"Сканування вірусів", "Оптимізація ресурсів ОС", "Прогнозування переваг користувача", "Видалення файлів"}, 2),
                    new Question("11. Що є входом у нейрон штучної нейронної мережі?",
                            new String[]{"Рядок коду", "Тільки зображення", "Вектор чисел з вагами", "Голосова команда"}, 2),
                    new Question("12. Яка функція використовується для активації у штучних нейронних мережах?",
                            new String[]{"Синус", "Функція втрат", "Сигмоїда", "Детермінант"}, 2),
                    new Question("13. Який метод використовують для навчання нейромережі зворотнім поширенням помилки?",
                            new String[]{"KNN", "Forward chaining", "Backpropagation", "Apriori"}, 2),
                    new Question("14. Яке поняття позначає якість передбачення моделі?",
                            new String[]{"Реалізація", "Точність (accuracy)", "Обсяг пам’яті", "Частота кадрів"}, 1),
                    new Question("15. Що таке агент в інтелектуальній системі?",
                            new String[]{"Підключення до сервера", "Сутність, що сприймає середовище і діє у ньому", "Назва процесу", "Вікно програми"}, 1)
            };
        } else if (discipline.getName().equals("Логічне програмування")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке логічне програмування?",
                            new String[]{"Парадигма, заснована на логіці предикатів", "Програмування графіки", "Робота з апаратним забезпеченням", "Програмування алгоритмів сортування"}, 0),
                    new Question("2. Яка мова є основною для логічного програмування?",
                            new String[]{"Java", "Prolog", "Python", "C++"}, 1),
                    new Question("3. Який оператор в Prolog означає логічне слідування (правило)?",
                            new String[]{":-", "->", "=>", "<-"}, 0),
                    new Question("4. Що таке факт у Prolog?",
                            new String[]{"Змінна", "Визначення істинного твердження", "Імпорт модуля", "Оператор обчислення"}, 1),
                    new Question("5. Що таке правило в Prolog?",
                            new String[]{"Інструкція повторення", "Опис збережених значень", "Логічне твердження з умовою", "Оператор додавання"}, 2),
                    new Question("6. Що таке уніфікація в логічному програмуванні?",
                            new String[]{"Вивід результатів", "Процес перевірки типів", "Процес зіставлення термів", "Сортування значень"}, 2),
                    new Question("7. Як називаються запити до бази знань у Prolog?",
                            new String[]{"Предикати", "Цілі", "Факти", "Масиви"}, 1),
                    new Question("8. Яке значення повертає Prolog у разі успішного виконання запиту?",
                            new String[]{"false", "null", "true", "1"}, 2),
                    new Question("9. Яка структура даних є базовою в Prolog?",
                            new String[]{"Масив", "Список", "Матриця", "Стек"}, 1),
                    new Question("10. Що таке рекурсія у логічному програмуванні?",
                            new String[]{"Тип змінної", "Виклик правил самим собою", "Обробка файлів", "Налаштування логіки"}, 1),
                    new Question("11. Яке ключове слово в Prolog використовується для запиту?",
                            new String[]{"query", "ask", ":-", "?-"}, 3),
                    new Question("12. Який принцип застосовується у висновку в логічному програмуванні?",
                            new String[]{"Індукція", "Інкапсуляція", "Модус поненс", "Дедукція"}, 3),
                    new Question("13. Як позначають змінні в Prolog?",
                            new String[]{"З великої літери", "З маленької літери", "З символу @", "У лапках"}, 0),
                    new Question("14. Що відбудеться, якщо Prolog не знайде рішення для запиту?",
                            new String[]{"Виведе помилку", "Програма завершиться", "Поверне false", "Згенерує випадкове значення"}, 2),
                    new Question("15. Як Prolog шукає рішення?",
                            new String[]{"За принципом стеку", "З допомогою жадібного алгоритму", "Зліва направо з backtracking", "Випадково"}, 2)
            };
        } else if (discipline.getName().equals("Машинне навчання / Machine Learning (англ. мовою)")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке машинне навчання?",
                            new String[]{"Алгоритм пошуку в інтернеті", "Процес написання коду вручну", "Підгалузь штучного інтелекту, де моделі навчаються з даних", "Інструмент для створення вебсайтів"}, 2),
                    new Question("2. Який із наведених типів навчання вимагає міток у даних?",
                            new String[]{"Навчання без учителя", "Підкріплення", "Навчання з учителем", "Підготовка даних"}, 2),
                    new Question("3. Який алгоритм використовується для задач класифікації?",
                            new String[]{"Linear Regression", "K-means", "KNN", "PCA"}, 2),
                    new Question("4. Що таке функція втрат (loss function)?",
                            new String[]{"Міра часу навчання", "Кількість параметрів", "Функція, яка оцінює помилку передбачення", "Оцінка розміру вибірки"}, 2),
                    new Question("5. Що означає перенавчання (overfitting)?",
                            new String[]{"Модель недотренована", "Модель має ідеальну точність", "Модель запам’ятала шум, втративши здатність до узагальнення", "Модель некоректно скомпільована"}, 2),
                    new Question("6. Яка метрика найбільше підходить для класифікації з незбалансованими класами?",
                            new String[]{"Accuracy", "Precision", "Recall", "F1-score"}, 3),
                    new Question("7. Що таке гіперпараметр у ML?",
                            new String[]{"Вихід моделі", "Вага нейрону", "Параметр, що задається до тренування і не оновлюється", "Результат активації"}, 2),
                    new Question("8. Який алгоритм використовується для зменшення розмірності даних?",
                            new String[]{"SVM", "PCA", "Naive Bayes", "Gradient Boosting"}, 1),
                    new Question("9. Який з алгоритмів є ансамблевим методом?",
                            new String[]{"Decision Tree", "SVM", "Random Forest", "Logistic Regression"}, 2),
                    new Question("10. У чому суть перехресної перевірки (cross-validation)?",
                            new String[]{"Повторення тренування на одній і тій самій підмножині", "Використання окремої тестової вибірки", "Оцінка моделі на кількох розбиттях даних", "Аналіз градієнтів"}, 2),
                    new Question("11. Що робить алгоритм K-means?",
                            new String[]{"Класифікує дані з мітками", "Навчає нейронну мережу", "Знаходить центри кластерів у немічених даних", "Оцінює ймовірність класу"}, 2),
                    new Question("12. Яка бібліотека Python популярна для побудови моделей ML?",
                            new String[]{"Flask", "Pandas", "Scikit-learn", "Matplotlib"}, 2),
                    new Question("13. Який тип регуляризації додає модуль коефіцієнтів до функції втрат?",
                            new String[]{"L1", "L2", "Dropout", "BatchNorm"}, 0),
                    new Question("14. Що таке дерево рішень?",
                            new String[]{"Метод зменшення розмірності", "Модель класифікації/регресії у вигляді послідовних розгалужень", "Графік функції", "Результат активації"}, 1),
                    new Question("15. Який алгоритм машинного навчання працює шляхом побудови гіперплощини?",
                            new String[]{"Naive Bayes", "SVM", "KNN", "Gradient Descent"}, 1)
            };
        } else if (discipline.getName().equals("Математичний аналіз")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке границя функції в точці?",
                            new String[]{"Максимальне значення", "Похідна у точці", "Значення, до якого прямує функція при наближенні аргументу до певної точки", "Межа області визначення"}, 2),
                    new Question("2. Яка з наведених формул є означенням похідної?",
                            new String[]{"f(x+h) – f(x)", "lim(h→0) [(f(x+h) – f(x)) / h]", "f(x) + f’(x)", "∫f(x)dx"}, 1),
                    new Question("3. Що таке неперервна функція?",
                            new String[]{"Функція з розривами", "Функція, яка всюди диференційовна", "Функція, значення якої змінюється стрибком", "Функція без розривів у заданій області"}, 3),
                    new Question("4. Яка похідна функції f(x) = sin(x)?",
                            new String[]{"cos(x)", "–cos(x)", "–sin(x)", "tg(x)"}, 0),
                    new Question("5. Обчисліть похідну функції f(x) = x².",
                            new String[]{"2", "x", "2x", "x²"}, 2),
                    new Question("6. Що таке визначений інтеграл?",
                            new String[]{"Площа під графіком на певному проміжку", "Граничне значення функції", "Графік похідної", "Нескінченна сума"}, 0),
                    new Question("7. Який з методів обчислення визначеного інтеграла є чисельним?",
                            new String[]{"Метод ланцюгового правила", "Метод Ньютона", "Метод прямокутників", "Метод підстановки"}, 2),
                    new Question("8. Що таке розклад функції в ряд Тейлора?",
                            new String[]{"Множення функцій", "Інтеграл від функції", "Розклад у степеневий ряд біля точки", "Добуток похідних"}, 2),
                    new Question("9. У якій точці функція f(x) = x³ – 3x має екстремум?",
                            new String[]{"x = 0", "x = ±1", "x = ±√3", "x = 3"}, 1),
                    new Question("10. Що таке необхідна умова екстремуму?",
                            new String[]{"f(x) = 0", "f’(x) = 0", "f''(x) > 0", "f(x) > 0"}, 1),
                    new Question("11. Чи може функція мати похідну, але не бути неперервною?",
                            new String[]{"Так", "Ні", "Тільки при x = 0", "Залежить від області"}, 1),
                    new Question("12. Що таке рівномірна збіжність ряду функцій?",
                            new String[]{"Збіжність при фіксованому x", "Збіжність похідної", "Однакова швидкість збіжності для всіх x з області", "Інтегрування по частинах"}, 2),
                    new Question("13. Який інтеграл називають невласним?",
                            new String[]{"Інтеграл, де підінтегральна функція неперервна", "Інтеграл з нескінченними межами або розривами", "Інтеграл з нульовими межами", "Інтеграл, що дорівнює нулю"}, 1),
                    new Question("14. Що таке диференціальне рівняння?",
                            new String[]{"Рівняння з похідною", "Рівняння з логарифмами", "Рівняння з модулем", "Система лінійних рівнянь"}, 0),
                    new Question("15. Яке з рівнянь є лінійним диференціальним рівнянням першого порядку?",
                            new String[]{"y'' + y = 0", "y' + y = x", "y² + y' = 0", "y = sin(x)"}, 1)
            };
        } else if (discipline.getName().equals("Методика викладання математики та інформатики у вищій школі")) {
            discipline.questions = new Question[]{
                    new Question("1. Яка з функцій вищої освіти є головною в контексті викладання математики та інформатики?",
                            new String[]{"Комерційна", "Репродуктивна", "Освітня і виховна", "Адміністративна"}, 2),
                    new Question("2. Що таке компетентнісний підхід у викладанні?",
                            new String[]{"Передавання лише теоретичних знань", "Навчання за підручником", "Формування знань, умінь і навичок для вирішення реальних задач", "Запам’ятовування фактів"}, 2),
                    new Question("3. Який із принципів є основним у дидактиці вищої школи?",
                            new String[]{"Єдність змісту і форми", "Наочність", "Науковість", "Формалізм"}, 2),
                    new Question("4. Що таке методика викладання?",
                            new String[]{"Збірник задач", "Наука про навчання", "Система форм, методів і засобів навчання певної дисципліни", "Рівень знань викладача"}, 2),
                    new Question("5. Який метод є доцільним при навчанні програмуванню?",
                            new String[]{"Пояснювально-ілюстративний", "Лабораторний", "Евристичний", "Проєктний"}, 3),
                    new Question("6. Що є основною формою організації навчального процесу у ВНЗ?",
                            new String[]{"Екскурсія", "Лекція", "Онлайн-опитування", "Самонавчання"}, 1),
                    new Question("7. Що таке індивідуалізація навчання?",
                            new String[]{"Навчання в групах", "Адаптація навчального процесу до потреб конкретного студента", "Стандартизоване оцінювання", "Іспит без варіантів"}, 1),
                    new Question("8. Який підхід вважається найефективнішим у викладанні інформатики?",
                            new String[]{"Репродуктивний", "Тестовий", "Практико-орієнтований", "Лекційний"}, 2),
                    new Question("9. Яка цифрова технологія є найпоширенішою у викладанні інформатики?",
                            new String[]{"Arduino", "MS Excel", "Системи управління навчанням (LMS)", "Статистичні калькулятори"}, 2),
                    new Question("10. Що таке рефлексія в педагогіці?",
                            new String[]{"Розв’язання задач", "Оцінка студентами діяльності викладача", "Самоаналіз учасників освітнього процесу", "Оцінка результатів тесту"}, 2),
                    new Question("11. Що таке Bloom’s Taxonomy у методиці викладання?",
                            new String[]{"Класифікація типів навчального контенту", "Типи лекцій", "Рівні когнітивних умінь у навчанні", "Оцінки за шкалою A–F"}, 2),
                    new Question("12. Яка компетентність формує викладання математики?",
                            new String[]{"Технологічна", "Мовна", "Логіко-аналітична", "Фізична"}, 2),
                    new Question("13. Що передбачає змішане навчання?",
                            new String[]{"Навчання тільки в аудиторії", "Використання лише тестів", "Поєднання очного і дистанційного форматів", "Тренінги викладачів"}, 2),
                    new Question("14. Який з етапів уроку передбачає формулювання теми і мети заняття?",
                            new String[]{"Мотивація", "Засвоєння нового матеріалу", "Актуалізація", "Закріплення"}, 0),
                    new Question("15. Який стиль викладання є найбільш ефективним у вищій школі?",
                            new String[]{"Авторитарний", "Партнерський", "Ігровий", "Формальний"}, 1)
            };
        } else if (discipline.getName().equals("Математична теорія ігор")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке стратегічна гра (гра в нормальній формі)?",
                            new String[]{"Гра, де результат залежить лише від випадку", "Гра, де учасники приймають рішення одночасно", "Гра з нескінченною кількістю стратегій", "Гра з одним гравцем"}, 1),
                    new Question("2. Який з варіантів є визначенням рівноваги Неша?",
                            new String[]{"Стан, коли обидва гравці виграють", "Стан, коли кожен гравець не має мотивації змінювати свою стратегію при фіксованих стратегіях інших", "Оптимальна стратегія", "Позиція з мінімальним виграшем"}, 1),
                    new Question("3. Що таке домінуюча стратегія?",
                            new String[]{"Стратегія, яка завжди програє", "Стратегія, яка дає гірший результат у всіх випадках", "Стратегія, яка не змінюється", "Стратегія, яка дає не гірший результат незалежно від дій суперника"}, 3),
                    new Question("4. У якій ситуації застосовується мінімаксна стратегія?",
                            new String[]{"У кооперативних іграх", "У випадкових іграх", "У двоосібних антагоністичних іграх", "У стохастичних процесах"}, 2),
                    new Question("5. Яка властивість притаманна грі з нульовою сумою?",
                            new String[]{"Сума виграшів усіх гравців завжди нуль", "Гравці мають спільну вигоду", "Можливі коаліції", "Використовується випадковий вибір"}, 0),
                    new Question("6. Що таке сідлова точка у матричній грі?",
                            new String[]{"Пара стратегій, де максимін дорівнює мінімаксу", "Точка максимуму", "Початкова стратегія", "Стратегія з найменшим ризиком"}, 0),
                    new Question("7. Яке з тверджень є вірним для рівноваги Неша в змішаних стратегіях?",
                            new String[]{"Гравці обирають лише одну стратегію", "Гравці випадковим чином обирають стратегії з певною ймовірністю", "Рішення приймає третя сторона", "Гравці завжди програють"}, 1),
                    new Question("8. Що означає повторювана гра?",
                            new String[]{"Гра з випадковим результатом", "Гра, що складається з багатьох раундів того самого типу", "Гра без стратегії", "Гра на вгадування"}, 1),
                    new Question("9. Яка стратегія є оптимальною у повторюваній грі типу «дилема в’язня» для кооперації?",
                            new String[]{"Завжди зраджувати", "Першим зрадити, далі копіювати", "Тіт-фо-тет (око за око)", "Випадковий вибір"}, 2),
                    new Question("10. Що таке кооперативна гра?",
                            new String[]{"Гра, де кожен гравець діє самостійно", "Гра з повною інформацією", "Гра, де дозволено формувати коаліції та ділити виграш", "Гра без виграшів"}, 2),
                    new Question("11. Яке значення має ядро (core) в кооперативній грі?",
                            new String[]{"Рівень складності", "Набір стратегій для рівноваги Неша", "Множина розподілів виграшу, що стабільні проти коаліцій", "Найменше можливе значення виграшу"}, 2),
                    new Question("12. Що таке функція корисності в теорії ігор?",
                            new String[]{"Функція, яка описує витрати гравця", "Функція, що визначає ризик", "Функція, яка відображає переваги гравця у вигляді чисел", "Формула для обчислення ймовірності"}, 2),
                    new Question("13. У якій грі результат залежить не тільки від стратегії гравця, а й від природи?",
                            new String[]{"Гра з нульовою сумою", "Стохастична гра", "Матрична гра", "Детермінована гра"}, 1),
                    new Question("14. Який із наведених підходів допомагає розв’язати гру в нормальній формі?",
                            new String[]{"Динамічне програмування", "Графи", "Табличний метод", "Симплекс-метод"}, 2),
                    new Question("15. Який метод застосовується для знаходження рівноваги Неша в змішаних стратегіях?",
                            new String[]{"Розв’язання системи лінійних рівнянь", "Ітераційний метод", "Метод Сімпсона", "Диференціювання функції"}, 0)
            };
        } else if (discipline.getName().equals("Багатозадачне та паралельне програмування")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке багатозадачність у програмуванні?",
                            new String[]{"Виконання кількох функцій однією програмою", "Здатність ОС одночасно виконувати кілька задач", "Використання одного потоку для всіх задач", "Зберігання кількох програм"}, 1),
                    new Question("2. Що таке потік (thread)?",
                            new String[]{"Окрема програма", "Файл конфігурації", "Одиниця виконання всередині процесу", "Серверна функція"}, 2),
                    new Question("3. Чим відрізняється процес від потоку?",
                            new String[]{"Процеси не мають пам'яті", "Потоки виконуються на інших комп’ютерах", "Процеси незалежні, потоки — спільно використовують ресурси", "Потоки повільніші"}, 2),
                    new Question("4. Який з механізмів використовується для синхронізації потоків?",
                            new String[]{"Компіляція", "Семафор", "Форматування", "Інтерфейс"}, 1),
                    new Question("5. Що таке стан гонки (race condition)?",
                            new String[]{"Оптимізований потік", "Конфлікт доступу до спільного ресурсу при одночасному виконанні", "Паралельна компіляція", "Цикл без завершення"}, 1),
                    new Question("6. Яка структура використовується для блокування доступу до ресурсу одним потоком за раз?",
                            new String[]{"Lock (блокування)", "Stack", "Array", "Event"}, 0),
                    new Question("7. Що таке deadlock (взаємне блокування)?",
                            new String[]{"Баг в графічному інтерфейсі", "Стан, коли два чи більше потоків чекають один одного", "Паралельне завершення задачі", "Рекурсивна помилка"}, 1),
                    new Question("8. Яка модель програмування дозволяє розділяти задачу на незалежні частини?",
                            new String[]{"Монолітна", "Синглтон", "Паралельна", "Функціональна"}, 2),
                    new Question("9. Що таке пул потоків (thread pool)?",
                            new String[]{"Група процесів ОС", "Набір попередньо створених потоків для повторного використання", "Тип пам’яті", "Список драйверів"}, 1),
                    new Question("10. Який з підходів реалізує паралелізм на рівні інструкцій процесора?",
                            new String[]{"OpenMP", "SIMD", "MapReduce", "Socket API"}, 1),
                    new Question("11. Що таке атомарна операція?",
                            new String[]{"Операція, що не виконується повністю", "Операція, яку можна перервати", "Неподільна операція, яка не допускає втручання інших потоків", "Операція над масивом"}, 2),
                    new Question("12. Що таке OpenMP?",
                            new String[]{"Фреймворк для вебпрограмування", "Мова програмування", "Бібліотека для паралельного програмування в C/C++", "База даних"}, 2),
                    new Question("13. Яка конструкція Java використовується для створення нового потоку?",
                            new String[]{"new Process()", "new Thread()", "createTask()", "forkThread()"}, 1),
                    new Question("14. Що таке конкурентне програмування?",
                            new String[]{"Програмування для смартфонів", "Використання GPU", "Робота з декількома потоками, що мають доступ до спільних ресурсів", "Штучний інтелект"}, 2),
                    new Question("15. Яке ключове слово в C# використовується для асинхронних методів?",
                            new String[]{"await", "async", "thread", "run"}, 1)
            };
        } else if (discipline.getName().equals("Основи мережевих технологій")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке IP-адреса?",
                            new String[]{
                                    "Унікальний ідентифікатор мережевого пристрою",
                                    "Тип мережевого кабелю",
                                    "Протокол передачі даних",
                                    "Мережевий комутатор"
                            }, 0),

                    new Question("2. Яка основна функція протоколу TCP?",
                            new String[]{
                                    "Передача відеопотоку",
                                    "Забезпечення надійності передачі даних",
                                    "Маршрутизація пакетів",
                                    "Кодування даних"
                            }, 1),

                    new Question("3. Що таке DNS?",
                            new String[]{
                                    "Система доменних імен, що перетворює домен на IP",
                                    "Протокол бездротового з'єднання",
                                    "Рядок HTTP‑запиту",
                                    "Сервер для зберігання файлів"
                            }, 0),

                    new Question("4. Яка модель описує сім рівнів мережевої взаємодії?",
                            new String[]{
                                    "TCP/IP",
                                    "OSI‑модель",
                                    "Ethernet‑структура",
                                    "HTTP‑архітектура"
                            }, 1),

                    new Question("5. Що таке MAC-адреса?",
                            new String[]{
                                    "Логічний адрес в мережі Інтернет",
                                    "Фізична адреса мережевого інтерфейсу",
                                    "Тип маршрутизатора",
                                    "Протокол шифрування"
                            }, 1),

                    new Question("6. Яку роль виконує маршрутизатор?",
                            new String[]{
                                    "Забезпечує бездротовий доступ",
                                    "Розподіляє IP‑адреси через DHCP",
                                    "Маршрутизує пакети між різними мережами",
                                    "Шифрує трафік"
                            }, 2),

                    new Question("7. Який протокол використовується для отримання IP-адреси автоматично?",
                            new String[]{
                                    "FTP",
                                    "SMTP",
                                    "DHCP",
                                    "ICMP"
                            }, 2),

                    new Question("8. Що таке підмережа (subnet)?",
                            new String[]{
                                    "Частина локальної мережі, визначена маскою підмережі",
                                    "Тип маршрутизатора",
                                    "Рівень у моделі OSI",
                                    "Протокол надійної передачі"
                            }, 0),

                    new Question("9. Для чого потрібен протокол ARP?",
                            new String[]{
                                    "Для пошуку IP-адреси по доменному імені",
                                    "Для прив’язки IP‑адреси до MAC-адреси",
                                    "Для передачі електронної пошти",
                                    "Для резервного копіювання"
                            }, 1),

                    new Question("10. Який порт за умовчанням використовує HTTP?",
                            new String[]{
                                    "21",
                                    "80",
                                    "443",
                                    "25"
                            }, 1),

                    new Question("11. Що таке протокол HTTPS?",
                            new String[]{
                                    "HTTP з шифруванням TLS/SSL",
                                    "Бездротовий мережевий стандарт",
                                    "Протокол передачі файлів",
                                    "Система доменних імен"
                            }, 0),

                    new Question("12. Який протокол використовується для передачі електронної пошти між серверами?",
                            new String[]{
                                    "FTP",
                                    "HTTP",
                                    "SMTP",
                                    "DHCP"
                            }, 2),

                    new Question("13. Що таке топологія «Зірка» (Star)?",
                            new String[]{
                                    "Кожен вузол підключений до центрального пристрою",
                                    "Усі вузли підключені послідовно одне до одного",
                                    "Вузли утворюють кільце",
                                    "Мережа на базі бездротових access point"
                            }, 0),

                    new Question("14. Який протокол забезпечує безпомилкову передачу даних на канальному рівні?",
                            new String[]{
                                    "UDP",
                                    "TCP",
                                    "Ethernet",
                                    "PPP"
                            }, 3),

                    new Question("15. Що таке NAT?",
                            new String[]{
                                    "Протокол безпеки",
                                    "Механізм трансляції внутрішніх IP-адрес у зовнішні",
                                    "Тип кабелю",
                                    "Мережевий комутатор"
                            }, 1)
            };
        } else if (discipline.getName().equals("Об'єктно-орієнтоване програмування")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке об'єкт в ООП?",
                            new String[]{"Процедура в програмі", "Змінна типу integer", "Екземпляр класу", "Функція, що повертає значення"}, 2),
                    new Question("2. Що таке клас в ООП?",
                            new String[]{"Тип алгоритму", "Формат файлу", "Шаблон для створення об'єктів", "Змінна, яка зберігає значення"}, 2),
                    new Question("3. Яке з понять не належить до принципів ООП?",
                            new String[]{"Інкапсуляція", "Наслідування", "Поліморфізм", "Рекурсія"}, 3),
                    new Question("4. Який модифікатор доступу дозволяє доступ тільки всередині класу?",
                            new String[]{"public", "private", "protected", "external"}, 1),
                    new Question("5. Що таке конструктор у класі?",
                            new String[]{"Метод для обчислення суми", "Функція з головною логікою", "Спеціальний метод для ініціалізації об'єкта", "Масив об'єктів"}, 2),
                    new Question("6. Який принцип ООП означає можливість створення похідних класів?",
                            new String[]{"Інкапсуляція", "Абстракція", "Наслідування", "Узагальнення"}, 2),
                    new Question("7. Як називається можливість одного інтерфейсу мати кілька реалізацій?",
                            new String[]{"Наслідування", "Поліморфізм", "Інкапсуляція", "Композиція"}, 1),
                    new Question("8. Що таке абстракція в ООП?",
                            new String[]{"Приховування всієї логіки", "Створення класу без властивостей", "Виділення суттєвих характеристик", "Повне дублювання коду"}, 2),
                    new Question("9. Як називається клас, який не можна інстанціювати?",
                            new String[]{"Інтерфейс", "Абстрактний клас", "Звичайний клас", "Клас-масив"}, 1),
                    new Question("10. Яка конструкція використовується в Java для наслідування класів?",
                            new String[]{"inherits", "extends", "implements", "override"}, 1),
                    new Question("11. Що таке перевантаження методу (overloading)?",
                            new String[]{"Зміна тіла методу", "Кілька методів з однаковим ім’ям, але різними параметрами", "Видалення методу", "Оголошення методу в іншому класі"}, 1),
                    new Question("12. Що таке перевизначення методу (overriding)?",
                            new String[]{"Оголошення методу без реалізації", "Виклик методу з батьківського класу", "Зміна поведінки методу в похідному класі", "Перетворення типів"}, 2),
                    new Question("13. Що таке інтерфейс у Java?",
                            new String[]{"Тип масиву", "Клас із приватними методами", "Контракт, який описує методи без реалізації", "Модуль пам'яті"}, 2),
                    new Question("14. Що таке this у контексті класу?",
                            new String[]{"Назва методу", "Посилання на поточний об'єкт", "Ключове слово для зупинки програми", "Ідентифікатор змінної"}, 1),
                    new Question("15. Що таке композиція в ООП?",
                            new String[]{"Використання одного об'єкта в іншому як частини", "Зміна типів", "Видалення об'єктів", "Підключення бібліотек"}, 0)
            };
        } else if (discipline.getName().equals("Методи оптимізації та дослідження операцій")) {
            discipline.questions = new Question[]{
                    new Question("1. Що вивчає дослідження операцій?",
                            new String[]{"Фізичні процеси в техніці", "Оптимізацію складних систем прийняття рішень", "Інженерні креслення", "Графічний дизайн"}, 1),
                    new Question("2. Що є метою задачі лінійного програмування?",
                            new String[]{"Знайти похідну функції", "Обчислити границю", "Максимізувати або мінімізувати цільову функцію при заданих обмеженнях", "Знайти інтеграл"}, 2),
                    new Question("3. Яка з наведених форм є канонічною формою задачі лінійного програмування?",
                            new String[]{"Мінімізувати f(x) без обмежень", "Задача з рівняннями та невід’ємними змінними", "Детермінантна форма", "Функція багатьох змінних"}, 1),
                    new Question("4. Який метод найчастіше використовується для розв’язання задачі лінійного програмування?",
                            new String[]{"Метод Крамера", "Метод Гауса", "Симплекс-метод", "Ітераційний метод"}, 2),
                    new Question("5. Що таке базисне розв’язання в симплекс-методі?",
                            new String[]{"Розв’язання системи рівнянь з усіма змінними", "Розв’язання, в якому всі змінні позитивні", "Розв’язання, де деякі змінні = 0, а решта — базисні", "Функція максимуму"}, 2),
                    new Question("6. Яка задача є прикладом транспортної задачі?",
                            new String[]{"Мінімізувати обсяг пам’яті", "Максимізувати прибуток компанії", "Оптимальний розподіл товарів між складами і магазинами", "Розв’язання рівнянь з похідними"}, 2),
                    new Question("7. Що таке граф у контексті методів оптимізації?",
                            new String[]{"Малюнок", "Набір точок", "Математична структура з вершинами та ребрами", "Формула для розкладу"}, 2),
                    new Question("8. Який алгоритм використовується для знаходження найкоротшого шляху в графі?",
                            new String[]{"Квіксорт", "Симплекс-метод", "Алгоритм Дейкстри", "Метод Лагранжа"}, 2),
                    new Question("9. Який метод застосовується для розв’язання задачі про призначення?",
                            new String[]{"Жадібний алгоритм", "Гілок і меж", "Метод Гауса", "Угорський метод"}, 3),
                    new Question("10. Що таке цільова функція?",
                            new String[]{"Обмеження задачі", "Допоміжна функція", "Функція, яку потрібно оптимізувати", "Гранична функція"}, 2),
                    new Question("11. Що таке двоїста задача в лінійному програмуванні?",
                            new String[]{"Задача з двома цільовими функціями", "Задача для перевірки точності", "Задача, що відповідає початковій та дає додаткову інформацію про неї", "Інтегральне перетворення"}, 2),
                    new Question("12. У якій задачі використовується поняття «план перевезень»?",
                            new String[]{"Задача про призначення", "Транспортна задача", "Задача про розфарбування графа", "Задача лінійної регресії"}, 1),
                    new Question("13. Яке з тверджень про симплекс-таблицю є вірним?",
                            new String[]{"У ній зберігається граф", "Це таблиця з даними для обчислення похідної", "Це структура для ітераційного поліпшення розв’язку", "Вона будується лише вручну"}, 2),
                    new Question("14. Яке з тверджень стосується методу гілок і меж?",
                            new String[]{"Застосовується для задачі пошуку коренів рівняння", "Застосовується в цілочисельному програмуванні", "Використовується для обчислення інтегралів", "Застосовується до транспортної задачі"}, 1),
                    new Question("15. Що таке допустиме розв’язання?",
                            new String[]{"Розв’язання без обмежень", "Розв’язання, що не задовольняє жодне обмеження", "Розв’язання, що задовольняє всі обмеження задачі", "Мінімум цільової функції"}, 2)
            };
        } else if (discipline.getName().equals("Управління проєктами в інженерії програмного забезпечення")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке проєкт в контексті ІПЗ?",
                            new String[]{"Набір вимог до ПЗ", "Тимчасова діяльність з чіткою метою — створити унікальний продукт або послугу", "Файл з кодом", "Серверна конфігурація"}, 1),
                    new Question("2. Який з етапів є першим у життєвому циклі ПЗ?",
                            new String[]{"Розгортання", "Підтримка", "Аналіз вимог", "Кодування"}, 2),
                    new Question("3. Який документ описує цілі, межі, бюджет та терміни проєкту?",
                            new String[]{"Довідка про виконання", "Проєктний план", "User Story", "SLA"}, 1),
                    new Question("4. Яка методологія управління проєктами ПЗ передбачає гнучкість і ітеративність?",
                            new String[]{"Waterfall", "V-модель", "Agile", "CMMI"}, 2),
                    new Question("5. Що є ключовим документом у Scrum-процесі?",
                            new String[]{"Project Charter", "Backlog продукту", "UML-діаграма", "RACI-матриця"}, 1),
                    new Question("6. Що означає скорочення MVP у розробці ПЗ?",
                            new String[]{"Minimum Verified Plan", "Most Valuable Phase", "Minimum Viable Product", "Model Visualization Protocol"}, 2),
                    new Question("7. Який з інструментів часто використовується для візуалізації завдань у Scrum?",
                            new String[]{"PERT-діаграма", "Канбан-дошка", "UML-схема", "ER-модель"}, 1),
                    new Question("8. Яка роль відповідає за максимізацію цінності продукту в Scrum?",
                            new String[]{"Scrum Master", "Product Owner", "Project Manager", "Business Analyst"}, 1),
                    new Question("9. Який інструмент використовують для визначення найтривалішої послідовності залежних завдань у проєкті?",
                            new String[]{"PERT-діаграма", "Метод критичного шляху (CPM)", "Sprint backlog", "Burn-down chart"}, 1),
                    new Question("10. Що входить у трикутник обмежень проєкту?",
                            new String[]{"Якість, інтерфейс, база даних", "Час, вартість, обсяг", "Бюджет, звіт, Scrum", "Тестування, безпека, API"}, 1),
                    new Question("11. Що таке ризик у проєкті?",
                            new String[]{"Подія, що точно станеться", "Вартість роботи", "Невизначена подія, що може вплинути на цілі проєкту", "Тип мови програмування"}, 2),
                    new Question("12. Який метод оцінки тривалості завдань використовує формулу (О + 4Н + П)/6?",
                            new String[]{"Метод Монте-Карло", "PERT-аналіз", "Метод критичного ланцюга", "TCO"}, 1),
                    new Question("13. Який із інструментів керування проєктами є хмарним сервісом?",
                            new String[]{"MS Excel", "JIRA", "Visual Studio", "Wireshark"}, 1),
                    new Question("14. Що таке sprint у Scrum?",
                            new String[]{"Зустріч команди", "Інтервал, протягом якого створюється інкремент продукту", "Оцінка ризиків", "Код модуля"}, 1),
                    new Question("15. Яка модель зрілості процесів включає 5 рівнів розвитку організації?",
                            new String[]{"Waterfall", "TOGAF", "CMMI", "Scrum"}, 2)
            };
        } else if (discipline.getName().equals("Методологія наукових досліджень в програмній інженерії")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке наукове дослідження?",
                            new String[]{"Систематизований процес здобуття нових знань", "Опис алгоритму", "Інтерфейсна розробка", "Базова програма"}, 0),
                    new Question("2. Яка з форм є типовою для наукового результату в ІПЗ?",
                            new String[]{"Новий дизайн сайту", "Удосконалення методу, моделі або алгоритму", "Встановлення прав доступу", "Оновлення бібліотеки"}, 1),
                    new Question("3. Що таке гіпотеза в контексті дослідження?",
                            new String[]{"Формат звіту", "Файл з даними", "Припущення, яке перевіряється в дослідженні", "Оцінка теми"}, 2),
                    new Question("4. Який етап передує формулюванню цілей дослідження?",
                            new String[]{"Захист результатів", "Висновки", "Аналіз стану проблеми та літератури", "Оцінка значущості даних"}, 2),
                    new Question("5. Що таке актуальність у науковій роботі?",
                            new String[]{"Оформлення ілюстрацій", "Чіткість викладу", "Обґрунтування необхідності дослідження", "Стиль мови"}, 2),
                    new Question("6. Який з методів є емпіричним?",
                            new String[]{"Аналіз джерел", "Спостереження, експеримент, тестування", "Формалізація", "Аксіоматичний"}, 1),
                    new Question("7. Що таке наукова новизна?",
                            new String[]{"Повторення вже відомих даних", "Оригінальність підходу або результату", "Обсяг теорії", "Глибина рецензії"}, 1),
                    new Question("8. Що є об’єктом дослідження?",
                            new String[]{"Конкретна програма", "Предметна галузь або явище, що досліджується", "Інтерфейс", "Список літератури"}, 1),
                    new Question("9. Як називається розділ, у якому обґрунтовується вибір методів?",
                            new String[]{"Актуальність", "Огляд літератури", "Методика дослідження", "Вступ"}, 2),
                    new Question("10. Який стиль використовується для посилань у наукових роботах з ІТ?",
                            new String[]{"Художній", "APA або ДСТУ", "MLA", "Рекламний"}, 1),
                    new Question("11. Що таке рецензія на наукову роботу?",
                            new String[]{"Оцінка куратора", "Критичний аналіз фахівця з галузі", "Опис теми", "Форма титульної сторінки"}, 1),
                    new Question("12. Який документ оформлюють після завершення дослідження?",
                            new String[]{"Протокол запуску", "Код у GitHub", "Науковий звіт або дипломна робота", "Технічна карта"}, 2),
                    new Question("13. Що таке верифікація в дослідженні ІПЗ?",
                            new String[]{"Оформлення графіків", "Підтвердження коректності результатів програмної реалізації", "Аналіз літератури", "Тестування електронної пошти"}, 1),
                    new Question("14. Який з методів дослідження найчастіше застосовується для оцінки ефективності алгоритмів?",
                            new String[]{"Інтерв’ю", "Експеримент", "Анкетування", "Статистичний прогноз"}, 1),
                    new Question("15. Що таке плагіат у науковій роботі?",
                            new String[]{"Посилання на джерела", "Повтор теми", "Привласнення чужого тексту або ідей без відповідного цитування", "Копіювання структури"}, 2)
            };
        } else if (discipline.getName().equals("Системне програмування")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке системне програмування?",
                            new String[]{"Програмування вебсторінок", "Розробка прикладного ПЗ", "Розробка програм, що взаємодіють з апаратним забезпеченням", "Створення баз даних"}, 2),
                    new Question("2. Яке основне призначення операційної системи?",
                            new String[]{"Графічне оформлення", "Зберігання мультимедіа", "Керування ресурсами комп’ютера", "Захист від вірусів"}, 2),
                    new Question("3. Що таке системний виклик?",
                            new String[]{"Функція в бібліотеці", "Команда користувача", "Механізм взаємодії користувача з BIOS", "Інтерфейс для взаємодії з ядром ОС"}, 3),
                    new Question("4. Яка з мов програмування найчастіше використовується в системному програмуванні?",
                            new String[]{"Java", "Python", "C", "SQL"}, 2),
                    new Question("5. Що таке ядро операційної системи?",
                            new String[]{"Графічна оболонка", "Центральна частина ОС, що керує ресурсами", "Антивірусна програма", "Служба друку"}, 1),
                    new Question("6. Що таке буфер обміну в контексті введення/виведення?",
                            new String[]{"Файл для зберігання даних", "Область пам’яті для тимчасового збереження даних", "Реєстр процесора", "Тип шини"}, 1),
                    new Question("7. Який режим дозволяє доступ до привілейованих інструкцій процесора?",
                            new String[]{"Користувацький режим", "Гостьовий режим", "Режим ядра", "Віртуальний режим"}, 2),
                    new Question("8. Що таке драйвер пристрою?",
                            new String[]{"Пристрій введення", "Файл з налаштуваннями", "Програма, що керує роботою апаратного пристрою", "Бібліотека користувача"}, 2),
                    new Question("9. Що таке розподіл пам’яті?",
                            new String[]{"Сортування файлів", "Визначення меж для процесів у пам’яті", "Зміна розміру екрана", "Перенаправлення потоків"}, 1),
                    new Question("10. Який із нижче наведених механізмів використовується для міжпроцесної взаємодії?",
                            new String[]{"POST-запити", "API функції", "Сокети", "GET-запити"}, 2),
                    new Question("11. Що таке стек у контексті процесора?",
                            new String[]{"Тип зовнішньої пам’яті", "Структура для зберігання кадрів виклику функцій", "Програма архівації", "Інтерфейс користувача"}, 1),
                    new Question("12. Що таке переривання (interrupt)?",
                            new String[]{"Процес завершення ОС", "Сигнал, що змінює хід виконання програми", "Помилка виконання", "Тип процесора"}, 1),
                    new Question("13. Яка програма компілює код асемблера?",
                            new String[]{"Компоновник", "Інтерпретатор", "Асемблер", "Редактор коду"}, 2),
                    new Question("14. Який системний виклик використовується для створення нового процесу в UNIX-подібних ОС?",
                            new String[]{"exec()", "open()", "fork()", "wait()"}, 2),
                    new Question("15. Що таке таблиця дескрипторів файлів?",
                            new String[]{"Графічний інтерфейс для файлів", "Масив назв файлів", "Список відкритих файлів у процесі", "Реєстр користувачів"}, 2)
            };
        } else if (discipline.getName().equals("Теорія функції комплексної змінної")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке аналітична функція в області D?",
                            new String[]{"Функція, що має похідну в одній точці", "Функція, що має похідну всюди в D", "Функція, що не обмежена", "Функція з розривом в точці"}, 1),
                    new Question("2. Яка умова є необхідною для диференційовності функції комплексної змінної?",
                            new String[]{"Неперервність модуля", "Наявність похідної в ℝ", "Виконання умов Коші–Рімана", "Реальність значення"}, 2),
                    new Question("3. Формула Коші для функції f(z) визначає:",
                            new String[]{"Значення модуля функції", "Обчислення похідної", "Значення функції через інтеграл по контуру", "Ряд Лорана"}, 2),
                    new Question("4. Що таке ізольована особлива точка функції?",
                            new String[]{"Точка, де функція зникає", "Точка, де похідна нескінченна", "Точка, де функція не визначена, але визначена в околі", "Справжнє число"}, 2),
                    new Question("5. Якщо f(z) аналітична в області, то інтеграл по замкненому контуру дорівнює:",
                            new String[]{"Модулю функції", "Залишку", "Нулю", "Нескінченності"}, 2),
                    new Question("6. Що таке залишок (residue) у точці z₀?",
                            new String[]{"Значення похідної", "Коефіцієнт при (z - z₀)^(-1) в ряді Лорана", "Максимум функції", "Реальна частина функції"}, 1),
                    new Question("7. Який інтеграл використовується для обчислення кількості нулів функції всередині контуру?",
                            new String[]{"Інтеграл Лапласа", "Інтеграл Пуассона", "Інтеграл аргументу", "Інтеграл залишків"}, 2),
                    new Question("8. У якій точці аналітична функція може мати полюс?",
                            new String[]{"У будь-якій точці, де вона нуль", "У точці, де її значення нескінченне", "У точці неперервності", "У точці максимуму"}, 1),
                    new Question("9. Що таке розширення функції у ряд Лорана?",
                            new String[]{"Сума похідних", "Розклад у ряд Тейлора з від’ємними степенями", "Сума модулів", "Розв’язок інтеграла"}, 1),
                    new Question("10. Яка умова є достатньою для аналітичності функції?",
                            new String[]{"Наявність похідної у двох точках", "Виконання умов Коші–Рімана та неперервність похідних", "Модуль рівний нулю", "Функція має максимум"}, 1),
                    new Question("11. Що таке головна частина ряду Лорана?",
                            new String[]{"Степеневі доданки з додатними показниками", "Члени з від’ємними степенями", "Постійна частина", "Значення на колі"}, 1),
                    new Question("12. Що таке однозв’язна область?",
                            new String[]{"Область без внутрішніх точок", "Область, де всі контури можна звести до точки", "Область із розривом", "Область з полюсом"}, 1),
                    new Question("13. Як називається сингулярність, в околі якої головна частина нескінченна?",
                            new String[]{"Знімна", "Полюс", "Суттєва", "Аналогова"}, 2),
                    new Question("14. У чому суть теореми про залишки?",
                            new String[]{"Формула для обчислення похідної", "Метод наближення", "Обчислення контурного інтегралу через суму залишків", "Побудова ряду Фур'є"}, 2),
                    new Question("15. Що таке голоморфна функція?",
                            new String[]{"Неперервна функція", "Функція, яка має похідну всюди в області", "Функція без границь", "Функція з дійсними значеннями"}, 1)
            };
        } else if (discipline.getName().equals("Теорія керування")) {
            discipline.questions = new Question[]{
                    new Question("1. Що вивчає теорія керування?",
                            new String[]{"Фізичні коливання", "Статистичні дані", "Принципи побудови і аналізу керованих динамічних систем", "Теорію чисел"}, 2),
                    new Question("2. Що таке об'єкт керування?",
                            new String[]{"Пристрій в інтернеті", "Сигнал з шумом", "Система, на яку впливають з метою зміни її поведінки", "Значення параметра"}, 2),
                    new Question("3. Що таке керованість системи?",
                            new String[]{"Можливість виміряти параметри", "Можливість перевірити стабільність", "Можливість перевести систему з будь-якого стану в будь-який інший", "Можливість побудови графіка"}, 2),
                    new Question("4. Яке з наведених є математичною моделлю динамічної системи?",
                            new String[]{"Алгоритм шифрування", "Інтегральне рівняння", "Диференціальне рівняння", "Тригонометричний ряд"}, 2),
                    new Question("5. Що таке стійкість системи керування?",
                            new String[]{"Спроможність не змінювати параметри", "Здатність зменшити похибку", "Здатність повернутися до рівноваги після збурення", "Періодичність дії"}, 2),
                    new Question("6. Що таке передатна функція?",
                            new String[]{"Сигнал у часі", "Відношення виходу до входу в області частот", "Формула коефіцієнтів", "Механізм регуляції"}, 1),
                    new Question("7. Як перевірити стійкість лінійної системи?",
                            new String[]{"За частотою входу", "Методом Ляпунова", "Інтегруванням", "Аналізом коефіцієнтів Фур'є"}, 1),
                    new Question("8. Яка типова динамічна ланка має передатну функцію 1/(Ts+1)?",
                            new String[]{"Інтегруюча", "Пропорційна", "Інерційна першого порядку", "Деривативна"}, 2),
                    new Question("9. Що таке зворотній зв’язок у системах керування?",
                            new String[]{"Вхідний сигнал", "Вивід, що впливає на вхід", "Постійна складова сигналу", "Налаштування амплітуди"}, 1),
                    new Question("10. Що характеризує час встановлення (settling time) у перехідному процесі системи керування?",
                            new String[]{"Час досягнення системою першого піку", "Час, за який вихідна величина вперше досягає нового значення", "Час, необхідний для того, щоб коливання вихідної величини зменшилися до заданого відсотка від кінцевого значення", "Час, коли система стає нестабільною"}, 2),
                    new Question("11. Який елемент формує сигнал управління на основі похибки?",
                            new String[]{"Сенсор", "Регулятор", "Об'єкт", "Виконавчий пристрій"}, 1),
                    new Question("12. Яке рівняння описує дискретну систему керування?",
                            new String[]{"Диференціальне", "Різницеве", "Інтегральне", "Логарифмічне"}, 1),
                    new Question("13. Який тип системи забезпечує нульову усталену похибку для одиничного входу?",
                            new String[]{"Інерційна", "Пропорційна", "Ідеальний інтегратор", "Система з диференціатором"}, 2),
                    new Question("14. Що означає стабілізація системи?",
                            new String[]{"Зміна її структури", "Налаштування амплітуди сигналу", "Забезпечення її стійкості", "Зменшення періоду"}, 2),
                    new Question("15. Яке з тверджень відповідає принципу суперпозиції?",
                            new String[]{"Система не залежить від початкових умов", "Сума реакцій відповідає сумі впливів", "Будь-який вхід призводить до сталого стану", "Система не має похибки"}, 1)
            };
        } else if (discipline.getName().equals("Вебпрограмування")) {
            discipline.questions = new Question[]{
                    new Question("1. Яке призначення HTML у вебпрограмуванні?",
                            new String[]{"Опис логіки роботи сайту", "Визначення структури вебсторінки", "Зберігання даних", "Шифрування контенту"}, 1),
                    new Question("2. Який тег HTML використовується для додавання зображення?",
                            new String[]{"<img>", "<image>", "<pic>", "<src>"}, 0),
                    new Question("3. Що таке CSS?",
                            new String[]{"Мова програмування", "Мова для обміну даними", "Мова стилів для оформлення сторінок", "Система безпеки"}, 2),
                    new Question("4. Який з варіантів є мовою програмування для клієнтської частини?",
                            new String[]{"PHP", "Python", "JavaScript", "SQL"}, 2),
                    new Question("5. Що таке DOM у вебпрограмуванні?",
                            new String[]{"Бібліотека JavaScript", "Об’єктна модель документа", "Файл конфігурації", "Протокол передачі даних"}, 1),
                    new Question("6. Який метод JavaScript використовується для обробки подій кліку?",
                            new String[]{"click()", "addClick()", "onclick", "handle()"}, 2),
                    new Question("7. Який тег HTML використовується для створення форми?",
                            new String[]{"<input>", "<form>", "<submit>", "<field>"}, 1),
                    new Question("8. Що таке HTTP?",
                            new String[]{"Мова стилів", "Формат картинки", "Протокол передачі гіпертексту", "Браузерна функція"}, 2),
                    new Question("9. Який код статусу HTTP означає «Успішно»?",
                            new String[]{"200", "404", "500", "302"}, 0),
                    new Question("10. Що таке AJAX у веброзробці?",
                            new String[]{"Бібліотека JavaScript", "Фреймворк CSS", "Технологія асинхронного обміну даними", "База даних"}, 2),
                    new Question("11. Яка мова найчастіше використовується для серверної частини?",
                            new String[]{"HTML", "CSS", "Java", "PHP"}, 3),
                    new Question("12. Що таке API у вебпрограмуванні?",
                            new String[]{"Інструмент стилізації", "Фреймворк", "Інтерфейс для взаємодії програм", "Компонент бази даних"}, 2),
                    new Question("13. Який з типів HTTP-запитів використовується для отримання даних?",
                            new String[]{"POST", "PUT", "DELETE", "GET"}, 3),
                    new Question("14. Яке розширення мають файли таблиць стилів?",
                            new String[]{".html", ".php", ".css", ".js"}, 2),
                    new Question("15. Який фреймворк JavaScript призначений для створення односторінкових застосунків?",
                            new String[]{"jQuery", "Bootstrap", "React", "SASS"}, 2)
            };
        } else if (discipline.getName().equals("Основи вебтехнологій")) {
            discipline.questions = new Question[]{
                    new Question("1. Яке призначення HTML у веброзробці?",
                            new String[]{"Створення стилів для сторінки", "Визначення структури вебсторінки", "Обробка запитів на сервері", "Створення баз даних"}, 1),
                    new Question("2. Який тег використовується для створення гіперпосилання в HTML?",
                            new String[]{"<link>", "<a>", "<href>", "<url>"}, 1),
                    new Question("3. Що таке CSS?",
                            new String[]{"Мова програмування", "Мова запитів до бази даних", "Мова стилів для оформлення вебсторінок", "Фреймворк JavaScript"}, 2),
                    new Question("4. Яка структура HTML-документа є обов’язковою?",
                            new String[]{"<head>, <body>", "<html>, <head>, <body>", "<header>, <footer>", "<main>, <section>"}, 1),
                    new Question("5. Який атрибут тега <img> вказує шлях до зображення?",
                            new String[]{"href", "src", "alt", "path"}, 1),
                    new Question("6. Яке призначення JavaScript у веброзробці?",
                            new String[]{"Створення стилів", "Визначення структури", "Додавання інтерактивності", "Зберігання даних"}, 2),
                    new Question("7. Який метод JavaScript використовується для виведення повідомлення?",
                            new String[]{"alert()", "print()", "echo()", "console()"}, 0),
                    new Question("8. Що таке DOM у контексті вебтехнологій?",
                            new String[]{"Формат даних", "Серверна мова", "Об’єктна модель документа", "Браузерний плагін"}, 2),
                    new Question("9. Який протокол використовується для передачі вебсторінок?",
                            new String[]{"FTP", "SMTP", "HTTP", "IP"}, 2),
                    new Question("10. Що таке URL?",
                            new String[]{"Мова запитів", "Інструмент веброзробки", "Адреса ресурсу в Інтернеті", "Структура HTML"}, 2),
                    new Question("11. Який тег HTML використовується для створення списку з маркерами?",
                            new String[]{"<ol>", "<ul>", "<li>", "<dl>"}, 1),
                    new Question("12. Який селектор у CSS відповідає за вибір елементів за їх класом?",
                            new String[]{"#", ".", "/", "@"}, 1),
                    new Question("13. Що таке client-server модель?",
                            new String[]{"Модель для обробки зображень", "Система керування стилями", "Модель взаємодії клієнта і сервера", "База даних"}, 2),
                    new Question("14. Яка функція відповідає за обробку подій у JavaScript?",
                            new String[]{"eventHandler()", "onclick", "getElementById()", "loadDocument()"}, 1),
                    new Question("15. Що таке форма (form) в HTML?",
                            new String[]{"Засіб стилізації", "Розмітка таблиці", "Інструмент взаємодії користувача з вебсторінкою", "Компонент навігації"}, 2)
            };
        } else if (discipline.getName().equals("Технології чисельного моделювання")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке чисельне моделювання?",
                            new String[]{"Емпіричне спостереження", "Використання числових методів для апроксимації розв’язків задач", "Проектування інтерфейсу", "Ручне обчислення формул"}, 1),
                    new Question("2. Який метод використовують для інтегрування звичайних диференціальних рівнянь?",
                            new String[]{"Метод Монте-Карло", "Метод Ейлера", "Метод Хаусхолдера", "Метод Грама–Шмідта"}, 1),
                    new Question("3. У чому перевага методу Рунге–Кутти над Ейлером?",
                            new String[]{"Менша складність", "Вища точність при тому ж кроці", "Менший об’єм пам’яті", "Відсутність помилки"}, 1),
                    new Question("4. Що таке сітка у чисельному моделюванні?",
                            new String[]{"UI-компонент", "Розбиття області на дискретні точки", "Графік функції", "Множина випадкових змінних"}, 1),
                    new Question("5. Який метод застосовують для крайових задач у частинних рівняннях?",
                            new String[]{"Метод Монте-Карло", "Метод скінченних різниць", "Метод Ньютон–Рафсона", "Метод цілочисленного програмування"}, 1),
                    new Question("6. Що таке явний метод чисельного інтегрування?",
                            new String[]{"Залежить лише від попереднього кроку", "Залежить від майбутніх значень", "Не має похибки", "Інтегрується вручну"}, 0),
                    new Question("7. Що таке неявний метод?",
                            new String[]{"Метод без похибки", "Залежить також від майбутнього кроку", "Слабка точність", "Метод лише для ODE"}, 1),
                    new Question("8. Який метод підходить для жорстких систем?",
                            new String[]{"Явний Ейлер", "Явний Рунге–Кутта", "Неявний Бекстроп", "Метод простої ітерації"}, 2),
                    new Question("9. Що таке стабільність методу?",
                            new String[]{"Наявність похибки", "Збереження обмеженості розв’язку при малих збуреннях", "Швидкість алгоритму", "Безпека пам’яті"}, 1),
                    new Question("10. Який з методів інтегрування має найвищу точність серед перелічених?",
                            new String[]{"Прямокутник", "Трапеції", "Сімпсон", "Монте-Карло"}, 2),
                    new Question("11. Що таке рішення покроково з контрольним порогом?",
                            new String[]{"Метод з адаптивним кроком", "Метод з постійним кроком", "Метод випадкових кроків", "Метод кінцевих елементів"}, 0),
                    new Question("12. Для чого застосовують варіаційне інтегрування?",
                            new String[]{"Для ODE", "Для PDE", "Для корекції інтеграла", "Для оптимізації"}, 1),
                    new Question("13. Що таке похибка апроксимації?",
                            new String[]{"Помилка округлення", "Помилка між точним і наближеним рішенням", "Системна помилка", "Незначуща"}, 1),
                    new Question("14. Що таке числова диференціація?",
                            new String[]{"Аналітичне обчислення похідної", "Обчислення похідних за допомогою дискретних різниць", "Малювання графіка", "Метод попереднього кроку"}, 1),
                    new Question("15. Який з методів використовується для інтеграції випадкових процесів?",
                            new String[]{"Метод Рунге–Кутта", "Метод Маркова", "Метод Монте-Карло", "Метод Ньютон–Гауса"}, 2),
            };
        } else if (discipline.getName().equals("Нелінійні процеси та моделі")) {
            discipline.questions = new Question[]{
                    new Question("1. Що таке нелінійна система?",
                            new String[]{"Система з однозначними розв’язками", "Система, в якій вихід не пропорційний входу", "Лінійна суперпозиція", "Система без змінних"}, 1),
                    new Question("2. Що таке біфуркація?",
                            new String[]{"Зміна структури рішення при зміні параметру", "Випадковий вибір", "Стабільний стан", "Метод інтегрування"}, 0),
                    new Question("3. Що описує атрактор?",
                            new String[]{"Графік похибки", "Стан або множину станів, до яких прямує система", "Похідну другого порядку", "Інтеграл функції"}, 1),
                    new Question("4. Що таке хаотична динаміка?",
                            new String[]{"Детермінована поведінка", "Висока чутливість до початкових умов", "Лінійна система", "Система зі сталим станом"}, 1),
                    new Question("5. Яке рівняння є прикладом хаотичного нелінійного процесу?",
                            new String[]{"Лінеарне ODE", "Логістичне відображення", "Лінійне рівняння похідного", "Рівняння Лапласа"}, 1),
                    new Question("6. Що таке фазовий портрет?",
                            new String[]{"Текстовий опис системи", "Графічне представлення траєкторій у просторі станів", "Графік функції", "Спектральний аналіз"}, 1),
                    new Question("7. Що таке Ляпунова характеристика?",
                            new String[]{"Інтеграл Ляпунова", "Метрика стабільності", "Числова похибка", "Тип біфуркації"}, 1),
                    new Question("8. Що таке сингулярна точка?",
                            new String[]{"Точка рівноваги", "Точка, де поведінка системи неаналізується лінійно", "Початкова умова", "Метод інтегрування"}, 1),
                    new Question("9. Що відбувається при гомоклінній біфуркації?",
                            new String[]{"Втрачається стабільність атрактора", "Фазовий портрет не змінюється", "Система стає лінійною", "Параметр зникає"}, 0),
                    new Question("10. Що таке резонанс у нелінійній системі?",
                            new String[]{"Відповідь на частотний вплив", "Постійна похибка", "Стабільна точка", "Метод інтегрування"}, 0),
                    new Question("11. Що таке фрактал у динамічних системах?",
                            new String[]{"Гладка крива", "Самоподібна структура в фазовому просторі", "Пряма лінія", "Система рівноваги"}, 1),
                    new Question("12. Що таке зміна атрактора внаслідок зміни параметра?",
                            new String[]{"Рівність розв’язків", "Біфуркація", "Числова похибка", "Метод інтегрування"}, 1),
                    new Question("13. Що таке збурена система?",
                            new String[]{"Система без похибок", "Система під впливом шуму чи змінного параметру", "Система з фіксованим рішенням", "Лінійна модель"}, 1),
                    new Question("14. Що таке Poincaré–карта?",
                            new String[]{"Спосіб аналізу періодичної траєкторії в перетинаючій площині", "Графік похибок", "Карта GPS", "Планування експерименту"}, 0),
                    new Question("15. Що таке синхронізація в нелінійних системах?",
                            new String[]{"Наявність стабільного стану", "Випадковість", "Припасування траєкторій двох систем під впливом зв’язку", "Зникнення атрактора"}, 2),
            };
        } else {
            discipline.questions = new Question[]{new Question("1. Що вивчає теорія керування?",
                    new String[]{"Фізичні коливання", "Статистичні дані", "Принципи побудови і аналізу керованих динамічних систем", "Теорію чисел"}, 2),
                    new Question("2. Що таке об'єкт керування?",
                            new String[]{"Пристрій в інтернеті", "Сигнал з шумом", "Система, на яку впливають з метою зміни її поведінки", "Значення параметра"}, 2),
                    new Question("3. Що таке керованість системи?",
                            new String[]{"Можливість виміряти параметри", "Можливість перевірити стабільність", "Можливість перевести систему з будь-якого стану в будь-який інший", "Можливість побудови графіка"}, 2),
                    new Question("4. Яке з наведених є математичною моделлю динамічної системи?",
                            new String[]{"Алгоритм шифрування", "Інтегральне рівняння", "Диференціальне рівняння", "Тригонометричний ряд"}, 2),
                    new Question("5. Що таке стійкість системи керування?",
                            new String[]{"Спроможність не змінювати параметри", "Здатність зменшити похибку", "Здатність повернутися до рівноваги після збурення", "Періодичність дії"}, 2),
                    new Question("6. Що таке передатна функція?",
                            new String[]{"Сигнал у часі", "Відношення виходу до входу в області частот", "Формула коефіцієнтів", "Механізм регуляції"}, 1),
                    new Question("7. Як перевірити стійкість лінійної системи?",
                            new String[]{"За частотою входу", "Методом Ляпунова", "Інтегруванням", "Аналізом коефіцієнтів Фур'є"}, 1),
                    new Question("8. Яка типова динамічна ланка має передатну функцію 1/(Ts+1)?",
                            new String[]{"Інтегруюча", "Пропорційна", "Інерційна першого порядку", "Деривативна"}, 2),
                    new Question("9. Що таке зворотній зв’язок у системах керування?",
                            new String[]{"Вхідний сигнал", "Вивід, що впливає на вхід", "Постійна складова сигналу", "Налаштування амплітуди"}, 1),
                    new Question("10. Що таке похибка усталеного режиму?",
                            new String[]{"Максимальна амплітуда", "Різниця між виходом і вхідним впливом при t → ∞", "Похибка похідної", "Розмах синусоїди"}, 1),
                    new Question("11. Який елемент формує сигнал управління на основі похибки?",
                            new String[]{"Сенсор", "Регулятор", "Об'єкт", "Виконавчий пристрій"}, 1),
                    new Question("12. Яке рівняння описує дискретну систему керування?",
                            new String[]{"Диференціальне", "Різницеве", "Інтегральне", "Логарифмічне"}, 1),
                    new Question("13. Який тип системи забезпечує нульову усталену похибку для одиничного входу?",
                            new String[]{"Інерційна", "Пропорційна", "Ідеальний інтегратор", "Система з диференціатором"}, 2),
                    new Question("14. Що означає стабілізація системи?",
                            new String[]{"Зміна її структури", "Налаштування амплітуди сигналу", "Забезпечення її стійкості", "Зменшення періоду"}, 2),
                    new Question("15. Яке з тверджень відповідає принципу суперпозиції?",
                            new String[]{"Система не залежить від початкових умов", "Сума реакцій відповідає сумі впливів", "Будь-який вхід призводить до сталого стану", "Система не має похибки"}, 1)
            };
        }
    }
}