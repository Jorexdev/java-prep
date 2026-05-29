import java.util.*;

// Profile-specific beans con @ConditionalOnProfile y expresiones compuestas

public class Ejercicio6 {

    // ====== Motor de expresiones de perfil ======
    // Soporta: "prod", "!debug", "prod & !debug", "dev | staging"

    static class ProfileExpression {
        private final String expression;

        ProfileExpression(String expression) {
            this.expression = expression.trim();
        }

        boolean matches(Set<String> activeProfiles) {
            return evaluateOr(expression.trim(), activeProfiles);
        }

        // Nivel OR: divide por |
        private boolean evaluateOr(String expr, Set<String> profiles) {
            String[] parts = expr.split("\\|");
            for (String part : parts) {
                if (evaluateAnd(part.trim(), profiles)) return true;
            }
            return false;
        }

        // Nivel AND: divide por &
        private boolean evaluateAnd(String expr, Set<String> profiles) {
            String[] parts = expr.split("&");
            for (String part : parts) {
                if (!evaluateAtom(part.trim(), profiles)) return false;
            }
            return true;
        }

        // Atomo: "prod" o "!prod"
        private boolean evaluateAtom(String atom, Set<String> profiles) {
            atom = atom.trim();
            if (atom.startsWith("!")) {
                return !profiles.contains(atom.substring(1).trim());
            }
            return profiles.contains(atom);
        }

        @Override public String toString() { return "\"" + expression + "\""; }
    }

    // ====== Bean registry con soporte de perfiles ======

    interface Bean {
        String describe();
    }

    static class BeanDefinition {
        final String name;
        final ProfileExpression profileExpr;
        final Bean instance;

        BeanDefinition(String name, String profileExpr, Bean instance) {
            this.name = name;
            this.profileExpr = new ProfileExpression(profileExpr);
            this.instance = instance;
        }
    }

    static class ProfileAwareRegistry {
        private final List<BeanDefinition> definitions = new ArrayList<>();

        void register(String name, String profileExpr, Bean bean) {
            definitions.add(new BeanDefinition(name, profileExpr, bean));
        }

        // Devuelve los beans activos para los perfiles dados
        List<BeanDefinition> getActiveBeans(Set<String> activeProfiles) {
            return definitions.stream()
                    .filter(def -> def.profileExpr.matches(activeProfiles))
                    .toList();
        }

        // Evalua todas las definiciones y muestra el resultado
        void evaluate(Set<String> activeProfiles) {
            System.out.printf("  Perfiles activos: %s%n", activeProfiles);
            System.out.println("  " + "-".repeat(65));
            System.out.printf("  %-25s %-30s %s%n", "Bean", "Expresion", "Activo");
            System.out.println("  " + "-".repeat(65));
            for (BeanDefinition def : definitions) {
                boolean active = def.profileExpr.matches(activeProfiles);
                System.out.printf("  %-25s %-30s %s%n",
                        def.name,
                        def.profileExpr.toString(),
                        active ? "[✓] " + def.instance.describe() : "[ ] -");
            }
            System.out.println();
        }
    }

    // ====== Beans de ejemplo ======

    // DataSource segun perfil
    static class EmbeddedDataSource implements Bean {
        public String describe() { return "EmbeddedDataSource (H2)"; }
    }
    static class ProductionDataSource implements Bean {
        public String describe() { return "ProductionDataSource (Postgres)"; }
    }
    static class StagingDataSource implements Bean {
        public String describe() { return "StagingDataSource (Postgres staging)"; }
    }

    // Logger segun perfil
    static class ConsoleLogger implements Bean {
        public String describe() { return "ConsoleLogger (nivel DEBUG)"; }
    }
    static class FileLogger implements Bean {
        public String describe() { return "FileLogger (nivel INFO, rotacion diaria)"; }
    }

    // Feature flags
    static class DebugPanel implements Bean {
        public String describe() { return "DebugPanel (herramientas de debug)"; }
    }
    static class SwaggerUI implements Bean {
        public String describe() { return "SwaggerUI (documentacion API)"; }
    }
    static class ProductionMetrics implements Bean {
        public String describe() { return "ProductionMetrics (Micrometer + Prometheus)"; }
    }

    // Cache
    static class NoCache implements Bean {
        public String describe() { return "NoCache (sin cache, para tests)"; }
    }
    static class RedisCache implements Bean {
        public String describe() { return "RedisCache (prod + staging)"; }
    }

    public static void main(String[] args) {
        System.out.println("=== Profile-specific beans con expresiones compuestas ===");
        System.out.println();

        ProfileAwareRegistry registry = new ProfileAwareRegistry();

        // DataSources
        registry.register("embeddedDataSource", "dev | test",    new EmbeddedDataSource());
        registry.register("stagingDataSource",  "staging",       new StagingDataSource());
        registry.register("productionDataSource","prod & !debug", new ProductionDataSource());

        // Loggers
        registry.register("consoleLogger", "dev | test | debug",     new ConsoleLogger());
        registry.register("fileLogger",    "prod | staging & !debug", new FileLogger());

        // Features
        registry.register("debugPanel",    "debug",           new DebugPanel());
        registry.register("swaggerUI",     "dev | staging",   new SwaggerUI());
        registry.register("prodMetrics",   "prod & !debug",   new ProductionMetrics());

        // Cache
        registry.register("noCache",   "test",              new NoCache());
        registry.register("redisCache","prod | staging",    new RedisCache());

        // --- Demo con distintos combos de perfiles ---
        List<Set<String>> scenarios = List.of(
            Set.of("dev"),
            Set.of("dev", "debug"),
            Set.of("staging"),
            Set.of("prod"),
            Set.of("prod", "debug"),   // prod+debug: no se activa productionDataSource ni prodMetrics
            Set.of("test")
        );

        for (Set<String> profiles : scenarios) {
            System.out.println("[ Perfiles: " + profiles + " ]");
            registry.evaluate(profiles);
        }

        // --- Verificacion de expresiones compuestas ---
        System.out.println("=== Verificacion de expresiones compuestas ===");
        String[][] cases = {
            {"prod & !debug",       "prod",          "true"},
            {"prod & !debug",       "prod,debug",    "false"},
            {"dev | staging",       "dev",           "true"},
            {"dev | staging",       "prod",          "false"},
            {"prod | staging & !debug", "staging",   "true"},
            {"prod | staging & !debug", "staging,debug", "false"},
        };
        System.out.printf("  %-30s %-20s %-10s %-10s%n",
                "Expresion", "Perfiles activos", "Esperado", "Resultado");
        System.out.println("  " + "-".repeat(75));
        for (String[] c : cases) {
            ProfileExpression expr = new ProfileExpression(c[0]);
            Set<String> profiles = new HashSet<>(Arrays.asList(c[1].split(",")));
            boolean result = expr.matches(profiles);
            boolean expected = Boolean.parseBoolean(c[2]);
            String status = result == expected ? "OK" : "FAIL";
            System.out.printf("  %-30s %-20s %-10s %-10s%n",
                    "\"" + c[0] + "\"", profiles, c[2], result + " [" + status + "]");
        }

        System.out.println();
        System.out.println("En Spring Boot: @Profile(\"prod & !debug\") sobre un @Bean o @Component.");
        System.out.println("Activar perfiles: --spring.profiles.active=prod o SPRING_PROFILES_ACTIVE=prod.");
    }
}
