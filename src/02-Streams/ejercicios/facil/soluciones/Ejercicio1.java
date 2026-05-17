import java.util.List;

public class Ejercicio1 {

    public static void main(String[] args) {

        // Ejercicio: filtrar personas mayores de 18 años y mostrarlas
        List<Person> people = List.of(
                new Person("Jorge", 20),
                new Person("Luis", 12),
                new Person("Ana", 19),
                new Person("Izaro", 21),
                new Person("Isabel", 17)
        );

        people
                .stream()
                .filter(x -> x.getAge() >= 18)  // descarta a los que no cumplen la condición
                .forEach(System.out::println);   // para solo contar: .count() en lugar de forEach
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
            return "Person{name='" + name + "', age=" + age + '}';
        }
    }
}
