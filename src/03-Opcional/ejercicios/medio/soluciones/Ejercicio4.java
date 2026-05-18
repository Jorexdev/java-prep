import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class Ejercicio4 {

    static class RepositorioUsuarios {
        private final Map<Integer, String> datos = new HashMap<>();

        RepositorioUsuarios() {
            datos.put(1, "Jorge");
            datos.put(2, "Ana");
            datos.put(3, "Luis");
        }

        Optional<String> findById(int id) {
            return Optional.ofNullable(datos.get(id));
        }
    }

    public static void main(String[] args) {
        RepositorioUsuarios repo = new RepositorioUsuarios();

        // Caso 1: id existente — orElseThrow no lanza excepción
        String usuario = repo.findById(1)
                .orElseThrow(() -> new NoSuchElementException("Usuario con id 1 no encontrado"));
        System.out.println("Usuario encontrado: " + usuario);

        // Caso 2: id inexistente — orElseThrow lanza excepción
        try {
            String noExiste = repo.findById(99)
                    .orElseThrow(() -> new NoSuchElementException("Usuario con id 99 no encontrado"));
            System.out.println("Usuario: " + noExiste); // no se alcanza
        } catch (NoSuchElementException e) {
            System.out.println("Excepción: " + e.getMessage());
        }
    }
}
