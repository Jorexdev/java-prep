import java.util.Map;

/**
 * The Twelve-Factor App aplicado a Java/Spring.
 * Cada factor se ilustra con código o explicación ejecutable.
 *
 * Referencia: https://12factor.net
 */
public class ExpTwelveFactor {

    // ═══════════════════════════════════════════════════════════════
    // FACTOR I: CODEBASE — un repo, múltiples deploys
    // ═══════════════════════════════════════════════════════════════
    // Un solo repositorio Git. Las diferencias entre staging y producción
    // se gestionan con variables de entorno, no con ramas ni forks.
    //
    // Bien:  git@github.com:empresa/mi-servicio.git  → deploy staging, producción, QA
    // Mal:   un repo por entorno (mi-servicio-prod, mi-servicio-staging)

    static void factorI() {
        System.out.println("Factor I — Codebase: un repo, entornos separados por config");
        System.out.println("  → spring.profiles.active=prod|staging|dev");
    }

    // ═══════════════════════════════════════════════════════════════
    // FACTOR III: CONFIG — env vars, nunca hardcoded
    // ═══════════════════════════════════════════════════════════════

    static void factorIII() {
        System.out.println("\nFactor III — Config:");

        // MAL: valor hardcodeado que varía entre entornos
        String dbUrlBad = "jdbc:postgresql://localhost:5432/mydb";

        // BIEN: viene del entorno; en Spring Boot sería @Value("${DB_URL}")
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl == null) dbUrl = "jdbc:postgresql://localhost:5432/mydb"; // fallback local

        System.out.println("  DB_URL (env): " + dbUrl);

        // La config que debe viajar por env vars:
        // - credenciales de BD y servicios externos
        // - puertos y hosts
        // - feature flags de infraestructura
        // Lo que NO va por env var: config interna de la app (timeouts de reintentos, etc.)
    }

    // ═══════════════════════════════════════════════════════════════
    // FACTOR IV: BACKING SERVICES — DB, queues, cache como recursos adjuntos
    // ═══════════════════════════════════════════════════════════════
    // Un backing service es cualquier servicio que la app consume por red.
    // No debe haber distinción en el código entre local y externo — solo cambia la URL.

    interface MessageQueue {
        void publish(String topic, String message);
    }

    static class KafkaQueue implements MessageQueue {
        private final String brokers;
        KafkaQueue(String brokers) { this.brokers = brokers; }
        public void publish(String topic, String message) {
            System.out.println("  [Kafka@" + brokers + "] topic=" + topic + " msg=" + message);
        }
    }

    static void factorIV() {
        System.out.println("\nFactor IV — Backing Services como recursos adjuntos:");
        String brokers = System.getenv("KAFKA_BROKERS");
        if (brokers == null) brokers = "localhost:9092";
        MessageQueue queue = new KafkaQueue(brokers);
        queue.publish("orders", "order-created");
        // Cambiar de Kafka local a Kafka gestionado = solo cambiar KAFKA_BROKERS
    }

    // ═══════════════════════════════════════════════════════════════
    // FACTOR VI: PROCESSES — stateless, sin estado compartido entre requests
    // ═══════════════════════════════════════════════════════════════

    // MAL: estado de sesión en memoria (no sobrevive a reinicios ni escala horizontal)
    static class StatefulSessionBad {
        private final Map<String, String> sessions = new java.util.HashMap<>();

        void login(String userId, String token) { sessions.put(userId, token); }
        boolean isAuthenticated(String userId) { return sessions.containsKey(userId); }
        // Si hay 3 instancias, solo 1 tiene el token → falla en N-1 instancias
    }

    // BIEN: estado externalizado (Redis, JWT, BD)
    static class StatelessRequestHandler {
        // El token JWT viaja en cada request; no hay estado local
        boolean isAuthenticated(String jwtToken) {
            return jwtToken != null && jwtToken.startsWith("Bearer ");
        }
    }

    static void factorVI() {
        System.out.println("\nFactor VI — Processes stateless:");
        StatelessRequestHandler handler = new StatelessRequestHandler();
        System.out.println("  Auth con JWT: " + handler.isAuthenticated("Bearer eyJ..."));
        System.out.println("  Auth sin token: " + handler.isAuthenticated(null));
    }

    // ═══════════════════════════════════════════════════════════════
    // FACTOR IX: DISPOSABILITY — arranque rápido y shutdown graceful
    // ═══════════════════════════════════════════════════════════════

    static void factorIX() {
        System.out.println("\nFactor IX — Disposability (shutdown hook):");
        // En Spring Boot: @PreDestroy o SmartLifecycle
        // En Java puro: Runtime shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("  [SHUTDOWN] Cerrando conexiones y drenando colas...");
            // 1. Dejar de aceptar nuevas peticiones
            // 2. Esperar a que las peticiones en vuelo terminen (timeout configurable)
            // 3. Cerrar conexiones a BD, Kafka, Redis
        }, "shutdown-hook"));

        System.out.println("  Shutdown hook registrado — el proceso puede terminar con seguridad");
    }

    // ═══════════════════════════════════════════════════════════════
    // FACTOR XI: LOGS — tratar logs como event streams (stdout)
    // ═══════════════════════════════════════════════════════════════
    // La app no gestiona ficheros de log ni rotación. Solo escribe a stdout.
    // La infraestructura (Docker, Kubernetes, Fluentd) captura y enruta los logs.

    static void factorXI() {
        System.out.println("\nFactor XI — Logs como event stream:");
        // En Spring Boot: logging.level.root=INFO en application.properties
        // y los logs van a stdout por defecto con Logback

        // Simula una línea de log estructurado a stdout (como hace slf4j/logback)
        long ts = System.currentTimeMillis();
        System.out.printf("  {\"timestamp\":%d,\"level\":\"INFO\",\"msg\":\"Pedido procesado\",\"orderId\":\"ORD-1\"}%n", ts);

        // MAL: escribir a fichero desde la app
        // FileWriter fw = new FileWriter("/var/log/myapp.log", true);
        // BIEN: System.out o un logger configurado para stdout
    }

    // ═══════════════════════════════════════════════════════════════
    // FACTOR XII: ADMIN PROCESSES — migraciones como one-off tasks
    // ═══════════════════════════════════════════════════════════════
    // Las tareas administrativas (migraciones de BD, scripts de limpieza) se ejecutan
    // como procesos one-off en el mismo entorno que la app, no como cron jobs separados.
    // En Spring Boot: Flyway o Liquibase se ejecutan al arrancar la app.

    static void factorXII() {
        System.out.println("\nFactor XII — Admin processes:");
        System.out.println("  Simulando migración one-off (equivalente a Flyway/Liquibase):");
        runMigration("V1__create_orders_table.sql");
        runMigration("V2__add_status_column.sql");
    }

    private static void runMigration(String scriptName) {
        System.out.println("  [MIGRATION] Ejecutando: " + scriptName + " ... OK");
    }

    // ═══════════════════════════════════════════════════════════════
    // MAIN
    // ═══════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        System.out.println("=== The Twelve-Factor App en Java ===\n");
        factorI();
        factorIII();
        factorIV();
        factorVI();
        factorIX();
        factorXI();
        factorXII();
        System.out.println("\n=== Fin demo 12-Factor ===");
    }
}
