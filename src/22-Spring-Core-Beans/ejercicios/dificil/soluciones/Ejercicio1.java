import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Ejercicio1 {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Bean {}

    static class DataSource {
        String url = "jdbc:h2:mem:testdb";
        @Override public String toString() { return "DataSource{url='" + url + "'}"; }
    }

    static class Cache {
        int maxSize = 1000;
        @Override public String toString() { return "Cache{maxSize=" + maxSize + "}"; }
    }

    static class TaskExecutor {
        int threads = 4;
        @Override public String toString() { return "TaskExecutor{threads=" + threads + "}"; }
    }

    // Simula @Configuration
    static class InfrastructureConfig {
        @Bean
        DataSource dataSource() {
            DataSource ds = new DataSource();
            ds.url = "jdbc:postgresql://localhost/prod";
            return ds;
        }

        @Bean
        Cache localCache() {
            Cache c = new Cache();
            c.maxSize = 500;
            return c;
        }

        @Bean
        TaskExecutor asyncExecutor() {
            TaskExecutor te = new TaskExecutor();
            te.threads = Runtime.getRuntime().availableProcessors();
            return te;
        }
    }

    static class ConfigurationProcessor {
        private final Map<String, Object> beans = new HashMap<>();

        void process(Object configInstance) throws Exception {
            for (Method m : configInstance.getClass().getDeclaredMethods()) {
                if (m.isAnnotationPresent(Bean.class)) {
                    m.setAccessible(true);
                    Object bean = m.invoke(configInstance);
                    beans.put(m.getName(), bean);
                    System.out.println("  @Bean registrado: " + m.getName() + " → " + bean);
                }
            }
        }

        Object getBean(String name) { return beans.get(name); }
        Map<String, Object> all()   { return beans; }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Procesando @Configuration con reflection ===\n");
        ConfigurationProcessor processor = new ConfigurationProcessor();
        processor.process(new InfrastructureConfig());

        System.out.println("\n=== Recuperando beans por nombre ===");
        DataSource ds = (DataSource) processor.getBean("dataSource");
        Cache cache   = (Cache)      processor.getBean("localCache");
        System.out.println("dataSource.url   = " + ds.url);
        System.out.println("localCache.maxSize = " + cache.maxSize);

        System.out.println("\nTotal beans registrados: " + processor.all().size());
    }
}
