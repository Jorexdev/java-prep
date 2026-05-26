import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

// Simula el patrón FactoryBean de Spring.
// Cuando Spring detecta un FactoryBean lo llama para obtener el objeto real;
// el factory en sí sigue siendo un bean accesible con el prefijo "&".
public class ExpFactoryBean {

    // ── Producto del factory ──────────────────────────────────────────────────

    static class Connection {
        private final String url;
        Connection(String url) { this.url = url; }
        public void execute(String sql) {
            System.out.println("  [" + url + "] " + sql);
        }
        @Override public String toString() { return "Connection(" + url + ")"; }
    }

    static class ConnectionPool {
        private final Deque<Connection> available = new ArrayDeque<>();
        private final int maxSize;

        ConnectionPool(String url, int maxSize) {
            this.maxSize = maxSize;
            for (int i = 0; i < maxSize; i++) {
                available.add(new Connection(url + "?conn=" + i));
            }
            System.out.println("  [Pool] creado con " + maxSize + " conexiones a " + url);
        }

        public Connection borrow() {
            Connection c = available.poll();
            if (c == null) throw new IllegalStateException("Pool agotado");
            System.out.println("  [Pool] prestando " + c);
            return c;
        }

        public void returnConnection(Connection c) {
            available.push(c);
            System.out.println("  [Pool] devuelta " + c);
        }

        public int available() { return available.size(); }
    }

    // ── FactoryBean ───────────────────────────────────────────────────────────

    // Equivalente a implementar org.springframework.beans.factory.FactoryBean<ConnectionPool>
    interface FactoryBean<T> {
        T getObject();
        Class<T> getObjectType();
        boolean isSingleton();
    }

    // @Component("connectionPool")
    static class ConnectionPoolFactory implements FactoryBean<ConnectionPool> {
        private final String url;
        private final int maxSize;
        private ConnectionPool cached;

        ConnectionPoolFactory(String url, int maxSize) {
            this.url     = url;
            this.maxSize = maxSize;
        }

        @Override
        public ConnectionPool getObject() {
            // isSingleton() = true → Spring cachea el resultado de getObject()
            if (isSingleton() && cached != null) return cached;
            cached = new ConnectionPool(url, maxSize);
            return cached;
        }

        @Override public Class<ConnectionPool> getObjectType() { return ConnectionPool.class; }
        @Override public boolean isSingleton() { return true; }
    }

    // ── BeanFactory que soporta FactoryBeans ─────────────────────────────────

    static class BeanFactory {
        private final Map<String, Object> beans           = new HashMap<>();
        private final Map<String, FactoryBean<?>> factories = new HashMap<>();

        void registerFactory(String name, FactoryBean<?> factory) {
            factories.put(name, factory);
            // En Spring: registrar "&name" da acceso al factory en sí
            beans.put("&" + name, factory);
        }

        void register(String name, Object bean) {
            beans.put(name, bean);
        }

        @SuppressWarnings("unchecked")
        <T> T getBean(String name) {
            // Prefijo "&" → devuelve el factory, no el producto
            if (name.startsWith("&")) {
                Object factory = beans.get(name);
                if (factory == null) throw new IllegalArgumentException("Factory no encontrado: " + name);
                return (T) factory;
            }
            if (factories.containsKey(name)) {
                return (T) factories.get(name).getObject();
            }
            Object bean = beans.get(name);
            if (bean == null) throw new IllegalArgumentException("Bean no encontrado: " + name);
            return (T) bean;
        }
    }

    public static void main(String[] args) {
        BeanFactory beanFactory = new BeanFactory();

        System.out.println("=== Registrando el factory ===");
        ConnectionPoolFactory factory = new ConnectionPoolFactory("jdbc:postgresql://localhost:5432/app", 3);
        beanFactory.registerFactory("connectionPool", factory);

        System.out.println("\n=== getBean(\"connectionPool\") → producto del factory ===");
        // Spring llama getObject() automáticamente — el cliente obtiene ConnectionPool, no el factory
        ConnectionPool pool = beanFactory.getBean("connectionPool");

        System.out.println("\n=== Uso del pool ===");
        Connection c1 = pool.borrow();
        Connection c2 = pool.borrow();
        c1.execute("SELECT 1");
        pool.returnConnection(c1);
        System.out.println("Disponibles: " + pool.available());

        System.out.println("\n=== getBean(\"&connectionPool\") → el factory en sí ===");
        // Prefijo "&" expone el FactoryBean para configuración avanzada
        ConnectionPoolFactory rawFactory = beanFactory.getBean("&connectionPool");
        System.out.println("Factory class: " + rawFactory.getClass().getSimpleName());
        System.out.println("isSingleton:   " + rawFactory.isSingleton());

        System.out.println("\n=== Singleton: segunda llamada reutiliza la misma instancia ===");
        ConnectionPool pool2 = beanFactory.getBean("connectionPool");
        System.out.println("pool == pool2 : " + (pool == pool2));  // true
    }
}
