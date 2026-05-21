import java.util.*;

public class Ejercicio2 {

    static class TerraformResource {
        String type;
        String name;
        Map<String, String> config;

        TerraformResource(String type, String name, Map<String, String> config) {
            this.type = type;
            this.name = name;
            this.config = new LinkedHashMap<>(config);
        }

        String id() { return type + "." + name; }

        @Override
        public String toString() {
            return id() + " " + config;
        }
    }

    static class TerraformState {
        private final Map<String, TerraformResource> state = new LinkedHashMap<>();

        void apply(Map<String, TerraformResource> desired) {
            System.out.println("Applying changes...");
            // Add or update
            for (TerraformResource d : desired.values()) {
                TerraformResource existing = state.get(d.id());
                if (existing == null) {
                    System.out.println("  + Creating: " + d.id());
                    state.put(d.id(), d);
                } else if (!existing.config.equals(d.config)) {
                    System.out.println("  ~ Updating: " + d.id());
                    state.put(d.id(), d);
                } else {
                    System.out.println("  = No change: " + d.id());
                }
            }
            // Destroy removed
            state.keySet().removeIf(id -> {
                if (!desired.containsKey(id)) {
                    System.out.println("  - Destroying: " + id);
                    return true;
                }
                return false;
            });
            System.out.println("Apply complete.");
        }

        void printState() {
            System.out.println("--- State (" + state.size() + " resources) ---");
            state.values().forEach(r -> System.out.println("  " + r));
            System.out.println("---");
        }
    }

    public static void main(String[] args) {
        TerraformState tfState = new TerraformState();

        // Primera configuración deseada
        Map<String, TerraformResource> desired1 = new LinkedHashMap<>();
        TerraformResource r1 = new TerraformResource("aws_instance", "web",
                Map.of("ami", "ami-abc", "instance_type", "t2.micro"));
        TerraformResource r2 = new TerraformResource("aws_s3_bucket", "logs",
                Map.of("bucket", "my-logs"));
        desired1.put(r1.id(), r1);
        desired1.put(r2.id(), r2);

        System.out.println("=== BEFORE FIRST APPLY ===");
        tfState.printState();
        System.out.println();

        tfState.apply(desired1);
        System.out.println();

        System.out.println("=== AFTER FIRST APPLY ===");
        tfState.printState();
        System.out.println();

        // Segunda configuración: actualizar web, eliminar logs, añadir db
        Map<String, TerraformResource> desired2 = new LinkedHashMap<>();
        TerraformResource r1v2 = new TerraformResource("aws_instance", "web",
                Map.of("ami", "ami-abc", "instance_type", "t3.small"));
        TerraformResource r3 = new TerraformResource("aws_db_instance", "db",
                Map.of("engine", "postgres", "size", "db.t3.micro"));
        desired2.put(r1v2.id(), r1v2);
        desired2.put(r3.id(), r3);

        tfState.apply(desired2);
        System.out.println();

        System.out.println("=== AFTER SECOND APPLY ===");
        tfState.printState();
    }
}
