package org.example;

import java.util.ArrayList;
import java.util.List;

//білети з питаннями
public class ExamTicket {
    private String ticketName;
    private List<Question> questions;

    public ExamTicket(String ticketName) {
        this.ticketName = ticketName;
        this.questions = new ArrayList<>();
    }
    public String getTicketName() {
        return ticketName;
    }
    public void addQuestion(Question question) {
        questions.add(question);
    }
    public List<Question> getQuestions() {
        return questions;
    }
    //узагалі було б добре, якби білети генерувались з питаннями рандомним чионм, але поки кілька дефолт білетів для тесту
    public static List<ExamTicket> generateSampleTickets() {
        List<ExamTicket> sampleTickets = new ArrayList<>();

        ExamTicket ticket1 = new ExamTicket("Білет 1");
        ticket1.addQuestion(new Question("Що таке змінна в програмуванні?", "text", "Це іменований контейнер для зберігання даних"));
        ticket1.addQuestion(new Question("Який із типів є числовим?", "choice", List.of("String", "boolean", "int", "char"), "int"));
        sampleTickets.add(ticket1);

        ExamTicket ticket2 = new ExamTicket("Білет 2");
        ticket2.addQuestion(new Question("Для чого використовують цикл for?", "text", "Для повторення дій певну кількість разів"));
        ticket2.addQuestion(new Question("Що з цього є умовним оператором?", "choice", List.of("for", "if", "int", "return"), "if"));
        sampleTickets.add(ticket2);

        ExamTicket ticket3 = new ExamTicket("Білет 3");
        ticket3.addQuestion(new Question("Який результат обчислення: 2 + 2 * 2?", "text", "6"));
        ticket3.addQuestion(new Question("Що з цього є логічним оператором?", "choice", List.of("&&", "+", "=", "/"), "&&"));
        sampleTickets.add(ticket3);
        return sampleTickets;
    }
}
