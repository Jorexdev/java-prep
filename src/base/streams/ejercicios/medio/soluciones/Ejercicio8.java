package base.streams.ejercicios.medio.soluciones;

import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio8 {

    public static void main(String[] args) {
        List<Person> people = List.of(
                new Person("Ana", 16),
                new Person("Luis", 22),
                new Person("Clara", 17),
                new Person("Pedro", 30),
                new Person("Marta", 18),
                new Person("Jorge", 12),
                new Person("Lucía", 25)
        );

        // Tengo que hacer una agrupacion por +18 y otra por -18


        people
                .stream()

                // partitioningBy mejor cuando solo se puede agrupar en dos casos true/false
                // groupingBy cuando hay varias posibilidades
                .collect(Collectors.partitioningBy(p -> p.getAge() >= 18))
                // .collect(Collectors.groupingBy(p -> p.getAge() >= 18))
                .entrySet()
                .forEach(System.out::println);


    }

    static class Person {
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
