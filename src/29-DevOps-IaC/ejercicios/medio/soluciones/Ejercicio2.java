import java.util.*;

public class Ejercicio2 {

    record TerraformModule(String name, List<String> resources,
                           Map<String, String> inputVars,
                           Map<String, String> outputVars) {}

    static class ModuleComposer {
        private final List<TerraformModule> modules = new ArrayList<>();

        void addModule(TerraformModule m) { modules.add(m); }

        // Resuelve outputs de un módulo como inputs de otro
        Map<String, String> resolveInputs(TerraformModule consumer) {
            Map<String, String> resolved = new HashMap<>(consumer.inputVars());
            for (Map.Entry<String, String> entry : consumer.inputVars().entrySet()) {
                String value = entry.getValue();
                if (value.startsWith("module.")) {
                    // format: module.<name>.<output>
                    String[] parts = value.split("\\.");
                    if (parts.length >= 3) {
                        String modName = parts[1];
                        String outKey  = parts[2];
                        modules.stream()
                            .filter(m -> m.name().equals(modName))
                            .findFirst()
                            .ifPresent(src -> {
                                String resolvedValue = src.outputVars().get(outKey);
                                if (resolvedValue != null)
                                    resolved.put(entry.getKey(), resolvedValue);
                            });
                    }
                }
            }
            return resolved;
        }

        void apply() {
            System.out.println("=== Terraform Module Apply ===\n");
            for (TerraformModule m : modules) {
                Map<String, String> resolved = resolveInputs(m);
                System.out.println("Module: " + m.name());
                resolved.forEach((k, v) -> System.out.println("  input." + k + " = " + v));
                m.resources().forEach(r -> System.out.println("  resource: " + r));
                m.outputVars().forEach((k, v) -> System.out.println("  output." + k + " = " + v));
                System.out.println();
            }
        }
    }

    public static void main(String[] args) {
        TerraformModule networking = new TerraformModule(
            "networking",
            List.of("aws_vpc.main", "aws_subnet.public", "aws_subnet.private"),
            Map.of("region", "eu-west-1", "cidr", "10.0.0.0/16"),
            Map.of("vpc_id", "vpc-abc123", "subnet_id", "subnet-def456")
        );

        TerraformModule compute = new TerraformModule(
            "compute",
            List.of("aws_instance.app", "aws_autoscaling_group.app"),
            Map.of(
                "vpc_id",    "module.networking.vpc_id",    // ← referencia a output de networking
                "subnet_id", "module.networking.subnet_id", // ← referencia a output de networking
                "instance_type", "t3.medium"
            ),
            Map.of("app_url", "http://app.example.com")
        );

        ModuleComposer composer = new ModuleComposer();
        composer.addModule(networking);
        composer.addModule(compute);
        composer.apply();

        System.out.println("Los inputs de 'compute' que referenciaban 'module.networking.*'");
        System.out.println("fueron resueltos con los outputs reales del módulo networking.");
    }
}
