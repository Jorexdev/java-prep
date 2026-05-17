import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio8 {

    public static void main(String[] args) {

        // Ejercicio: agrupar personas en mayores de edad y menores de edad
        List<Person> people = List.of(
                new Person("Ana",   16),
                new Person("Luis",  22),
                new Person("Clara", 17),
                new Person("Pedro", 30),
                new Person("Marta", 18),
                new Person("Jorge", 12),
                new Person("Lucía", 25)
        );

        // partitioningBy cuando solo hay dos grupos (true/false)
        // groupingBy cuando hay más de dos posibilidades
        people
                .stream()
                .collect(Collectors.partitioningBy(p -> p.getAge() >= 18))
                .forEach((esMayor, lista) ->
                        System.out.println((esMayor ? "Mayores" : "Menores") + ": " + lista));
    }

    static class Person {

        private final String name;
        private final int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
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
