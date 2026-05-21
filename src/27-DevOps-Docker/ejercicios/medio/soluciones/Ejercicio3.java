import java.util.*;

public class Ejercicio3 {

    enum LayerStatus { CACHED, REBUILT }

    static class Layer {
        int index;
        String command;
        LayerStatus status;

        Layer(int index, String command) {
            this.index   = index;
            this.command = command;
        }
    }

    static class BuildResult {
        List<Layer> layers;

        BuildResult(List<Layer> layers) {
            this.layers = new ArrayList<>(layers);
        }

        void print(String title) {
            System.out.println("\n" + title);
            System.out.printf("  %-2s  %-40s  %s%n", "#", "Command", "Status");
            System.out.println("  " + "-".repeat(55));
            for (Layer l : layers) {
                System.out.printf("  %-2d  %-40s  %s%n",
                        l.index, l.command, l.status == null ? "BUILT" : l.status);
            }
        }
    }

    static BuildResult rebuild(List<String> previousCommands, List<String> newCommands) {
        List<Layer> result    = new ArrayList<>();
        boolean cacheInvalid  = false;

        int size = Math.max(previousCommands.size(), newCommands.size());
        for (int i = 0; i < size; i++) {
            String newCmd  = i < newCommands.size()      ? newCommands.get(i)      : null;
            String prevCmd = i < previousCommands.size() ? previousCommands.get(i) : null;

            if (newCmd == null) continue;

            Layer l = new Layer(i + 1, newCmd);
            if (!cacheInvalid && newCmd.equals(prevCmd)) {
                l.status = LayerStatus.CACHED;
            } else {
                l.status    = LayerStatus.REBUILT;
                cacheInvalid = true;
            }
            result.add(l);
        }
        return new BuildResult(result);
    }

    public static void main(String[] args) {
        System.out.println("=== Docker Layer Cache Simulation ===");

        List<String> prev = List.of(
                "FROM eclipse-temurin:21-jre-alpine",
                "RUN apk add --no-cache curl",
                "COPY pom.xml .",
                "RUN mvn dependency:resolve",
                "COPY src ./src"
        );

        // El layer 3 cambia: se añade un nuevo archivo en la copia
        List<String> next = List.of(
                "FROM eclipse-temurin:21-jre-alpine",
                "RUN apk add --no-cache curl",
                "COPY pom.xml ./  # modified",    // <-- cambio
                "RUN mvn dependency:resolve",
                "COPY src ./src"
        );

        System.out.println("\nBuild anterior:");
        for (int i = 0; i < prev.size(); i++) {
            System.out.printf("  %d: %s%n", i + 1, prev.get(i));
        }

        BuildResult result = rebuild(prev, next);
        result.print("Rebuild result:");

        long cached  = result.layers.stream().filter(l -> l.status == LayerStatus.CACHED).count();
        long rebuilt = result.layers.stream().filter(l -> l.status == LayerStatus.REBUILT).count();
        System.out.printf("%nResumen: %d CACHED, %d REBUILT%n", cached, rebuilt);
    }
}
