package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

public class EnrollmentSystem {
    private Map<String, Student> students;
    private Map<String, Discipline> disciplines;
    private Random random = new Random();

    // Видаляємо MAX_ELECTIVES_PER_STUDENT
    // public static final int MAX_ELECTIVES_PER_STUDENT = 5;
    public static final int ELECTIVE_CAPACITY = 15;

    // Credit limits per course
    private static final Map<Integer, Integer> COURSE_CREDIT_LIMITS = new HashMap<>();
    static {
        COURSE_CREDIT_LIMITS.put(2, 62);
        COURSE_CREDIT_LIMITS.put(3, 63);
        COURSE_CREDIT_LIMITS.put(4, 60);
    }

    // Мінімальна кількість кредитів для завершення вибору
    public static final int MIN_CREDITS_TO_CONFIRM = 55;

    // New variables for tracking virtual students
    private int virtualStudentsEnrolledCount = 0;
    public static final int MAX_VIRTUAL_STUDENTS_TO_ENROLL = 65;
    private Map<String, Integer> virtualStudentElectiveCounts;
    private Map<String, Set<String>> virtualStudentEnrolledElectives;

    public EnrollmentSystem() {
        this.students = new HashMap<>();
        this.disciplines = new HashMap<>();
        this.virtualStudentElectiveCounts = new HashMap<>();
        this.virtualStudentEnrolledElectives = new HashMap<>();
    }

    // Methods for adding students and disciplines
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

    // Getters for objects
    public Optional<Student> getStudentById(String studentId) {
        return Optional.ofNullable(students.get(studentId));
    }

    public Optional<Discipline> getDisciplineById(String disciplineId) {
        return Optional.ofNullable(disciplines.get(disciplineId));
    }

    public Map<String, Student> getStudents() {
        return Collections.unmodifiableMap(students);
    }

    // Get only mandatory disciplines for a specific course
    public List<Discipline> getMandatoryDisciplines(int course) {
        return disciplines.values().stream()
                .filter(d -> d.isMandatory() && d.getTargetCourse() == course)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    // Get only elective disciplines for a specific course
    public List<Discipline> getElectiveDisciplines(int course) {
        return disciplines.values().stream()
                .filter(d -> !d.isMandatory() && d.getTargetCourse() == course)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    // Method to get all mandatory disciplines (used for initial student enrollment)
    public List<Discipline> getAllMandatoryDisciplines() {
        return disciplines.values().stream()
                .filter(Discipline::isMandatory)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    // Method to get all elective disciplines
    public List<Discipline> getAllElectiveDisciplines() {
        return disciplines.values().stream()
                .filter(d -> !d.isMandatory())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    /**
     * Simulates random enrollment of "other" students for elective disciplines.
     * Each virtual student now randomly picks up to 5 disciplines, but we don't enforce a hard limit on total electives
     * for them to reach `MAX_VIRTUAL_STUDENTS_TO_ENROLL`. Instead, we count how many have finished their "picking phase".
     */
    public synchronized void randomlyIncrementElectiveEnrollment() {
        if (virtualStudentsEnrolledCount >= MAX_VIRTUAL_STUDENTS_TO_ENROLL) {
            return; // Stop if limit reached
        }

        // Get all elective disciplines, regardless of course for virtual students
        List<Discipline> electives = getAllElectiveDisciplines();
        if (electives.isEmpty()) return;

        // Probability that a "virtual student" attempts to enroll
        if (random.nextDouble() < 0.8) {
            String virtualStudentId = "VirtualStudent_" + random.nextInt(MAX_VIRTUAL_STUDENTS_TO_ENROLL * 2);
            virtualStudentElectiveCounts.putIfAbsent(virtualStudentId, 0);
            virtualStudentEnrolledElectives.putIfAbsent(virtualStudentId, new HashSet<>());

            int currentVirtualStudentElectives = virtualStudentElectiveCounts.get(virtualStudentId);

            // Virtual students will still try to pick up to 5 electives
            if (currentVirtualStudentElectives < 5) {
                List<Discipline> availableElectivesForVirtualStudent = electives.stream()
                        .filter(d -> d.hasAvailableSlots() && !virtualStudentEnrolledElectives.get(virtualStudentId).contains(d.getDisciplineId()))
                        .collect(Collectors.toList());

                if (!availableElectivesForVirtualStudent.isEmpty()) {
                    Discipline randomElective = availableElectivesForVirtualStudent.get(random.nextInt(availableElectivesForVirtualStudent.size()));

                    if (randomElective.enrollStudent()) {
                        virtualStudentElectiveCounts.put(virtualStudentId, currentVirtualStudentElectives + 1);
                        virtualStudentEnrolledElectives.get(virtualStudentId).add(randomElective.getDisciplineId());

                        if (virtualStudentElectiveCounts.get(virtualStudentId) == 5) { // If a virtual student completed their 5 picks
                            virtualStudentsEnrolledCount++; // This virtual student completed their selection
                        }
                    }
                }
            }
        }
    }

    /**
     * Enrolls a student in a discipline.
     * This method includes business logic and checks.
     * @param studentId ID of the student.
     * @param disciplineId ID of the discipline.
     * @return Message about the enrollment result (success or error).
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

        // 1. Mandatory disciplines cannot be manually selected
        if (discipline.isMandatory()) {
            return "Помилка: Обов'язкові дисципліни не можуть бути обрані вручну. Студенти автоматично зараховуються на них.";
        }

        // 2. Check if already enrolled
        if (student.getEnrolledDisciplines().contains(discipline)) {
            return "Помилка запису: Ви вже записані на дисципліну " + discipline.getName() + ".";
        }

        // 3. Check credit limit
        int currentTotalCredits = student.getEnrolledDisciplines().stream()
                .mapToInt(Discipline::getCredits)
                .sum();
        int newTotalCredits = currentTotalCredits + discipline.getCredits();
        int courseCreditLimit = COURSE_CREDIT_LIMITS.getOrDefault(student.getCourse(), Integer.MAX_VALUE); // Get limit for current course

        if (newTotalCredits > courseCreditLimit) {
            return "Помилка запису: Запис на дисципліну '" + discipline.getName() + "' призведе до перевищення сумарної кількості кредитів (" + courseCreditLimit + ") для " + student.getCourse() + "-го курсу. Поточна сума: " + currentTotalCredits + ", з цією дисципліною: " + newTotalCredits + ".";
        }

        // 4. Check available slots (scenario "Unfortunately, you were too late")
        if (!discipline.hasAvailableSlots()) {
            return "На жаль, Ви не встигли. На дисципліну " + discipline.getName() + " записалася максимальна кількість студентів.";
        }

        // If all checks pass, proceed with enrollment
        if (discipline.enrollStudent()) { // Increment enrolled student counter for the discipline
            student.enrollDiscipline(discipline); // Add discipline to student's list
            return "Успішний запис! Ви записані на вибіркову дисципліну " + discipline.getName() + ".";
        }
        return "Невідома помилка під час спроби запису на дисципліну.";
    }

    /**
     * Unenrolls a student from a discipline.
     * @param studentId ID of the student.
     * @param disciplineId ID of the discipline.
     * @return Message about the unenrollment result (success or error).
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

        // 1. Cannot unenroll from mandatory disciplines
        if (discipline.isMandatory()) {
            return "Помилка виписки: з обов'язкових дисциплін не можна виписатися вручну.";
        }

        // 2. Check if the student is actually enrolled in this discipline
        if (!student.getEnrolledDisciplines().contains(discipline)) {
            return "Помилка відписки: Ви не записані на дисципліну " + discipline.getName() + ".";
        }

        // If all checks pass, proceed with unenrollment
        if (discipline.dropStudent()) { // Decrement enrolled student counter for the discipline
            student.dropDiscipline(discipline); // Remove discipline from student's list
            return "Успішна виписка: Ви виписані з дисципліни " + discipline.getName() + ".";
        }
        return "Невідома помилка під час спроби виписки з дисципліни.";
    }

    public int getVirtualStudentsEnrolledCount() {
        return virtualStudentsEnrolledCount;
    }
}