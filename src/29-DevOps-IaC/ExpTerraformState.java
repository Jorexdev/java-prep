import java.util.*;

public class ExpTerraformState {

    // Attributes of a provisioned resource (key→value pairs)
    static class ResourceAttrs {
        private final Map<String, String> attrs;

        ResourceAttrs(Map<String, String> attrs) {
            this.attrs = new LinkedHashMap<>(attrs);
        }

        Map<String, String> getAttrs() { return attrs; }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ResourceAttrs r)) return false;
            return attrs.equals(r.attrs);
        }

        @Override public int hashCode() { return attrs.hashCode(); }
        @Override public String toString() { return attrs.toString(); }
    }

    // terraform.tfstate: address → current attributes of the resource
    static class TfState {
        private final Map<String, ResourceAttrs> resources = new LinkedHashMap<>();

        void put(String address, ResourceAttrs attrs) { resources.put(address, attrs); }
        void remove(String address)                   { resources.remove(address); }
        boolean has(String address)                   { return resources.containsKey(address); }
        ResourceAttrs get(String address)             { return resources.get(address); }
        Map<String, ResourceAttrs> all()              { return Collections.unmodifiableMap(resources); }
    }

    enum PlanAction { ADD, CHANGE, DESTROY, NO_CHANGE }

    static class PlanEntry {
        final String address;
        final PlanAction action;
        final ResourceAttrs before;
        final ResourceAttrs after;

        PlanEntry(String address, PlanAction action, ResourceAttrs before, ResourceAttrs after) {
            this.address = address;
            this.action = action;
            this.before = before;
            this.after = after;
        }
    }

    // Diff desired config against current state
    static class TfPlan {
        static List<PlanEntry> compute(Map<String, ResourceAttrs> desired, TfState state) {
            List<PlanEntry> plan = new ArrayList<>();

            for (Map.Entry<String, ResourceAttrs> e : desired.entrySet()) {
                String addr = e.getKey();
                ResourceAttrs want = e.getValue();
                if (!state.has(addr)) {
                    plan.add(new PlanEntry(addr, PlanAction.ADD, null, want));
                } else if (!state.get(addr).equals(want)) {
                    plan.add(new PlanEntry(addr, PlanAction.CHANGE, state.get(addr), want));
                } else {
                    plan.add(new PlanEntry(addr, PlanAction.NO_CHANGE, want, want));
                }
            }

            // Resources in state but not in desired config → destroy
            for (String addr : state.all().keySet()) {
                if (!desired.containsKey(addr)) {
                    plan.add(new PlanEntry(addr, PlanAction.DESTROY, state.get(addr), null));
                }
            }
            return plan;
        }

        static void print(List<PlanEntry> plan) {
            System.out.println("  Plan:");
            for (PlanEntry e : plan) {
                switch (e.action) {
                    case ADD      -> System.out.printf("    + %s  %s%n", e.address, e.after);
                    case CHANGE   -> System.out.printf("    ~ %s  %s → %s%n", e.address, e.before, e.after);
                    case DESTROY  -> System.out.printf("    - %s  %s%n", e.address, e.before);
                    case NO_CHANGE-> System.out.printf("    = %s  (no changes)%n", e.address);
                }
            }
        }
    }

    // Execute the plan and update state
    static class TfApply {
        static void apply(List<PlanEntry> plan, TfState state) {
            for (PlanEntry e : plan) {
                switch (e.action) {
                    case ADD     -> { state.put(e.address, e.after);  System.out.printf("  [APPLY] Created  %s%n", e.address); }
                    case CHANGE  -> { state.put(e.address, e.after);  System.out.printf("  [APPLY] Updated  %s%n", e.address); }
                    case DESTROY -> { state.remove(e.address);        System.out.printf("  [APPLY] Destroyed %s%n", e.address); }
                    case NO_CHANGE -> {}
                }
            }
        }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(60));
        System.out.println("  TERRAFORM STATE MANAGEMENT — simulación");
        System.out.println("═".repeat(60));

        TfState state = new TfState();

        // ── Apply 1: crear 3 recursos ─────────────────────────────
        System.out.println("\n[Apply 1] Crear 3 recursos");
        System.out.println("─".repeat(60));
        Map<String, ResourceAttrs> desired1 = new LinkedHashMap<>();
        desired1.put("aws_vpc.main",    new ResourceAttrs(Map.of("cidr", "10.0.0.0/16", "region", "eu-west-1")));
        desired1.put("aws_subnet.pub",  new ResourceAttrs(Map.of("cidr", "10.0.1.0/24", "az", "eu-west-1a")));
        desired1.put("aws_instance.web",new ResourceAttrs(Map.of("type", "t3.micro",   "ami", "ami-abc123")));

        List<PlanEntry> plan1 = TfPlan.compute(desired1, state);
        TfPlan.print(plan1);
        TfApply.apply(plan1, state);

        // ── Apply 2: cambiar tipo de instancia ───────────────────
        System.out.println("\n[Apply 2] Cambiar tipo de instancia");
        System.out.println("─".repeat(60));
        Map<String, ResourceAttrs> desired2 = new LinkedHashMap<>(desired1);
        desired2.put("aws_instance.web", new ResourceAttrs(Map.of("type", "t3.small", "ami", "ami-abc123")));

        List<PlanEntry> plan2 = TfPlan.compute(desired2, state);
        TfPlan.print(plan2);
        TfApply.apply(plan2, state);

        // ── Apply 3: destruir subnet ──────────────────────────────
        System.out.println("\n[Apply 3] Eliminar subnet del config");
        System.out.println("─".repeat(60));
        Map<String, ResourceAttrs> desired3 = new LinkedHashMap<>(desired2);
        desired3.remove("aws_subnet.pub");

        List<PlanEntry> plan3 = TfPlan.compute(desired3, state);
        TfPlan.print(plan3);
        TfApply.apply(plan3, state);

        System.out.println("\n[Estado final]");
        state.all().forEach((addr, attrs) -> System.out.printf("  %s → %s%n", addr, attrs));
    }
}
