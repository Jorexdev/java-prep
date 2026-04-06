package base.streams.ejercicios.medio.soluciones;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Ejercicio5 {

    public static void main(String[] args) {
        List<Person> people = List.of(
                new Person("Alice", List.of("1984", "El Principito", "Dune")),
                new Person("Bob", List.of("Dune", "Fundación", "1984")),
                new Person("Clara", List.of("Dune", "El Hobbit")),
                new Person("David", List.of("El Hobbit", "Fundación")),
                new Person("Eva", List.of("El Principito", "1984"))
        );

        people
                .stream()
//                .map(Person::getBooksRead)
//                .flatMap(List::stream)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ))
                .entrySet()
                .forEach(System.out::println);


    }

    static class Person {
        private final String name;
        private final List<String> booksRead;

        public Person(String name, List<String> booksRead) {
            this.name = name;
            this.booksRead = booksRead;
        }

        public String getName() {
            return name;
        }

        public List<String> getBooksRead() {
            return booksRead;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", booksRead=" + booksRead +
                    '}';
        }
    }
}
