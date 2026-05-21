import java.util.*;

public class Ejercicio4 {

    enum ActionType { CREATE, UPDATE, DELETE }

    static class Action {
        ActionType type;
        String resourceName;
        String detail;

        Action(ActionType type, String resourceName, String detail) {
            this.type         = type;
            this.resourceName = resourceName;
            this.detail       = detail;
        }

        @Override
        public String toString() {
            String symbol = switch (type) {
                case CREATE -> "+ CREATE";
                case UPDATE -> "~ UPDATE";
                case DELETE -> "- DELETE";
            };
            return String.format("  %s %-20s  # %s", symbol, resourceName, detail);
        }
    }

    static class CustomResource {
        String kind;
        String name;
        Map<String, String> spec;

        CustomResource(String kind, String name, Map<String, String> spec) {
            this.kind = kind;
            this.name = name;
            this.spec = new LinkedHashMap<>(spec);
        }

        String id() { return kind + "/" + name; }
    }

    static class Reconciler {
        // actual: id → CustomResource
        Map<String, CustomResource> actual = new LinkedHashMap<>();

        List<Action> reconcile(List<CustomResource> desired) {
            List<Action> actions = new ArrayList<>();
            Map<String, CustomResource> desiredMap = new LinkedHashMap<>();
            for (CustomResource cr : desired) desiredMap.put(cr.id(), cr);

            // CREATE o UPDATE
            for (Map.Entry<String, CustomResource> e : desiredMap.entrySet()) {
                String id = e.getKey();
                CustomResource desiredCr = e.getValue();

                if (!actual.containsKey(id)) {
                    actions.add(new Action(ActionType.CREATE, id,
                            "nuevo recurso spec=" + desiredCr.spec));
                } else {
                    CustomResource actualCr = actual.get(id);
                    if (!actualCr.spec.equals(desiredCr.spec)) {
                        actions.add(new Action(ActionType.UPDATE, id,
                                "spec cambió de " + actualCr.spec + " a " + desiredCr.spec));
                    }
                }
            }

            // DELETE: recursos en actual que no están en desired
            for (String id : actual.keySet()) {
                if (!desiredMap.containsKey(id)) {
                    actions.add(new Action(ActionType.DELETE, id, "ya no está en estado deseado"));
                }
            }

            // Aplicar acciones → actual = desired
            actual.clear();
            actual.putAll(desiredMap);

            return actions;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Kubernetes Operator Reconciler ===\n");

        Reconciler reconciler = new Reconciler();

        // Ciclo 1: crear 3 recursos
        List<CustomResource> desired1 = List.of(
                new CustomResource("Deployment", "web",    Map.of("replicas", "2", "image", "web:v1")),
                new CustomResource("Service",    "web-svc",Map.of("port", "80")),
                new CustomResource("ConfigMap",  "app-cfg",Map.of("env", "prod"))
        );
        System.out.println("=== Ciclo 1 (estado inicial vacío) ===");
        reconciler.reconcile(desired1).forEach(System.out::println);

        // Ciclo 2: modificar web, añadir db, eliminar app-cfg
        List<CustomResource> desired2 = List.of(
                new CustomResource("Deployment", "web",    Map.of("replicas", "4", "image", "web:v2")),
                new CustomResource("Service",    "web-svc",Map.of("port", "80")),
                new CustomResource("Deployment", "db",     Map.of("replicas", "1", "image", "postgres:15"))
        );
        System.out.println("\n=== Ciclo 2 (update web, add db, delete app-cfg) ===");
        reconciler.reconcile(desired2).forEach(System.out::println);

        // Ciclo 3: sin cambios
        List<CustomResource> desired3 = new ArrayList<>(desired2);
        System.out.println("\n=== Ciclo 3 (sin cambios) ===");
        List<Action> actions3 = reconciler.reconcile(desired3);
        if (actions3.isEmpty()) {
            System.out.println("  (ninguna acción necesaria — estado convergido)");
        } else {
            actions3.forEach(System.out::println);
        }

        System.out.println("\n=== Estado actual final ===");
        reconciler.actual.values().forEach(cr ->
                System.out.printf("  %-25s spec=%s%n", cr.id(), cr.spec));
    }
}
