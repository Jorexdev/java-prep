import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Diferencias clave Flyway vs Liquibase:
//
//   Flyway         → flyway_schema_history        | identifica por versión
//   Liquibase      → DATABASECHANGELOG +           | identifica por id+author+filename
//                    DATABASECHANGELOGLOCK          |
//
//   Rollback       → Flyway: NO soporta rollback en versionadas
//                    Liquibase: rollback definido explícitamente por el autor (<rollback>)
//
//   Formato        → Flyway: solo SQL
//                    Liquibase: XML, YAML, JSON o SQL
//
// Liquibase usa DATABASECHANGELOGLOCK para prevenir que dos instancias migren
// simultáneamente (arranque de múltiples pods en Kubernetes, por ejemplo).

// ── Changelog XML simulado (equivalente en texto) ─────────────────────────────

// <?xml version="1.0" encoding="UTF-8"?>
// <databaseChangeLog>
//
//   <changeSet id="001" author="jorex">
//     <preConditions onFail="MARK_RAN">
//       <not><tableExists tableName="users"/></not>
//     </preConditions>
//     <createTable tableName="users">
//       <column name="id" type="BIGINT" autoIncrement="true"><constraints primaryKey="true"/></column>
//       <column name="name" type="VARCHAR(255)"><constraints nullable="false"/></column>
//       <column name="email" type="VARCHAR(255)"><constraints nullable="false" unique="true"/></column>
//     </createTable>
//     <rollback>DROP TABLE users;</rollback>
//   </changeSet>
//
//   <changeSet id="002" author="jorex">
//     <addColumn tableName="users">
//       <column name="created_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP"/>
//     </addColumn>
//     <rollback>ALTER TABLE users DROP COLUMN created_at;</rollback>
//   </changeSet>
//
//   <changeSet id="003" author="jorex" context="!test">
//     <createTable tableName="roles">
//       <column name="id" type="BIGINT" autoIncrement="true"><constraints primaryKey="true"/></column>
//       <column name="name" type="VARCHAR(50)"><constraints nullable="false" unique="true"/></column>
//     </createTable>
//     <rollback>DROP TABLE roles;</rollback>
//   </changeSet>
//
//   <changeSet id="004" author="jorex" context="test">
//     <insert tableName="roles"><column name="name" value="ADMIN"/></insert>
//     <insert tableName="roles"><column name="name" value="USER"/></insert>
//     <rollback>DELETE FROM roles WHERE name IN ('ADMIN', 'USER');</rollback>
//   </changeSet>
//
// </databaseChangeLog>

// ── Precondition ──────────────────────────────────────────────────────────────

// onFail="HALT"     → detiene la migración (default)
// onFail="CONTINUE" → salta el changeset y continúa
// onFail="MARK_RAN" → marca como ejecutado sin correr el SQL
enum OnFail { HALT, CONTINUE, MARK_RAN }

class Precondition {
    final String description; // ej. "tableNotExists:users"
    final boolean passes;     // resultado evaluado en simulación
    final OnFail onFail;

    Precondition(String description, boolean passes, OnFail onFail) {
        this.description = description;
        this.passes      = passes;
        this.onFail      = onFail;
    }
}

// ── Changeset ─────────────────────────────────────────────────────────────────

class Changeset {
    final String       id;
    final String       author;
    final String       sql;
    final String       rollbackSql;    // null si no se define rollback
    final String       context;        // null = se aplica siempre
    final Precondition precondition;   // null = sin precondición

    Changeset(String id, String author, String sql, String rollbackSql,
              String context, Precondition precondition) {
        this.id          = id;
        this.author      = author;
        this.sql         = sql;
        this.rollbackSql = rollbackSql;
        this.context     = context;
        this.precondition = precondition;
    }

    // Checksum MD5 simulado — Liquibase lo recalcula en cada arranque
    // y falla si difiere del registrado en DATABASECHANGELOG
    int checksum() { return sql.hashCode(); }
}

// ── Changelog ─────────────────────────────────────────────────────────────────

class Changelog {
    private final List<Changeset> changesets = new ArrayList<>();

    void add(Changeset cs) { changesets.add(cs); }

    List<Changeset> getChangesets() { return List.copyOf(changesets); }
}

// ── Registro en DATABASECHANGELOG ─────────────────────────────────────────────

class ChangelogEntry {
    final String        id;
    final String        author;
    final int           orderExecuted;
    final int           checksum;
    final LocalDateTime dateExecuted;
    final String        execType; // EXECUTED, MARK_RAN, FAILED, RERAN
    String              tag;      // para rollback por tag

    ChangelogEntry(String id, String author, int order, int checksum, String execType) {
        this.id            = id;
        this.author        = author;
        this.orderExecuted = order;
        this.checksum      = checksum;
        this.dateExecuted  = LocalDateTime.now();
        this.execType      = execType;
        this.tag           = null;
    }
}

// ── LiquibaseSim ──────────────────────────────────────────────────────────────

class LiquibaseSim {

    // DATABASECHANGELOG — tabla que Liquibase gestiona
    private final Map<String, ChangelogEntry> databaseChangelog = new LinkedHashMap<>();

    // DATABASECHANGELOGLOCK — asegura que solo un proceso migra a la vez
    private boolean locked = false;
    private int execCounter = 0;

    private final Changelog changelog;
    private String activeContext;

    LiquibaseSim(Changelog changelog, String context) {
        this.changelog     = changelog;
        this.activeContext = context;
    }

    // liquibase update — aplica todos los changesets pendientes del changelog
    void update() {
        acquireLock();
        System.out.println("  [Liquibase] update() — contexto: " + activeContext);

        for (Changeset cs : changelog.getChangesets()) {

            // Saltar changesets cuyo contexto no coincide con el activo
            if (cs.context != null && !cs.context.equals(activeContext)) {
                System.out.println("  [Liquibase] SKIP " + cs.id + " (contexto '" + cs.context
                    + "' != '" + activeContext + "')");
                continue;
            }

            // Verificar si ya está ejecutado
            if (databaseChangelog.containsKey(cs.id)) {
                ChangelogEntry existing = databaseChangelog.get(cs.id);
                // Liquibase valida el checksum: si el SQL cambió, falla
                if (existing.checksum != cs.checksum()) {
                    System.out.println("  [Liquibase] ERROR: checksum mismatch en changeset "
                        + cs.id + " — el SQL fue modificado tras ser ejecutado");
                    releaseLock();
                    throw new IllegalStateException("Checksum mismatch en " + cs.id);
                }
                System.out.println("  [Liquibase] SKIP " + cs.id + " (ya ejecutado, checksum OK)");
                continue;
            }

            // Evaluar precondición
            if (cs.precondition != null) {
                if (!cs.precondition.passes) {
                    switch (cs.precondition.onFail) {
                        case HALT -> {
                            System.out.println("  [Liquibase] HALT — precondición falló: "
                                + cs.precondition.description);
                            releaseLock();
                            throw new IllegalStateException("Precondición falló: " + cs.precondition.description);
                        }
                        case CONTINUE -> {
                            System.out.println("  [Liquibase] CONTINUE — precondición falló, saltando: "
                                + cs.precondition.description);
                            continue;
                        }
                        case MARK_RAN -> {
                            System.out.println("  [Liquibase] MARK_RAN — precondición falló, marcado sin ejecutar: "
                                + cs.id);
                            databaseChangelog.put(cs.id,
                                new ChangelogEntry(cs.id, cs.author, ++execCounter, cs.checksum(), "MARK_RAN"));
                            continue;
                        }
                    }
                }
            }

            // Ejecutar changeset
            System.out.println("  [Liquibase] EXECUTE changeset " + cs.id
                + " (author=" + cs.author + ")");
            databaseChangelog.put(cs.id,
                new ChangelogEntry(cs.id, cs.author, ++execCounter, cs.checksum(), "EXECUTED"));
        }

        releaseLock();
        System.out.println("  [Liquibase] update() completado\n");
    }

    // liquibase rollback --count N — revierte los últimos N changesets ejecutados
    // Solo funciona si el changeset tiene <rollback> definido
    void rollback(int count) {
        acquireLock();
        System.out.println("  [Liquibase] rollback(count=" + count + ")");

        // Changesets en orden inverso de ejecución
        List<ChangelogEntry> executed = databaseChangelog.values().stream()
            .filter(e -> "EXECUTED".equals(e.execType))
            .sorted((a, b) -> Integer.compare(b.orderExecuted, a.orderExecuted))
            .toList();

        int reverted = 0;
        for (ChangelogEntry entry : executed) {
            if (reverted >= count) break;

            // Buscar el changeset original para obtener el rollbackSql
            Changeset original = changelog.getChangesets().stream()
                .filter(cs -> cs.id.equals(entry.id))
                .findFirst()
                .orElse(null);

            if (original == null || original.rollbackSql == null) {
                System.out.println("  [Liquibase] ERROR: changeset " + entry.id
                    + " no tiene <rollback> definido — rollback imposible");
                releaseLock();
                throw new UnsupportedOperationException("No hay rollback definido para " + entry.id);
            }

            System.out.println("  [Liquibase] ROLLBACK changeset " + entry.id
                + ": " + original.rollbackSql.trim());
            databaseChangelog.remove(entry.id);
            reverted++;
        }

        releaseLock();
        System.out.println("  [Liquibase] rollback completado — " + reverted + " changeset(s) revertido(s)\n");
    }

    // liquibase status — muestra qué changesets están pendientes
    void status() {
        System.out.println("\n  ┌────────────────────────────────────────────────────────────────────┐");
        System.out.println("  │            DATABASECHANGELOG                                       │");
        System.out.println("  ├──────┬─────────┬──────────────────────────┬──────────┬────────────┤");
        System.out.println("  │ Ord  │ Id      │ Author                   │ ExecType │ Tag        │");
        System.out.println("  ├──────┼─────────┼──────────────────────────┼──────────┼────────────┤");

        databaseChangelog.values().forEach(e ->
            System.out.printf("  │ %-4d │ %-7s │ %-24s │ %-8s │ %-10s │%n",
                e.orderExecuted, e.id, e.author, e.execType,
                e.tag != null ? e.tag : ""));

        System.out.println("  └──────┴─────────┴──────────────────────────┴──────────┴────────────┘");

        long pending = changelog.getChangesets().stream()
            .filter(cs -> {
                boolean contextOk = cs.context == null || cs.context.equals(activeContext);
                boolean notRun    = !databaseChangelog.containsKey(cs.id);
                return contextOk && notRun;
            })
            .count();

        System.out.println("  Changesets pendientes para contexto '" + activeContext + "': " + pending + "\n");
    }

    // ── Lock ──────────────────────────────────────────────────────────────────

    // DATABASECHANGELOGLOCK evita migraciones concurrentes
    private void acquireLock() {
        if (locked) throw new IllegalStateException("[Liquibase] No se pudo adquirir DATABASECHANGELOGLOCK — otro proceso está migrando");
        locked = true;
        System.out.println("  [Liquibase] DATABASECHANGELOGLOCK adquirido");
    }

    private void releaseLock() {
        locked = false;
        System.out.println("  [Liquibase] DATABASECHANGELOGLOCK liberado");
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpLiquibase {
    public static void main(String[] args) {

        System.out.println("=== Simulación Liquibase — Migraciones de BD ===\n");

        // ─── Construcción del changelog ──────────────────────────────────────
        Changelog changelog = new Changelog();

        changelog.add(new Changeset("001", "jorex",
            "CREATE TABLE users (id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), email VARCHAR(255) UNIQUE);",
            "DROP TABLE users;",
            null,
            new Precondition("tableNotExists:users", true, OnFail.MARK_RAN)));

        changelog.add(new Changeset("002", "jorex",
            "ALTER TABLE users ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;",
            "ALTER TABLE users DROP COLUMN created_at;",
            null,
            null));

        changelog.add(new Changeset("003", "jorex",
            "CREATE TABLE roles (id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(50) UNIQUE);",
            "DROP TABLE roles;",
            "!test",   // no se aplica en contexto test
            null));

        changelog.add(new Changeset("004", "jorex",
            "INSERT INTO roles (name) VALUES ('ADMIN'), ('USER');",
            "DELETE FROM roles WHERE name IN ('ADMIN', 'USER');",
            "test",    // solo en contexto test
            null));

        // ─── Escenario 1: entorno de producción ──────────────────────────────
        System.out.println("[ Escenario 1: entorno producción (context=prod) ]\n");
        {
            LiquibaseSim lb = new LiquibaseSim(changelog, "prod");

            System.out.println("Estado inicial:");
            lb.status();

            System.out.println("Ejecutando update():");
            lb.update();

            System.out.println("Estado tras update():");
            lb.status();
        }

        System.out.println("─".repeat(72) + "\n");

        // ─── Escenario 2: entorno de test ─────────────────────────────────────
        System.out.println("[ Escenario 2: entorno test (context=test) ]\n");
        {
            LiquibaseSim lb = new LiquibaseSim(changelog, "test");
            System.out.println("Ejecutando update() — 003 se saltará, 004 se aplicará:");
            lb.update();

            System.out.println("Estado tras update():");
            lb.status();
        }

        System.out.println("─".repeat(72) + "\n");

        // ─── Escenario 3: rollback de los últimos 2 changesets ────────────────
        System.out.println("[ Escenario 3: rollback de los últimos 2 changesets ]\n");
        {
            LiquibaseSim lb = new LiquibaseSim(changelog, "prod");
            lb.update();

            System.out.println("Ejecutando rollback(2):");
            lb.rollback(2);

            System.out.println("Estado tras rollback:");
            lb.status();
        }

        System.out.println("─".repeat(72) + "\n");

        // ─── Escenario 4: precondición MARK_RAN ───────────────────────────────
        System.out.println("[ Escenario 4: precondición onFail=MARK_RAN ]\n");
        System.out.println("Simulamos que la tabla 'users' YA EXISTE en la BD:");
        {
            Changelog cl2 = new Changelog();
            // precondition.passes = false → la tabla ya existe, no crear
            cl2.add(new Changeset("001", "jorex",
                "CREATE TABLE users (id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255));",
                "DROP TABLE users;",
                null,
                new Precondition("tableNotExists:users", false, OnFail.MARK_RAN)));

            cl2.add(new Changeset("002", "jorex",
                "ALTER TABLE users ADD COLUMN email VARCHAR(255);",
                "ALTER TABLE users DROP COLUMN email;",
                null, null));

            LiquibaseSim lb = new LiquibaseSim(cl2, "prod");
            lb.update();

            System.out.println("Estado — 001 marcado como MARK_RAN, 002 ejecutado:");
            lb.status();
        }

        System.out.println("─".repeat(72) + "\n");

        // ─── Comparativa Flyway vs Liquibase ──────────────────────────────────
        System.out.println("[ Comparativa Flyway vs Liquibase ]");
        System.out.printf("  %-28s %-25s %-25s%n", "Característica", "Flyway", "Liquibase");
        System.out.printf("  %-28s %-25s %-25s%n", "-".repeat(28), "-".repeat(25), "-".repeat(25));
        System.out.printf("  %-28s %-25s %-25s%n", "Tabla de historial",   "flyway_schema_history", "DATABASECHANGELOG");
        System.out.printf("  %-28s %-25s %-25s%n", "Lock de migración",    "No (por defecto)",      "DATABASECHANGELOGLOCK");
        System.out.printf("  %-28s %-25s %-25s%n", "Identificador",        "Versión (V1, V2...)",   "id + author + filename");
        System.out.printf("  %-28s %-25s %-25s%n", "Rollback versionado",  "No soportado",          "Sí (<rollback>)");
        System.out.printf("  %-28s %-25s %-25s%n", "Contextos",            "No",                    "Sí (context=)");
        System.out.printf("  %-28s %-25s %-25s%n", "Precondiciones",       "No",                    "Sí (<preConditions>)");
        System.out.printf("  %-28s %-25s %-25s%n", "Formatos",             "Solo SQL",              "SQL, XML, YAML, JSON");
        System.out.printf("  %-28s %-25s %-25s%n", "Curva de aprendizaje", "Baja",                  "Media");
    }
}
