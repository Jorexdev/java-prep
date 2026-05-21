import java.util.*;

public class Ejercicio2 {

    record Resource(String id, Map<String, String> config) {}

    static class TerraformPlanDiff {

        record ResourceDiff(String id, String action, Map<String, String> changes) {}

        static List<ResourceDiff> diff(Map<String, Resource> v1, Map<String, Resource> v2) {
            List<ResourceDiff> diffs = new ArrayList<>();

            // Recursos añadidos
            for (String id : v2.keySet()) {
                if (!v1.containsKey(id)) {
                    diffs.add(new ResourceDiff(id, "ADD", v2.get(id).config()));
                }
            }

            // Recursos eliminados
            for (String id : v1.keySet()) {
                if (!v2.containsKey(id)) {
                    diffs.add(new ResourceDiff(id, "DESTROY", v1.get(id).config()));
                }
            }

            // Recursos cambiados
            for (String id : v1.keySet()) {
                if (v2.containsKey(id)) {
                    Map<String, String> c1 = v1.get(id).config();
                    Map<String, String> c2 = v2.get(id).config();
                    Map<String, String> changedAttrs = new LinkedHashMap<>();

                    Set<String> allKeys = new HashSet<>();
                    allKeys.addAll(c1.keySet());
                    allKeys.addAll(c2.keySet());

                    for (String key : allKeys) {
                        String old = c1.getOrDefault(key, "(none)");
                        String nw  = c2.getOrDefault(key, "(none)");
                        if (!old.equals(nw)) {
                            changedAttrs.put(key, old + " → " + nw);
                        }
                    }
                    if (!changedAttrs.isEmpty()) {
                        diffs.add(new ResourceDiff(id, "CHANGE", changedAttrs));
                    }
                }
            }
            return diffs;
        }

        static void print(List<ResourceDiff> diffs) {
            System.out.println("Terraform will perform the following actions:\n");
            int add = 0, change = 0, destroy = 0;

            for (ResourceDiff d : diffs) {
                String symbol = switch (d.action()) {
                    case "ADD"     -> "+";
                    case "DESTROY" -> "-";
                    default        -> "~";
                };
                System.out.println("  " + symbol + " resource \"" + d.id() + "\" {");
                d.changes().forEach((k, v) -> System.out.println("      " + k + " = " + v));
                System.out.println("    }");

                if ("ADD".equals(d.action()))     add++;
                if ("CHANGE".equals(d.action()))  change++;
                if ("DESTROY".equals(d.action())) destroy++;
            }

            System.out.println("\nPlan: " + add + " to add, " + change + " to change, " + destroy + " to destroy.");
        }
    }

    public static void main(String[] args) {
        Map<String, Resource> v1 = Map.of(
            "aws_instance.app",   new Resource("aws_instance.app",   Map.of("type", "t3.medium", "ami", "ami-111")),
            "aws_s3_bucket.logs", new Resource("aws_s3_bucket.logs", Map.of("versioning", "true")),
            "aws_rds.db",         new Resource("aws_rds.db",         Map.of("engine", "postgres", "version", "13"))
        );

        Map<String, Resource> v2 = Map.of(
            "aws_instance.app",       new Resource("aws_instance.app",       Map.of("type", "t3.large", "ami", "ami-222")), // changed
            "aws_rds.db",             new Resource("aws_rds.db",             Map.of("engine", "postgres", "version", "14")), // version changed
            "aws_elasticache.cache",  new Resource("aws_elasticache.cache",  Map.of("node_type", "cache.t3.micro")) // new
            // aws_s3_bucket.logs eliminado
        );

        System.out.println("=== Terraform Plan Diff: v1 → v2 ===\n");
        List<TerraformPlanDiff.ResourceDiff> diffs = TerraformPlanDiff.diff(v1, v2);
        TerraformPlanDiff.print(diffs);
    }
}
