import java.util.*;

public class Ejercicio5 {

    static class HealthStatus {
        final String component;
        final String status;
        final Map<String, Object> details;

        HealthStatus(String component, String status, Map<String, Object> details) {
            this.component = component;
            this.status = status;
            this.details = details;
        }

        @Override
        public String toString() {
            return String.format("  %-28s [%s] %s", component, status, details);
        }
    }

    interface HealthIndicator {
        HealthStatus check();
    }

    static class DatabaseHealthIndicator implements HealthIndicator {
        @Override
        public HealthStatus check() {
            long pingMs = 10;
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("pingMs", pingMs);
            details.put("pool", "5/10");
            String status = pingMs < 100 ? "UP" : "DEGRADED";
            return new HealthStatus("database", status, details);
        }
    }

    static class DiskSpaceIndicator implements HealthIndicator {
        @Override
        public HealthStatus check() {
            long freeGb = 42;
            long totalGb = 100;
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("freeGb", freeGb);
            details.put("totalGb", totalGb);
            details.put("usedPct", (totalGb - freeGb) * 100 / totalGb + "%");
            String status = freeGb > 10 ? "UP" : "DEGRADED";
            return new HealthStatus("disk-space", status, details);
        }
    }

    static class DependencyHealthIndicator implements HealthIndicator {
        private final String dependencyName;
        private final boolean available;

        DependencyHealthIndicator(String dependencyName, boolean available) {
            this.dependencyName = dependencyName;
            this.available = available;
        }

        @Override
        public HealthStatus check() {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("url", "http://" + dependencyName + "/health");
            details.put("responseMs", available ? 45 : -1);
            String status = available ? "UP" : "DOWN";
            return new HealthStatus("dependency:" + dependencyName, status, details);
        }
    }

    static class HealthEndpoint {
        private final List<HealthIndicator> indicators;

        HealthEndpoint(List<HealthIndicator> indicators) {
            this.indicators = indicators;
        }

        void checkAll() {
            List<HealthStatus> results = new ArrayList<>();
            for (HealthIndicator indicator : indicators) {
                results.add(indicator.check());
            }

            String overall = "UP";
            for (HealthStatus s : results) {
                if ("DOWN".equals(s.status)) { overall = "DOWN"; break; }
                if ("DEGRADED".equals(s.status)) overall = "DEGRADED";
            }

            System.out.println("=== Health Report ===");
            System.out.println("Estado global: " + overall);
            System.out.println("Componentes:");
            results.forEach(System.out::println);
        }
    }

    public static void main(String[] args) {
        List<HealthIndicator> indicators = new ArrayList<>();
        indicators.add(new DatabaseHealthIndicator());
        indicators.add(new DiskSpaceIndicator());
        indicators.add(new DependencyHealthIndicator("payment-service", true));
        indicators.add(new DependencyHealthIndicator("notification-service", false));

        new HealthEndpoint(indicators).checkAll();
    }
}
