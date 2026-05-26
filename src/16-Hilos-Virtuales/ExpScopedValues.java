import java.util.concurrent.*;

// ScopedValue es una API de preview en Java 21 (--enable-preview).
// Este archivo simula la semántica con un ThreadLocal para que compile sin flags extra.
// El comportamiento es equivalente: valor visible en todo el árbol de llamadas sin pasarlo explícitamente.
public class ExpScopedValues {

    // En Java 21+ real: static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
    // Aquí usamos ThreadLocal como sustituto compilable
    static final ScopedValueSim<String> REQUEST_ID = new ScopedValueSim<>();

    public static void main(String[] args) throws Exception {

        System.out.println("=== ScopedValue con virtual threads ===\n");

        ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();

        // Simular 3 requests HTTP concurrentes, cada uno con su propio REQUEST_ID
        // En real: ScopedValue.where(REQUEST_ID, id).run(() -> handleRequest())
        Future<?> r1 = exec.submit(() -> REQUEST_ID.where("req-A").run(() -> handleRequest("GET /users")));
        Future<?> r2 = exec.submit(() -> REQUEST_ID.where("req-B").run(() -> handleRequest("POST /orders")));
        Future<?> r3 = exec.submit(() -> REQUEST_ID.where("req-C").run(() -> handleRequest("GET /products")));

        r1.get(); r2.get(); r3.get();

        System.out.println("\n=== Comparación: ThreadLocal vs ScopedValue ===");
        System.out.println("ThreadLocal:");
        System.out.println("  - mutable: set() en cualquier punto puede cambiar el valor");
        System.out.println("  - requiere remove() manual o ThreadPoolExecutor.afterExecute()");
        System.out.println("  - hilo de plataforma reusado: puede leer valor del request anterior");
        System.out.println("ScopedValue:");
        System.out.println("  - inmutable: una vez ligado, nadie puede reasignarlo");
        System.out.println("  - limpieza automática al salir del bloque where().run()");
        System.out.println("  - compatible con virtual threads: scope ligado al frame, no al carrier thread");

        exec.shutdown();
    }

    // ── Capas de la aplicación ──────────────────────────────────────────────

    // Capa HTTP: establece el ScopedValue y despacha al servicio
    static void handleRequest(String path) {
        // REQUEST_ID ya está ligado por el llamador — no se pasa como parámetro
        log("HTTP handler", "procesando " + path);
        userService(path);
    }

    // Capa Service: lee REQUEST_ID sin recibirlo como argumento
    static void userService(String path) {
        log("UserService", "lógica de negocio para " + path);
        userRepository(path);
    }

    // Capa Repository: también puede leer REQUEST_ID para correlación de logs/trazas
    static void userRepository(String path) {
        log("Repository", "consulta DB para " + path);
    }

    // Lee REQUEST_ID directamente — disponible en todo el árbol de llamadas del scope
    static void log(String layer, String msg) {
        System.out.println("[" + REQUEST_ID.get() + "] " + layer + ": " + msg);
    }

    // ── Simulación de ScopedValue ───────────────────────────────────────────

    // Modela la semántica inmutable y de limpieza automática de ScopedValue real
    static class ScopedValueSim<T> {
        // InheritableThreadLocal para que virtual threads hijos hereden el valor
        private final InheritableThreadLocal<T> tl = new InheritableThreadLocal<>();

        // where() devuelve un Runner con el valor fijado
        Runner where(T value) { return new Runner(value); }

        T get() { return tl.get(); }

        class Runner {
            private final T value;
            Runner(T value) { this.value = value; }

            // run() liga el valor, ejecuta la acción, y lo limpia automáticamente al salir
            void run(Runnable action) {
                tl.set(value);
                try { action.run(); }
                finally { tl.remove(); }  // limpieza garantizada aunque la acción lance excepción
            }
        }
    }
}
