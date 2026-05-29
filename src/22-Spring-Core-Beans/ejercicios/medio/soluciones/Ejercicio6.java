import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

// Scope prototype vs singleton: contador de instancias para demostrar la diferencia

public class Ejercicio6 {

    // Contador global de instancias de cada clase
    static final Map<String, AtomicInteger> instanceCounters = new HashMap<>();

    static int nextId(String name) {
        return instanceCounters
                .computeIfAbsent(name, k -> new AtomicInteger(0))
                .incrementAndGet();
    }

    // ====== Beans de ejemplo ======

    static class DatabaseConnection {
        private final int instanceId;
        private final String url;

        DatabaseConnection() {
            this.instanceId = nextId("DatabaseConnection");
            this.url = "jdbc:mysql://localhost/app?pool=" + instanceId;
            System.out.printf("  [new] DatabaseConnection #%d creada%n", instanceId);
        }

        @Override public String toString() {
            return "DatabaseConnection#" + instanceId + "(url=" + url + ")";
        }
    }

    static class UserSession {
        private final int instanceId;
        private final String sessionId;

        UserSession() {
            this.instanceId = nextId("UserSession");
            this.sessionId = "sess-" + instanceId + "-" + System.nanoTime() % 10000;
            System.out.printf("  [new] UserSession #%d creada (sessionId=%s)%n",
                    instanceId, sessionId);
        }

        @Override public String toString() {
            return "UserSession#" + instanceId + "(id=" + sessionId + ")";
        }
    }

    // ====== Contenedor con soporte de scopes ======

    static class ScopeContainer {
        private final Map<Class<?>, Object> singletons = new HashMap<>();
        private final Map<Class<?>, Class<?>> prototypeRegistry = new HashMap<>();

        // Registra un bean como singleton
        <T> void registerSingleton(Class<T> type, T instance) {
            singletons.put(type, instance);
            System.out.printf("  [Container] singleton registrado: %s (id=%s)%n",
                    type.getSimpleName(), System.identityHashCode(instance));
        }

        // Registra un bean como prototype (se crea una nueva instancia en cada getBean)
        void registerPrototype(Class<?> type) {
            prototypeRegistry.put(type, type);
            System.out.printf("  [Container] prototype registrado: %s%n", type.getSimpleName());
        }

        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> type) {
            // Singleton: siempre la misma instancia
            if (singletons.containsKey(type)) {
                T instance = (T) singletons.get(type);
                System.out.printf("  [Container] getBean(%s) [singleton] -> #%d%n",
                        type.getSimpleName(), System.identityHashCode(instance));
                return instance;
            }
            // Prototype: nueva instancia en cada llamada
            if (prototypeRegistry.containsKey(type)) {
                try {
                    T instance = (T) prototypeRegistry.get(type)
                            .getDeclaredConstructor().newInstance();
                    System.out.printf("  [Container] getBean(%s) [prototype] -> nueva instancia%n",
                            type.getSimpleName());
                    return instance;
                } catch (Exception e) {
                    throw new RuntimeException("Error creando prototipo " + type.getSimpleName(), e);
                }
            }
            throw new NoSuchElementException("Bean no registrado: " + type.getSimpleName());
        }

        int getTotalInstances(String beanName) {
            AtomicInteger counter = instanceCounters.get(beanName);
            return counter == null ? 0 : counter.get();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Scope Singleton vs Prototype con contador de instancias ===");
        System.out.println();

        ScopeContainer container = new ScopeContainer();

        // --- Singleton ---
        System.out.println("[ Registro ]");
        container.registerSingleton(DatabaseConnection.class, new DatabaseConnection());
        container.registerPrototype(UserSession.class);
        System.out.println();

        // --- Demo Singleton ---
        System.out.println("[ Demo Singleton: DatabaseConnection ]");
        System.out.println("  Llamando getBean 4 veces:");
        DatabaseConnection db1 = container.getBean(DatabaseConnection.class);
        DatabaseConnection db2 = container.getBean(DatabaseConnection.class);
        DatabaseConnection db3 = container.getBean(DatabaseConnection.class);
        DatabaseConnection db4 = container.getBean(DatabaseConnection.class);
        System.out.println();
        System.out.println("  Comparacion de referencias (== ):");
        System.out.printf("    db1 == db2: %b%n", db1 == db2);
        System.out.printf("    db2 == db3: %b%n", db2 == db3);
        System.out.printf("    db3 == db4: %b%n", db3 == db4);
        System.out.printf("  Instancias totales creadas: %d (debe ser 1)%n",
                container.getTotalInstances("DatabaseConnection"));
        System.out.printf("  hashCode identico: %b%n",
                System.identityHashCode(db1) == System.identityHashCode(db4));
        System.out.println();

        // --- Demo Prototype ---
        System.out.println("[ Demo Prototype: UserSession ]");
        System.out.println("  Llamando getBean 4 veces:");
        UserSession s1 = container.getBean(UserSession.class);
        UserSession s2 = container.getBean(UserSession.class);
        UserSession s3 = container.getBean(UserSession.class);
        UserSession s4 = container.getBean(UserSession.class);
        System.out.println();
        System.out.println("  Comparacion de referencias (== ):");
        System.out.printf("    s1 == s2: %b (esperado: false)%n", s1 == s2);
        System.out.printf("    s2 == s3: %b (esperado: false)%n", s2 == s3);
        System.out.printf("    s3 == s4: %b (esperado: false)%n", s3 == s4);
        System.out.println("  Comparacion de hashCode:");
        System.out.printf("    s1: %d%n", System.identityHashCode(s1));
        System.out.printf("    s2: %d%n", System.identityHashCode(s2));
        System.out.printf("    s3: %d%n", System.identityHashCode(s3));
        System.out.printf("    s4: %d%n", System.identityHashCode(s4));
        System.out.printf("  Instancias totales creadas: %d (debe ser 4)%n",
                container.getTotalInstances("UserSession"));
        System.out.println();

        // --- Resumen ---
        System.out.println("=== Resumen ===");
        System.out.printf("DatabaseConnection (singleton): %d instancia creada, reutilizada siempre.%n",
                container.getTotalInstances("DatabaseConnection"));
        System.out.printf("UserSession (prototype)       : %d instancias, una nueva por peticion.%n",
                container.getTotalInstances("UserSession"));
        System.out.println();
        System.out.println("Cuando usar cada scope:");
        System.out.println("  Singleton  -> beans sin estado o con estado compartido (services, repos, configs).");
        System.out.println("  Prototype  -> beans con estado por operacion (sesiones, DTOs de peticion).");
        System.out.println("  En Spring: @Scope(\"singleton\") [por defecto] | @Scope(\"prototype\")");
    }
}
