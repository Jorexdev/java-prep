import java.util.*;

public class Ejercicio3 {

    record Resource(String id, Map<String, String> config) {}

    static class TerraformState {
        private final Map<String, Resource> state = new HashMap<>();

        void apply(List<Resource> desired) {
            for (Resource r : desired) state.put(r.id(), r);
        }

        Map<String, Resource> snapshot() { return Collections.unmodifiableMap(state); }
    }

    // Simula el estado real en la nube (puede diferir del state file)
    static class CloudProvider {
        private final Map<String, Resource> cloud = new HashMap<>();

        void seed(List<Resource> resources) {
            for (Resource r : resources) cloud.put(r.id(), r);
        }

        void modifyExternally(String id, Map<String, String> newConfig) {
            cloud.put(id, new Resource(id, new HashMap<>(newConfig)));
        }

        Map<String, Resource> currentState() { return Collections.unmodifiableMap(cloud); }
    }

    static void detectDrift(TerraformState state, CloudProvider cloud) {
        System.out.println("=== Drift Detection ===\n");
        boolean driftFound = false;

        for (Map.Entry<String, Resource> entry : state.snapshot().entrySet()) {
            String id = entry.getKey();
            Resource stateResource = entry.getValue();
            Resource cloudResource = cloud.currentState().get(id);

            if (cloudResource == null) {
                System.out.println("  DRIFT [DELETED]: " + id + " existe en el state pero no en la nube");
                driftFound = true;
                continue;
            }

            for (Map.Entry<String, String> attr : stateResource.config().entrySet()) {
                String cloudVal = cloudResource.config().get(attr.getKey());
                if (!attr.getValue().equals(cloudVal)) {
                    System.out.println("  DRIFT [CHANGED]: " + id + " — atributo '" + attr.getKey()
                        + "' state='" + attr.getValue() + "' cloud='" + cloudVal + "'");
                    driftFound = true;
                }
            }
        }

        if (!driftFound) System.out.println("  No drift detectado. Infraestructura en sync.");

        System.out.println("\n  → Remediación: ejecutar `terraform apply` para restaurar el estado deseado.");
    }

    public static void main(String[] args) {
        TerraformState tfState = new TerraformState();
        CloudProvider cloud = new CloudProvider();

        List<Resource> initial = List.of(
            new Resource("aws_instance.app",    Map.of("type", "t3.medium", "region", "eu-west-1")),
            new Resource("aws_s3_bucket.logs",  Map.of("versioning", "true", "region", "eu-west-1")),
            new Resource("aws_rds.db",          Map.of("engine", "postgres", "version", "14"))
        );

        tfState.apply(initial);
        cloud.seed(initial);

        System.out.println("Estado inicial sincronizado.\n");
        detectDrift(tfState, cloud);

        // Alguien modifica manualmente la instancia en la nube
        cloud.modifyExternally("aws_instance.app",
            Map.of("type", "t3.large", "region", "eu-west-1")); // cambia el tipo!

        // Alguien borra el bucket desde la consola
        // (no añadimos aws_s3_bucket.logs en la "cloud" simulada)
        Map<String, Resource> cloudState = new HashMap<>(cloud.currentState());
        cloudState.remove("aws_s3_bucket.logs");

        System.out.println("\nTras modificaciones externas:\n");
        // Usamos un cloud provider actualizado
        CloudProvider modifiedCloud = new CloudProvider();
        modifiedCloud.seed(new ArrayList<>(cloudState.values()));
        detectDrift(tfState, modifiedCloud);
    }
}
