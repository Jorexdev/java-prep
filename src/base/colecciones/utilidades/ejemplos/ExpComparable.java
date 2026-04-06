package base.colecciones.utilidades.ejemplos;

import java.util.*;

public class ExpComparable {

    /*
        Comparable es una interfaz que define el orden natural
        de los objetos mediante compareTo().
        Muchas clases del JDK ya la implementan (String, Integer, etc.).
     */


    /*                             KEY FEATURES                            */

    /*
        - int compareTo(T o):
            < 0 → this < o
            = 0 → this == o
            > 0 → this > o
        - Usado por defecto en sort() y en estructuras ordenadas.
     */


    // Clase ejemplo con orden natural propio
    static class Persona implements Comparable<Persona> {
        String nombre;
        int edad;

        Persona(String nombre, int edad) {
            this.nombre = nombre;
            this.edad = edad;
        }

        @Override
        public int compareTo(Persona otra) {
            return Integer.compare(this.edad, otra.edad); // orden natural: edad
        }

        @Override
        public String toString() {
            return nombre + "(" + edad + ")";
        }
    }

    public static void main(String[] args) {

        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona("Ana", 30));
        personas.add(new Persona("Luis", 25));
        personas.add(new Persona("Marta", 35));

        // sort() usa compareTo() definido en Persona
        Collections.sort(personas);
        System.out.println("Orden natural (edad): " + personas);

        // Podemos usar también Comparator adicional si queremos otro criterio
        personas.sort(Comparator.comparing(p -> p.nombre));
        System.out.println("Orden por nombre: " + personas);
    }
}
