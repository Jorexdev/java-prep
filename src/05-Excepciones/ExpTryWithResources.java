import java.util.ArrayList;
import java.util.List;

// ===== Custom AutoCloseable: simula una conexión a base de datos =====

class DatabaseConnection implements AutoCloseable {
    private final String name;
    private boolean closed = false;

    public DatabaseConnection(String name) {
        this.name = name;
        System.out.println("[OPEN]  " + name);
    }

    public void query(String sql) {
        if (closed) throw new IllegalStateException(name + " ya está cerrada");
        System.out.println("[QUERY] " + name + " → " + sql);
    }

    @Override
    public void close() {
        closed = true;
        System.out.println("[CLOSE] " + name);
    }
}

// ===== AutoCloseable que lanza excepción al cerrar =====

class FaultyResource implements AutoCloseable {
    private final String name;

    public FaultyResource(String name) {
        System.out.println("[OPEN]  " + name);
    }

    public void use() {
        System.out.println("[USE]   " + name);
    }

    @Override
    public void close() throws Exception {
        System.out.println("[CLOSE] " + name + " → lanzando excepción al cerrar");
        throw new Exception("Error al cerrar " + name);
    }
}

public class ExpTryWithResources {

    // ─── 1. Recurso único: se cierra automáticamente ──────────────────────────
    static void singleResource() {
        System.out.println("\n── 1. Recurso único ──");
        try (DatabaseConnection conn = new DatabaseConnection("conn-A")) {
            conn.query("SELECT * FROM users");
        }
        // conn.close() se llama automáticamente al salir del bloque,
        // ya sea por éxito o por excepción.
        System.out.println("Bloque terminado — conn-A ya está cerrada.");
    }

    // ─── 2. Múltiples recursos: se cierran en orden INVERSO al de apertura ────
    static void multipleResources() {
        System.out.println("\n── 2. Múltiples recursos (cierre en orden inverso) ──");
        try (DatabaseConnection connPrimary   = new DatabaseConnection("primary");
             DatabaseConnection connSecondary = new DatabaseConnection("secondary")) {

            connPrimary.query("BEGIN TRANSACTION");
            connSecondary.query("SELECT version()");
            connPrimary.query("COMMIT");
        }
        // Cierre: secondary → primary  (inverso a la declaración)
    }

    // ─── 3. Suppressed exceptions ─────────────────────────────────────────────
    // Cuando el cuerpo del try Y close() lanzan excepción, la del close()
    // queda "suprimida" — accesible via getSuppressed(), no se pierde.
    static void suppressedExceptions() {
        System.out.println("\n── 3. Suppressed exceptions ──");
        try (FaultyResource r1 = new FaultyResource("r1");
             FaultyResource r2 = new FaultyResource("r2")) {
            r1.use();
            r2.use();
            throw new RuntimeException("Excepción principal en el cuerpo");
        } catch (Exception primary) {
            System.out.println("Excepción principal: " + primary.getMessage());
            // Recuperar las excepciones suprimidas
            Throwable[] suppressed = primary.getSuppressed();
            System.out.println("Excepciones suprimidas: " + suppressed.length);
            for (Throwable s : suppressed) {
                System.out.println("  → suprimida: " + s.getMessage());
            }
        }
    }

    // ─── 4. DatabaseConnection como AutoCloseable ─────────────────────────────
    static void customAutoCloseable() {
        System.out.println("\n── 4. Custom AutoCloseable (DatabaseConnection) ──");
        try (DatabaseConnection conn = new DatabaseConnection("analytics-db")) {
            conn.query("SELECT count(*) FROM events");
            conn.query("SELECT avg(duration) FROM sessions");
        }
        // Al salir del try, close() se invoca automáticamente.
    }

    // ─── 5. try-with-resources vs try-finally ─────────────────────────────────
    // El caso peligroso: en try-finally, si el body lanza E1 y finally lanza E2,
    // E1 se PIERDE completamente — sólo llega E2 al caller. Con TWR, E1 es la
    // excepción principal y E2 queda suprimida (accesible).
    static void twrVsTryFinally() {
        System.out.println("\n── 5. try-with-resources vs try-finally ──");

        // Versión try-finally: la excepción original se TRAGA
        System.out.println("[try-finally] — la excepción del cuerpo se pierde:");
        try {
            FaultyResource r = new FaultyResource("finally-resource");
            try {
                throw new RuntimeException("Excepción ORIGINAL del cuerpo");
            } finally {
                try {
                    r.close(); // lanza → tapa la excepción original
                } catch (Exception e) {
                    // En un finally real sin catch, la original se perdería.
                    System.out.println("  finally captura: " + e.getMessage());
                    System.out.println("  ¡La excepción original ha sido TAPADA!");
                }
            }
        } catch (RuntimeException e) {
            System.out.println("  Excepción que llega al caller: " + e.getMessage());
        }

        // Versión try-with-resources: la excepción original SE CONSERVA
        System.out.println("[try-with-resources] — la excepción original se preserva:");
        try (FaultyResource r = new FaultyResource("twr-resource")) {
            throw new RuntimeException("Excepción ORIGINAL del cuerpo");
        } catch (RuntimeException e) {
            System.out.println("  Excepción principal: " + e.getMessage());
            for (Throwable s : e.getSuppressed()) {
                System.out.println("  Suprimida (close): " + s.getMessage());
            }
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        singleResource();
        multipleResources();
        suppressedExceptions();
        customAutoCloseable();
        twrVsTryFinally();
    }
}
