package base.streams.ejercicios.medio.soluciones;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Ejercicio3 {

    public static void main(String[] args) {

        // Ejercicio: obtener los 3 estudiantes con mayor nota por cada curso
        List<Student> students = List.of(
                new Student("Ana",    "Matemáticas", 9.1),
                new Student("Luis",   "Matemáticas", 8.5),
                new Student("Carlos", "Matemáticas", 9.7),
                new Student("Marta",  "Matemáticas", 7.8),
                new Student("Lucía",  "Historia",    8.9),
                new Student("Pedro",  "Historia",    9.2),
                new Student("Elena",  "Historia",    8.3)
        );

        Map<String, List<Student>> top3PorCurso = students.stream()
                .collect(Collectors.groupingBy(Student::getCourse))  // agrupar por curso
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .sorted(Comparator.comparing(Student::getGrade).reversed()) // mayor nota primero
                                .limit(3)  // los 3 mejores
                                .toList()
                ));

        top3PorCurso.forEach((curso, top) -> System.out.println(curso + " -> " + top));
    }

    static class Student {

        private final String name;
        private final String course;
        private final double grade;

        Student(String name, String course, double grade) {
            this.name = name;
            this.course = course;
            this.grade = grade;
        }

        public String getCourse() {
            return course;
        }

        public double getGrade() {
            return grade;
        }

        @Override
        public String toString() {
            return name + " (" + grade + ")";
        }
    }
}
