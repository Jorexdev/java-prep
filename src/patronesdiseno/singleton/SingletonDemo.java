package patronesdiseno.singleton;

// ═══════════════════════════════════════════════════════════════
//  PATRÓN SINGLETON — Creacional
// ═══════════════════════════════════════════════════════════════

/*
    ¿Qué es?
    Garantiza que una clase tenga UNA sola instancia en toda la aplicación
    y proporciona un punto de acceso global a ella.

    ¿Para qué sirve?
    Para coordinar un único recurso compartido que no debe duplicarse:
    configuración global, conexiones a base de datos, loggers, caché, etc.

    ¿Cuándo usarlo?
    - Cuando tener más de una instancia sería un error (ej: pool de conexiones).
    - Cuando necesitas un estado global coherente en toda la aplicación.

    ¿Cuándo NO usarlo?
    - Si el estado puede cambiar por contexto (prefiere inyección de dependencias).
    - En tests, el Singleton dificulta el mockeo y aísla mal los casos.

    Preguntas típicas de entrevista:
    - ¿Cómo haces un Singleton thread-safe?
    - ¿Qué diferencia hay entre lazy y eager initialization?
    - ¿Por qué el enum Singleton es el más seguro?
    - ¿Cómo rompe la reflexión un Singleton y cómo lo proteges?
*/

public class SingletonDemo {

    // ── 1. IMPLEMENTACIÓN CON HOLDER IDIOM (recomendada) ────────────────────

    /*
        El "Initialization-on-demand holder" es la forma más limpia:
        - Lazy: la instancia se crea solo cuando se necesita por primera vez.
        - Thread-safe: la JVM garantiza que la clase interna se inicializa una vez.
        - Sin synchronized: no penaliza el rendimiento en lecturas.
    */
    static class ConfigService {
        private ConfigService() { }

        private static class Holder {
            private static final ConfigService INSTANCE = new ConfigService();
        }

        public static ConfigService getInstance() {
            return Holder.INSTANCE;
        }

        public String get(String key) {
            return switch (key) {
                case "env" -> "prod";
                default -> "n/a";
            };
        }
    }

    // ── 2. IMPLEMENTACIÓN CON ENUM (más segura contra reflexión) ────────────

    /*
        Joshua Bloch recomienda el enum Singleton en Effective Java.
        Ventajas:
        - Protegido contra reflexión y serialización automáticamente.
        - Garantizado por la JVM como thread-safe.
        Desventaja:
        - No permite herencia (el enum ya extiende java.lang.Enum).
    */
    enum DatabaseConnection {
        INSTANCE;

        public void query(String sql) {
            System.out.println("Ejecutando: " + sql);
        }
    }

    // ── 3. DEMOSTRACIÓN ──────────────────────────────────────────────────────

    public static void main(String[] args) {

        // Holder idiom: ambas referencias apuntan a la misma instancia
        ConfigService a = ConfigService.getInstance();
        ConfigService b = ConfigService.getInstance();
        System.out.println("¿Misma instancia? " + (a == b)); // true
        System.out.println("env = " + a.get("env"));          // prod

        // Enum Singleton: acceso directo y seguro
        DatabaseConnection.INSTANCE.query("SELECT * FROM users");
    }

    // ── GOTCHAS ──────────────────────────────────────────────────────────────

    /*
        ⚠ La reflexión puede romper el Singleton clásico:
              Constructor<?> c = ConfigService.class.getDeclaredConstructor();
              c.setAccessible(true);
              ConfigService hack = (ConfigService) c.newInstance(); // segunda instancia!
          → Solución: lanza excepción en el constructor si ya existe instancia.
          → O usa enum (inmune por diseño).

        ⚠ En Spring Boot: no necesitas Singleton manual.
          Los beans son Singleton por defecto con @Component / @Service.
          Usa el patrón directamente solo cuando no estés en el contexto de Spring.
    */
}
