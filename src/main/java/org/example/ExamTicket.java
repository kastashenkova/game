package org.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an exam ticket, which contains a collection of questions for an exam.
 * Each ticket has a name and a list of {@link Question} objects.
 */
public class ExamTicket {
    private String ticketName;
    private List<Question> questions;

    /**
     * Constructs a new {@code ExamTicket} with the specified name.
     * Initializes an empty list of questions.
     *
     * @param ticketName The name of the exam ticket (e.g., "Білет 1").
     */
    public ExamTicket(String ticketName) {
        this.ticketName = ticketName;
        this.questions = new ArrayList<>();
    }

    /**
     * Returns the name of this exam ticket.
     *
     * @return The ticket name as a {@code String}.
     */
    public String getTicketName() {
        return ticketName;
    }

    /**
     * Adds a {@link Question} to this exam ticket.
     *
     * @param question The {@code Question} object to be added.
     */
    public void addQuestion(Question question) {
        questions.add(question);
    }

    /**
     * Returns the list of questions associated with this exam ticket.
     *
     * @return A {@link List} of {@link Question} objects.
     */
    public List<Question> getQuestions() {
        return questions;
    }

    /**
     * Generates and returns a list of sample {@code ExamTicket} objects for testing purposes.
     * Each sample ticket contains a mix of text-based and multiple-choice questions.
     * In a real-world application, tickets and questions might be loaded from a database or file,
     * or generated randomly.
     *
     * @return A {@link List} of sample {@link ExamTicket} objects.
     */
    public static List<ExamTicket> generateSampleTickets() {
        List<ExamTicket> sampleTickets = new ArrayList<>();

        // Sample Ticket 1
        ExamTicket ticket1 = new ExamTicket("Білет 1");
        ticket1.addQuestion(new Question("Що таке змінна в програмуванні?", "text", "Це іменований контейнер для зберігання даних"));
        ticket1.addQuestion(new Question("Який з типів є числовим?", "choice", List.of("String", "boolean", "int", "char"), "int"));
        sampleTickets.add(ticket1);

        // Sample Ticket 2
        ExamTicket ticket2 = new ExamTicket("Білет 2");
        ticket2.addQuestion(new Question("Для чого використовується цикл for?", "text", "Для повторення дій певну кількість разів"));
        ticket2.addQuestion(new Question("Що з цього є умовним оператором?", "choice", List.of("for", "if", "int", "return"), "if"));
        sampleTickets.add(ticket2);

        // Sample Ticket 3
        ExamTicket ticket3 = new ExamTicket("Білет 3");
        ticket3.addQuestion(new Question("Який результат обчислення: 2 + 2 * 2?", "text", "6"));
        ticket3.addQuestion(new Question("Що з цього є логічним оператором?", "choice", List.of("&&", "+", "=", "/"), "&&"));
        sampleTickets.add(ticket3);

        return sampleTickets;
    }
}