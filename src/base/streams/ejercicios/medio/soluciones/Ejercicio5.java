package base.streams.ejercicios.medio.soluciones;

import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio5 {

    public static void main(String[] args) {

        // Ejercicio: mapa libro -> número de personas que lo han leído
        List<Person> people = List.of(
                new Person("Alice", List.of("1984", "El Principito", "Dune")),
                new Person("Bob",   List.of("Dune", "Fundación", "1984")),
                new Person("Clara", List.of("Dune", "El Hobbit")),
                new Person("David", List.of("El Hobbit", "Fundación")),
                new Person("Eva",   List.of("El Principito", "1984"))
        );

        people
                .stream()
                .flatMap(p -> p.getBooksRead().stream()) // aplana a Stream<String> de títulos
                .collect(Collectors.groupingBy(
                        title -> title,        // clave: título del libro
                        Collectors.counting()  // valor: cuántas personas lo han leído
                ))
                .forEach((book, count) -> System.out.println(book + " -> " + count));
    }

    static class Person {

        private final String name;
        private final List<String> booksRead;

        Person(String name, List<String> booksRead) {
            this.name = name;
            this.booksRead = booksRead;
        }

        public List<String> getBooksRead() {
            return booksRead;
        }
    }
}
