package base.streams.ejercicios.facil.soluciones;

import java.util.List;

public class Ejercicio7 {
    static List<Persona> people = List.of(
            new Persona("Jorge", List.of("Amazon", "Oracle")),
            new Persona("Luis", List.of("BytesColaborativos", "Oracle")),
            new Persona("David", List.of("Microsoft", "Red Hat")),
            new Persona("Juan", List.of("Amazon", "Google")),
            new Persona("Ana", List.of("Google", "BytesColaborativos")),
            new Persona("Izaro", List.of("Amazon", "Microsoft")));

    public static void main(String[] args) {
        people
                .stream()
                .map(Persona::getTitles)
                .flatMap(List::stream)
                .distinct()
                .forEach(System.out::println);
    }

    static class Persona {
        private final List<String> titles;

        Persona(String nombre, List<String> titles) {
            this.titles = titles;
        }

        public List<String> getTitles() {
            return titles;
        }
    }
}
