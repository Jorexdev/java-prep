import java.util.*;

public class Ejercicio3 {

    enum ActionType { APPLY, DELETE }

    static class GitOpsAction {
        ActionType type;
        String resource;
        String spec;

        GitOpsAction(ActionType type, String resource, String spec) {
            this.type     = type;
            this.resource = resource;
            this.spec     = spec;
        }

        @Override
        public String toString() {
            String prefix = type == ActionType.APPLY ? "+ APPLY  " : "- DELETE ";
            return prefix + resource + (spec != null ? " → " + spec : "");
        }
    }

    static class GitOpsController {
        Map<String, String> desired  = new LinkedHashMap<>();
        Map<String, String> deployed = new LinkedHashMap<>();
        int cycle = 0;

        void setDesired(Map<String, String> d) {
            desired = new LinkedHashMap<>(d);
        }

        List<GitOpsAction> reconcile() {
            cycle++;
            System.out.printf("=== Ciclo %d de reconciliación ===%n", cycle);

            List<GitOpsAction> actions = new ArrayList<>();

            // APPLY: recursos en desired pero no en deployed, o con spec diferente
            for (Map.Entry<String, String> e : desired.entrySet()) {
                String resource = e.getKey();
                String spec     = e.getValue();
                if (!deployed.containsKey(resource)) {
                    actions.add(new GitOpsAction(ActionType.APPLY, resource, spec + " [CREATE]"));
                } else if (!deployed.get(resource).equals(spec)) {
                    actions.add(new GitOpsAction(ActionType.APPLY, resource, spec + " [UPDATE]"));
                }
            }

            // DELETE: recursos en deployed pero no en desired
            for (String resource : deployed.keySet()) {
                if (!desired.containsKey(resource)) {
                    actions.add(new GitOpsAction(ActionType.DELETE, resource, null));
                }
            }

            if (actions.isEmpty()) {
                System.out.println("  (sin acciones — estado convergido)");
            } else {
                actions.forEach(a -> System.out.println("  " + a));
                // Aplicar
                for (GitOpsAction action : actions) {
                    if (action.type == ActionType.APPLY) {
                        deployed.put(action.resource, desired.get(action.resource));
                    } else {
                        deployed.remove(action.resource);
                    }
                }
            }
            System.out.printf("  Deployed tras reconcile: %s%n%n", deployed);
            return actions;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== GitOps Reconciliation Demo ===\n");

        GitOpsController ctrl = new GitOpsController();

        // Ciclo 1: estado inicial vacío, git tiene 3 recursos
        Map<String, String> state1 = new LinkedHashMap<>();
        state1.put("Deployment/web",    "replicas=2,image=web:v1");
        state1.put("Service/web-svc",   "port=80");
        state1.put("ConfigMap/app-cfg", "env=prod");
        ctrl.setDesired(state1);
        ctrl.reconcile();

        // Ciclo 2: se modifica web, se añade db, se elimina app-cfg
        Map<String, String> state2 = new LinkedHashMap<>();
        state2.put("Deployment/web",    "replicas=4,image=web:v2");
        state2.put("Service/web-svc",   "port=80");
        state2.put("Deployment/db",     "replicas=1,image=postgres:15");
        ctrl.setDesired(state2);
        ctrl.reconcile();

        // Ciclo 3: sin cambios
        ctrl.setDesired(state2);
        ctrl.reconcile();

        // Ciclo 4: eliminar todo
        ctrl.setDesired(new LinkedHashMap<>());
        ctrl.reconcile();
    }
}
