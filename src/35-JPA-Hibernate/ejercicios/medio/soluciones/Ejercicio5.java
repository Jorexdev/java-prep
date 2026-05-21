import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio5 {

    // @Entity
    static class Usuario {
        // @Id
        int id;
        String nombre;
        String depto;
        boolean activo;

        Usuario(int id, String nombre, String depto, boolean activo) {
            this.id = id;
            this.nombre = nombre;
            this.depto = depto;
            this.activo = activo;
        }
    }

    static class UsuarioRepository {
        private final List<Usuario> datos = new ArrayList<>();

        void cargar(int total) {
            String[] deptos = {"Backend", "Frontend", "DevOps", "QA"};
            for (int i = 1; i <= total; i++) {
                datos.add(new Usuario(i, "Usuario-" + i, deptos[i % deptos.length], i % 3 != 0));
            }
        }

        void resetActivos() {
            datos.forEach(u -> u.activo = u.id % 3 != 0);
        }

        // Modo ineficiente: bucle uno a uno
        void updateActivoByDepto(String depto, boolean activo) {
            for (Usuario u : datos) {
                if (u.depto.equals(depto)) {
                    u.activo = activo;
                }
            }
        }

        // Modo eficiente: operación en bloque con stream
        void bulkUpdateActivo(String depto, boolean activo) {
            datos.stream()
                 .filter(u -> u.depto.equals(depto))
                 .forEach(u -> u.activo = activo);
        }

        long countActivosByDepto(String depto) {
            return datos.stream().filter(u -> u.depto.equals(depto) && u.activo).count();
        }
    }

    public static void main(String[] args) {

        UsuarioRepository repo = new UsuarioRepository();
        repo.cargar(100);

        String depto = "Backend";

        repo.resetActivos();
        long t0 = System.nanoTime();
        for (int i = 0; i < 100_000; i++) {
            repo.updateActivoByDepto(depto, false);
        }
        long tiempoIneficiente = System.nanoTime() - t0;

        repo.resetActivos();
        long t1 = System.nanoTime();
        for (int i = 0; i < 100_000; i++) {
            repo.bulkUpdateActivo(depto, false);
        }
        long tiempoEficiente = System.nanoTime() - t1;

        System.out.println("updateActivoByDepto (bucle):  " + tiempoIneficiente / 1_000_000 + " ms");
        System.out.println("bulkUpdateActivo    (stream): " + tiempoEficiente   / 1_000_000 + " ms");

        repo.resetActivos();
        repo.bulkUpdateActivo(depto, false);
        System.out.println("\nActivos en Backend tras bulk: " + repo.countActivosByDepto(depto));
    }
}
