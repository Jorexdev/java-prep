package base.streams.ejercicios.dificil.soluciones;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Ejercicio8 {

    public static void main(String[] args) {

        List<Person> people = List.of(
                new Person("Ana", 22, "Madrid"),
                new Person("Luis", 29, "Barcelona"),
                new Person("Marta", 35, "Valencia"),
                new Person("Jorge", 50, "Sevilla"),
                new Person("Pilar", 61, "Bilbao"),
                new Person("Carlos", 70, "Zaragoza"),
                new Person("Lucía", 19, "Madrid"),
                new Person("Pedro", 40, "Granada")
        );

        Map<String, List<Person>> grouped = people.stream()
                .collect(Collectors.groupingBy(Ex8::range));

        grouped.forEach((r, p) -> {
            System.out.println(r);
            p.forEach(System.out::println);
        });
    }

    private static String range(Person p) {
        if (p.getAge() < 30) return "Jóvenes";
        if (p.getAge() <= 60) return "Adultos";
        return "Mayores";
    }

    static class Person {
        private final String name;
        private final int age;
        private final String city;

        public Person(String name, int age, String city) {
            this.name = name;
            this.age = age;
            this.city = city;
        }

        public int getAge() {
            return age;
        }

        @Override
        public String toString() {
            return name + " (" + age + ", " + city + ")";
        }
    }
}
