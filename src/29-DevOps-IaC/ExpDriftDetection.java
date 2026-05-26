import java.util.*;

public class ExpDriftDetection {

    static class Resource {
        private final String address;
        private final Map<String, String> attrs;

        Resource(String address, Map<String, String> attrs) {
            this.address = address;
            this.attrs = new LinkedHashMap<>(attrs);
        }

        String getAddress()           { return address; }
        Map<String, String> getAttrs(){ return attrs; }

        @Override public String toString() { return address + " " + attrs; }
    }

    // The IaC definition: what we declared in .tf files
    static class DesiredState {
        private final Map<String, Resource> resources = new LinkedHashMap<>();

        void define(Resource r) { resources.put(r.getAddress(), r); }
        Map<String, Resource> all() { return resources; }
    }

    // What is actually running (can diverge via manual changes)
    static class ActualState {
        private final Map<String, Resource> resources = new LinkedHashMap<>();

        void set(Resource r) { resources.put(r.getAddress(), r); }

        // Simulates a manual change made outside IaC (e.g., via AWS Console)
        void manualChange(String address, String key, String newValue) {
            Resource r = resources.get(address);
            if (r != null) {
                r.getAttrs().put(key, newValue);
                System.out.printf("[MANUAL CHANGE] %s.%s → '%s' (outside IaC!)%n",
                        address, key, newValue);
            }
        }

        Map<String, Resource> all() { return resources; }
    }

    static class DriftEntry {
        final String address;
        final String key;
        final String desired;
        final String actual;

        DriftEntry(String address, String key, String desired, String actual) {
            this.address = address;
            this.key = key;
            this.desired = desired;
            this.actual = actual;
        }
    }

    static class DriftDetector {
        static List<DriftEntry> compare(DesiredState desired, ActualState actual) {
            List<DriftEntry> drifts = new ArrayList<>();
            for (Map.Entry<String, Resource> e : desired.all().entrySet()) {
                String addr = e.getKey();
                Resource want = e.getValue();
                Resource have = actual.all().get(addr);

                if (have == null) {
                    // Resource declared but not deployed
                    drifts.add(new DriftEntry(addr, "<exists>", "true", "false"));
                    continue;
                }

                for (Map.Entry<String, String> attr : want.getAttrs().entrySet()) {
                    String k = attr.getKey();
                    String wantVal = attr.getValue();
                    String haveVal = have.getAttrs().get(k);
                    if (!wantVal.equals(haveVal)) {
                        drifts.add(new DriftEntry(addr, k, wantVal, haveVal));
                    }
                }
            }
            return drifts;
        }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(60));
        System.out.println("  IaC DRIFT DETECTION — simulación");
        System.out.println("═".repeat(60));

        // ── Definir estado deseado (IaC) ─────────────────────────
        DesiredState iac = new DesiredState();
        iac.define(new Resource("aws_instance.api",
                Map.of("type", "t3.micro", "ami", "ami-abc123", "sg", "sg-web")));
        iac.define(new Resource("aws_s3_bucket.assets",
                Map.of("versioning", "enabled", "acl", "private")));
        iac.define(new Resource("aws_rds.main",
                Map.of("instance_class", "db.t3.small", "multi_az", "false")));

        // ── Estado inicial en producción: igual al IaC ────────────
        ActualState prod = new ActualState();
        prod.set(new Resource("aws_instance.api",
                Map.of("type", "t3.micro", "ami", "ami-abc123", "sg", "sg-web")));
        prod.set(new Resource("aws_s3_bucket.assets",
                Map.of("versioning", "enabled", "acl", "private")));
        prod.set(new Resource("aws_rds.main",
                Map.of("instance_class", "db.t3.small", "multi_az", "false")));

        System.out.println("\n[Verificación inicial] Estado coincide con IaC");
        System.out.println("─".repeat(60));
        List<DriftEntry> drifts1 = DriftDetector.compare(iac, prod);
        if (drifts1.isEmpty()) System.out.println("  Sin drift detectado.");

        // ── Cambios manuales realizados fuera del IaC ─────────────
        System.out.println("\n[Cambios manuales en producción (fuera de IaC)]");
        System.out.println("─".repeat(60));
        prod.manualChange("aws_instance.api",  "type",       "t3.large");  // upsize
        prod.manualChange("aws_rds.main",      "multi_az",   "true");      // failover activado
        prod.manualChange("aws_s3_bucket.assets", "acl",     "public-read"); // peligroso

        // ── Detectar drift ────────────────────────────────────────
        System.out.println("\n[Drift Detection]");
        System.out.println("─".repeat(60));
        List<DriftEntry> drifts2 = DriftDetector.compare(iac, prod);
        if (drifts2.isEmpty()) {
            System.out.println("  Sin drift.");
        } else {
            System.out.printf("  %d atributo(s) en drift:%n", drifts2.size());
            for (DriftEntry d : drifts2) {
                System.out.printf("  ~ %-30s  %-15s  IaC='%s'  actual='%s'%n",
                        d.address, d.key, d.desired, d.actual);
            }
        }

        System.out.println("\n[Plan de remediación]");
        System.out.println("─".repeat(60));
        for (DriftEntry d : drifts2) {
            System.out.printf("  terraform apply → restaurar %s.%s a '%s'%n",
                    d.address, d.key, d.desired);
        }

        System.out.println("\n── Conclusión ──");
        System.out.println("  El drift ocurre cuando el estado real diverge del IaC.");
        System.out.println("  terraform plan lo detecta; terraform apply lo remedia.");
    }
}
