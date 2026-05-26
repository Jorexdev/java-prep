import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

// Simula singleton vs prototype scope de Spring.
// Singleton: una instancia compartida por todo el contexto.
// Prototype: nueva instancia en cada solicitud.
public class ExpBeanScopes {

    static class ConexionDB {
        private static int instancias = 0;
        private final int id = ++instancias;

        public void query(String sql) {
            System.out.println("  [Conexión #" + id + "] " + sql);
        }

        @Override
        public String toString() { return "ConexionDB#" + id; }
    }

    static class Tarea {
        private static int contador = 0;
        private final int id = ++contador;

        public void ejecutar() {
            System.out.println("  Tarea #" + id + " ejecutándose");
        }

        @Override
        public String toString() { return "Tarea#" + id; }
    }

    // Registro que implementa ambos scopes
    static class ScopedRegistry {
        private final Map<String, Object> singletons = new HashMap<>();
        private final Map<String, Supplier<?>> prototypes = new HashMap<>();

        // Registra bean singleton — la instancia se crea una sola vez
        // @Bean (sin @Scope → singleton por defecto en Spring)
        <T> void registerSingleton(String name, T instance) {
            singletons.put(name, instance);
        }

        // Registra bean prototype — el supplier se llama en cada getBean()
        // @Bean @Scope("prototype")
        <T> void registerPrototype(String name, Supplier<T> factory) {
            prototypes.put(name, factory);
        }

        @SuppressWarnings("unchecked")
        <T> T getBean(String name) {
            if (singletons.containsKey(name)) {
                return (T) singletons.get(name);
            }
            if (prototypes.containsKey(name)) {
                return (T) prototypes.get(name).get();
            }
            throw new IllegalArgumentException("Bean no registrado: " + name);
        }
    }

    public static void main(String[] args) {
        ScopedRegistry registry = new ScopedRegistry();

        // Singleton: la misma instancia siempre
        registry.registerSingleton("conexion", new ConexionDB());

        // Prototype: nueva instancia en cada llamada
        registry.registerPrototype("tarea", Tarea::new);

        System.out.println("=== Singleton scope ===");
        ConexionDB a = registry.getBean("conexion");
        ConexionDB b = registry.getBean("conexion");
        System.out.println("  a → " + a);
        System.out.println("  b → " + b);
        System.out.println("  a == b : " + (a == b));  // true — misma referencia
        a.query("SELECT * FROM usuarios");

        System.out.println("\n=== Prototype scope ===");
        Tarea t1 = registry.getBean("tarea");
        Tarea t2 = registry.getBean("tarea");
        System.out.println("  t1 → " + t1);
        System.out.println("  t2 → " + t2);
        System.out.println("  t1 == t2 : " + (t1 == t2));  // false — instancias distintas
        t1.ejecutar();
        t2.ejecutar();

        System.out.println("\n=== Scopes adicionales en Spring Boot ===");
        // @Scope("request")  → nueva instancia por HTTP request (requiere web context)
        // @Scope("session")  → una instancia por sesión HTTP
        // @Scope("websocket")→ una instancia por conexión WebSocket
        // Todos los scopes web necesitan un ScopedProxyMode para beans singleton que los inyectan
        System.out.println("  request   → una instancia por HTTP request");
        System.out.println("  session   → una instancia por sesión HTTP");
        System.out.println("  websocket → una instancia por conexión WebSocket");
        System.out.println("  (request/session/websocket requieren contexto web)");
    }
}
