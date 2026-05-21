import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio4 {

    // @Entity
    static class Usuario {
        // @Id
        int id;
        String nombre;
        boolean activo;
        String depto;

        Usuario(int id, String nombre, boolean activo, String depto) {
            this.id = id;
            this.nombre = nombre;
            this.activo = activo;
            this.depto = depto;
        }

        @Override
        public String toString() {
            return "Usuario{id=" + id + ", nombre='" + nombre + "', activo=" + activo + ", depto='" + depto + "'}";
        }
    }

    // @Repository
    static class UsuarioRepository {
        private final List<Usuario> datos = new ArrayList<>();

        void agregar(Usuario u) {
            datos.add(u);
        }

        // SELECT u FROM Usuario u
        List<Usuario> findAll() {
            return new ArrayList<>(datos);
        }

        // SELECT u FROM Usuario u WHERE u.activo = :activo
        List<Usuario> findByActivo(boolean activo) {
            return datos.stream()
                    .filter(u -> u.activo == activo)
                    .collect(Collectors.toList());
        }

        // SELECT u FROM Usuario u WHERE u.nombre LIKE :substring
        List<Usuario> findByNombreContaining(String substring) {
            return datos.stream()
                    .filter(u -> u.nombre.contains(substring))
                    .collect(Collectors.toList());
        }

        // SELECT COUNT(u) FROM Usuario u WHERE u.depto = :depto
        long countByDepto(String depto) {
            return datos.stream()
                    .filter(u -> u.depto.equals(depto))
                    .count();
        }
    }

    public static void main(String[] args) {

        UsuarioRepository repo = new UsuarioRepository();
        repo.agregar(new Usuario(1, "Ana García",    true,  "Backend"));
        repo.agregar(new Usuario(2, "Luis Martín",   false, "Frontend"));
        repo.agregar(new Usuario(3, "Ana López",     true,  "Backend"));
        repo.agregar(new Usuario(4, "Pedro Ruiz",    true,  "DevOps"));
        repo.agregar(new Usuario(5, "Marta Sánchez", false, "Backend"));

        System.out.println("--- findAll ---");
        repo.findAll().forEach(System.out::println);

        System.out.println("\n--- findByActivo(true) ---");
        repo.findByActivo(true).forEach(System.out::println);

        System.out.println("\n--- findByNombreContaining(\"Ana\") ---");
        repo.findByNombreContaining("Ana").forEach(System.out::println);

        System.out.println("\n--- countByDepto(\"Backend\") ---");
        System.out.println("Total: " + repo.countByDepto("Backend"));
    }
}
