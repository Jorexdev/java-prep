import java.util.*;

// Simula el orden de precedencia de PropertySources en Spring Boot.
// En Spring: Environment agrega PropertySources en orden; la primera que define
// una propiedad gana. Fuentes de mayor a menor prioridad:
//   1. Argumentos de línea de comandos (--server.port=9090)
//   2. Variables de entorno del SO (SERVER_PORT)
//   3. application-{profile}.properties/yml
//   4. application.properties/yml
//   5. @PropertySource en clases @Configuration
//   6. Valores por defecto (@Value("${x:default}"))
public class ExpPropertyPrecedence {

    // ── Simular la cadena de PropertySources ──────────────────────────────────
    static class PropertySourceChain {
        // Nombre → Map de propiedades. Orden = prioridad (índice 0 = más alta).
        private final List<Map.Entry<String, Map<String, String>>> sources = new ArrayList<>();

        void addSource(String name, Map<String, String> props) {
            sources.add(Map.entry(name, props));
        }

        // Primera fuente que define la clave gana (misma semántica que Spring)
        Optional<String> getProperty(String key) {
            for (var source : sources) {
                String val = source.getValue().get(key);
                if (val != null) {
                    return Optional.of("[" + source.getKey() + "] " + val);
                }
            }
            return Optional.empty();
        }

        void showAll(String key) {
            System.out.println("  Resolución de '" + key + "':");
            boolean found = false;
            for (var source : sources) {
                String val = source.getValue().get(key);
                if (val != null && !found) {
                    System.out.println("    ✓ " + source.getKey() + " = " + val + "  ← GANA");
                    found = true;
                } else if (val != null) {
                    System.out.println("    ✗ " + source.getKey() + " = " + val + "  (tapado)");
                }
            }
            if (!found) System.out.println("    (no definida en ninguna fuente)");
        }
    }

    // ── 1. Precedencia básica: args > env > profile > default ────────────────
    static void basicPrecedence() {
        System.out.println("── 1. Precedencia básica de PropertySources ──");

        PropertySourceChain chain = new PropertySourceChain();

        // Prioridad 1: argumentos de línea de comandos
        chain.addSource("CommandLineArgs", Map.of("server.port", "9090"));

        // Prioridad 2: variables de entorno (relaxed binding: SERVER_PORT → server.port)
        chain.addSource("SystemEnvironment", Map.of("SERVER_PORT", "8080", "DB_URL", "jdbc:mysql://prod/db"));

        // Prioridad 3: application-prod.yml (perfil activo = prod)
        chain.addSource("application-prod.yml", Map.of(
            "server.port", "443",
            "db.url",      "jdbc:mysql://prod-db/app",
            "app.name",    "mi-app-prod"
        ));

        // Prioridad 4: application.yml (defecto)
        chain.addSource("application.yml", Map.of(
            "server.port", "8080",
            "db.url",      "jdbc:h2:mem:test",
            "app.name",    "mi-app",
            "app.timeout", "30"
        ));

        chain.showAll("server.port"); // → CommandLineArgs (9090)
        chain.showAll("db.url");      // → application-prod.yml
        chain.showAll("app.name");    // → application-prod.yml
        chain.showAll("app.timeout"); // → application.yml (solo en defecto)
    }

    // ── 2. Relaxed Binding — múltiples formatos para la misma clave ───────────
    // Spring Boot normaliza estas variantes a la forma canónica (kebab-case):
    //   db.max-pool-size  ←→  db.maxPoolSize  ←→  DB_MAX_POOL_SIZE  ←→  db.max_pool_size
    // Útil para variables de entorno (SO solo soporta mayúsculas + guión bajo).
    static void relaxedBinding() {
        System.out.println("\n── 2. Relaxed Binding ──");

        // Normalizar cualquier variante a kebab-case
        String[] variants = {
            "db.maxPoolSize",     // camelCase
            "DB_MAX_POOL_SIZE",   // SCREAMING_SNAKE (env var)
            "db.max_pool_size",   // snake_case
            "db.max-pool-size"    // kebab-case (canónico)
        };

        for (String v : variants) {
            String normalized = v.toLowerCase().replace('_', '-').replace('.', '-');
            System.out.println("  " + v + " → normalizado: " + normalized);
        }

        System.out.println();
        System.out.println("  En application.yml usa kebab-case: db.max-pool-size: 10");
        System.out.println("  En docker/k8s env var usa: DB_MAX_POOL_SIZE=10");
        System.out.println("  Ambos bindean al mismo campo en @ConfigurationProperties");
    }

    // ── 3. spring.config.import — importar fuentes adicionales ───────────────
    // Desde Spring Boot 2.4+:
    //   spring.config.import=optional:classpath:extra.yml
    //   spring.config.import=optional:file:/etc/app/secrets.yml
    //   spring.config.import=optional:configserver:http://config-srv
    //
    // Archivos importados tienen MENOR prioridad que el archivo que los importa.
    static void configImport() {
        System.out.println("\n── 3. spring.config.import ──");

        PropertySourceChain chain = new PropertySourceChain();

        // application.yml importa secrets.yml; secrets tiene menor prioridad
        chain.addSource("application.yml", Map.of(
            "app.name",       "mi-app",
            "db.password",    "${DB_PASS}" // placeholder
        ));
        chain.addSource("secrets.yml (imported)", Map.of(
            "db.password", "supersecret123",
            "api.key",     "abc-def-123"
        ));

        chain.showAll("db.password"); // → application.yml (aunque usa placeholder, tiene prioridad)
        chain.showAll("api.key");     // → secrets.yml
        System.out.println();
        System.out.println("  Regla: el importador tiene prioridad sobre el importado.");
    }

    // ── 4. @TestPropertySource — sobreescribir propiedades en tests ───────────
    // @TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:test")
    // @TestPropertySource(locations = "classpath:test.properties")
    //
    // Prioridad: más alta que application.yml, más baja que args de línea de comandos.
    // Permite tests herméticamente aislados sin tocar application.yml.
    static void testPropertySource() {
        System.out.println("\n── 4. @TestPropertySource ──");
        System.out.println("  Prioridad en tests (mayor → menor):");
        System.out.println("  1. @DynamicPropertySource (Testcontainers)");
        System.out.println("  2. @TestPropertySource(properties = \"...\")");
        System.out.println("  3. @TestPropertySource(locations = \"...\")");
        System.out.println("  4. application.yml del classpath de test");
        System.out.println("  5. application.yml del main");
        System.out.println();
        System.out.println("  Patrón con Testcontainers:");
        System.out.println("  @DynamicPropertySource");
        System.out.println("  static void props(DynamicPropertyRegistry r) {");
        System.out.println("      r.add(\"spring.datasource.url\", container::getJdbcUrl);");
        System.out.println("  }");
    }

    public static void main(String[] args) {
        basicPrecedence();
        relaxedBinding();
        configImport();
        testPropertySource();
    }
}
