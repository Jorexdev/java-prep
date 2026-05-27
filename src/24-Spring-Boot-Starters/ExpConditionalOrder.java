import java.util.*;

// Simula el orden de autoconfiguración de Spring Boot y las condiciones de orden.
// En Spring: @AutoConfigureBefore, @AutoConfigureAfter, @AutoConfigureOrder.
// El orden determina qué beans están disponibles cuando se evalúa una @Conditional.
public class ExpConditionalOrder {

    // ── Simular el registro y ordenación de AutoConfigurations ───────────────
    static class AutoConfigRegistry {
        record Config(String name, int order, String before, String after) {}

        private final List<Config> configs = new ArrayList<>();

        void register(String name, int order, String before, String after) {
            configs.add(new Config(name, order, before, after));
        }

        // Ordenar: primero por after/before constraints, luego por order numérico.
        // Spring Boot usa un TopologicalSort interno basado en estas declaraciones.
        List<String> resolve() {
            List<Config> sorted = new ArrayList<>(configs);
            sorted.sort((a, b) -> {
                // Si a debe ir antes de b → a va primero
                if (a.name.equals(b.before)) return -1;
                if (b.name.equals(a.before)) return  1;
                // Si a debe ir después de b → b va primero
                if (a.after != null && a.after.equals(b.name)) return  1;
                if (b.after != null && b.after.equals(a.name)) return -1;
                return Integer.compare(a.order, b.order);
            });
            return sorted.stream().map(Config::name).toList();
        }
    }

    // ── 1. @AutoConfigureAfter y @AutoConfigureBefore ────────────────────────
    // Ejemplo real: DataSourceAutoConfiguration debe ir ANTES de
    // JpaRepositoriesAutoConfiguration (JPA necesita el DataSource ya configurado).
    //
    // @AutoConfigureAfter(DataSourceAutoConfiguration.class)
    // public class JpaRepositoriesAutoConfiguration { ... }
    static void orderingDemo() {
        System.out.println("── 1. @AutoConfigureAfter / @AutoConfigureBefore ──");

        AutoConfigRegistry registry = new AutoConfigRegistry();
        // order numérico (menor = antes), before/after = nombre de otra autoconfig
        registry.register("SecurityAutoConfig",         300, null,                       null);
        registry.register("DataSourceAutoConfig",       100, null,                       null);
        registry.register("JpaRepositoriesAutoConfig",  200, null,   "DataSourceAutoConfig");
        registry.register("WebMvcAutoConfig",           150, null,                       null);
        registry.register("TransactionAutoConfig",      250, null,   "JpaRepositoriesAutoConfig");

        List<String> orden = registry.resolve();
        System.out.println("  Orden de autoconfiguración resuelto:");
        for (int i = 0; i < orden.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + orden.get(i));
        }
        System.out.println();
        System.out.println("  Regla: si JPA necesita DataSource → declarar @AutoConfigureAfter(DataSourceAutoConfig)");
    }

    // ── 2. @ConditionalOnProperty — activar config por propiedad ─────────────
    // @ConditionalOnProperty(name = "feature.cache.enabled", havingValue = "true",
    //                        matchIfMissing = false)
    // → el bean solo se registra si la propiedad existe y vale "true".
    // matchIfMissing = true → se registra también si la propiedad no existe.
    static void conditionalOnProperty() {
        System.out.println("── 2. @ConditionalOnProperty ──");

        Map<String, String> properties = Map.of(
            "feature.cache.enabled",    "true",
            "feature.metrics.enabled",  "false",
            "feature.tracing.enabled",  "true"
        );

        record Feature(String property, String havingValue, boolean matchIfMissing) {
            boolean matches(Map<String, String> props) {
                String val = props.get(property);
                if (val == null) return matchIfMissing;
                return val.equals(havingValue);
            }
        }

        List<Feature> features = List.of(
            new Feature("feature.cache.enabled",   "true",  false),
            new Feature("feature.metrics.enabled", "true",  false),
            new Feature("feature.tracing.enabled", "true",  false),
            new Feature("feature.audit.enabled",   "true",  true)   // matchIfMissing=true
        );

        for (Feature f : features) {
            boolean activo = f.matches(properties);
            System.out.printf("  %-35s → %s%n", f.property(), activo ? "ACTIVO" : "INACTIVO");
        }
    }

    // ── 3. @ConditionalOnMissingBean — no registrar si ya existe un bean ──────
    // Patrón fundamental de autoconfiguración:
    //   1. El usuario define su propio DataSource → Spring no crea uno.
    //   2. Si no hay DataSource → Spring Boot crea uno con HikariCP por defecto.
    //
    // @ConditionalOnMissingBean(DataSource.class)
    // public DataSource defaultDataSource() { ... }
    static void conditionalOnMissingBean() {
        System.out.println("\n── 3. @ConditionalOnMissingBean ──");

        Set<String> beansRegistrados = new HashSet<>();
        // El usuario registró su propio DataSource
        beansRegistrados.add("myCustomDataSource");

        record AutoConfigBean(String beanName, String missingType) {
            boolean shouldCreate(Set<String> registered) {
                // Si ya existe algún bean del tipo → no crear
                return registered.stream().noneMatch(b -> b.toLowerCase().contains(missingType.toLowerCase()));
            }
        }

        List<AutoConfigBean> autoConfigs = List.of(
            new AutoConfigBean("defaultDataSource",   "DataSource"),   // no crea: myCustomDataSource existe
            new AutoConfigBean("hikariConnectionPool","HikariPool"),    // no existe → crea
            new AutoConfigBean("defaultJdbcTemplate", "JdbcTemplate")  // no existe → crea
        );

        for (AutoConfigBean ac : autoConfigs) {
            boolean crea = ac.shouldCreate(beansRegistrados);
            System.out.printf("  %-30s → %s%n", ac.beanName(),
                    crea ? "CREADO por autoconfig" : "OMITIDO (bean del usuario existe)");
        }
    }

    // ── 4. Orden de evaluación de condiciones ─────────────────────────────────
    // Spring Boot evalúa las condiciones en este orden:
    // 1. @ConditionalOnClass / @ConditionalOnMissingClass (classpath)
    // 2. @ConditionalOnBean / @ConditionalOnMissingBean  (contexto)
    // 3. @ConditionalOnProperty                          (environment)
    // 4. @ConditionalOnExpression                        (SpEL)
    //
    // Importante: @ConditionalOnBean en autoconfiguración puede ser frágil
    // porque el orden de registro de beans no está garantizado entre configs
    // del mismo nivel. Por eso se combina con @AutoConfigureAfter.
    static void evaluationOrder() {
        System.out.println("\n── 4. Orden de evaluación de condiciones ──");
        System.out.println("  1. @ConditionalOnClass/MissingClass  → ¿está en el classpath?");
        System.out.println("  2. @ConditionalOnBean/MissingBean    → ¿existe el bean?");
        System.out.println("  3. @ConditionalOnProperty            → ¿vale la propiedad?");
        System.out.println("  4. @ConditionalOnExpression (SpEL)   → expresión arbitraria");
        System.out.println();
        System.out.println("  Regla: combinar @AutoConfigureAfter con @ConditionalOnBean");
        System.out.println("  para garantizar que el bean dependiente ya esté registrado.");
    }

    public static void main(String[] args) {
        orderingDemo();
        conditionalOnProperty();
        conditionalOnMissingBean();
        evaluationOrder();
    }
}
