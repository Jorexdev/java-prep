package base.streams.ejercicios.facil.soluciones;

import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio5 {

    public static void main(String[] args) {

        // Ejercicio: agrupar personas por ciudad y contar cuántas hay en cada una
        List<Person> people = List.of(
                new Person("Jorge",   "Madrid"),
                new Person("Luis",    "Barcelona"),
                new Person("Ana",     "Madrid"),
                new Person("Alberto", "Salamanca"),
                new Person("Izaro",   "Pais Vasco"),
                new Person("Jose",    "Barcelona")
        );

        people
                .stream()
                .collect(Collectors.groupingBy(
                        Person::getResidence,  // clave de agrupación: la ciudad
                        Collectors.counting()  // valor: número de personas en esa ciudad
                ))
                .forEach((city, count) -> System.out.println(city + " -> " + count));
    }

    static class Person {

        private final String name;
        private final String residence;

        Person(String name, String residence) {
            this.name = name;
            this.residence = residence;
        }

        public String getResidence() {
            return residence;
        }

        @Override
        public String toString() {
            return "Person{name='" + name + "', residence=" + residence + '}';
        }
    }
}
