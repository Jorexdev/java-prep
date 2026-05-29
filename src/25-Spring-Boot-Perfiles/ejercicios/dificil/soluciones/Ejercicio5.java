import java.util.*;

// Multi-environment config con precedencia y override manual demostrado

public class Ejercicio5 {

    // ====== Fuentes de configuracion por precedencia (menor = mas baja) ======

    enum Priority {
        DEFAULTS(1, "Defaults"),
        SHARED_ENV(2, "application.yml (base)"),
        PROFILE_ENV(3, "application-{profile}.yml"),
        ENV_VARS(4, "Variables de entorno"),
        SYSTEM_PROPS(5, "System properties (-D...)"),
        CLI_ARGS(6, "CLI args (--key=value)");

        final int level;
        final String label;
        Priority(int level, String label) { this.level = level; this.label = label; }
    }

    static class PropertyEntry {
        final String value;
        final Priority source;

        PropertyEntry(String value, Priority source) {
            this.value = value;
            this.source = source;
        }
    }

    // ====== Environment con soporte de multiples fuentes y precedencia ======

    static class MultiEnvConfig {
        // Para cada clave: lista de valores por fuente (en orden de prioridad descendente)
        private final Map<String, List<PropertyEntry>> entries = new LinkedHashMap<>();

        void addSource(Map<String, String> props, Priority priority) {
            for (Map.Entry<String, String> e : props.entrySet()) {
                entries.computeIfAbsent(e.getKey(), k -> new ArrayList<>())
                        .add(new PropertyEntry(e.getValue(), priority));
            }
        }

        // Resuelve la clave: gana la fuente con mayor prioridad
        String get(String key) {
            List<PropertyEntry> candidates = entries.get(key);
            if (candidates == null || candidates.isEmpty()) return null;
            return candidates.stream()
                    .max(Comparator.comparingInt(e -> e.source.level))
                    .map(e -> e.value)
                    .orElse(null);
        }

        String get(String key, String defaultVal) {
            String v = get(key);
            return v != null ? v : defaultVal;
        }

        // Muestra la cadena completa de precedencia para una clave
        void showPrecedence(String key) {
            List<PropertyEntry> candidates = entries.get(key);
            if (candidates == null || candidates.isEmpty()) {
                System.out.printf("  %s: (no definida)%n", key);
                return;
            }
            // Ordenar por prioridad descendente
            List<PropertyEntry> sorted = candidates.stream()
                    .sorted(Comparator.comparingInt((PropertyEntry e) -> e.source.level).reversed())
                    .toList();

            String winner = sorted.get(0).value;
            System.out.printf("  %s = \"%s\" <- GANA (%s)%n", key, winner, sorted.get(0).source.label);
            for (int i = 1; i < sorted.size(); i++) {
                PropertyEntry e = sorted.get(i);
                System.out.printf("    overriden: \"%s\" (%s)%n", e.value, e.source.label);
            }
        }

        // Vista completa de todas las claves
        void printAll(String title) {
            System.out.println("  [ " + title + " ]");
            Set<String> keys = new LinkedHashSet<>(entries.keySet());
            for (String key : keys) {
                System.out.printf("  %-40s = %s%n", key, get(key));
            }
        }
    }

    // ====== Builder de configuracion por entorno ======

    static MultiEnvConfig buildForEnvironment(String profile) {
        MultiEnvConfig config = new MultiEnvConfig();

        // 1. Defaults (prioridad minima)
        Map<String, String> defaults = new LinkedHashMap<>();
        defaults.put("server.port",              "8080");
        defaults.put("server.contextPath",       "/");
        defaults.put("logging.level.root",       "INFO");
        defaults.put("cache.ttl",                "300");
        defaults.put("db.pool.min",              "2");
        defaults.put("db.pool.max",              "10");
        defaults.put("feature.newUI",            "false");
        defaults.put("feature.betaApi",          "false");
        defaults.put("app.name",                 "my-service");
        config.addSource(defaults, Priority.DEFAULTS);

        // 2. application.yml (base, compartido entre perfiles)
        Map<String, String> baseYml = new LinkedHashMap<>();
        baseYml.put("app.name",                 "my-service");
        baseYml.put("app.version",              "2.0.0");
        baseYml.put("logging.pattern",          "%d{ISO8601} [%t] %-5level %logger - %msg%n");
        baseYml.put("actuator.enabled",         "true");
        baseYml.put("db.pool.min",              "5");
        config.addSource(baseYml, Priority.SHARED_ENV);

        // 3. application-{profile}.yml (especifico del perfil)
        Map<String, String> profileYml = new LinkedHashMap<>();
        switch (profile) {
            case "dev" -> {
                profileYml.put("server.port",           "8081");
                profileYml.put("logging.level.root",    "DEBUG");
                profileYml.put("db.url",                "jdbc:h2:mem:devdb");
                profileYml.put("db.pool.max",           "5");
                profileYml.put("feature.newUI",         "true");
                profileYml.put("feature.betaApi",       "true");
                profileYml.put("cache.ttl",             "60");
            }
            case "staging" -> {
                profileYml.put("server.port",           "8080");
                profileYml.put("logging.level.root",    "INFO");
                profileYml.put("db.url",                "jdbc:postgresql://staging-db:5432/app");
                profileYml.put("db.pool.max",           "20");
                profileYml.put("feature.newUI",         "true");
                profileYml.put("feature.betaApi",       "false");
            }
            case "prod" -> {
                profileYml.put("server.port",           "80");
                profileYml.put("logging.level.root",    "WARN");
                profileYml.put("db.url",                "jdbc:postgresql://prod-db:5432/app");
                profileYml.put("db.pool.min",           "10");
                profileYml.put("db.pool.max",           "100");
                profileYml.put("feature.newUI",         "false");
                profileYml.put("cache.ttl",             "900");
            }
        }
        config.addSource(profileYml, Priority.PROFILE_ENV);

        // 4. Variables de entorno (simuladas segun perfil)
        Map<String, String> envVars = new LinkedHashMap<>();
        if (profile.equals("prod")) {
            envVars.put("db.password",  "***prod-secret***");
            envVars.put("db.pool.max",  "150"); // ajuste operacional
        } else if (profile.equals("staging")) {
            envVars.put("db.password",  "staging-pass");
        } else {
            envVars.put("db.password",  "devpassword");
        }
        config.addSource(envVars, Priority.ENV_VARS);

        // 5. System properties (simuladas)
        Map<String, String> systemProps = new LinkedHashMap<>();
        if (profile.equals("prod")) {
            systemProps.put("server.port", "443"); // HTTPS en prod
        }
        config.addSource(systemProps, Priority.SYSTEM_PROPS);

        // 6. CLI args (siempre la maxima prioridad)
        Map<String, String> cliArgs = new LinkedHashMap<>();
        // Solo en prod simulamos un override de CLI (ej. canary deployment)
        if (profile.equals("prod")) {
            cliArgs.put("app.version", "2.0.1-hotfix"); // override urgente
        }
        config.addSource(cliArgs, Priority.CLI_ARGS);

        return config;
    }

    public static void main(String[] args) {
        System.out.println("=== Multi-environment Config: precedencia y override ===");
        System.out.println();

        String[] profiles = {"dev", "staging", "prod"};

        for (String profile : profiles) {
            System.out.println("═".repeat(60));
            System.out.println("PERFIL: " + profile.toUpperCase());
            System.out.println("═".repeat(60));

            MultiEnvConfig config = buildForEnvironment(profile);
            config.printAll("Config resuelta");
            System.out.println();
        }

        // --- Analisis de precedencia para claves clave ---
        System.out.println("═".repeat(60));
        System.out.println("ANALISIS DE PRECEDENCIA (perfil: prod)");
        System.out.println("═".repeat(60));

        MultiEnvConfig prodConfig = buildForEnvironment("prod");
        String[] keysToAnalyze = {
            "server.port",      // Defaults=8080, Base=-, Profile=80, SysProp=443 -> gana CLI/SysProp
            "db.pool.max",      // Defaults=10, Profile=100, EnvVar=150 -> gana EnvVar
            "app.version",      // Base=2.0.0, CLI=2.0.1-hotfix -> gana CLI
            "logging.level.root",// Defaults=INFO, Profile=WARN -> gana Profile
            "feature.newUI"     // Defaults=false, Profile=false -> Profile
        };

        System.out.println();
        for (String key : keysToAnalyze) {
            prodConfig.showPrecedence(key);
            System.out.println();
        }

        // --- Tabla comparativa entre entornos ---
        System.out.println("=== Tabla comparativa ===");
        String[] compareKeys = {
            "server.port", "logging.level.root", "db.pool.max",
            "feature.newUI", "feature.betaApi", "cache.ttl"
        };

        System.out.printf("  %-35s %-10s %-10s %-10s%n", "Propiedad", "dev", "staging", "prod");
        System.out.println("  " + "-".repeat(68));

        Map<String, MultiEnvConfig> configs = new LinkedHashMap<>();
        for (String p : profiles) configs.put(p, buildForEnvironment(p));

        for (String key : compareKeys) {
            System.out.printf("  %-35s %-10s %-10s %-10s%n",
                    key,
                    configs.get("dev").get(key, "-"),
                    configs.get("staging").get(key, "-"),
                    configs.get("prod").get(key, "-"));
        }

        System.out.println();
        System.out.println("=== Conclusion ===");
        System.out.println("Orden de precedencia (mayor gana):");
        for (Priority p : Priority.values()) {
            System.out.printf("  %d. %s%n", p.level, p.label);
        }
        System.out.println("En Spring Boot: PropertySource chain sigue este mismo orden.");
    }
}
