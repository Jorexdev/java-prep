package base.streams.ejercicios.medio.soluciones;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Ejercicio3 {

    public static void main(String[] args) {
        List<Student> students = List.of(
                new Student("Ana", "Matemáticas", 9.1),
                new Student("Luis", "Matemáticas", 8.5),
                new Student("Carlos", "Matemáticas", 9.7),
                new Student("Marta", "Matemáticas", 7.8),
                new Student("Lucía", "Historia", 8.9),
                new Student("Pedro", "Historia", 9.2),
                new Student("Elena", "Historia", 8.3)
        );

        students
                .stream()
                .collect(Collectors.groupingBy(Student::getCourse))
                .entrySet()
                .stream()
               // .sorted(Comparator.comparing().reversed())
                .forEach(System.out::println);


                // primero las 3 mejores medias luego agrupo

        Map<String, List<Student>> top3PorCurso = students.stream()
                .collect(Collectors.groupingBy(Student::getCourse)) // Agrupar por curso
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, // curso
                        entry -> entry.getValue().stream() // lista de estudiantes del curso
                                .sorted(Comparator.comparing(Student::getGrade).reversed()) // ordenar por nota desc
                                .limit(3) // tomar los 3 primeros
                                .toList() // convertir a lista
                ));


    }

    static class Student {
        private final String name;
        private final String course;
        private final double grade;

        public Student(String name, String course, double grade) {
            this.name = name;
            this.course = course;
            this.grade = grade;
        }

        public String getName() {
            return name;
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
