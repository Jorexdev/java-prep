import java.util.*;

public class Ejercicio5 {

    static class VolumeMount {
        String hostPath;
        String containerPath;
        boolean readOnly;

        VolumeMount(String hostPath, String containerPath, boolean readOnly) {
            this.hostPath      = hostPath;
            this.containerPath = containerPath;
            this.readOnly      = readOnly;
        }

        @Override
        public String toString() {
            return hostPath + ":" + containerPath + (readOnly ? ":ro" : ":rw");
        }
    }

    static class Container {
        String name;
        List<VolumeMount> mounts;

        Container(String name, VolumeMount... mounts) {
            this.name   = name;
            this.mounts = new ArrayList<>(Arrays.asList(mounts));
        }
    }

    static void detectWriteConflicts(List<Container> containers) {
        System.out.println("=== Volume Mount Write Conflicts ===\n");

        // Mostrar todos los mounts
        System.out.printf("%-14s %-25s %-25s %-6s%n",
                "Container", "HostPath", "ContainerPath", "Mode");
        System.out.println("-".repeat(74));
        for (Container c : containers) {
            for (VolumeMount m : c.mounts) {
                System.out.printf("%-14s %-25s %-25s %-6s%n",
                        c.name, m.hostPath, m.containerPath,
                        m.readOnly ? "ro" : "rw");
            }
        }

        System.out.println("\n=== Detección de conflictos (write-write) ===");
        // hostPath → containers que montan en rw
        Map<String, List<String>> writers = new LinkedHashMap<>();
        for (Container c : containers) {
            for (VolumeMount m : c.mounts) {
                if (!m.readOnly) {
                    writers.computeIfAbsent(m.hostPath, k -> new ArrayList<>()).add(c.name);
                }
            }
        }

        boolean hasConflict = false;
        for (Map.Entry<String, List<String>> e : writers.entrySet()) {
            if (e.getValue().size() > 1) {
                System.out.printf("CONFLICTO: '%s' montado en rw por: %s%n",
                        e.getKey(), e.getValue());
                hasConflict = true;
            }
        }
        if (!hasConflict) {
            System.out.println("Sin conflictos de escritura.");
        }
    }

    public static void main(String[] args) {
        List<Container> containers = new ArrayList<>();

        containers.add(new Container("app-1",
                new VolumeMount("/data/uploads", "/app/uploads", false),
                new VolumeMount("/data/config",  "/app/config",  true)));

        containers.add(new Container("app-2",
                new VolumeMount("/data/uploads", "/app/uploads", false),  // conflicto
                new VolumeMount("/data/logs",    "/var/log",     false)));

        containers.add(new Container("backup",
                new VolumeMount("/data/uploads", "/backup/src",  true),   // solo lectura, ok
                new VolumeMount("/data/logs",    "/backup/logs", false))); // conflicto con app-2

        containers.add(new Container("monitor",
                new VolumeMount("/data/config",  "/etc/config",  true)));  // solo lectura, ok

        detectWriteConflicts(containers);
    }
}
