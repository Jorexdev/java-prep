import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

// Ciclo de vida de beans simulado con un BeanContainer Java plano
// En Spring: @Bean, @Scope("prototype"), InitializingBean, DisposableBean
// Aquí: BeanContainer llama manualmente a afterPropertiesSet() y destroy(),
//       y gestiona singleton vs prototype sin ninguna dependencia de Spring.
public class ExpBeans {

    // --- Interfaces de ciclo de vida (idénticas a las de Spring, sin import) ---
    // En Spring: import org.springframework.beans.factory.InitializingBean
    interface InitializingBean {
        void afterPropertiesSet() throws Exception;
    }

    // En Spring: import org.springframework.beans.factory.DisposableBean
    interface DisposableBean {
        void destroy() throws Exception;
    }

    // --- Bean singleton con hooks de ciclo de vida ---
    // InitializingBean → equivalente a @PostConstruct en Spring Boot
    // DisposableBean   → equivalente a @PreDestroy  en Spring Boot
    static class ConexionDB implements InitializingBean, DisposableBean {

        private final String url;

        ConexionDB(String url) {
            this.url = url;
            System.out.println("[CONSTRUCTOR] ConexionDB creada para: " + url);
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            // Se ejecuta después de que el contenedor inyecta todas las dependencias
            System.out.println("[INIT] Conexión abierta → " + url);
        }

        @Override
        public void destroy() throws Exception {
            // Se ejecuta al cerrar el contenedor
            System.out.println("[DESTROY] Conexión cerrada → " + url);
        }

        public void query(String sql) {
            System.out.println("[QUERY] " + sql);
        }
    }

    // --- Bean prototype: nueva instancia en cada getBean() ---
    static class Tarea {
        private static int contador = 0;
        private final int id = ++contador;

        public void ejecutar() {
            System.out.println("  Tarea #" + id + " ejecutándose");
        }
    }

    // --- Contenedor simple que simula ApplicationContext ---
    static class BeanContainer implements AutoCloseable {

        // Almacén de singletons ya inicializados
        private final Map<String, Object> singletons = new HashMap<>();
        // Fábricas para beans prototype (nueva instancia en cada llamada)
        private final Map<String, Supplier<?>> prototypes = new HashMap<>();

        // Registra un singleton, llama afterPropertiesSet() si aplica
        // En Spring: @Bean (scope singleton por defecto)
        void registerSingleton(String name, Object bean) throws Exception {
            if (bean instanceof InitializingBean ib) {
                ib.afterPropertiesSet();
            }
            singletons.put(name, bean);
        }

        // Registra un prototipo — la fábrica se invoca en cada getBean()
        // En Spring: @Bean @Scope("prototype")
        void registerPrototype(String name, Supplier<?> factory) {
            prototypes.put(name, factory);
        }

        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> type) {
            // Busca primero en singletons
            for (Object bean : singletons.values()) {
                if (type.isInstance(bean)) return type.cast(bean);
            }
            // Luego en prototypes — crea nueva instancia cada vez
            for (Supplier<?> factory : prototypes.values()) {
                Object bean = factory.get();
                if (type.isInstance(bean)) return type.cast(bean);
            }
            throw new RuntimeException("Bean de tipo " + type.getSimpleName() + " no encontrado");
        }

        // Llama destroy() en todos los singletons al cerrar — como ctx.close()
        @Override
        public void close() throws Exception {
            for (Object bean : singletons.values()) {
                if (bean instanceof DisposableBean db) {
                    db.destroy();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Singleton ===");
        try (BeanContainer ctx = new BeanContainer()) {

            // En Spring: @Bean ConexionDB conexion() { return new ConexionDB("jdbc:..."); }
            ConexionDB instancia = new ConexionDB("jdbc:postgresql://localhost:5432/app");
            ctx.registerSingleton("conexion", instancia);

            // En Spring: @Bean @Scope("prototype") Tarea tarea() { return new Tarea(); }
            ctx.registerPrototype("tarea", Tarea::new);

            ConexionDB c1 = ctx.getBean(ConexionDB.class);
            ConexionDB c2 = ctx.getBean(ConexionDB.class);
            System.out.println("Misma instancia: " + (c1 == c2)); // true — singleton

            c1.query("SELECT * FROM usuarios");

            System.out.println("\n=== Prototype ===");
            Tarea t1 = ctx.getBean(Tarea.class);
            Tarea t2 = ctx.getBean(Tarea.class);
            System.out.println("Misma instancia: " + (t1 == t2)); // false — prototype
            t1.ejecutar();
            t2.ejecutar();

            System.out.println("\n=== Cerrando contenedor ===");
            // Al salir del try-with-resources se llama close() → destroy() en singletons
        }
    }
}
