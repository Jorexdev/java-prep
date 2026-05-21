import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// @RestController
// @RequestMapping("/api/tareas")
public class Ejercicio1 {

    record Tarea(int id, String titulo, boolean completada) {
        @Override
        public String toString() {
            return "Tarea{id=" + id + ", titulo='" + titulo + "', completada=" + completada + "}";
        }
    }

    static class TareaController {
        private final Map<Integer, Tarea> store = new HashMap<>();
        private int nextId = 1;

        // @GetMapping
        Collection<Tarea> getAll() {
            return store.values();
        }

        // @GetMapping("/{id}")
        Tarea getById(int id) {
            Tarea t = store.get(id);
            if (t == null) throw new RuntimeException("Tarea no encontrada: " + id);
            return t;
        }

        // @PostMapping
        Tarea create(String titulo) {
            Tarea t = new Tarea(nextId++, titulo, false);
            store.put(t.id(), t);
            return t;
        }

        // @PutMapping("/{id}")
        Tarea update(int id, boolean completada) {
            Tarea existing = getById(id);
            Tarea updated = new Tarea(existing.id(), existing.titulo(), completada);
            store.put(id, updated);
            return updated;
        }

        // @DeleteMapping("/{id}")
        void delete(int id) {
            if (store.remove(id) == null) throw new RuntimeException("Tarea no encontrada: " + id);
        }
    }

    public static void main(String[] args) {
        TareaController ctrl = new TareaController();

        ctrl.create("Aprender Spring MVC");
        ctrl.create("Practicar REST");
        ctrl.create("Revisar HTTP");

        System.out.println("-- Todas las tareas --");
        ctrl.getAll().forEach(System.out::println);

        System.out.println("\n-- Obtener tarea 2 --");
        System.out.println(ctrl.getById(2));

        System.out.println("\n-- Actualizar tarea 1 como completada --");
        System.out.println(ctrl.update(1, true));

        System.out.println("\n-- Eliminar tarea 3 --");
        ctrl.delete(3);

        System.out.println("\n-- Tareas finales --");
        ctrl.getAll().forEach(System.out::println);
    }
}
