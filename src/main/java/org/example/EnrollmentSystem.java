package org.example;

import org.example.Discipline;
import org.example.Student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class EnrollmentSystem {
    private Map<String, Student> students;
    private Map<String, Discipline> disciplines;
    private Random random = new Random();

    public static final int MAX_ELECTIVES_PER_STUDENT = 3;
    public static final int ELECTIVE_CAPACITY = 12;

    // Нові змінні для відстеження віртуальних студентів
    private int virtualStudentsEnrolledCount = 0; // Лічильник, скільки віртуальних студентів "завершили" свій вибір
    public static final int MAX_VIRTUAL_STUDENTS_TO_ENROLL = 50; // Кількість віртуальних студентів, які мають обрати по 3 дисципліни
    private Map<String, Integer> virtualStudentElectiveCounts; // Відстежує вибір для кожного "віртуального" студента

    public EnrollmentSystem() {
        this.students = new HashMap<>();
        this.disciplines = new HashMap<>();
        this.virtualStudentElectiveCounts = new HashMap<>();
    }

    // Методи для додавання студентів та дисциплін
    public String addStudent(Student student) {
        if (!students.containsKey(student.getStudentId())) {
            students.put(student.getStudentId(), student);
            return "Студент '" + student.getName() + "' (ID: " + student.getStudentId() + ") успішно доданий.";
        } else {
            return "Помилка: Студент з ID '" + student.getStudentId() + "' вже існує.";
        }
    }

    public String addDiscipline(Discipline discipline) {
        if (!disciplines.containsKey(discipline.getDisciplineId())) {
            disciplines.put(discipline.getDisciplineId(), discipline);
            return "Дисципліна '" + discipline.getName() + "' (ID: " + discipline.getDisciplineId() + ") успішно додана.";
        } else {
            return "Помилка: Дисципліна з ID '" + discipline.getDisciplineId() + "' вже існує.";
        }
    }

    // Гетери для отримання об'єктів
    public Optional<Student> getStudentById(String studentId) {
        return Optional.ofNullable(students.get(studentId));
    }

    public Optional<Discipline> getDisciplineById(String disciplineId) {
        return Optional.ofNullable(disciplines.get(disciplineId));
    }

    public Map<String, Student> getStudents() {
        return Collections.unmodifiableMap(students);
    }

    // Отримати лише обов'язкові дисципліни
    public List<Discipline> getMandatoryDisciplines() {
        return disciplines.values().stream()
                .filter(Discipline::isMandatory)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    // Отримати лише вибіркові дисципліни
    public List<Discipline> getElectiveDisciplines() {
        return disciplines.values().stream()
                .filter(d -> !d.isMandatory())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    /**
     * Симулює випадковий запис "інших" студентів на вибіркові дисципліни.
     * Цей метод тепер враховує, що кожен віртуальний студент має обрати 3 дисципліни.
     */
    public synchronized void randomlyIncrementElectiveEnrollment() {
        if (virtualStudentsEnrolledCount >= MAX_VIRTUAL_STUDENTS_TO_ENROLL) {
            return; // Зупиняємо, якщо досягнуто ліміту
        }

        List<Discipline> electives = getElectiveDisciplines();
        if (electives.isEmpty()) return;

        // Ймовірність, що "віртуальний студент" спробує записатися
        if (random.nextDouble() < 0.3) { // Залишаємо 0.3 або змініть, як обговорювали раніше
            // Створюємо "віртуального студента" або знаходимо існуючого
            String virtualStudentId = "VirtualStudent_" + random.nextInt(MAX_VIRTUAL_STUDENTS_TO_ENROLL * 2); // Більший діапазон, щоб імітувати більше спроб
            virtualStudentElectiveCounts.putIfAbsent(virtualStudentId, 0);

            int currentVirtualStudentElectives = virtualStudentElectiveCounts.get(virtualStudentId);

            if (currentVirtualStudentElectives < MAX_ELECTIVES_PER_STUDENT) {
                Discipline randomElective = electives.get(random.nextInt(electives.size()));

                // Перевіряємо, чи цей віртуальний студент вже записаний на цю дисципліну
                // (імітація, що віртуальний студент не може записатися на ту ж дисципліну двічі)
                // Для простоти, ми не ведемо список дисциплін для кожного віртуального студента,
                // а лише їхню кількість. Якщо потрібно суворіше, можна додати Map<String, Set<String>> для discipline IDs.
                // Наразі, просто дозволяємо йому спробувати записатися.
                if (randomElective.enrollStudent()) { // Збільшуємо лічильник записаних студентів на дисципліні
                    virtualStudentElectiveCounts.put(virtualStudentId, currentVirtualStudentElectives + 1);
                    if (virtualStudentElectiveCounts.get(virtualStudentId) == MAX_ELECTIVES_PER_STUDENT) {
                        virtualStudentsEnrolledCount++; // Цей віртуальний студент завершив свій вибір
                    }
                }
            }
        }
    }


    /**
     * Записує студента на дисципліну.
     * Цей метод включає бізнес-логіку та перевірки.
     * @param studentId ID студента.
     * @param disciplineId ID дисципліни.
     * @return Повідомлення про результат запису (успіх або помилка).
     */
    public String enrollStudentToDiscipline(String studentId, String disciplineId) {
        Optional<Student> studentOpt = getStudentById(studentId);
        Optional<Discipline> disciplineOpt = getDisciplineById(disciplineId);

        if (studentOpt.isEmpty()) {
            return "Помилка запису: Студента з ID '" + studentId + "' не знайдено.";
        }
        if (disciplineOpt.isEmpty()) {
            return "Помилка запису: Дисципліни з ID '" + disciplineId + "' не знайдено.";
        }

        Student student = studentOpt.get();
        Discipline discipline = disciplineOpt.get();

        // 1. Обов'язкові дисципліни не можуть бути обрані вручну
        if (discipline.isMandatory()) {
            return "Помилка: Обов'язкові дисципліни не можуть бути обрані вручну. Студенти автоматично зараховуються на них.";
        }

        // 2. Перевірка на вже записану дисципліну
        if (student.getEnrolledDisciplines().contains(discipline)) {
            return "Помилка запису: Ви вже записані на дисципліну " + discipline.getName() + ".";
        }

        // 3. Перевірка ліміту вибіркових дисциплін
        if (student.getElectiveCount() >= MAX_ELECTIVES_PER_STUDENT) {
            return "Помилка запису: Ви вже обрали максимальну кількість вибіркових дисциплін (" + MAX_ELECTIVES_PER_STUDENT + ").";
        }

        // 4. Перевірка наявності вільних місць (сценарій "На жаль, ви не встигли")
        if (!discipline.hasAvailableSlots()) {
            return "На жаль, Ви не встигли. На дисципліну " + discipline.getName() + " записалася максимальна кількість студентів.";
        }

        // 5. Перевірка курсу (якщо потрібно)
        // Для спрощення, вважаємо, що вибіркові дисципліни, які відображаються, вже відповідають курсу студента
        // або що вони є загальними для всіх курсів.

        // Якщо всі перевірки пройдені, виконуємо запис
        if (discipline.enrollStudent()) { // Збільшуємо лічильник записаних студентів на дисципліні
            student.enrollDiscipline(discipline); // Додаємо дисципліну до списку студента
            return "Успішний запис: Ви записані на вибіркову дисципліну " + discipline.getName() + ".";
        }
        return "Невідома помилка під час спроби запису на дисципліну.";
    }

    /**
     * Відписує студента від дисципліни.
     * @param studentId ID студента.
     * @param disciplineId ID дисципліни.
     * @return Повідомлення про результат відписки (успіх або помилка).
     */
    public String dropStudentFromDiscipline(String studentId, String disciplineId) {
        Optional<Student> studentOpt = getStudentById(studentId);
        Optional<Discipline> disciplineOpt = getDisciplineById(disciplineId);

        if (studentOpt.isEmpty()) {
            return "Помилка відписки: Студента з ID '" + studentId + "' не знайдено.";
        }
        if (disciplineOpt.isEmpty()) {
            return "Помилка відписки: Дисципліни з ID '" + disciplineId + "' не знайдено.";
        }

        Student student = studentOpt.get();
        Discipline discipline = disciplineOpt.get();

        // 1. Неможливо відписатися від обов'язкової дисципліни
        if (discipline.isMandatory()) {
            return "Помилка виписки: з обов'язкових дисциплін не можна виписатися вручну.";
        }

        // 2. Перевірка, чи студент дійсно записаний на цю дисципліну
        if (!student.getEnrolledDisciplines().contains(discipline)) {
            return "Помилка відписки: Ви не записані на дисципліну " + discipline.getName() + ".";
        }

        // Якщо всі перевірки пройдені, виконуємо відписку
        if (discipline.dropStudent()) { // Зменшуємо лічильник записаних студентів на дисципліні
            student.dropDiscipline(discipline); // Видаляємо дисципліну зі списку студента
            return "Успішна виписка: Ви виписані з дисципліни " + discipline.getName() + ".";
        }
        return "Невідома помилка під час спроби виписки з дисципліни.";
    }

    public int getVirtualStudentsEnrolledCount() {
        return virtualStudentsEnrolledCount;
    }
}