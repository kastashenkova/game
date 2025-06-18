package org.example;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * The `EnrollmentSystem` class manages student and discipline data,
 * handling enrollment and unenrollment processes. It simulates aspects of
 * a university enrollment system, including capacity limits, credit limits,
 * and a simulation of other "virtual" students enrolling in electives.
 */
public class EnrollmentSystem {
    /**
     * A map storing all students, keyed by their student ID.
     */
    private Map<String, Student> students;
    /**
     * A map storing all disciplines, keyed by their discipline ID.
     */
    private Map<String, Discipline> disciplines;
    /**
     * A {@link Random} instance used for various probabilistic operations,
     * such as simulating virtual student enrollments.
     */
    private Random random = new Random();

    /**
     * Constant representing an unlimited capacity for a discipline.
     */
    public static final int UNLIMITED_CAPACITY = -1;
    /**
     * Default capacity for elective disciplines (if not specified otherwise).
     */
    public static final int ELECTIVE_CAPACITY = 10;
    /**
     * Default capacity for mandatory disciplines.
     */
    public static final int MANDATORY_DISCIPLINE_CAPACITY = 65;

    /**
     * A static map defining the maximum credit limits per academic course (year).
     */
    private static final Map<Integer, Integer> COURSE_CREDIT_LIMITS = new HashMap<>();
    static {
        COURSE_CREDIT_LIMITS.put(1, 61); // 1st year max credits
        COURSE_CREDIT_LIMITS.put(2, 62); // 2nd year max credits
        COURSE_CREDIT_LIMITS.put(3, 62); // 3rd year max credits
        COURSE_CREDIT_LIMITS.put(4, 62); // 4th year max credits
    }

    /**
     * The minimum number of credits a student must select to confirm their course choices.
     */
    public static final int MIN_CREDITS_TO_CONFIRM = 55;

    /**
     * Counter for the number of virtual students who have completed their elective selection.
     */
    private int virtualStudentsEnrolledCount = 0;
    /**
     * The maximum number of virtual students to simulate for elective enrollment.
     */
    public static final int MAX_VIRTUAL_STUDENTS_TO_ENROLL = 30;
    /**
     * A map tracking the number of electives picked by each virtual student.
     * Key: Virtual student ID, Value: Number of electives picked.
     */
    private Map<String, Integer> virtualStudentElectiveCounts;
    /**
     * A map tracking the specific electives each virtual student has enrolled in.
     * Key: Virtual student ID, Value: Set of discipline IDs they are enrolled in.
     */
    private Map<String, Set<String>> virtualStudentEnrolledElectives;

    /**
     * Constructs a new `EnrollmentSystem`.
     * Initializes empty maps for students, disciplines, and virtual student tracking.
     */
    public EnrollmentSystem() {
        this.students = new HashMap<>();
        this.disciplines = new HashMap<>();
        this.virtualStudentElectiveCounts = new HashMap<>();
        this.virtualStudentEnrolledElectives = new HashMap<>();
    }

    /**
     * Adds a new student to the system.
     *
     * @param student The {@link Student} object to add.
     * @return A message indicating success or if a student with the same ID already exists.
     */
    public String addStudent(Student student) {
        if (!students.containsKey(student.getStudentId())) {
            students.put(student.getStudentId(), student);
            return "Студент " + student.getName() + " (ID: " + student.getStudentId() + ") успішно доданий.";
        } else {
            return "Помилка. Студент з ID '" + student.getStudentId() + "' вже існує.";
        }
    }

    /**
     * Adds a new discipline to the system.
     *
     * @param discipline The {@link Discipline} object to add.
     * @return A message indicating success or if a discipline with the same ID already exists.
     */
    public String addDiscipline(Discipline discipline) {
        if (!disciplines.containsKey(discipline.getDisciplineId())) {
            disciplines.put(discipline.getDisciplineId(), discipline);
            return "Дисципліна '" + discipline.getName() + "' (ID: " + discipline.getDisciplineId() + ") успішно додана.";
        } else {
            return "Помилка. Дисципліна з ID '" + discipline.getDisciplineId() + "' вже існує.";
        }
    }

    /**
     * Retrieves a student by their ID.
     *
     * @param studentId The ID of the student to retrieve.
     * @return An {@link Optional} containing the {@link Student} if found, or empty if not.
     */
    public Optional<Student> getStudentById(String studentId) {
        return Optional.ofNullable(students.get(studentId));
    }

    /**
     * Retrieves a discipline by its ID.
     *
     * @param disciplineId The ID of the discipline to retrieve.
     * @return An {@link Optional} containing the {@link Discipline} if found, or empty if not.
     */
    public Optional<Discipline> getDisciplineById(String disciplineId) {
        return Optional.ofNullable(disciplines.get(disciplineId));
    }

    /**
     * Returns an unmodifiable map of all students in the system.
     *
     * @return A {@link Map} of student IDs to {@link Student} objects.
     */
    public Map<String, Student> getStudents() {
        return Collections.unmodifiableMap(students);
    }

    /**
     * Retrieves a list of all mandatory disciplines for a specific academic course.
     * The returned list is unmodifiable.
     *
     * @param course The academic course number (e.g., 1, 2, 3, 4).
     * @return A {@link List} of mandatory {@link Discipline} objects for the given course.
     */
    public List<Discipline> getMandatoryDisciplines(int course) {
        return disciplines.values().stream()
                .filter(d -> d.isMandatory() && d.getTargetCourse() == course)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    /**
     * Retrieves a list of all elective disciplines for a specific academic course.
     * The returned list is unmodifiable.
     *
     * @param course The academic course number (e.g., 1, 2, 3, 4).
     * @return A {@link List} of elective {@link Discipline} objects for the given course.
     */
    public List<Discipline> getElectiveDisciplines(int course) {
        return disciplines.values().stream()
                .filter(d -> !d.isMandatory() && d.getTargetCourse() == course)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    /**
     * Retrieves a list of all mandatory disciplines in the system, regardless of course.
     * The returned list is unmodifiable.
     *
     * @return A {@link List} of all mandatory {@link Discipline} objects.
     */
    public List<Discipline> getAllMandatoryDisciplines() {
        return disciplines.values().stream()
                .filter(Discipline::isMandatory)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    /**
     * Retrieves a list of all elective disciplines in the system, regardless of course.
     * The returned list is unmodifiable.
     *
     * @return A {@link List} of all elective {@link Discipline} objects.
     */
    public List<Discipline> getAllElectiveDisciplines() {
        return disciplines.values().stream()
                .filter(d -> !d.isMandatory())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    /**
     * Simulates random enrollment of "other" (virtual) students in elective disciplines.
     * Each virtual student attempts to pick up to 5 electives. The method
     * is synchronized to handle concurrent enrollment attempts.
     * The simulation stops when {@link #MAX_VIRTUAL_STUDENTS_TO_ENROLL} virtual students
     * have completed their selection of 5 electives.
     */
    public synchronized void randomlyIncrementElectiveEnrollment() {
        if (virtualStudentsEnrolledCount >= MAX_VIRTUAL_STUDENTS_TO_ENROLL) {
            return; // Stop if the target limit of virtual students who completed their picks is reached
        }

        List<Discipline> electives = getAllElectiveDisciplines();
        if (electives.isEmpty()) return; // No electives to enroll in

        // Probability (10%) that a "virtual student" attempts to enroll in an elective
        if (random.nextDouble() < 0.1) {
            // Generate a unique ID for the virtual student
            String virtualStudentId = "VirtualStudent_" + random.nextInt(MAX_VIRTUAL_STUDENTS_TO_ENROLL * 2);
            // Initialize counts and enrolled electives for this virtual student if not already present
            virtualStudentElectiveCounts.putIfAbsent(virtualStudentId, 0);
            virtualStudentEnrolledElectives.putIfAbsent(virtualStudentId, new HashSet<>());

            int currentVirtualStudentElectives = virtualStudentElectiveCounts.get(virtualStudentId);

            // Virtual students will try to pick up to 5 electives
            if (currentVirtualStudentElectives < 5) {
                // Filter for electives that have available slots and haven't been picked by this virtual student yet
                List<Discipline> availableElectivesForVirtualStudent = electives.stream()
                        .filter(d -> d.hasAvailableSlots() && !virtualStudentEnrolledElectives.get(virtualStudentId).contains(d.getDisciplineId()))
                        .collect(Collectors.toList());

                if (!availableElectivesForVirtualStudent.isEmpty()) {
                    // Pick a random available elective
                    Discipline randomElective = availableElectivesForVirtualStudent.get(random.nextInt(availableElectivesForVirtualStudent.size()));

                    // Attempt to enroll the virtual student
                    if (randomElective.enrollStudent()) {
                        // Update the count of electives picked by this virtual student
                        virtualStudentElectiveCounts.put(virtualStudentId, currentVirtualStudentElectives + 1);
                        // Record the discipline as enrolled by this virtual student
                        virtualStudentEnrolledElectives.get(virtualStudentId).add(randomElective.getDisciplineId());

                        // If this virtual student has now picked 5 electives, increment the global counter
                        if (virtualStudentElectiveCounts.get(virtualStudentId) == 5) {
                            virtualStudentsEnrolledCount++;
                        }
                    }
                }
            }
        }
    }

    /**
     * Enrolls a specified student in a specified discipline.
     * This method performs several checks:
     * <ul>
     * <li>Student and discipline existence.</li>
     * <li>Mandatory disciplines cannot be chosen manually.</li>
     * <li>Student is not already enrolled in the discipline.</li>
     * <li>Enrollment does not exceed the student's credit limit for their course.</li>
     * <li>Discipline has available slots (for electives with limited capacity).</li>
     * </ul>
     *
     * @param studentId The ID of the student to enroll.
     * @param disciplineId The ID of the discipline to enroll in.
     * @return A {@link String} message detailing the outcome of the enrollment attempt.
     */
    public String enrollStudentToDiscipline(String studentId, String disciplineId) {
        Optional<Student> studentOpt = getStudentById(studentId);
        Optional<Discipline> disciplineOpt = getDisciplineById(disciplineId);

        if (studentOpt.isEmpty()) {
            return "Помилка запису. Студента з ID '" + studentId + "' не знайдено.";
        }
        if (disciplineOpt.isEmpty()) {
            return "Помилка запису. Дисципліни з ID '" + disciplineId + "' не знайдено.";
        }

        Student student = studentOpt.get();
        Discipline discipline = disciplineOpt.get();

        // 1. Mandatory disciplines cannot be chosen manually by students
        if (discipline.isMandatory()) {
            return "Помилка. Обов'язкові дисципліни не можуть бути обрані вручну. Студенти автоматично зараховуються на них.";
        }

        // 2. Check if student is already enrolled in this discipline
        if (student.getEnrolledDisciplines().contains(discipline)) {
            return "Помилка запису. Ви вже записані на дисципліну " + discipline.getName() + ".";
        }

        // 3. Check credit limit for the student's course
        int sum = 0;
        for (Discipline enrolledDisc : student.getEnrolledDisciplines()) {
            sum += enrolledDisc.getCredits();
        }
        double currentTotalCredits = sum;
        double newTotalCredits = currentTotalCredits + discipline.getCredits();
        // Get the credit limit for the student's current course, default to max if not defined
        int courseCreditLimit = COURSE_CREDIT_LIMITS.getOrDefault(student.getCourse(), Integer.MAX_VALUE);

        if (newTotalCredits > courseCreditLimit) {
            return "Помилка запису. Запис на дисципліну '" + discipline.getName() + "' призведе до перевищення сумарної кількості кредитів (" + courseCreditLimit + ") для " + student.getCourse() + "-го курсу. Поточна сума: " + currentTotalCredits + ", з цією дисципліною: " + newTotalCredits + ".";
        }

        // 4. Enrollment logic for elective disciplines (both unlimited and limited capacity)
        if (discipline.getMaxCapacity() == UNLIMITED_CAPACITY) {
            student.enrollDiscipline(discipline); // Enroll student
            discipline.enrollStudent(); // Increment discipline's enrollment count
            return "Успішний запис! Ви записані на вибіркову дисципліну " + discipline.getName() + ".";
        }
        else if (!discipline.hasAvailableSlots()) {
            // If capacity is limited and no slots are available
            return "На жаль, Ви не встигли. На дисципліну " + discipline.getName() + " записана максимальна кількість студентів.";
        }
        else {
            // If capacity is limited and slots are available
            student.enrollDiscipline(discipline);
            discipline.enrollStudent();
            return "Успішний запис! Ви записані на вибіркову дисципліну " + discipline.getName() + ".";
        }
    }

    /**
     * Unenrolls a specified student from a specified discipline.
     * This method includes checks to ensure:
     * <ul>
     * <li>Student and discipline existence.</li>
     * <li>Mandatory disciplines cannot be unenrolled manually.</li>
     * <li>Student is actually enrolled in the discipline.</li>
     * </ul>
     *
     * @param studentId The ID of the student to unenroll.
     * @param disciplineId The ID of the discipline to unenroll from.
     * @return A {@link String} message detailing the outcome of the unenrollment attempt.
     */
    public String dropStudentFromDiscipline(String studentId, String disciplineId) {
        Optional<Student> studentOpt = getStudentById(studentId);
        Optional<Discipline> disciplineOpt = getDisciplineById(disciplineId);

        if (studentOpt.isEmpty()) {
            return "Помилка виписки. Студента з ID '" + studentId + "' не знайдено.";
        }
        if (disciplineOpt.isEmpty()) {
            return "Помилка виписки. Дисципліни з ID '" + disciplineId + "' не знайдено.";
        }

        Student student = studentOpt.get();
        Discipline discipline = disciplineOpt.get();

        // 1. Cannot unenroll from mandatory disciplines
        if (discipline.isMandatory()) {
            return "Помилка виписки. з обов'язкових дисциплін не можна виписатися вручну.";
        }

        // 2. Check if the student is actually enrolled in this discipline
        if (!student.getEnrolledDisciplines().contains(discipline)) {
            return "Помилка виписки. Ви не записані на дисципліну " + discipline.getName() + ".";
        }

        // If all checks pass, proceed with unenrollment
        if (discipline.dropStudent()) { // Decrement enrolled student counter for the discipline
            student.dropDiscipline(discipline); // Remove discipline from student's list
            return "Успішна виписка! Ви виписані з дисципліни " + discipline.getName() + ".";
        }
        return "Невідома помилка під час спроби виписки з дисципліни."; // Generic error if dropStudent fails unexpectedly
    }

    /**
     * Returns the current count of virtual students who have completed their elective selections.
     *
     * @return The number of virtual students who have finished picking their electives.
     */
    public int getVirtualStudentsEnrolledCount() {
        return virtualStudentsEnrolledCount;
    }
}