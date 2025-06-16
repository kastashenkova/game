package Tests;

import org.example.Discipline;
import org.example.Hero;
import org.example.Student;

import java.util.List;

public class TestManager {

    private Hero hero;
    private Student student;

    private List<Discipline> examDisciplines;

    public TestManager(Hero hero) {

        this.hero = hero;
        this.student = hero.getStudent();
        this.examDisciplines = student.getExamDisciplines();

    }
}