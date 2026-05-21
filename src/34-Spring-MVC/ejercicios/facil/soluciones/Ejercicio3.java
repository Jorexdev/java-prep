import java.util.Map;

// @RestController
// @RequestMapping("/api")
public class Ejercicio3 {

    static final Map<Integer, String> usuarios = Map.of(
        1, "Ana García",
        2, "Luis Martín",
        3, "Sara López"
    );

    // @GetMapping("/usuarios/{id}")
    static String getUsuario(/* @PathVariable */ int id) {
        String nombre = usuarios.get(id);
        if (nombre == null) {
            throw new RuntimeException("404 Not Found: " + id);
        }
        return nombre;
    }

    public static void main(String[] args) {
        try {
            System.out.println("Usuario 1: " + getUsuario(1));
            System.out.println("Usuario 2: " + getUsuario(2));
            System.out.println("Usuario 99: " + getUsuario(99));
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
