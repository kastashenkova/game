package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import java.util.HashMap;
import java.util.Map;

public class Student implements Serializable {
    private String studentId;
    private String name;
    private int course;
    private String major;
    private List<Discipline> enrolledDisciplines;
    private Map<String, Integer> trimesterScores;
    private Map<String, Integer> zalikAttempts = new HashMap<>();
    private int electiveCount;
    private boolean expelled;


    public Student(String studentId, String name, int course, String major) {
        this.studentId = studentId;
        this.name = name;
        this.course = course;
        this.major = major;
        this.enrolledDisciplines = new ArrayList<>();
        this.electiveCount = 0;
        this.trimesterScores = new HashMap<>();
    }

    // --- Гетери ---
    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public int getCourse() {
        return course;
    }

    public String getMajor() {
        return major;
    }

    public List<Discipline> getEnrolledDisciplines() {
        return enrolledDisciplines;
    }

    public int getElectiveCount() {
        return electiveCount;
    }

    /**
     * Отримує бал за триместр для вказаної дисципліни.
     * @param disciplineId Ідентифікатор дисципліни.
     * @return Бал (0-100) або null, якщо студент не записаний на дисципліну або бал не встановлено.
     */
    public Integer getTrimesterScore(String disciplineId) {
        return trimesterScores.get(disciplineId);
    }

    /**
     * Отримує мапу всіх балів за триместр (ідентифікатор дисципліни -> бал).
     * @return Мапа балів.
     */
    public Map<String, Integer> getTrimesterScores() {
        return trimesterScores;
    }

    /**
     * Записує студента на дисципліну та, за бажанням, встановлює початковий бал.
     * Якщо студент вже записаний, оновлює бал, якщо він наданий.
     * @param discipline Дисципліна для запису.
     * @param initialScore Початковий бал за триместр (0-100). Може бути null, якщо бал не встановлюється одразу.
     */
    public void enrollDiscipline(Discipline discipline, Integer initialScore) {
        if (!enrolledDisciplines.contains(discipline)) {
            enrolledDisciplines.add(discipline);
            if (!discipline.isMandatory()) {
                electiveCount++;
            }
        }
        if (initialScore != null) {
            setTrimesterScore(discipline.getDisciplineId(), initialScore);
        }
    }

    /**
     * Перевантажений метод для запису на дисципліну без початкового балу.
     * Бал може бути встановлений пізніше за допомогою setTrimesterScore.
     * @param discipline Дисципліна для запису.
     */
    public void enrollDiscipline(Discipline discipline) {
        enrollDiscipline(discipline, null);
    }

    /**
     * Відписує студента від дисципліни та видаляє відповідний бал.
     * @param discipline Дисципліна для відписки.
     */
    public void dropDiscipline(Discipline discipline) {
        if (enrolledDisciplines.remove(discipline)) {
            if (!discipline.isMandatory()) {
                electiveCount--;
            }
            trimesterScores.remove(discipline.getDisciplineId()); // <--- Видаляємо бал також
        }
    }

    /**
     * Встановлює або оновлює бал за триместр для конкретної дисципліни.
     * Бал може бути встановлений тільки для дисципліни, на яку студент записаний.
     * @param disciplineId Ідентифікатор дисципліни.
     * @param score Бал за триместр (0-100).
     * @throws IllegalArgumentException якщо бал виходить за допустимий діапазон (0-100).
     * @throws IllegalStateException якщо студент не записаний на цю дисципліну.
     */
    public void setTrimesterScore(String disciplineId, int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Бал за триместр має бути від 0 до 100.");
        }
        // Перевіряємо, чи студент взагалі записаний на цю дисципліну, перш ніж встановлювати бал
        boolean isEnrolled = false;
        for (Discipline d : enrolledDisciplines) {
            if (d.getDisciplineId().equals(disciplineId)) {
                isEnrolled = true;
                break;
            }
        }
        if (!isEnrolled) {
            throw new IllegalStateException("Студент не записаний на дисципліну з ID: " + disciplineId);
        }

        trimesterScores.put(disciplineId, score); // <--- Зберігаємо бал у мапі
    }

    public int getZalikAttempts(String disciplineId) {
        return zalikAttempts.getOrDefault(disciplineId, 0);
    }

    public void incrementZalikAttempts(String disciplineId) {
        zalikAttempts.put(disciplineId, getZalikAttempts(disciplineId) + 1);
    }

    public void expel() {
        this.expelled = true;
    }

    public boolean isExpelled() {
        return expelled;
    }

    @Override
    public String toString() {
        return "Студент [ID=" + studentId + ", Ім'я=" + name + ", Курс=" + course + ", Спец.=" + major + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(studentId, student.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }
}