import java.util.*;

public class Ejercicio1 {

    static class TerraformResource {
        final String type;
        final String name;
        final Map<String, String> config;
        final List<String> dependsOn;

        TerraformResource(String type, String name, Map<String, String> config, List<String> dependsOn) {
            this.type = type;
            this.name = name;
            this.config = config;
            this.dependsOn = dependsOn;
        }

        String id() { return type + "." + name; }
    }

    static class CircularDependencyException extends RuntimeException {
        CircularDependencyException(String msg) { super(msg); }
    }

    static List<TerraformResource> topologicalSort(List<TerraformResource> resources) {
        Map<String, TerraformResource> byId = new LinkedHashMap<>();
        for (TerraformResource r : resources) byId.put(r.id(), r);

        List<TerraformResource> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> inStack = new HashSet<>();

        for (TerraformResource r : resources) {
            dfs(r, byId, visited, inStack, sorted);
        }
        return sorted;
    }

    static void dfs(TerraformResource r, Map<String, TerraformResource> byId,
                    Set<String> visited, Set<String> inStack, List<TerraformResource> sorted) {
        if (inStack.contains(r.id()))
            throw new CircularDependencyException("Dependencia circular detectada en: " + r.id());
        if (visited.contains(r.id())) return;

        inStack.add(r.id());
        for (String dep : r.dependsOn) {
            TerraformResource depResource = byId.get(dep);
            if (depResource != null) dfs(depResource, byId, visited, inStack, sorted);
        }
        inStack.remove(r.id());
        visited.add(r.id());
        sorted.add(r);
    }

    public static void main(String[] args) {
        List<TerraformResource> resources = List.of(
            new TerraformResource("aws_instance", "app_server",
                Map.of("ami", "ami-12345", "subnet_id", "${aws_subnet.main.id}"),
                List.of("aws_subnet.main", "aws_security_group.app")),
            new TerraformResource("aws_security_group", "app",
                Map.of("vpc_id", "${aws_vpc.main.id}"),
                List.of("aws_vpc.main")),
            new TerraformResource("aws_subnet", "main",
                Map.of("vpc_id", "${aws_vpc.main.id}", "cidr", "10.0.1.0/24"),
                List.of("aws_vpc.main")),
            new TerraformResource("aws_vpc", "main",
                Map.of("cidr_block", "10.0.0.0/16"),
                List.of())
        );

        System.out.println("=== Recursos en orden declarado ===");
        resources.forEach(r -> System.out.println("  " + r.id() + " depends_on=" + r.dependsOn));

        System.out.println("\n=== Orden de apply (topológico) ===");
        List<TerraformResource> sorted = topologicalSort(new ArrayList<>(resources));
        for (int i = 0; i < sorted.size(); i++) {
            System.out.println("  " + (i+1) + ". " + sorted.get(i).id());
        }

        System.out.println("\n=== Detección de ciclo ===");
        List<TerraformResource> cyclic = new ArrayList<>();
        cyclic.add(new TerraformResource("res", "a", Map.of(), List.of("res.b")));
        cyclic.add(new TerraformResource("res", "b", Map.of(), List.of("res.a")));
        try {
            topologicalSort(cyclic);
        } catch (CircularDependencyException e) {
            System.out.println("  Capturado: " + e.getMessage());
        }
    }
}
