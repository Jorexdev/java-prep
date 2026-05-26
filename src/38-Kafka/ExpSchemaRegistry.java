import java.util.*;

/**
 * Simulación de Schema Registry y evolución de schemas con Java puro.
 *
 * Conceptos demostrados:
 *  - SchemaRegistry: almacena versiones de schema por subject
 *  - CompatibilityChecker: valida BACKWARD, FORWARD y FULL compatibility
 *  - Schema evolution: añadir campo opcional (compatible) vs borrar campo requerido (breaking)
 *
 * Reglas de compatibilidad:
 *  BACKWARD  → el nuevo schema puede leer datos escritos con el schema anterior
 *  FORWARD   → el schema anterior puede leer datos escritos con el nuevo schema
 *  FULL      → compatible en ambas direcciones (BACKWARD + FORWARD)
 */
public class ExpSchemaRegistry {

    // ─────────────────────────────────────────────
    // CAMPO DE SCHEMA
    // ─────────────────────────────────────────────

    record Campo(String nombre, String tipo, boolean requerido) {
        @Override
        public String toString() {
            return String.format("  %-12s %-8s %s", nombre, tipo, requerido ? "[required]" : "[optional]");
        }
    }

    // ─────────────────────────────────────────────
    // SCHEMA: conjunto de campos versionado
    // ─────────────────────────────────────────────

    static class Schema {
        private final int version;
        private final List<Campo> campos;

        Schema(int version, List<Campo> campos) {
            this.version = version;
            this.campos = new ArrayList<>(campos);
        }

        int version() { return version; }
        List<Campo> campos() { return Collections.unmodifiableList(campos); }

        Optional<Campo> campo(String nombre) {
            return campos.stream().filter(c -> c.nombre().equals(nombre)).findFirst();
        }

        Set<String> nombresCampos() {
            Set<String> set = new LinkedHashSet<>();
            campos.forEach(c -> set.add(c.nombre()));
            return set;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Schema v" + version + ":\n");
            campos.forEach(c -> sb.append(c).append("\n"));
            return sb.toString();
        }
    }

    // ─────────────────────────────────────────────
    // SCHEMA REGISTRY: almacena schemas por subject
    // ─────────────────────────────────────────────

    static class SchemaRegistry {
        // subject (ej. "user-value") → lista de schemas por versión
        private final Map<String, List<Schema>> registry = new LinkedHashMap<>();
        // subject → modo de compatibilidad configurado
        private final Map<String, String> compatibilityMode = new HashMap<>();

        void setCompatibility(String subject, String mode) {
            compatibilityMode.put(subject, mode);
        }

        // Registrar un nuevo schema; valida compatibilidad con el anterior
        boolean registerSchema(String subject, Schema schema) {
            List<Schema> versions = registry.computeIfAbsent(subject, k -> new ArrayList<>());
            String mode = compatibilityMode.getOrDefault(subject, "BACKWARD");

            if (!versions.isEmpty()) {
                Schema anterior = versions.get(versions.size() - 1);
                CompatibilityResult result = CompatibilityChecker.check(anterior, schema, mode);
                System.out.printf("  [Registry] Registrando subject='%s' v%d (modo=%s):%n",
                        subject, schema.version(), mode);
                result.imprimir();
                if (!result.compatible()) {
                    System.out.printf("  [Registry] RECHAZADO: schema v%d no es %s-compatible con v%d%n",
                            schema.version(), mode, anterior.version());
                    return false;
                }
            } else {
                System.out.printf("  [Registry] Registrando subject='%s' v%d (primer schema)%n",
                        subject, schema.version());
            }

            versions.add(schema);
            System.out.printf("  [Registry] REGISTRADO: subject='%s' v%d%n", subject, schema.version());
            return true;
        }

        Optional<Schema> getLatest(String subject) {
            List<Schema> versions = registry.get(subject);
            if (versions == null || versions.isEmpty()) return Optional.empty();
            return Optional.of(versions.get(versions.size() - 1));
        }

        Optional<Schema> getVersion(String subject, int version) {
            List<Schema> versions = registry.getOrDefault(subject, Collections.emptyList());
            return versions.stream().filter(s -> s.version() == version).findFirst();
        }

        void mostrarHistorial(String subject) {
            System.out.printf("  Historial de '%s':%n", subject);
            registry.getOrDefault(subject, Collections.emptyList())
                    .forEach(s -> System.out.printf("    v%d → %d campos: %s%n",
                            s.version(), s.campos().size(), s.nombresCampos()));
        }
    }

    // ─────────────────────────────────────────────
    // COMPATIBILITY CHECKER
    // ─────────────────────────────────────────────

    record CompatibilityResult(boolean compatible, List<String> razones) {
        void imprimir() {
            razones.forEach(r -> System.out.println("    " + r));
        }
    }

    static class CompatibilityChecker {

        static CompatibilityResult check(Schema anterior, Schema nuevo, String mode) {
            return switch (mode) {
                case "BACKWARD" -> checkBackward(anterior, nuevo);
                case "FORWARD"  -> checkForward(anterior, nuevo);
                case "FULL"     -> checkFull(anterior, nuevo);
                default -> new CompatibilityResult(false,
                        List.of("Modo de compatibilidad desconocido: " + mode));
            };
        }

        // BACKWARD: el nuevo schema puede leer datos escritos con el anterior.
        // Reglas: no se puede eliminar un campo required; se pueden añadir campos (con default).
        static CompatibilityResult checkBackward(Schema anterior, Schema nuevo) {
            List<String> razones = new ArrayList<>();
            boolean ok = true;

            // Verificar que los campos required del anterior siguen en el nuevo
            for (Campo c : anterior.campos()) {
                if (c.requerido() && nuevo.campo(c.nombre()).isEmpty()) {
                    razones.add("✗ BACKWARD: campo required '" + c.nombre() +
                            "' eliminado — lector nuevo no puede leer datos viejos");
                    ok = false;
                }
            }

            // Los campos nuevos añadidos deben ser opcionales (tienen default implícito)
            for (Campo c : nuevo.campos()) {
                if (anterior.campo(c.nombre()).isEmpty()) {
                    if (c.requerido()) {
                        razones.add("✗ BACKWARD: campo '" + c.nombre() +
                                "' añadido como required — datos viejos no lo tienen");
                        ok = false;
                    } else {
                        razones.add("✓ BACKWARD: campo optional '" + c.nombre() +
                                "' añadido — datos viejos usan el default");
                    }
                }
            }

            if (ok && razones.isEmpty()) razones.add("✓ BACKWARD: sin cambios incompatibles");
            return new CompatibilityResult(ok, razones);
        }

        // FORWARD: el schema anterior puede leer datos escritos con el nuevo.
        // Reglas: no se puede añadir un campo required; se pueden eliminar campos opcionales.
        static CompatibilityResult checkForward(Schema anterior, Schema nuevo) {
            List<String> razones = new ArrayList<>();
            boolean ok = true;

            // Campos nuevos que el lector viejo no conoce → deben ser ignorables (opcionales)
            for (Campo c : nuevo.campos()) {
                if (anterior.campo(c.nombre()).isEmpty() && c.requerido()) {
                    razones.add("✗ FORWARD: campo required '" + c.nombre() +
                            "' añadido — lector viejo no sabe manejarlo");
                    ok = false;
                }
            }

            // Campos del anterior que desaparecen en el nuevo — el lector viejo los espera
            for (Campo c : anterior.campos()) {
                if (nuevo.campo(c.nombre()).isEmpty()) {
                    if (c.requerido()) {
                        razones.add("✗ FORWARD: campo required '" + c.nombre() +
                                "' eliminado — lector viejo lo necesita");
                        ok = false;
                    } else {
                        razones.add("✓ FORWARD: campo optional '" + c.nombre() +
                                "' eliminado — lector viejo lo ignora si no está");
                    }
                }
            }

            if (ok && razones.isEmpty()) razones.add("✓ FORWARD: sin cambios incompatibles");
            return new CompatibilityResult(ok, razones);
        }

        // FULL: compatible en ambas direcciones
        static CompatibilityResult checkFull(Schema anterior, Schema nuevo) {
            CompatibilityResult backward = checkBackward(anterior, nuevo);
            CompatibilityResult forward  = checkForward(anterior, nuevo);
            List<String> razones = new ArrayList<>();
            razones.add("  [BACKWARD check]:");
            razones.addAll(backward.razones().stream().map(r -> "    " + r).toList());
            razones.add("  [FORWARD check]:");
            razones.addAll(forward.razones().stream().map(r -> "    " + r).toList());
            return new CompatibilityResult(backward.compatible() && forward.compatible(), razones);
        }
    }

    // ─────────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────────

    public static void main(String[] args) {

        System.out.println("═".repeat(65));
        System.out.println("  SCHEMA REGISTRY — Java puro");
        System.out.println("═".repeat(65));

        SchemaRegistry registry = new SchemaRegistry();
        registry.setCompatibility("user-value", "BACKWARD");

        // v1: schema inicial {name, email} — ambos required
        Schema v1 = new Schema(1, List.of(
                new Campo("name",  "string", true),
                new Campo("email", "string", true)
        ));

        // v2: añade campo optional 'phone' — BACKWARD compatible
        Schema v2 = new Schema(2, List.of(
                new Campo("name",  "string", true),
                new Campo("email", "string", true),
                new Campo("phone", "string", false)  // optional → datos viejos sin phone usan null/default
        ));

        // v3: elimina 'email' (required) — BREAKING: lector nuevo no puede leer datos viejos
        Schema v3breaking = new Schema(3, List.of(
                new Campo("name",  "string", true),
                new Campo("phone", "string", false)
        ));

        // ── Registro de schemas ────────────────────────────────────────
        System.out.println("\n── Registrar v1 (primer schema) ──");
        registry.registerSchema("user-value", v1);

        System.out.println("\n── Registrar v2 (añade phone optional) ──");
        registry.registerSchema("user-value", v2);

        System.out.println("\n── Registrar v3 (elimina email required) ──");
        registry.registerSchema("user-value", v3breaking);

        // ── Historial ─────────────────────────────────────────────────
        System.out.println("\n── Historial del subject ──");
        registry.mostrarHistorial("user-value");

        // ── Demo FORWARD compatibility ─────────────────────────────────
        System.out.println("\n── Demo FORWARD: lector viejo con datos nuevos ──");
        SchemaRegistry reg2 = new SchemaRegistry();
        reg2.setCompatibility("order-value", "FORWARD");

        Schema ov1 = new Schema(1, List.of(
                new Campo("orderId", "string", true),
                new Campo("amount",  "number", true)
        ));
        Schema ov2 = new Schema(2, List.of(
                new Campo("orderId",  "string", true),
                new Campo("amount",   "number", true),
                new Campo("currency", "string", false) // optional: FORWARD ok
        ));
        Schema ov3breaking = new Schema(3, List.of(
                new Campo("orderId",  "string", true),
                new Campo("amount",   "number", true),
                new Campo("currency", "string", true) // required nuevo: FORWARD break
        ));

        reg2.registerSchema("order-value", ov1);
        System.out.println();
        reg2.registerSchema("order-value", ov2);
        System.out.println();
        reg2.registerSchema("order-value", ov3breaking);

        // ── Demo FULL compatibility ────────────────────────────────────
        System.out.println("\n── Demo FULL: reglas más estrictas ──");
        SchemaRegistry reg3 = new SchemaRegistry();
        reg3.setCompatibility("payment-value", "FULL");

        Schema pv1 = new Schema(1, List.of(
                new Campo("paymentId", "string", true),
                new Campo("status",    "string", true)
        ));
        Schema pv2ok = new Schema(2, List.of(
                new Campo("paymentId", "string", true),
                new Campo("status",    "string", true),
                new Campo("reference", "string", false) // FULL: optional → ok en ambas dir.
        ));
        reg3.registerSchema("payment-value", pv1);
        System.out.println();
        reg3.registerSchema("payment-value", pv2ok);

        System.out.println("\n" + "═".repeat(65));
        System.out.println("  RESUMEN REGLAS DE COMPATIBILIDAD");
        System.out.println("═".repeat(65));
        System.out.println("  BACKWARD: nuevo reader, datos viejos");
        System.out.println("    ✓ Añadir campo optional   ✗ Eliminar required  ✗ Añadir required");
        System.out.println("  FORWARD: viejo reader, datos nuevos");
        System.out.println("    ✓ Eliminar optional        ✗ Añadir required   ✗ Eliminar required");
        System.out.println("  FULL = BACKWARD + FORWARD (más seguro, más restrictivo)");
        System.out.println("    ✓ Solo se puede añadir/quitar campos opcionales");
        System.out.println("═".repeat(65));
    }
}
