package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Student {
    private String studentId;
    private String name;
    private int course;
    private String major;
    private List<Discipline> enrolledDisciplines;
    private int electiveCount;

    public Student(String studentId, String name, int course, String major) {
        this.studentId = studentId;
        this.name = name;
        this.course = course;
        this.major = major;
        this.enrolledDisciplines = new ArrayList<>();
        this.electiveCount = 0;
    }

    // Гетери
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

    // Методи для керування записаними дисциплінами
    public void enrollDiscipline(Discipline discipline) {
        if (!enrolledDisciplines.contains(discipline)) {
            enrolledDisciplines.add(discipline);
            if (!discipline.isMandatory()) { // Збільшуємо лічильник лише для вибіркових
                electiveCount++;
            }
        }
    }

    public void dropDiscipline(Discipline discipline) {
        if (enrolledDisciplines.remove(discipline)) {
            if (!discipline.isMandatory()) { // Зменшуємо лічильник лише для вибіркових
                electiveCount--;
            }
        }
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