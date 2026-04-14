package base.streams.ejercicios.dificil.soluciones;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Ejercicio8 {

    public static void main(String[] args) {

        // Ejercicio: agrupar personas en Jóvenes (<30), Adultos (30-60), Mayores (>60)
        List<Person> people = List.of(
                new Person("Ana",    22, "Madrid"),
                new Person("Luis",   29, "Barcelona"),
                new Person("Marta",  35, "Valencia"),
                new Person("Jorge",  50, "Sevilla"),
                new Person("Pilar",  61, "Bilbao"),
                new Person("Carlos", 70, "Zaragoza"),
                new Person("Lucía",  19, "Madrid"),
                new Person("Pedro",  40, "Granada")
        );

        // groupingBy con función clasificadora extraída como método estático
        Map<String, List<Person>> grouped = people.stream()
                .collect(Collectors.groupingBy(Ejercicio8::range));

        grouped.forEach((rango, lista) -> {
            System.out.println(rango + ":");
            lista.forEach(p -> System.out.println("  " + p));
        });
    }

    private static String range(Person p) {
        if (p.getAge() < 30) return "Jóvenes";   // menos de 30
        if (p.getAge() <= 60) return "Adultos";   // entre 30 y 60
        return "Mayores";                          // más de 60
    }

    static class Person {

        private final String name;
        private final int age;
        private final String city;

        Person(String name, int age, String city) {
            this.name = name;
            this.age = age;
            this.city = city;
        }

        public int getAge() { return age; }

        @Override
        public String toString() { return name + " (" + age + ", " + city + ")"; }
    }
}
