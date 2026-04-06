package base.streams.ejemplosercicios.medio.soluciones;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Ejercicio10 {

    public static void main(String[] args) {
        List<Person> people = List.of(
                new Person("Ana", 28),
                new Person("Luis", 35),
                new Person("Clara", 40),
                new Person("Pedro", 30),
                new Person("Marta", 45),
                new Person("Luis", 50),
                new Person("Lucía", 38)
        );

        people
                .stream()
                .filter(p -> p.getAge() >= 30)
                .collect(Collectors.groupingBy(
                        Person::getName,
                        Collectors.maxBy(Comparator.comparing(Person::getAge))
                ))
                .values()
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(Person::getAge).reversed())
                .forEach(System.out::println);


    }

    static  class Person {
        private final String name;
        private final int age;

        public Person(String name, int age) {
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
