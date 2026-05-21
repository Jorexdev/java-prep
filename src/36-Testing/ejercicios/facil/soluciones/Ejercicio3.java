import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class Ejercicio3 {

    static class ServicioUsuarios {
        private final Map<Integer, String> usuarios = new HashMap<>();
        private final Set<Integer> conPedidos = new HashSet<>();
        private int nextId = 1;

        int crear(String nombre) {
            if (nombre == null || nombre.isBlank())
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            int id = nextId++;
            usuarios.put(id, nombre);
            return id;
        }

        String buscar(int id) {
            if (!usuarios.containsKey(id))
                throw new NoSuchElementException("Usuario con id " + id + " no encontrado");
            return usuarios.get(id);
        }

        void marcarConPedidos(int id) { conPedidos.add(id); }

        void eliminar(int id) {
            if (conPedidos.contains(id))
                throw new IllegalStateException("No se puede eliminar: el usuario " + id + " tiene pedidos activos");
            usuarios.remove(id);
        }
    }

    static void assertThrowsConMensaje(Class<? extends Exception> tipo, String mensajeEsperado, Runnable r, String nombre) {
        try {
            r.run();
            System.out.println("FAIL: " + nombre + " — no se lanzó ninguna excepción");
        } catch (Exception e) {
            if (!tipo.isInstance(e)) {
                System.out.println("FAIL: " + nombre + " — tipo incorrecto: " + e.getClass().getSimpleName());
                return;
            }
            if (!e.getMessage().contains(mensajeEsperado)) {
                System.out.println("FAIL: " + nombre + " — mensaje incorrecto: \"" + e.getMessage() + "\"");
                return;
            }
            System.out.println("PASS: " + nombre);
        }
    }

    public static void main(String[] args) {
        ServicioUsuarios svc = new ServicioUsuarios();
        int id = svc.crear("Ana");
        svc.marcarConPedidos(id);

        assertThrowsConMensaje(
            IllegalArgumentException.class,
            "vacío",
            () -> svc.crear("   "),
            "crear con nombre blank lanza IllegalArgumentException"
        );

        assertThrowsConMensaje(
            NoSuchElementException.class,
            "no encontrado",
            () -> svc.buscar(999),
            "buscar id inexistente lanza NoSuchElementException"
        );

        assertThrowsConMensaje(
            IllegalStateException.class,
            "pedidos activos",
            () -> svc.eliminar(id),
            "eliminar usuario con pedidos lanza IllegalStateException"
        );
    }
}
