import java.util.*;
import java.util.function.Supplier;

// Custom starter simulado con auto-configuracion condicional

public class Ejercicio6 {

    // ====== Modelo de propiedades del starter ======

    static class StarterProperties {
        final Map<String, String> props;

        StarterProperties(Map<String, String> props) {
            this.props = props;
        }

        String get(String key) { return props.get(key); }
        String get(String key, String def) { return props.getOrDefault(key, def); }
        boolean getBoolean(String key, boolean def) {
            String v = props.get(key);
            return v == null ? def : Boolean.parseBoolean(v);
        }
        int getInt(String key, int def) {
            String v = props.get(key);
            return v == null ? def : Integer.parseInt(v);
        }
    }

    // ====== Condiciones ======

    interface Condition {
        boolean matches(StarterProperties ctx);
        String description();
    }

    static class PropertyEqualsCondition implements Condition {
        private final String key;
        private final String expected;

        PropertyEqualsCondition(String key, String expected) {
            this.key = key;
            this.expected = expected;
        }

        public boolean matches(StarterProperties ctx) {
            return expected.equals(ctx.get(key));
        }

        public String description() {
            return "@ConditionalOnProperty(" + key + "=" + expected + ")";
        }
    }

    static class PropertyMissingCondition implements Condition {
        private final String key;

        PropertyMissingCondition(String key) { this.key = key; }

        public boolean matches(StarterProperties ctx) {
            return !ctx.props.containsKey(key);
        }

        public String description() {
            return "@ConditionalOnMissingProperty(" + key + ")";
        }
    }

    // ====== Beans del starter ======

    // Starter "cache": activa CacheManager si cache.enabled=true
    static class CacheManager {
        private final String type;
        private final int maxSize;
        private final Map<String, Object> store = new LinkedHashMap<>();

        CacheManager(String type, int maxSize) {
            this.type = type;
            this.maxSize = maxSize;
            System.out.printf("    [CacheManager] inicializado (type=%s, maxSize=%d)%n", type, maxSize);
        }

        void put(String key, Object val) {
            if (store.size() >= maxSize) store.remove(store.keySet().iterator().next());
            store.put(key, val);
        }
        Object get(String key) { return store.get(key); }
        @Override public String toString() { return "CacheManager{type=" + type + ", max=" + maxSize + "}"; }
    }

    // Starter "cache": activa CacheStatistics si cache.stats.enabled=true
    static class CacheStatistics {
        private int hits = 0;
        private int misses = 0;

        CacheStatistics() {
            System.out.println("    [CacheStatistics] inicializado");
        }

        void recordHit()  { hits++; }
        void recordMiss() { misses++; }
        double hitRate()  { return hits + misses == 0 ? 0 : (double) hits / (hits + misses); }
        @Override public String toString() {
            return String.format("CacheStats{hits=%d, misses=%d, hitRate=%.1f%%}",
                    hits, misses, hitRate() * 100);
        }
    }

    // Starter "security": activa SecurityFilter si security.enabled=true
    static class SecurityFilter {
        private final String realm;
        private final List<String> allowedIps;

        SecurityFilter(String realm, List<String> allowedIps) {
            this.realm = realm;
            this.allowedIps = allowedIps;
            System.out.printf("    [SecurityFilter] inicializado (realm=%s, ips=%s)%n",
                    realm, allowedIps);
        }

        boolean isAllowed(String ip) { return allowedIps.isEmpty() || allowedIps.contains(ip); }
        @Override public String toString() { return "SecurityFilter{realm=" + realm + "}"; }
    }

    // Starter "metrics": siempre activo si metrics.enabled=true
    static class MetricsRegistry {
        private final Map<String, Long> counters = new LinkedHashMap<>();

        MetricsRegistry() {
            System.out.println("    [MetricsRegistry] inicializado");
        }

        void increment(String name) { counters.merge(name, 1L, Long::sum); }
        @Override public String toString() { return "MetricsRegistry{counters=" + counters + "}"; }
    }

    // ====== Auto-configuracion del starter ======

    static class AutoConfigResult {
        final String beanName;
        final boolean created;
        final String conditionResult;
        final Object bean;

        AutoConfigResult(String beanName, boolean created, String conditionResult, Object bean) {
            this.beanName = beanName;
            this.created = created;
            this.conditionResult = conditionResult;
            this.bean = bean;
        }
    }

    static class CustomStarterAutoConfig {
        private final StarterProperties props;
        private final List<AutoConfigResult> report = new ArrayList<>();
        private final Map<String, Object> beans = new LinkedHashMap<>();

        CustomStarterAutoConfig(StarterProperties props) {
            this.props = props;
        }

        void configure() {
            System.out.println("  [ Auto-configurando custom starter ]");

            // Bean 1: CacheManager (requiere cache.enabled=true)
            configureBean("cacheManager",
                    new PropertyEqualsCondition("cache.enabled", "true"),
                    () -> new CacheManager(
                            props.get("cache.type", "CAFFEINE"),
                            props.getInt("cache.maxSize", 1000)));

            // Bean 2: CacheStatistics (requiere cache.enabled=true Y cache.stats.enabled=true)
            configureBean("cacheStatistics",
                    ctx -> ctx.getBoolean("cache.enabled", false)
                            && ctx.getBoolean("cache.stats.enabled", false),
                    () -> new CacheStatistics(),
                    "@ConditionalOnProperty(cache.enabled=true AND cache.stats.enabled=true)");

            // Bean 3: SecurityFilter (requiere security.enabled=true)
            configureBean("securityFilter",
                    new PropertyEqualsCondition("security.enabled", "true"),
                    () -> new SecurityFilter(
                            props.get("security.realm", "default"),
                            parseList(props.get("security.allowedIps", ""))));

            // Bean 4: MetricsRegistry (requiere metrics.enabled=true)
            configureBean("metricsRegistry",
                    new PropertyEqualsCondition("metrics.enabled", "true"),
                    () -> new MetricsRegistry());

            // Bean 5: DefaultCacheManager si NO hay CacheManager activo
            configureBean("defaultCacheManager",
                    new PropertyMissingCondition("cache.enabled"),
                    () -> new CacheManager("SIMPLE", 100));
        }

        private void configureBean(String name, Condition condition, Supplier<Object> factory) {
            boolean matches = condition.matches(props);
            Object bean = null;
            if (matches) bean = factory.get();
            if (bean != null) beans.put(name, bean);
            report.add(new AutoConfigResult(name, matches, condition.description(), bean));
        }

        private void configureBean(String name,
                                    java.util.function.Predicate<StarterProperties> pred,
                                    Supplier<Object> factory,
                                    String description) {
            boolean matches = pred.test(props);
            Object bean = null;
            if (matches) bean = factory.get();
            if (bean != null) beans.put(name, bean);
            report.add(new AutoConfigResult(name, matches, description, bean));
        }

        void printReport() {
            System.out.println();
            System.out.println("  ====== Conditions Evaluation Report ======");
            System.out.printf("  %-25s %-55s %s%n", "Bean", "Condicion", "Resultado");
            System.out.println("  " + "-".repeat(90));
            for (AutoConfigResult r : report) {
                String status = r.created ? "[✓ MATCH   ]" : "[✗ NO_MATCH]";
                System.out.printf("  %-25s %-55s %s%n",
                        r.beanName, r.conditionResult, status);
            }
            System.out.println();
            System.out.printf("  Beans creados: %d / %d%n", beans.size(), report.size());
        }

        @SuppressWarnings("unchecked")
        <T> Optional<T> getBean(Class<T> type) {
            return beans.values().stream()
                    .filter(type::isInstance)
                    .map(b -> (T) b)
                    .findFirst();
        }

        private List<String> parseList(String csv) {
            if (csv == null || csv.isBlank()) return Collections.emptyList();
            return Arrays.asList(csv.split(","));
        }
    }

    // ====== DEMO ======

    public static void main(String[] args) {
        System.out.println("=== Custom Starter con Auto-configuracion Condicional ===");
        System.out.println();

        // --- Escenario 1: todas las features habilitadas ---
        System.out.println("[ Escenario 1: todas las features ON ]");
        Map<String, String> fullConfig = new LinkedHashMap<>();
        fullConfig.put("cache.enabled",       "true");
        fullConfig.put("cache.type",          "CAFFEINE");
        fullConfig.put("cache.maxSize",       "5000");
        fullConfig.put("cache.stats.enabled", "true");
        fullConfig.put("security.enabled",    "true");
        fullConfig.put("security.realm",      "mi-app");
        fullConfig.put("security.allowedIps", "10.0.0.1,10.0.0.2");
        fullConfig.put("metrics.enabled",     "true");

        CustomStarterAutoConfig config1 = new CustomStarterAutoConfig(new StarterProperties(fullConfig));
        config1.configure();
        config1.printReport();

        // Usar los beans
        config1.getBean(CacheManager.class).ifPresent(cm -> {
            cm.put("user:1", "Alice");
            System.out.println("  CacheManager get: " + cm.get("user:1"));
        });
        config1.getBean(SecurityFilter.class).ifPresent(sf -> {
            System.out.println("  SecurityFilter 10.0.0.1 allowed: " + sf.isAllowed("10.0.0.1"));
            System.out.println("  SecurityFilter 1.2.3.4 allowed: " + sf.isAllowed("1.2.3.4"));
        });
        config1.getBean(MetricsRegistry.class).ifPresent(mr -> {
            mr.increment("requests"); mr.increment("requests"); mr.increment("errors");
            System.out.println("  " + mr);
        });

        System.out.println();

        // --- Escenario 2: cache deshabilitado, security OFF ---
        System.out.println("[ Escenario 2: cache y security OFF, metrics ON ]");
        Map<String, String> minimalConfig = new LinkedHashMap<>();
        minimalConfig.put("metrics.enabled", "true");

        CustomStarterAutoConfig config2 = new CustomStarterAutoConfig(new StarterProperties(minimalConfig));
        config2.configure();
        config2.printReport();

        System.out.println();
        System.out.println("=== Conclusion ===");
        System.out.println("El starter activa solo los beans cuyas condiciones se cumplen.");
        System.out.println("Cuando cache.enabled=true falta, defaultCacheManager se activa en su lugar.");
        System.out.println("En Spring Boot: @ConditionalOnProperty(prefix='cache', name='enabled', havingValue='true').");
    }
}
