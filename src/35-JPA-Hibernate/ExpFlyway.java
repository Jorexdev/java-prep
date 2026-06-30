import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Convención de nombres:
//   V{versión}__{descripcion}.sql   → migraciones versionadas (V1, V2, V2_1, ...)
//   R__{descripcion}.sql            → repeatable (se re-ejecutan si cambia el checksum)
//   U{versión}__{descripcion}.sql   → undo (requiere Flyway Teams)
//
// Flyway escanea classpath en db/migration/, compara checksums con flyway_schema_history
// y ejecuta las pendientes en orden ascendente de versión.

// ── Tipos de migración ────────────────────────────────────────────────────────

enum MigrationType { VERSIONED, REPEATABLE }

// ── Scripts SQL simulados como constantes ─────────────────────────────────────

class SqlScripts {
    // V1__create_users.sql
    static final String V1_CREATE_USERS = """
            CREATE TABLE users (
                id   BIGINT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(255) NOT NULL,
                email VARCHAR(255) UNIQUE NOT NULL
            );
            """;

    // V2__add_created_at.sql
    static final String V2_ADD_CREATED_AT = """
            ALTER TABLE users
                ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
            """;

    // V3__create_roles.sql
    static final String V3_CREATE_ROLES = """
            CREATE TABLE roles (
                id   BIGINT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(50) UNIQUE NOT NULL
            );
            CREATE TABLE user_roles (
                user_id BIGINT REFERENCES users(id),
                role_id BIGINT REFERENCES roles(id),
                PRIMARY KEY (user_id, role_id)
            );
            """;

    // R__insert_default_roles.sql  — repeatable: se vuelve a ejecutar si el contenido cambia
    static final String R_DEFAULT_ROLES = """
            MERGE INTO roles (name) VALUES ('ADMIN'), ('USER'), ('GUEST');
            """;
}

// ── Registro en flyway_schema_history ────────────────────────────────────────

class MigrationRecord {
    final int          installedRank;
    final String       version;     // null en repeatables
    final String       description;
    final MigrationType type;
    final String       script;
    final int          checksum;
    final LocalDateTime installedOn;
    boolean            success;

    MigrationRecord(int rank, String version, String description,
                    MigrationType type, String script, boolean success) {
        this.installedRank = rank;
        this.version       = version;
        this.description   = description;
        this.type          = type;
        this.script        = script;
        this.checksum      = script.hashCode();
        this.installedOn   = LocalDateTime.now();
        this.success       = success;
    }
}

// ── Callback ──────────────────────────────────────────────────────────────────

// Equivale a org.flywaydb.core.api.callback.FlywayCallback
interface FlywayCallback {
    void beforeMigrate();
    void afterMigrate();
}

class AuditCallback implements FlywayCallback {
    @Override public void beforeMigrate() {
        System.out.println("  [Callback] beforeMigrate — iniciando sesión de migración");
    }
    @Override public void afterMigrate() {
        System.out.println("  [Callback] afterMigrate  — sesión de migración finalizada");
    }
}

// ── FlywaySim ─────────────────────────────────────────────────────────────────

class FlywaySim {

    // Representa flyway_schema_history
    final List<MigrationRecord> history = new ArrayList<>();

    // Scripts registrados (classpath scan)
    private final List<Object[]> classpath = new ArrayList<>();
    // Cada entry: [version_or_null, description, type, sql]

    private final List<FlywayCallback> callbacks = new ArrayList<>();
    private String baselineVersion = null; // versión hasta la que se hizo baseline

    FlywaySim() {
        // Simula el classpath scan de db/migration/
        classpath.add(new Object[]{"1",   "create_users",     MigrationType.VERSIONED,  SqlScripts.V1_CREATE_USERS});
        classpath.add(new Object[]{"2",   "add_created_at",   MigrationType.VERSIONED,  SqlScripts.V2_ADD_CREATED_AT});
        classpath.add(new Object[]{"3",   "create_roles",     MigrationType.VERSIONED,  SqlScripts.V3_CREATE_ROLES});
        classpath.add(new Object[]{null,  "insert_default_roles", MigrationType.REPEATABLE, SqlScripts.R_DEFAULT_ROLES});
    }

    void addCallback(FlywayCallback cb) { callbacks.add(cb); }

    // flyway.baseline() — marca la BD existente como ya migrada hasta cierta versión
    // Se usa cuando la BD ya tiene tablas pero nunca tuvo flyway_schema_history
    void baseline(String version) {
        System.out.println("  [Flyway] BASELINE en versión " + version
            + " — marcando estado inicial sin ejecutar scripts");
        history.add(new MigrationRecord(history.size() + 1, version, "<< Flyway Baseline >>",
                MigrationType.VERSIONED, "", true));
        // Versiones <= baselineVersion se consideran ya aplicadas aunque no estén en history
        this.baselineVersion = version;
    }

    // flyway.migrate() — ejecuta las migraciones pendientes
    void migrate() {
        callbacks.forEach(FlywayCallback::beforeMigrate);

        int applied = 0;
        for (Object[] entry : classpath) {
            String       version = (String) entry[0];
            String       desc    = (String) entry[1];
            MigrationType type   = (MigrationType) entry[2];
            String       sql     = (String) entry[3];

            if (type == MigrationType.VERSIONED && isApplied(version)) {
                System.out.println("  [Flyway] SKIP V" + version + "__" + desc + ".sql (ya aplicada)");
                continue;
            }
            if (type == MigrationType.REPEATABLE && !checksumChanged(desc, sql)) {
                System.out.println("  [Flyway] SKIP R__" + desc + ".sql (checksum sin cambios)");
                continue;
            }

            System.out.println("  [Flyway] EXECUTE "
                + (type == MigrationType.VERSIONED ? "V" + version + "__" : "R__")
                + desc + ".sql");

            // Simula ejecución — en producción aquí va la conexión JDBC real
            history.add(new MigrationRecord(history.size() + 1, version, desc, type, sql, true));
            applied++;
        }

        callbacks.forEach(FlywayCallback::afterMigrate);
        System.out.println("  [Flyway] migrate() completado — " + applied + " script(s) aplicado(s)");
    }

    // flyway.validate() — compara checksums actuales con los registrados en history
    void validate() {
        System.out.println("  [Flyway] validate():");
        boolean ok = true;
        for (MigrationRecord rec : history) {
            if (rec.version == null) continue; // repeatables tienen su propia lógica
            // En una BD real compararíamos contra el fichero del classpath
            if (!rec.success) {
                System.out.println("    ERROR: migración " + rec.script + " falló previamente");
                ok = false;
            }
        }
        System.out.println(ok ? "    Validación OK — todos los checksums coinciden"
                              : "    Validación FAILED — usa repair() para limpiar");
    }

    // flyway.repair() — elimina entradas fallidas de flyway_schema_history
    // Útil tras corregir un script que falló a mitad de ejecución
    void repair() {
        int before = history.size();
        history.removeIf(r -> !r.success);
        int removed = before - history.size();
        System.out.println("  [Flyway] repair() — " + removed + " entrada(s) fallida(s) eliminada(s) de flyway_schema_history");
    }

    // flyway.info() — muestra el estado de todas las migraciones
    void info() {
        System.out.println("\n  ┌─────────────────────────────────────────────────────────────────────┐");
        System.out.println("  │           flyway_schema_history                                      │");
        System.out.println("  ├──────┬──────────┬──────────────────────────────┬──────────┬─────────┤");
        System.out.println("  │ Rank │ Versión  │ Descripción                  │ Tipo     │ Estado  │");
        System.out.println("  ├──────┼──────────┼──────────────────────────────┼──────────┼─────────┤");

        history.stream()
               .sorted(Comparator.comparingInt(r -> r.installedRank))
               .forEach(r -> System.out.printf(
                   "  │ %-4d │ %-8s │ %-28s │ %-8s │ %-7s │%n",
                   r.installedRank,
                   r.version != null ? "V" + r.version : "R",
                   r.description.length() > 28 ? r.description.substring(0, 28) : r.description,
                   r.type,
                   r.success ? "SUCCESS" : "FAILED"));

        System.out.println("  └──────┴──────────┴──────────────────────────────┴──────────┴─────────┘");

        // Migraciones pendientes (en classpath pero no en history)
        List<String> pending = classpath.stream()
            .filter(e -> {
                String v = (String) e[0];
                MigrationType t = (MigrationType) e[2];
                return t == MigrationType.VERSIONED && !isApplied(v);
            })
            .map(e -> "V" + e[0] + "__" + e[1])
            .toList();

        if (!pending.isEmpty()) {
            System.out.println("  Pendientes: " + pending);
        } else {
            System.out.println("  Sin migraciones pendientes.");
        }
        System.out.println();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isApplied(String version) {
        // Versiones menores o iguales al baseline se consideran ya aplicadas
        if (baselineVersion != null) {
            try {
                if (Double.parseDouble(version) <= Double.parseDouble(baselineVersion)) return true;
            } catch (NumberFormatException ignored) {}
        }
        return history.stream()
            .anyMatch(r -> version.equals(r.version) && r.success);
    }

    private boolean checksumChanged(String desc, String sql) {
        return history.stream()
            .filter(r -> desc.equals(r.description) && r.type == MigrationType.REPEATABLE)
            .noneMatch(r -> r.checksum == sql.hashCode());
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpFlyway {
    public static void main(String[] args) {

        System.out.println("=== Simulación Flyway — Migraciones de BD ===\n");

        // ─── Escenario 1: migración desde cero ───────────────────────────────
        System.out.println("[ Escenario 1: migración desde cero ]\n");
        {
            FlywaySim flyway = new FlywaySim();
            flyway.addCallback(new AuditCallback());

            System.out.println("Estado inicial:");
            flyway.info();

            System.out.println("Ejecutando migrate():");
            flyway.migrate();

            System.out.println("\nEstado tras migración:");
            flyway.info();

            System.out.println("Validación tras migración:");
            flyway.validate();
        }

        System.out.println("\n" + "─".repeat(72) + "\n");

        // ─── Escenario 2: baseline (BD existente sin historial) ───────────────
        System.out.println("[ Escenario 2: baseline — BD ya existente sin flyway_schema_history ]\n");
        {
            FlywaySim flyway = new FlywaySim();

            // Las tablas V1 y V2 ya existen en producción; solo hay que aplicar V3
            System.out.println("Aplicando baseline en V2 (V1 y V2 ya están en producción):");
            flyway.baseline("2");

            System.out.println("\nEstado tras baseline:");
            flyway.info();

            System.out.println("Ejecutando migrate() (solo aplicará V3 y el repeatable):");
            flyway.migrate();

            System.out.println("\nEstado final:");
            flyway.info();
        }

        System.out.println("\n" + "─".repeat(72) + "\n");

        // ─── Escenario 3: repair ──────────────────────────────────────────────
        System.out.println("[ Escenario 3: repair — limpiar entrada fallida ]\n");
        {
            FlywaySim flyway = new FlywaySim();

            // Simulamos una migración que falló (entry manual en history)
            flyway.history.add(new MigrationRecord(1, "1", "create_users",
                    MigrationType.VERSIONED, SqlScripts.V1_CREATE_USERS, true));
            flyway.history.add(new MigrationRecord(2, "2", "add_created_at",
                    MigrationType.VERSIONED, SqlScripts.V2_ADD_CREATED_AT, false)); // FAILED

            System.out.println("Estado con migración fallida:");
            flyway.info();

            System.out.println("Ejecutando repair():");
            flyway.repair();

            System.out.println("\nEstado tras repair():");
            flyway.info();

            System.out.println("Ejecutando migrate() para reaplicar lo que faltaba:");
            flyway.migrate();

            System.out.println("\nEstado final:");
            flyway.info();
        }
    }
}
