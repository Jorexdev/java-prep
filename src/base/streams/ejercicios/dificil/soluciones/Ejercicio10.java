package base.streams.ejemplosercicios.dificil.soluciones;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Ejercicio10 {

    public static void main(String[] args) {

        List<Evaluation> evaluations = List.of(
                new Evaluation("Ana", "Java", 8.5),
                new Evaluation("Luis", "Java", 7.0),
                new Evaluation("Marta", "Java", 9.2),
                new Evaluation("Carlos", "SQL", 6.5),
                new Evaluation("Pilar", "SQL", 7.8),
                new Evaluation("Lucía", "Docker", 8.0),
                new Evaluation("Jorge", "Docker", 8.5),
                new Evaluation("Pedro", "Docker", 9.0),
                new Evaluation("Ana", "SQL", 8.2)
        );

        Map<String, DoubleSummaryStatistics> stats = evaluations.stream()
                .collect(Collectors.groupingBy(
                        Evaluation::getCourse,
                        Collectors.summarizingDouble(Evaluation::getGrade)
                ));

        stats.forEach((c, s) -> {
            System.out.println("Curso: " + c);
            System.out.println(s);
        });
    }

    static class Evaluation {
        private final String student;
        private final String course;
        private final double grade;

        public Evaluation(String student, String course, double grade) {
            this.student = student;
            this.course = course;
            this.grade = grade;
        }

        public String getCourse() {
            return course;
        }

        public double getGrade() {
            return grade;
        }
    }
}
