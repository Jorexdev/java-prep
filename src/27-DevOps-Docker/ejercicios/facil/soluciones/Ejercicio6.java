import java.util.*;

public class Ejercicio6 {

    static class Layer {
        String instruction;
        String argument;
        double sizeMB;

        Layer(String instruction, String argument, double sizeMB) {
            this.instruction = instruction;
            this.argument    = argument;
            this.sizeMB      = sizeMB;
        }
    }

    static class DockerImage {
        String baseImage;
        List<Layer> layers = new ArrayList<>();
        List<Integer> exposedPorts = new ArrayList<>();
        Map<String, String> envVars = new LinkedHashMap<>();
        String cmd;

        void addLayer(String instruction, String argument) {
            double size = switch (instruction) {
                case "FROM" -> 72.0;
                case "RUN"  -> 45.0;
                case "COPY" -> 12.5;
                case "ENV"  -> 0.0;
                default     -> 0.0;
            };
            layers.add(new Layer(instruction, argument, size));
        }

        double totalSize() {
            return layers.stream().mapToDouble(l -> l.sizeMB).sum();
        }

        void print() {
            System.out.println("\n=== DockerImage construida ===");
            System.out.printf("%-8s %-40s %8s%n", "Instr.", "Argumento", "Size MB");
            System.out.println("-".repeat(60));
            for (Layer l : layers) {
                System.out.printf("%-8s %-40s %8.1f%n",
                        l.instruction,
                        l.argument.length() > 40 ? l.argument.substring(0, 37) + "..." : l.argument,
                        l.sizeMB);
            }
            System.out.println("-".repeat(60));
            System.out.printf("%-48s %8.1f MB%n", "TOTAL", totalSize());
            System.out.println("Puertos expuestos: " + exposedPorts);
            System.out.println("Env vars: " + envVars);
            System.out.println("CMD: " + cmd);
        }
    }

    static DockerImage parseDockerfile(String dockerfile) {
        DockerImage image = new DockerImage();
        for (String raw : dockerfile.split("\n")) {
            String line = raw.strip();
            if (line.isEmpty() || line.startsWith("#")) continue;
            int space = line.indexOf(' ');
            if (space < 0) continue;
            String instr = line.substring(0, space).toUpperCase();
            String arg   = line.substring(space + 1).strip();

            switch (instr) {
                case "FROM"   -> { image.baseImage = arg; image.addLayer(instr, arg); }
                case "RUN"    -> image.addLayer(instr, arg);
                case "COPY"   -> image.addLayer(instr, arg);
                case "EXPOSE" -> { image.exposedPorts.add(Integer.parseInt(arg)); image.addLayer(instr, arg); }
                case "ENV"    -> {
                    String[] parts = arg.split("=", 2);
                    if (parts.length == 2) image.envVars.put(parts[0].strip(), parts[1].strip());
                    image.addLayer(instr, arg);
                }
                case "CMD"    -> { image.cmd = arg; image.addLayer(instr, arg); }
            }
        }
        return image;
    }

    public static void main(String[] args) {
        String dockerfile = """
                FROM eclipse-temurin:21-jre-alpine
                ENV APP_ENV=production
                ENV LOG_LEVEL=INFO
                RUN apk add --no-cache curl
                COPY target/app.jar /app/app.jar
                COPY src/main/resources/application.yml /app/config/
                EXPOSE 8080
                CMD java -jar /app/app.jar
                """;

        System.out.println("=== Dockerfile de entrada ===");
        System.out.println(dockerfile);

        DockerImage image = parseDockerfile(dockerfile);
        image.print();

        System.out.println("\nCapas generadas: " + image.layers.size());
    }
}
