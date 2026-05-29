import java.util.*;
import java.util.function.Supplier;

// ConditionsEvaluationReport manual: lista que condiciones pasaron/fallaron
// Simula el reporte de Spring Boot al arrancar con --debug

public class Ejercicio5 {

    // ====== Condiciones ======

    interface Condition {
        ConditionResult evaluate(Map<String, String> props, Map<String, Object> beans);
    }

    static class ConditionResult {
        final boolean match;
        final String description;
        final String reason; // solo si no hace match

        ConditionResult(boolean match, String description, String reason) {
            this.match = match;
            this.description = description;
            this.reason = reason;
        }

        static ConditionResult match(String description) {
            return new ConditionResult(true, description, null);
        }

        static ConditionResult noMatch(String description, String reason) {
            return new ConditionResult(false, description, reason);
        }
    }

    // @ConditionalOnProperty
    static class OnProperty implements Condition {
        private final String key;
        private final String havingValue;
        private final boolean matchIfMissing;

        OnProperty(String key, String havingValue, boolean matchIfMissing) {
            this.key = key;
            this.havingValue = havingValue;
            this.matchIfMissing = matchIfMissing;
        }

        public ConditionResult evaluate(Map<String, String> props, Map<String, Object> beans) {
            String val = props.get(key);
            String desc = "@ConditionalOnProperty(" + key + "=" + havingValue + ")";
            if (val == null) {
                if (matchIfMissing) return ConditionResult.match(desc + " [missing=match]");
                return ConditionResult.noMatch(desc, key + " no esta definida");
            }
            if (val.equals(havingValue)) return ConditionResult.match(desc);
            return ConditionResult.noMatch(desc, key + "=" + val + " != " + havingValue);
        }
    }

    // @ConditionalOnMissingBean
    static class OnMissingBean implements Condition {
        private final Class<?> beanType;

        OnMissingBean(Class<?> beanType) { this.beanType = beanType; }

        public ConditionResult evaluate(Map<String, String> props, Map<String, Object> beans) {
            String desc = "@ConditionalOnMissingBean(" + beanType.getSimpleName() + ")";
            boolean exists = beans.values().stream().anyMatch(beanType::isInstance);
            if (!exists) return ConditionResult.match(desc);
            return ConditionResult.noMatch(desc,
                    "ya existe un bean de tipo " + beanType.getSimpleName());
        }
    }

    // @ConditionalOnBean
    static class OnBean implements Condition {
        private final Class<?> beanType;

        OnBean(Class<?> beanType) { this.beanType = beanType; }

        public ConditionResult evaluate(Map<String, String> props, Map<String, Object> beans) {
            String desc = "@ConditionalOnBean(" + beanType.getSimpleName() + ")";
            boolean exists = beans.values().stream().anyMatch(beanType::isInstance);
            if (exists) return ConditionResult.match(desc);
            return ConditionResult.noMatch(desc, "no existe bean de tipo " + beanType.getSimpleName());
        }
    }

    // Condicion compuesta AND
    static class AllConditions implements Condition {
        private final List<Condition> conditions;

        AllConditions(Condition... conditions) {
            this.conditions = List.of(conditions);
        }

        public ConditionResult evaluate(Map<String, String> props, Map<String, Object> beans) {
            List<String> failures = new ArrayList<>();
            StringBuilder desc = new StringBuilder("AllOf[");
            for (int i = 0; i < conditions.size(); i++) {
                ConditionResult r = conditions.get(i).evaluate(props, beans);
                if (i > 0) desc.append(", ");
                desc.append(r.description);
                if (!r.match) failures.add(r.reason);
            }
            desc.append("]");
            if (failures.isEmpty()) return ConditionResult.match(desc.toString());
            return ConditionResult.noMatch(desc.toString(), String.join(" && ", failures));
        }
    }

    // ====== Beans de ejemplo ======

    interface DataSource { String getUrl(); }
    static class HikariDataSource implements DataSource {
        private final String url;
        HikariDataSource(String url) {
            this.url = url;
            System.out.println("    [HikariDataSource] creado: " + url);
        }
        public String getUrl() { return url; }
        @Override public String toString() { return "HikariDS{" + url + "}"; }
    }

    interface CacheManager { void put(String k, Object v); }
    static class RedisCacheManager implements CacheManager {
        RedisCacheManager() { System.out.println("    [RedisCacheManager] creado"); }
        public void put(String k, Object v) { System.out.println("    [Redis] " + k + "=" + v); }
        @Override public String toString() { return "RedisCacheManager"; }
    }

    static class SimpleCacheManager implements CacheManager {
        SimpleCacheManager() { System.out.println("    [SimpleCacheManager] creado (fallback)"); }
        public void put(String k, Object v) { System.out.println("    [Simple] " + k + "=" + v); }
        @Override public String toString() { return "SimpleCacheManager"; }
    }

    static class SecurityAutoConfig {
        SecurityAutoConfig() { System.out.println("    [SecurityAutoConfig] seguridad configurada"); }
        @Override public String toString() { return "SecurityAutoConfig"; }
    }

    static class JpaRepositories {
        private final DataSource ds;
        JpaRepositories(DataSource ds) {
            System.out.println("    [JpaRepositories] vinculado a " + ds.getUrl());
            this.ds = ds;
        }
        @Override public String toString() { return "JpaRepositories{ds=" + ds.getUrl() + "}"; }
    }

    static class AuditingConfig {
        AuditingConfig() { System.out.println("    [AuditingConfig] auditoria habilitada"); }
        @Override public String toString() { return "AuditingConfig"; }
    }

    // ====== Auto-config definition ======

    static class AutoConfigDef {
        final String name;
        final Condition condition;
        final Supplier<Object> factory;

        AutoConfigDef(String name, Condition condition, Supplier<Object> factory) {
            this.name = name;
            this.condition = condition;
            this.factory = factory;
        }
    }

    // ====== Conditions Evaluation Report ======

    static class ConditionReportEntry {
        final String autoConfig;
        final ConditionResult result;
        final String excluded;

        ConditionReportEntry(String autoConfig, ConditionResult result, String excluded) {
            this.autoConfig = autoConfig;
            this.result = result;
            this.excluded = excluded;
        }
    }

    static class ConditionsEvaluationReport {
        private final List<ConditionReportEntry> entries = new ArrayList<>();

        void add(String name, ConditionResult result) {
            entries.add(new ConditionReportEntry(name, result, null));
        }

        void addExcluded(String name, String reason) {
            ConditionResult r = ConditionResult.noMatch("Excluded", reason);
            entries.add(new ConditionReportEntry(name, r, reason));
        }

        void print() {
            System.out.println();
            System.out.println("  ╔══════════════════════════════════════════════════════════════════╗");
            System.out.println("  ║          CONDITIONS EVALUATION REPORT                           ║");
            System.out.println("  ╠══════════════════════════════════════════════════════════════════╣");

            List<ConditionReportEntry> matched   = entries.stream().filter(e -> e.result.match).toList();
            List<ConditionReportEntry> unmatched = entries.stream().filter(e -> !e.result.match).toList();

            System.out.println("  ║ POSITIVE MATCHES                                                ║");
            for (ConditionReportEntry e : matched) {
                System.out.printf("  ║  [✓] %-60s║%n", e.autoConfig);
                System.out.printf("  ║      %-60s║%n", e.result.description.length() > 60
                        ? e.result.description.substring(0, 57) + "..." : e.result.description);
            }

            System.out.println("  ╠══════════════════════════════════════════════════════════════════╣");
            System.out.println("  ║ NEGATIVE MATCHES                                                ║");
            for (ConditionReportEntry e : unmatched) {
                String prefix = e.excluded != null ? "[X]" : "[✗]";
                System.out.printf("  ║  %s %-59s║%n", prefix, e.autoConfig);
                String reason = (e.excluded != null ? "Excluded: " + e.excluded : e.result.reason);
                if (reason != null)
                    System.out.printf("  ║      -> %-57s║%n",
                            reason.length() > 57 ? reason.substring(0, 54) + "..." : reason);
            }

            System.out.println("  ╚══════════════════════════════════════════════════════════════════╝");
            System.out.printf("  Total: %d matched, %d not matched%n", matched.size(), unmatched.size());
        }
    }

    // ====== AutoConfiguration Processor ======

    static class AutoConfigProcessor {
        private final Map<String, String> props;
        private final List<String> excluded;
        private final Map<String, Object> beans = new LinkedHashMap<>();
        private final ConditionsEvaluationReport report = new ConditionsEvaluationReport();

        AutoConfigProcessor(Map<String, String> props, List<String> excluded) {
            this.props = props;
            this.excluded = excluded;
        }

        void process(List<AutoConfigDef> defs) {
            System.out.println("  [ Evaluando auto-configuraciones ]");
            for (AutoConfigDef def : defs) {
                if (excluded.contains(def.name)) {
                    System.out.printf("  [EXCLUDED] %s%n", def.name);
                    report.addExcluded(def.name, "excluido por el usuario");
                    continue;
                }
                ConditionResult result = def.condition.evaluate(props, beans);
                report.add(def.name, result);
                if (result.match) {
                    System.out.printf("  [MATCH] %s -> creando bean%n", def.name);
                    Object bean = def.factory.get();
                    beans.put(def.name, bean);
                } else {
                    System.out.printf("  [SKIP]  %s -> %s%n", def.name, result.reason);
                }
            }
        }

        void printReport() { report.print(); }

        @SuppressWarnings("unchecked")
        <T> Optional<T> getBean(Class<T> type) {
            return beans.values().stream().filter(type::isInstance).map(b -> (T) b).findFirst();
        }
    }

    // ====== DEMO ======

    static List<AutoConfigDef> buildAutoConfigs(Map<String, String> props) {
        return List.of(
            new AutoConfigDef("DataSourceAutoConfig",
                new OnProperty("spring.datasource.url", null, false) {
                    public ConditionResult evaluate(Map<String, String> p, Map<String, Object> b) {
                        boolean hasUrl = p.containsKey("spring.datasource.url");
                        String desc = "@ConditionalOnProperty(spring.datasource.url)";
                        if (hasUrl) return ConditionResult.match(desc);
                        return ConditionResult.noMatch(desc, "spring.datasource.url no definida");
                    }
                },
                () -> new HikariDataSource(props.getOrDefault("spring.datasource.url", ""))),

            new AutoConfigDef("JpaRepositoriesAutoConfig",
                new OnBean(DataSource.class),
                () -> {
                    DataSource ds = new HikariDataSource(props.get("spring.datasource.url"));
                    return new JpaRepositories(ds);
                }),

            new AutoConfigDef("RedisCacheAutoConfig",
                new AllConditions(
                    new OnProperty("spring.cache.type", "redis", false),
                    new OnMissingBean(CacheManager.class)
                ),
                RedisCacheManager::new),

            new AutoConfigDef("SimpleCacheAutoConfig",
                new AllConditions(
                    new OnProperty("spring.cache.enabled", "true", false),
                    new OnMissingBean(CacheManager.class)
                ),
                SimpleCacheManager::new),

            new AutoConfigDef("SecurityAutoConfig",
                new OnProperty("spring.security.enabled", "true", false),
                SecurityAutoConfig::new),

            new AutoConfigDef("AuditingAutoConfig",
                new AllConditions(
                    new OnBean(DataSource.class),
                    new OnProperty("spring.jpa.auditing", "true", false)
                ),
                AuditingConfig::new)
        );
    }

    public static void main(String[] args) {
        System.out.println("=== ConditionsEvaluationReport Manual ===");
        System.out.println();

        // --- Escenario 1: configuracion completa ---
        System.out.println("[ Escenario 1: configuracion completa (ninguna exclusion) ]");
        Map<String, String> fullProps = new LinkedHashMap<>();
        fullProps.put("spring.datasource.url",  "jdbc:postgresql://db:5432/app");
        fullProps.put("spring.cache.type",       "redis");
        fullProps.put("spring.cache.enabled",    "true");
        fullProps.put("spring.security.enabled", "true");
        fullProps.put("spring.jpa.auditing",     "true");

        AutoConfigProcessor p1 = new AutoConfigProcessor(fullProps, List.of());
        p1.process(buildAutoConfigs(fullProps));
        p1.printReport();

        System.out.println();

        // --- Escenario 2: sin datasource, cache simple, con exclusiones ---
        System.out.println("[ Escenario 2: sin datasource, cache simple, excluir Security y Redis ]");
        Map<String, String> minimalProps = new LinkedHashMap<>();
        minimalProps.put("spring.cache.enabled", "true");

        List<String> excluded = List.of("SecurityAutoConfig", "RedisCacheAutoConfig");
        AutoConfigProcessor p2 = new AutoConfigProcessor(minimalProps, excluded);
        p2.process(buildAutoConfigs(minimalProps));
        p2.printReport();

        System.out.println();
        System.out.println("=== Conclusion ===");
        System.out.println("Cada auto-config evalua sus condiciones antes de crear el bean.");
        System.out.println("OnMissingBean permite definir fallbacks (SimpleCacheManager si no hay Redis).");
        System.out.println("En Spring Boot: arrancar con --debug muestra este reporte real.");
        System.out.println("Activar con: java -jar app.jar --debug");
    }
}
