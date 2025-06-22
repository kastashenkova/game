package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a student within the educational system.
 * This class stores information about the student, their enrolled disciplines,
 * trimester scores, and academic status.
 */
public class Student implements Serializable {
    private String studentId;
    private String name;
    private int course;
    private String major;
    private List<Discipline> enrolledDisciplines;
    private List<Discipline> examDisciplines;
    private Map<String, Integer> trimesterScores;
    private Map<String, Integer> zalikAttempts = new HashMap<>(); // Stores the number of attempts for "zalik" (pass/fail) disciplines
    private int electiveCount;
    private boolean expelled;

    /**
     * Constructs a new Student object with specified name, course, and major.
     * The student ID is automatically generated based on the course (bachelor or master).
     *
     * @param name The full name of the student.
     * @param course The current course (year) of the student.
     * @param major The student's major.
     */
    public Student(String name, int course, String major) {
        this.name = name;
        this.course = course;
        // Assign student ID based on course (assuming bachelor/master distinction at course 4)
        this.studentId = course <= 4 ? "І 005/24 бп" : "І 005/24 мп";
        this.major = major;
        this.enrolledDisciplines = new ArrayList<>();
        this.examDisciplines = new ArrayList<>();
        this.electiveCount = 0;
        this.trimesterScores = new HashMap<>();
    }

    /**
     * Default constructor for Student.
     * Used typically for deserialization or when properties will be set later.
     */
    public Student() {
        // Default constructor
    }

    /**
     * Gets the unique identifier of the student.
     *
     * @return The student's ID.
     */
    public String getStudentId() {
        return studentId;
    }

    /**
     * Gets the full name of the student.
     *
     * @return The student's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the current course (year) of the student.
     *
     * @return The student's course.
     */
    public int getCourse() {
        return course;
    }

    /**
     * Gets the major of the student.
     *
     * @return The student's major.
     */
    public String getMajor() {
        return major;
    }

    /**
     * Gets the list of disciplines the student is currently enrolled in.
     *
     * @return A list of {@link Discipline} objects.
     */
    public List<Discipline> getEnrolledDisciplines() {
        return enrolledDisciplines;
    }

    /**
     * Gets the count of elective disciplines the student is enrolled in.
     *
     * @return The number of elective disciplines.
     */
    public int getElectiveCount() {
        return electiveCount;
    }

    /**
     * Retrieves the trimester score for a specific discipline.
     *
     * @param disciplineId The identifier of the discipline.
     * @return The score (0-100) or {@code null} if the student is not enrolled in the discipline or the score has not been set.
     */
    public Integer getTrimesterScore(String disciplineId) {
        return trimesterScores.get(disciplineId);
    }

    /**
     * Retrieves a map of all trimester scores for the student,
     * where the key is the discipline ID and the value is the score.
     *
     * @return A map of discipline IDs to their respective scores.
     */
    public Map<String, Integer> getTrimesterScores() {
        return trimesterScores;
    }

    /**
     * Enrolls the student in a discipline and optionally sets an initial score.
     * If the student is already enrolled, it will update the score if provided.
     * It also increments the elective count if the discipline is not mandatory
     * and adds the discipline to exam disciplines if its control type is "Екзамен".
     *
     * @param discipline The {@link Discipline} to enroll in.
     * @param initialScore The initial trimester score (0-100). Can be {@code null} if the score is not to be set immediately.
     */
    public void enrollDiscipline(Discipline discipline, Integer initialScore) {
        if (!enrolledDisciplines.contains(discipline)) {
            enrolledDisciplines.add(discipline);
            if (!discipline.isMandatory()) {
                electiveCount++;
            }
            if (discipline.getControlType().equals("Екзамен")) {
                examDisciplines.add(discipline);
            }
        }
        if (initialScore != null) {
            setTrimesterScore(discipline.getDisciplineId(), initialScore);
        }
    }

    /**
     * Overloaded method to enroll a student in a discipline without an initial score.
     * The score can be set later using {@link #setTrimesterScore(String, int)}.
     *
     * @param discipline The {@link Discipline} to enroll in.
     */
    public void enrollDiscipline(Discipline discipline) {
        enrollDiscipline(discipline, null);
    }

    /**
     * Drops a student from a discipline and removes the corresponding score.
     * If the discipline is elective, it decrements the elective count.
     *
     * @param discipline The {@link Discipline} to drop.
     */
    public void dropDiscipline(Discipline discipline) {
        if (enrolledDisciplines.remove(discipline)) {
            if (!discipline.isMandatory()) {
                electiveCount--;
            }
            trimesterScores.remove(discipline.getDisciplineId()); // Remove the score as well
            examDisciplines.remove(discipline); // Also remove from exam disciplines if it was there
        }
    }

    /**
     * Sets or updates the trimester score for a specific discipline.
     * The score can only be set for a discipline the student is currently enrolled in.
     *
     * @param disciplineId The identifier of the discipline.
     * @param score The trimester score (0-100).
     * @throws IllegalArgumentException If the score is outside the valid range (0-100).
     * @throws IllegalStateException If the student is not enrolled in the specified discipline.
     */
    public void setTrimesterScore(String disciplineId, int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Бал за триместр має бути від 0 до 100.");
        }
        // Check if the student is actually enrolled in this discipline before setting the score
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

        trimesterScores.put(disciplineId, score); // Store the score in the map
    }

    /**
     * Gets the number of "zalik" (pass/fail) attempts for a specific discipline.
     *
     * @param disciplineId The identifier of the discipline.
     * @return The number of attempts, or 0 if no attempts have been recorded.
     */
    public int getZalikAttempts(String disciplineId) {
        return zalikAttempts.getOrDefault(disciplineId, 0);
    }

    /**
     * Increments the number of "zalik" (pass/fail) attempts for a specific discipline by one.
     *
     * @param disciplineId The identifier of the discipline.
     */
    public void incrementZalikAttempts(String disciplineId) {
        zalikAttempts.put(disciplineId, getZalikAttempts(disciplineId) + 1);
    }

    /**
     * Sets the student's status to expelled.
     */
    public void expel() {
        this.expelled = true;
    }

    /**
     * Checks if the student has been expelled.
     *
     * @return {@code true} if the student is expelled, {@code false} otherwise.
     */
    public boolean isExpelled() {
        return expelled;
    }

    /**
     * Returns a string representation of the Student object.
     *
     * @return A string containing the student's ID, name, course, and major.
     */
    @Override
    public String toString() {
        return "Студент [ID=" + studentId + ", Ім'я=" + name + ", Курс=" + course + ", Спец.=" + major + "]";
    }

    /**
     * Compares this student to the specified object. The result is {@code true}
     * if and only if the argument is not {@code null} and is a {@code Student} object
     * that represents the same student ID as this object.
     *
     * @param o The object to compare this {@code Student} against.
     * @return {@code true} if the given object represents a {@code Student} equivalent to this student, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(studentId, student.studentId);
    }

    /**
     * Returns a hash code value for the student.
     * This method is supported for the benefit of hash tables such as those provided by {@link HashMap}.
     *
     * @return A hash code value for this student.
     */
    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }

    /**
     * Gets the list of disciplines for which the student has to take an exam.
     *
     * @return A list of {@link Discipline} objects that are exam disciplines.
     */
    public List<Discipline> getExamDisciplines() {
        return examDisciplines;
    }

    /**
     * Sets the list of disciplines for which the student has to take an exam.
     *
     * @param examDisciplines The new list of exam disciplines.
     */
    public void setExamDisciplines(List<Discipline> examDisciplines) {
        this.examDisciplines = examDisciplines;
    }

    /**
     * Sets the list of disciplines the student is currently enrolled in.
     * This method should be used with caution as it replaces the existing list.
     *
     * @param enrolledDisciplines The new list of enrolled disciplines.
     */
    public void setEnrolledDisciplines(List<Discipline> enrolledDisciplines) {
        this.enrolledDisciplines = enrolledDisciplines;
    }
}