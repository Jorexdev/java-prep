import java.util.*;

public class Ejercicio1 {

    enum Action { ADD, CHANGE, DESTROY }

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
    }

    static class PlanAction {
        Action action;
        String resourceId;
        String detail;

        PlanAction(Action action, String resourceId, String detail) {
            this.action = action;
            this.resourceId = resourceId;
            this.detail = detail;
        }

        @Override
        public String toString() {
            String symbol = switch (action) {
                case ADD     -> "+ ";
                case CHANGE  -> "~ ";
                case DESTROY -> "- ";
            };
            return symbol + resourceId + (detail.isEmpty() ? "" : "  # " + detail);
        }
    }

    static List<PlanAction> plan(
            Map<String, TerraformResource> current,
            Map<String, TerraformResource> desired) {

        List<PlanAction> actions = new ArrayList<>();

        // Resources to ADD or CHANGE
        for (TerraformResource d : desired.values()) {
            TerraformResource c = current.get(d.id());
            if (c == null) {
                actions.add(new PlanAction(Action.ADD, d.id(), "nuevo recurso"));
            } else if (!c.config.equals(d.config)) {
                List<String> diffs = new ArrayList<>();
                for (String key : d.config.keySet()) {
                    String cv = c.config.getOrDefault(key, "<none>");
                    String dv = d.config.get(key);
                    if (!cv.equals(dv)) diffs.add(key + ": " + cv + " -> " + dv);
                }
                actions.add(new PlanAction(Action.CHANGE, d.id(), String.join(", ", diffs)));
            }
        }

        // Resources to DESTROY
        for (String id : current.keySet()) {
            if (!desired.containsKey(id)) {
                actions.add(new PlanAction(Action.DESTROY, id, "ya no existe en config"));
            }
        }

        return actions;
    }

    public static void main(String[] args) {
        // Estado actual (lo que está desplegado)
        Map<String, TerraformResource> current = new LinkedHashMap<>();
        TerraformResource ec2Old = new TerraformResource("aws_instance", "web",
                Map.of("ami", "ami-0001", "type", "t2.micro"));
        TerraformResource rds = new TerraformResource("aws_db_instance", "db",
                Map.of("engine", "mysql", "size", "db.t2.small"));
        current.put(ec2Old.id(), ec2Old);
        current.put(rds.id(), rds);

        // Estado deseado (lo que hay en los .tf)
        Map<String, TerraformResource> desired = new LinkedHashMap<>();
        // Recurso existente con cambio de tipo
        TerraformResource ec2New = new TerraformResource("aws_instance", "web",
                Map.of("ami", "ami-0001", "type", "t3.medium"));
        // Recurso nuevo
        TerraformResource s3 = new TerraformResource("aws_s3_bucket", "static",
                Map.of("bucket", "my-static-assets", "acl", "private"));
        desired.put(ec2New.id(), ec2New);
        desired.put(s3.id(), s3);
        // aws_db_instance.db no aparece -> DESTROY

        System.out.println("Terraform Plan");
        System.out.println("==============");
        System.out.println();

        List<PlanAction> actions = plan(current, desired);
        for (PlanAction a : actions) {
            System.out.println("  " + a);
        }

        long adds     = actions.stream().filter(a -> a.action == Action.ADD).count();
        long changes  = actions.stream().filter(a -> a.action == Action.CHANGE).count();
        long destroys = actions.stream().filter(a -> a.action == Action.DESTROY).count();

        System.out.println();
        System.out.printf("Plan: %d to add, %d to change, %d to destroy.%n",
                adds, changes, destroys);
    }
}
