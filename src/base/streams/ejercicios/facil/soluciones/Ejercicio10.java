package base.streams.ejercicios.facil.soluciones;

import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio10 {
    static List<Persona> people = List.of(
            new Persona("Jorge", 20),
            new Persona("Luis", 15),
            new Persona("David",34),
            new Persona("Juan", 10),
            new Persona("Ana", 45),
            new Persona("Izaro", 21));


    public static void main(String[] args) {
            people
                    .stream()
                    .collect(Collectors.toMap(Persona::getName, Persona::getEdad))
                    .entrySet()
                    .forEach(System.out::println);

        }

        static class Persona {
            private final String name;
            private final int edad;


            Persona(String name, int edad) {
                this.name = name;
                this.edad = edad;
            }

            public String getName() {
                return name;
            }

            public int getEdad() {
                return edad;
            }
        }
}

