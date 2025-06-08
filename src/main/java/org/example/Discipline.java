package org.example;

import java.util.Objects;

public class Discipline {
    private String disciplineId;
    private String name;
    private String lecturer;
    private int credits;
    private int maxCapacity;
    private int currentEnrollment;
    private boolean isMandatory;
    private int targetCourse;

    public Discipline(String disciplineId, String name, String lecturer, int credits, int maxCapacity, boolean isMandatory, int targetCourse) {
        this.disciplineId = disciplineId;
        this.name = name;
        this.lecturer = lecturer;
        this.credits = credits;
        this.maxCapacity = maxCapacity;
        this.isMandatory = isMandatory;
        this.targetCourse = targetCourse;
        this.currentEnrollment = 0; // Починаємо з 0 записаних
    }

    // Гетери
    public String getDisciplineId() {
        return disciplineId;
    }

    public String getName() {
        return name;
    }

    public String getLecturer() {
        return lecturer;
    }

    public int getCredits() {
        return credits;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getCurrentEnrollment() {
        return currentEnrollment;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public int getTargetCourse() {
        return targetCourse;
    }

    // Методи для керування заповненням дисципліни (потокобезпечні)
    public synchronized boolean incrementEnrollment() {
        if (currentEnrollment < maxCapacity) {
            currentEnrollment++;
            return true;
        }
        return false;
    }

    public synchronized boolean enrollStudent() {
        return incrementEnrollment(); // Просто викликаємо incrementEnrollment
    }

    public synchronized boolean dropStudent() {
        if (currentEnrollment > 0) {
            currentEnrollment--;
            return true;
        }
        return false;
    }

    public boolean hasAvailableSlots() {
        return currentEnrollment < maxCapacity;
    }

    @Override
    public String toString() {
        // Відображаємо заповнення лише для вибіркових дисциплін
        if (!isMandatory) {
            return name + " (Вибіркова, зайнято місць: " + currentEnrollment + "/" + maxCapacity + ")";
        } else {
            return name + " (Обов'язкова)";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Discipline that = (Discipline) o;
        return Objects.equals(disciplineId, that.disciplineId);
    }
}