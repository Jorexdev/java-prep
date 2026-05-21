import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ejercicio2 {

    // Equivale a implementar InitializingBean + DisposableBean de Spring
    interface BeanLifecycle {
        void init();    // equivale a @PostConstruct
        void destroy(); // equivale a @PreDestroy
    }

    // Bean 1: conexión de base de datos
    static class ConexionDB implements BeanLifecycle {
        private boolean abierta = false;

        @Override
        public void init() {
            abierta = true;
            System.out.println("[ConexionDB] init() — conexión abierta");
        }

        @Override
        public void destroy() {
            abierta = false;
            System.out.println("[ConexionDB] destroy() — conexión cerrada");
        }

        String query(String sql) {
            if (!abierta) throw new IllegalStateException("Conexión cerrada");
            return "ResultSet de: " + sql;
        }
    }

    // Bean 2: cache en memoria
    static class CacheServicio implements BeanLifecycle {
        private final java.util.Map<String, String> cache = new java.util.HashMap<>();

        @Override
        public void init() {
            cache.put("config", "valor-inicial");
            System.out.println("[CacheServicio] init() — cache precargada con " + cache.size() + " entradas");
        }

        @Override
        public void destroy() {
            int size = cache.size();
            cache.clear();
            System.out.println("[CacheServicio] destroy() — cache limpiada (" + size + " entradas eliminadas)");
        }
    }

    // Bean 3: scheduler periódico
    static class Scheduler implements BeanLifecycle {
        private boolean activo = false;

        @Override
        public void init() {
            activo = true;
            System.out.println("[Scheduler] init() — tareas programadas iniciadas");
        }

        @Override
        public void destroy() {
            activo = false;
            System.out.println("[Scheduler] destroy() — tareas canceladas");
        }

        boolean isActivo() { return activo; }
    }

    // Contenedor que gestiona el ciclo de vida
    static class LifecycleContainer {
        // Lista ordenada: el índice 0 fue el primero en registrarse
        private final List<BeanLifecycle> beans = new ArrayList<>();

        void register(BeanLifecycle bean) {
            String nombre = bean.getClass().getSimpleName();
            System.out.println("[Container] Registrando: " + nombre);
            bean.init(); // equivale a llamar @PostConstruct
            beans.add(bean);
            System.out.println("[Container] " + nombre + " listo\n");
        }

        // Cierra el contexto: destroy en orden inverso
        void close() {
            System.out.println("[Container] Cerrando contexto...\n");
            List<BeanLifecycle> reversed = new ArrayList<>(beans);
            Collections.reverse(reversed);
            for (BeanLifecycle bean : reversed) {
                bean.destroy();
            }
            beans.clear();
            System.out.println("\n[Container] Contexto cerrado");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== @PostConstruct / @PreDestroy simulado ===\n");

        LifecycleContainer container = new LifecycleContainer();

        // Registrar en orden: DB -> Cache -> Scheduler
        ConexionDB db = new ConexionDB();
        CacheServicio cache = new CacheServicio();
        Scheduler scheduler = new Scheduler();

        container.register(db);
        container.register(cache);
        container.register(scheduler);

        System.out.println("--- Usando los beans ---");
        System.out.println(db.query("SELECT * FROM usuarios"));
        System.out.println("Scheduler activo: " + scheduler.isActivo());

        System.out.println();

        // Cerrar: destroy en orden inverso (Scheduler -> Cache -> DB)
        container.close();

        System.out.println("\nObservación: destroy en orden inverso al de registro (LIFO)");
    }
}
