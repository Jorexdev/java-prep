import java.util.*;

public class ExpIdempotency {

    static class ResourceSpec {
        private final String address;
        private final Map<String, String> attrs;

        ResourceSpec(String address, Map<String, String> attrs) {
            this.address = address;
            this.attrs = new LinkedHashMap<>(attrs);
        }

        String getAddress()           { return address; }
        Map<String, String> getAttrs(){ return attrs; }
    }

    // Tracks provisioned resources (acts as both the provider API and the state store)
    static class ProviderRegistry {
        private final Map<String, Map<String, String>> provisioned = new LinkedHashMap<>();

        boolean exists(String address) {
            return provisioned.containsKey(address);
        }

        void create(String address, Map<String, String> attrs) {
            provisioned.put(address, new LinkedHashMap<>(attrs));
        }

        Map<String, String> get(String address) {
            return provisioned.get(address);
        }

        Map<String, Map<String, String>> all() { return provisioned; }
    }

    // ensure(resource) is safe to call N times: creates only if absent
    static class IdempotentProvisioner {
        private final ProviderRegistry registry;

        IdempotentProvisioner(ProviderRegistry registry) {
            this.registry = registry;
        }

        void ensure(ResourceSpec spec) {
            if (registry.exists(spec.getAddress())) {
                System.out.printf("  [SKIP]   %s — ya existe, sin cambios%n", spec.getAddress());
            } else {
                registry.create(spec.getAddress(), spec.getAttrs());
                System.out.printf("  [CREATE] %s — %s%n", spec.getAddress(), spec.getAttrs());
            }
        }

        void provision(List<ResourceSpec> resources) {
            System.out.println("─".repeat(55));
            for (ResourceSpec r : resources) {
                ensure(r);
            }
        }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(55));
        System.out.println("  IaC IDEMPOTENCY — simulación");
        System.out.println("═".repeat(55));

        ProviderRegistry registry = new ProviderRegistry();
        IdempotentProvisioner provisioner = new IdempotentProvisioner(registry);

        List<ResourceSpec> config = List.of(
                new ResourceSpec("aws_vpc.main",
                        Map.of("cidr", "10.0.0.0/16", "region", "eu-west-1")),
                new ResourceSpec("aws_subnet.pub",
                        Map.of("cidr", "10.0.1.0/24", "az", "eu-west-1a")),
                new ResourceSpec("aws_instance.web",
                        Map.of("type", "t3.micro", "ami", "ami-abc123"))
        );

        // ── Ejecución 1: todos son nuevos → CREATE ────────────────
        System.out.println("\n[Ejecución 1] Primera vez — todo se crea");
        provisioner.provision(config);

        // ── Ejecución 2: misma config → todos SKIP ────────────────
        System.out.println("\n[Ejecución 2] Segunda ejecución (idempotente)");
        provisioner.provision(config);

        // ── Ejecución 3: misma config → todos SKIP ────────────────
        System.out.println("\n[Ejecución 3] Tercera ejecución (idempotente)");
        provisioner.provision(config);

        System.out.println("\n[Recursos provisionados]");
        System.out.println("─".repeat(55));
        registry.all().forEach((addr, attrs) ->
                System.out.printf("  %s → %s%n", addr, attrs));

        System.out.println("\n── Conclusión ──");
        System.out.println("  Idempotencia: ejecutar N veces produce el mismo resultado que 1 vez.");
        System.out.println("  En Terraform: terraform apply es seguro de correr repetidamente.");
    }
}
