package org.example;

import java.io.Serializable;
import java.util.Objects;

import static org.example.EnrollmentSystem.UNLIMITED_CAPACITY;

public class Discipline implements Serializable {
    private String disciplineId;
    private String name;
    private String instructor; // Changed from 'lecturer' to 'instructor' for consistency
    private double credits;
    private int maxCapacity;
    private int currentEnrollment;
    private boolean isMandatory;
    private int targetCourse;
    private String controlType;
    private boolean isZalik;

    public static final String CONTROL_TYPE_ZALIK = "Залік";
    public static final String CONTROL_TYPE_EXAM = "Екзамен";

    public Discipline(String disciplineId, String name, String instructor, double credits, int currentEnrollment, int maxCapacity, boolean isMandatory, int targetCourse) {
        this.disciplineId = disciplineId;
        this.name = name;
        this.instructor = instructor;
        this.credits = credits;
        this.currentEnrollment = currentEnrollment;
        this.maxCapacity = maxCapacity;
        this.isMandatory = isMandatory;
        this.targetCourse = targetCourse;
    }

    public Discipline(String disciplineId, String name, String instructor, double credits, int currentEnrollment, int maxCapacity, boolean isMandatory, int targetCourse, String controlType) {
        this.disciplineId = disciplineId;
        this.name = name;
        this.instructor = instructor;
        this.credits = credits;
        this.currentEnrollment = currentEnrollment;
        this.maxCapacity = maxCapacity;
        this.isMandatory = isMandatory;
        this.targetCourse = targetCourse;
        if (controlType != null && (controlType.equals(CONTROL_TYPE_ZALIK) || controlType.equals(CONTROL_TYPE_EXAM))) {
            this.controlType = controlType;
        } else {
            this.controlType = CONTROL_TYPE_ZALIK;
        }
    }

    public Discipline(String disciplineId, String name, String instructor, double credits, int currentEnrollment, int maxCapacity, boolean isMandatory, int targetCourse, String controlType, boolean isZalik) {
        this.disciplineId = disciplineId;
        this.name = name;
        this.instructor = instructor;
        this.credits = credits;
        this.currentEnrollment = currentEnrollment;
        this.maxCapacity = maxCapacity;
        this.isMandatory = isMandatory;
        this.targetCourse = targetCourse;
        if (controlType != null && (controlType.equals(CONTROL_TYPE_ZALIK) || controlType.equals(CONTROL_TYPE_EXAM))) {
            this.controlType = controlType;
        } else {
            this.controlType = CONTROL_TYPE_ZALIK;
        }
        this.isZalik = isZalik;
    }

    // Гетери
    public String getDisciplineId() {
        return disciplineId;
    }

    public String getName() {
        return name;
    }

    public String getInstructor() { // Changed getter name
        return instructor;
    }

    public double getCredits() {
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

    public String getControlType() {
        return controlType; }

    // Методи для керування заповненням дисципліни (потокобезпечні)
    public synchronized boolean enrollStudent() {
        if (currentEnrollment < maxCapacity) {
            currentEnrollment++;
            return true;
        } else if (maxCapacity == UNLIMITED_CAPACITY){
            currentEnrollment++;
        }
        return false;
    }

    public synchronized boolean dropStudent() {
        if (currentEnrollment > 0) {
            currentEnrollment--;
            return true;
        }
        return false;
    }

    public boolean hasAvailableSlots() {
        if (currentEnrollment < maxCapacity) {
            return currentEnrollment < maxCapacity;
        } else if (maxCapacity == UNLIMITED_CAPACITY){
            return currentEnrollment < 64;
        }
        return false;
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

    public boolean isZalik() {
        return isZalik;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Discipline that = (Discipline) o;
        return Objects.equals(disciplineId, that.disciplineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(disciplineId);
    }

    public void setControlType(String controlType) {
        this.controlType = controlType;
    }
}