package base.streams.ejemplosercicios.dificil.soluciones;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Ejercicio5 {

    public static void main(String[] args) {

        List<Course> courses = List.of(
                new Course("Java Básico", "Programación", 20),
                new Course("Java Avanzado", "Programación", 40),
                new Course("Introducción a Bases de Datos", "Datos", 15),
                new Course("SQL Avanzado", "Datos", 30),
                new Course("UX Fundamentals", "Diseño", 25),
                new Course("Patrones de Diseño", "Programación", 35),
                new Course("Big Data", "Datos", 45),
                new Course("Diseño de Interfaces", "Diseño", 30)
        );

        Map<String, List<Course>> coursesByArea = courses.stream()
                .collect(Collectors.groupingBy(Course::getArea));

        coursesByArea.values().forEach(list ->
                list.sort(Comparator.comparingInt(Course::getDurationHours))
        );

        coursesByArea.forEach((a, l) -> {
            System.out.println(a);
            l.forEach(System.out::println);
        });
    }

    static class Course {
        private final String name;
        private final String area;
        private final int durationHours;

        public Course(String name, String area, int durationHours) {
            this.name = name;
            this.area = area;
            this.durationHours = durationHours;
        }

        public String getArea() {
            return area;
        }

        public int getDurationHours() {
            return durationHours;
        }

        @Override
        public String toString() {
            return name + " - " + durationHours;
        }
    }
}
