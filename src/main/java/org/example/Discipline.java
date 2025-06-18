package org.example;

import Tests.Question;

import java.io.Serializable;
import java.util.Objects;

import static org.example.EnrollmentSystem.UNLIMITED_CAPACITY;

/**
 * The `Discipline` class represents an academic course or subject offered to students.
 * It holds information such as its unique ID, name, instructor, credit value,
 * enrollment capacity, mandatory status, target course year, and control type (e.g., "Залік" or "Екзамен").
 * It also manages current enrollment and provides methods for student enrollment and dropping.
 */
public class Discipline implements Serializable {
    /**
     * Unique identifier for the discipline.
     */
    private String disciplineId;
    /**
     * The name of the discipline.
     */
    private String name;
    /**
     * The name of the instructor teaching the discipline.
     */
    private String instructor;
    /**
     * The number of academic credits awarded for completing the discipline.
     */
    private double credits;
    /**
     * The maximum number of students that can enroll in this discipline.
     * Can be {@link EnrollmentSystem#UNLIMITED_CAPACITY}.
     */
    private int maxCapacity;
    /**
     * The current number of students enrolled in the discipline.
     */
    private int currentEnrollment;
    /**
     * Indicates whether the discipline is mandatory for students.
     */
    private boolean isMandatory;
    /**
     * The target academic year/course for which this discipline is intended.
     */
    private int targetCourse;
    /**
     * The type of control for the discipline (e.g., "Залік" or "Екзамен").
     * Defaults to {@link #CONTROL_TYPE_ZALIK}.
     */
    private String controlType = CONTROL_TYPE_ZALIK;
    /**
     * Indicates if the control type is "Залік". This might be redundant with {@code controlType},
     * depending on usage, but is kept as per original code.
     */
    private boolean isZalik;
    /**
     * Flag indicating if a student has received an "avtomat" (automatic pass) for this discipline.
     */
    private boolean avtomat = false;

    /**
     * An array of {@link Question} objects associated with this discipline, typically for exams or tests.
     */
    public Question[] questions;

    /**
     * The current mark/score obtained by a student for this discipline.
     * Note: This field seems to represent a single student's mark within the context
     * of a specific operation, rather than a general attribute of the discipline itself.
     * Consider if this should be moved to a {@code StudentDiscipline} or similar linking class.
     */
    private int currentStudentsMark;

    /**
     * Constant for the "Залік" control type.
     */
    public static final String CONTROL_TYPE_ZALIK = "Залік";
    /**
     * Constant for the "Екзамен" control type.
     */
    public static final String CONTROL_TYPE_EXAM = "Екзамен";

    /**
     * Constructs a new Discipline object with basic information. The control type
     * defaults to "Залік".
     *
     * @param disciplineId The unique identifier for the discipline.
     * @param name The name of the discipline.
     * @param instructor The name of the instructor.
     * @param credits The number of credits for the discipline.
     * @param currentEnrollment The initial number of students currently enrolled.
     * @param maxCapacity The maximum number of students allowed to enroll.
     * @param isMandatory True if the discipline is mandatory, false otherwise.
     * @param targetCourse The target academic course (year) for the discipline.
     */
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

    /**
     * Constructs a new Discipline object, allowing specification of the control type.
     * If the provided control type is invalid, it defaults to "Залік".
     *
     * @param disciplineId The unique identifier for the discipline.
     * @param name The name of the discipline.
     * @param instructor The name of the instructor.
     * @param credits The number of credits for the discipline.
     * @param currentEnrollment The initial number of students currently enrolled.
     * @param maxCapacity The maximum number of students allowed to enroll.
     * @param isMandatory True if the discipline is mandatory, false otherwise.
     * @param targetCourse The target academic course (year) for the discipline.
     * @param controlType The type of control for the discipline (e.g., "Залік", "Екзамен").
     */
    public Discipline(String disciplineId, String name, String instructor, double credits, int currentEnrollment, int maxCapacity, boolean isMandatory, int targetCourse, String controlType) {
        this(disciplineId, name, instructor, credits, currentEnrollment, maxCapacity, isMandatory, targetCourse); // Call previous constructor
        if (controlType != null && (controlType.equals(CONTROL_TYPE_ZALIK) || controlType.equals(CONTROL_TYPE_EXAM))) {
            this.controlType = controlType;
        } else {
            this.controlType = CONTROL_TYPE_ZALIK; // Default if invalid
        }
    }

    /**
     * Constructs a new Discipline object, allowing specification of control type and `isZalik` flag.
     *
     * @param disciplineId The unique identifier for the discipline.
     * @param name The name of the discipline.
     * @param instructor The name of the instructor.
     * @param credits The number of credits for the discipline.
     * @param currentEnrollment The initial number of students currently enrolled.
     * @param maxCapacity The maximum number of students allowed to enroll.
     * @param isMandatory True if the discipline is mandatory, false otherwise.
     * @param targetCourse The target academic course (year) for the discipline.
     * @param controlType The type of control for the discipline (e.g., "Залік", "Екзамен").
     * @param isZalik True if the control type is "Залік", false otherwise.
     */
    public Discipline(String disciplineId, String name, String instructor, double credits, int currentEnrollment, int maxCapacity, boolean isMandatory, int targetCourse, String controlType, boolean isZalik) {
        this(disciplineId, name, instructor, credits, currentEnrollment, maxCapacity, isMandatory, targetCourse, controlType); // Call previous constructor
        this.isZalik = isZalik;
    }

    /**
     * Constructs a new Discipline object with only its name.
     * This constructor might be used for quick lookup or partial object creation.
     *
     * @param name The name of the discipline.
     */
    public Discipline (String name){
        this.name = name;
    }

    /**
     * Returns the unique identifier of the discipline.
     * @return The discipline ID.
     */
    public String getDisciplineId() {
        return disciplineId;
    }

    /**
     * Returns the name of the discipline.
     * @return The discipline name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name of the instructor for the discipline.
     * @return The instructor's name.
     */
    public String getInstructor() {
        return instructor;
    }

    /**
     * Returns the number of credits for the discipline.
     * @return The number of credits.
     */
    public double getCredits() {
        return credits;
    }

    /**
     * Returns the maximum enrollment capacity of the discipline.
     * @return The maximum capacity.
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Returns the current number of students enrolled in the discipline.
     * @return The current enrollment count.
     */
    public int getCurrentEnrollment() {
        return currentEnrollment;
    }

    /**
     * Checks if the discipline is mandatory.
     * @return True if mandatory, false otherwise.
     */
    public boolean isMandatory() {
        return isMandatory;
    }

    /**
     * Returns the target academic course (year) for which this discipline is intended.
     * @return The target course number.
     */
    public int getTargetCourse() {
        return targetCourse;
    }

    /**
     * Returns the control type of the discipline (e.g., "Залік" or "Екзамен").
     * @return The control type string.
     */
    public String getControlType() {
        return controlType;
    }

    /**
     * Attempts to enroll a student in the discipline. This method is synchronized
     * to ensure thread-safety when multiple enrollments occur concurrently.
     * Enrollment is successful if there is available capacity or if capacity is unlimited.
     *
     * @return True if the student was successfully enrolled, false otherwise (e.g., if full).
     */
    public synchronized boolean enrollStudent() {
        if (currentEnrollment < maxCapacity) {
            currentEnrollment++;
            return true;
        } else if (maxCapacity == UNLIMITED_CAPACITY){
            currentEnrollment++; // If unlimited, just increment without capacity check
            return true; // Student is enrolled
        }
        return false;
    }

    /**
     * Attempts to drop a student from the discipline. This method is synchronized
     * to ensure thread-safety.
     * A student can only be dropped if the current enrollment is greater than zero.
     *
     * @return True if a student was successfully dropped, false otherwise.
     */
    public synchronized boolean dropStudent() {
        if (currentEnrollment > 0) {
            currentEnrollment--;
            return true;
        }
        return false;
    }

    /**
     * Checks if there are available slots for enrollment in the discipline.
     * This considers both fixed maximum capacity and a soft cap for "unlimited" capacity.
     *
     * @return True if there are available slots, false otherwise.
     */
    public boolean hasAvailableSlots() {
        if (maxCapacity == UNLIMITED_CAPACITY){
            // If capacity is "unlimited," check against a practical limit (e.g., 64)
            // or simply return true if we don't strictly cap it.
            // The original code implies a soft cap of 64 for UNLIMITED_CAPACITY.
            return currentEnrollment < 64;
        }
        // For limited capacity, check if current enrollment is less than max capacity
        return currentEnrollment < maxCapacity;
    }

    /**
     * Provides a string representation of the Discipline object.
     * For non-mandatory disciplines, it includes current enrollment and capacity.
     *
     * @return A string detailing the discipline's name and its mandatory/optional status,
     * including enrollment numbers for optional courses.
     */
    @Override
    public String toString() {
        if (!isMandatory) {
            return name + " (Вибіркова, зайнято місць: " + currentEnrollment + "/" + maxCapacity + ")";
        } else {
            return name + " (Обов'язкова)";
        }
    }

    /**
     * Checks if the control type for this discipline is "Залік".
     * @return True if the control type is "Залік", false otherwise.
     */
    public boolean isZalik() {
        return isZalik;
    }

    /**
     * Compares this Discipline object with another object for equality.
     * Two disciplines are considered equal if their {@code disciplineId} are the same.
     *
     * @param o The object to compare with.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Discipline that = (Discipline) o;
        return Objects.equals(disciplineId, that.disciplineId);
    }

    /**
     * Returns a hash code value for the Discipline object.
     * The hash code is based on the {@code disciplineId}.
     *
     * @return A hash code for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(disciplineId);
    }

    /**
     * Sets the control type for the discipline.
     * @param controlType The new control type (e.g., "Залік", "Екзамен").
     */
    public void setControlType(String controlType) {
        this.controlType = controlType;
    }

    /**
     * Returns the current mark/score set for a student in this discipline.
     * This field is typically used temporarily to hold a mark during specific operations.
     * @return The current student's mark.
     */
    public int getCurrentStudentsMark() {
        return currentStudentsMark;
    }

    /**
     * Sets the current mark/score for a student in this discipline.
     * @param currentStudentsMark The mark to set.
     */
    public void setCurrentStudentsMark(int currentStudentsMark) {
        this.currentStudentsMark = currentStudentsMark;
    }

    /**
     * Sets the "avtomat" (automatic pass) status to true for this discipline.
     * This typically means a student doesn't need to take the final control.
     */
    public void setAvtomat() {
        this.avtomat = true;
    }

    /**
     * Checks if the "avtomat" (automatic pass) status is set for this discipline.
     * @return True if "avtomat" is granted, false otherwise.
     */
    public boolean getAvtomat(){
        return  avtomat;
    }
}