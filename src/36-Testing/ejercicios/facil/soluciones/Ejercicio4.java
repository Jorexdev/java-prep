import java.util.ArrayList;
import java.util.List;

public class Ejercicio4 {

    static void assertAll(String nombre, Runnable... assertions) {
        List<String> fallos = new ArrayList<>();
        for (Runnable a : assertions) {
            try {
                a.run();
            } catch (AssertionError e) {
                fallos.add(e.getMessage());
            }
        }
        if (fallos.isEmpty()) {
            System.out.println("PASS: " + nombre);
        } else {
            System.out.println("FAIL: " + nombre + " (" + fallos.size() + " fallos)");
            fallos.forEach(f -> System.out.println("     · " + f));
        }
    }

    static void check(boolean cond, String mensaje) {
        if (!cond) throw new AssertionError(mensaje);
    }

    record Persona(String nombre, int edad, String email) {}

    static void validarPersona(Persona p, String contexto) {
        assertAll("validar " + contexto,
            () -> check(p.nombre() != null && !p.nombre().isBlank(), "nombre no puede estar vacío"),
            () -> check(p.edad() >= 0 && p.edad() <= 150,           "edad fuera de rango: " + p.edad()),
            () -> check(p.email() != null && p.email().contains("@"), "email inválido: " + p.email())
        );
    }

    public static void main(String[] args) {
        Persona valida   = new Persona("Carlos", 30, "carlos@example.com");
        Persona invalida = new Persona("", -5, "sinArroba");

        validarPersona(valida,   "persona válida");
        validarPersona(invalida, "persona inválida");
    }
}
