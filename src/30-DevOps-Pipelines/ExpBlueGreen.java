import java.util.*;

public class ExpBlueGreen {

    static class Environment {
        private final String name;
        private String version;
        private boolean live;

        Environment(String name, String version, boolean live) {
            this.name = name;
            this.version = version;
            this.live = live;
        }

        void deploy(String newVersion) {
            System.out.printf("  [DEPLOY] %s ← version %s%n", name, newVersion);
            this.version = newVersion;
        }

        // Simulated smoke test: always passes unless version contains "broken"
        boolean smokeTest() {
            boolean ok = !version.contains("broken");
            System.out.printf("  [SMOKE TEST] %s (version=%s) → %s%n",
                    name, version, ok ? "PASS" : "FAIL");
            return ok;
        }

        String getName()    { return name; }
        String getVersion() { return version; }
        boolean isLive()    { return live; }
        void setLive(boolean live) { this.live = live; }
    }

    static class LoadBalancer {
        private Environment active;
        private Environment standby;

        LoadBalancer(Environment blue, Environment green) {
            if (blue.isLive()) { active = blue;  standby = green; }
            else               { active = green; standby = blue;  }
        }

        // One atomic switch: old active → standby, new active gets traffic
        void switchTraffic() {
            active.setLive(false);
            standby.setLive(true);
            Environment tmp = active;
            active  = standby;
            standby = tmp;
            System.out.printf("  [LB SWITCH] Tráfico → %s (version=%s)  |  standby: %s (version=%s)%n",
                    active.getName(), active.getVersion(),
                    standby.getName(), standby.getVersion());
        }

        void rollback() {
            System.out.println("  [ROLLBACK] Revirtiendo al entorno anterior...");
            switchTraffic();
        }

        void status() {
            System.out.printf("  LB → active=%-5s (v=%s)  standby=%-5s (v=%s)%n",
                    active.getName(), active.getVersion(),
                    standby.getName(), standby.getVersion());
        }

        Environment getActive()  { return active; }
        Environment getStandby() { return standby; }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(60));
        System.out.println("  BLUE-GREEN DEPLOYMENT — simulación");
        System.out.println("═".repeat(60));

        Environment blue  = new Environment("blue",  "v1.0", true);   // initially active
        Environment green = new Environment("green", "v1.0", false);  // standby

        LoadBalancer lb = new LoadBalancer(blue, green);

        System.out.println("\n[Estado inicial]");
        System.out.println("─".repeat(60));
        lb.status();

        // ── Deploy v2 a green (inactivo), luego switch ────────────
        System.out.println("\n[Deploy v2 al entorno standby (green)]");
        System.out.println("─".repeat(60));
        lb.getStandby().deploy("v2.0");
        boolean passed = lb.getStandby().smokeTest();

        if (passed) {
            System.out.println("\n[Switch de tráfico]");
            System.out.println("─".repeat(60));
            lb.switchTraffic();
        } else {
            System.out.println("  Smoke test falló — sin switch de tráfico.");
        }

        lb.status();

        // ── Simular rollback: problema detectado en v2 ────────────
        System.out.println("\n[Problema detectado en v2 → rollback]");
        System.out.println("─".repeat(60));
        lb.rollback();
        lb.status();

        System.out.println("\n── Conclusión ──");
        System.out.println("  Blue-green: zero-downtime deployment.");
        System.out.println("  El rollback es un switch atómico al entorno anterior — sin re-deploy.");
    }
}
