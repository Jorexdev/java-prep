package base.streams.ejercicios.medio.soluciones;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Ejercicio10 {

    public static void main(String[] args) {

        // Ejercicio: lista ordenada por edad descendente de nombres únicos de mayores de 30
        List<Person> people = List.of(
                new Person("Ana",   28),
                new Person("Luis",  35),
                new Person("Clara", 40),
                new Person("Pedro", 30),
                new Person("Marta", 45),
                new Person("Luis",  50), // nombre duplicado con edad distinta
                new Person("Lucía", 38)
        );

        // cuando hay nombres duplicados, groupingBy + maxBy conserva la instancia con mayor edad
        people
                .stream()
                .filter(p -> p.getAge() >= 30)                        // descartar menores de 30
                .collect(Collectors.groupingBy(
                        Person::getName,
                        Collectors.maxBy(Comparator.comparing(Person::getAge)) // mayor edad si hay duplicados
                ))
                .values()
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(Person::getAge).reversed()) // ordenar por edad desc
                .forEach(System.out::println);
    }

    static class Person {

        private final String name;
        private final int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        @Override
        public String toString() {
            return name + " (" + age + ")";
        }
    }
}
