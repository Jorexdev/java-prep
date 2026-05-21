import java.util.*;

public class Ejercicio3 {

    record Resource(String id, Map<String, String> config) {}

    static class TerraformImport {
        private final Map<String, Resource> stateFile = new HashMap<>();
        private final Map<String, Resource> cloudSimulation;

        TerraformImport(Map<String, Resource> cloudSimulation) {
            this.cloudSimulation = new HashMap<>(cloudSimulation);
        }

        // terraform import <resource_id>
        void importResource(String resourceId) {
            Resource cloudResource = cloudSimulation.get(resourceId);
            if (cloudResource == null) {
                System.out.println("  ERROR: " + resourceId + " no existe en la nube.");
                return;
            }
            if (stateFile.containsKey(resourceId)) {
                System.out.println("  WARN: " + resourceId + " ya está en el state file.");
                return;
            }
            stateFile.put(resourceId, cloudResource);
            System.out.println("  IMPORTED: " + resourceId + " → state file actualizado");
            cloudResource.config().forEach((k, v) -> System.out.println("    " + k + " = " + v));
        }

        // Reconciliar con la config deseada
        void reconcile(Map<String, Resource> desired) {
            System.out.println("\n  === Reconciliación con configuración deseada ===");
            for (Map.Entry<String, Resource> entry : desired.entrySet()) {
                String id = entry.getKey();
                Resource desiredResource = entry.getValue();
                Resource stateResource = stateFile.get(id);

                if (stateResource == null) {
                    System.out.println("  PENDING: " + id + " no importado, se creará en el próximo apply");
                    continue;
                }

                Map<String, String> diffs = new LinkedHashMap<>();
                desiredResource.config().forEach((k, v) -> {
                    String stateVal = stateResource.config().get(k);
                    if (!v.equals(stateVal)) diffs.put(k, stateVal + " → " + v);
                });

                if (diffs.isEmpty()) {
                    System.out.println("  IN SYNC:  " + id);
                } else {
                    System.out.println("  DRIFT:    " + id + " — se modificará en el próximo apply:");
                    diffs.forEach((k, v) -> System.out.println("    " + k + ": " + v));
                }
            }
        }
    }

    public static void main(String[] args) {
        // Recursos que ya existen en la nube pero NO están en el state file
        Map<String, Resource> cloud = Map.of(
            "aws_vpc.legacy",      new Resource("aws_vpc.legacy",      Map.of("cidr", "192.168.0.0/16", "tags", "legacy")),
            "aws_instance.old_app",new Resource("aws_instance.old_app",Map.of("type", "t2.micro", "ami", "ami-old"))
        );

        TerraformImport tf = new TerraformImport(cloud);

        System.out.println("=== terraform import ===\n");
        tf.importResource("aws_vpc.legacy");
        tf.importResource("aws_instance.old_app");
        tf.importResource("aws_rds.nonexistent"); // no existe

        // Config deseada puede diferir del estado actual en la nube
        Map<String, Resource> desired = Map.of(
            "aws_vpc.legacy",       new Resource("aws_vpc.legacy",       Map.of("cidr", "192.168.0.0/16", "tags", "migrated")), // tags changed
            "aws_instance.old_app", new Resource("aws_instance.old_app", Map.of("type", "t3.medium", "ami", "ami-new")),         // upgrade
            "aws_instance.new_app", new Resource("aws_instance.new_app", Map.of("type", "t3.small", "ami", "ami-new"))           // nuevo
        );

        tf.reconcile(desired);
    }
}
