package base.streams.ejemplosercicios.facil.soluciones;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Ejercicio5 {

    public static void main(String[] args) {
        List<Person> people = List.of(
                new Person("Jorge", "Madrid"),
                new Person("Luis", "Barcelona"),
                new Person("Ana", "Madrid"),
                new Person("Alberto", "Salamanca"),
                new Person("Izaro", "Pais Vasco"),
                new Person("Jose", "Barcelona")
        );

        people
                .stream()
                //.map(Person::getResidence) // Cuidao que si no especificas el valor que usara
                // Function.identity(), usara el valor hash de tod0 el objeto y la agrupacion
                // dara valor de 1
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .forEach(System.out::println);


    }


    static class Person {

        private final String name;
        private final String residence;

        Person(String name, String age) {
            this.name = name;
            this.residence = age;
        }

        public String getResidence() {
            return residence;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", residence=" + residence +
                    '}';
        }
    }

}
