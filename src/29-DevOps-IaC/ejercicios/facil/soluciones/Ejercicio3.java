import java.util.*;

public class Ejercicio3 {

    static class TerraformResource {
        final String type;
        final String name;
        final Map<String, String> config;

        TerraformResource(String type, String name, Map<String, String> config) {
            this.type = type;
            this.name = name;
            this.config = new LinkedHashMap<>(config);
        }

        String id() { return type + "." + name; }
    }

    static class TerraformState {
        private final Map<String, TerraformResource> state = new LinkedHashMap<>();

        // Returns a list of changes made; empty = idempotent (no changes)
        List<String> apply(Map<String, TerraformResource> desired) {
            List<String> changes = new ArrayList<>();

            for (TerraformResource d : desired.values()) {
                TerraformResource existing = state.get(d.id());
                if (existing == null) {
                    changes.add("ADD " + d.id());
                    state.put(d.id(), d);
                } else if (!existing.config.equals(d.config)) {
                    changes.add("CHANGE " + d.id());
                    state.put(d.id(), d);
                }
                // else: same config -> no change, no entry added
            }

            List<String> toRemove = new ArrayList<>();
            for (String id : state.keySet()) {
                if (!desired.containsKey(id)) {
                    changes.add("DESTROY " + id);
                    toRemove.add(id);
                }
            }
            toRemove.forEach(state::remove);

            return changes;
        }
    }

    public static void main(String[] args) {
        TerraformState tfState = new TerraformState();

        Map<String, TerraformResource> config = new LinkedHashMap<>();
        TerraformResource vm = new TerraformResource("aws_instance", "app",
                Map.of("ami", "ami-xyz", "type", "t3.micro"));
        TerraformResource bucket = new TerraformResource("aws_s3_bucket", "data",
                Map.of("bucket", "my-data-bucket", "versioning", "true"));
        config.put(vm.id(), vm);
        config.put(bucket.id(), bucket);

        System.out.println("=== Primera ejecución de apply ===");
        List<String> changes1 = tfState.apply(config);
        if (changes1.isEmpty()) {
            System.out.println("No changes. Infrastructure is up-to-date.");
        } else {
            changes1.forEach(c -> System.out.println("  " + c));
            System.out.println("Apply complete: " + changes1.size() + " change(s).");
        }

        System.out.println();
        System.out.println("=== Segunda ejecución de apply (misma config) ===");
        List<String> changes2 = tfState.apply(config);
        if (changes2.isEmpty()) {
            System.out.println("No changes. Infrastructure is up-to-date.");
            System.out.println("IDEMPOTENCY VERIFIED: second apply produced zero changes.");
        } else {
            changes2.forEach(c -> System.out.println("  " + c));
            System.out.println("WARNING: unexpected changes on second apply!");
        }

        System.out.println();
        System.out.println("=== Tercera ejecución con cambio real ===");
        // Modify one resource to prove changes DO show up when config differs
        Map<String, TerraformResource> configV2 = new LinkedHashMap<>(config);
        TerraformResource vmV2 = new TerraformResource("aws_instance", "app",
                Map.of("ami", "ami-xyz", "type", "t3.medium")); // type changed
        configV2.put(vmV2.id(), vmV2);

        List<String> changes3 = tfState.apply(configV2);
        if (changes3.isEmpty()) {
            System.out.println("No changes.");
        } else {
            changes3.forEach(c -> System.out.println("  " + c));
            System.out.println("Apply complete: " + changes3.size() + " change(s).");
        }
    }
}
