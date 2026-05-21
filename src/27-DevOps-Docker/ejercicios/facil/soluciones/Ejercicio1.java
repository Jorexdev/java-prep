import java.util.*;

public class Ejercicio1 {

    static class Layer {
        String name;
        String command;
        double sizeMB;

        Layer(String name, String command, double sizeMB) {
            this.name = name;
            this.command = command;
            this.sizeMB = sizeMB;
        }
    }

    static class DockerImage {
        String name;
        List<Layer> layers;

        DockerImage(String name, List<Layer> layers) {
            this.name = name;
            this.layers = new ArrayList<>(layers);
        }

        double totalSize() {
            return layers.stream().mapToDouble(l -> l.sizeMB).sum();
        }

        void print() {
            System.out.println("Image: " + name);
            System.out.println("+----+----------+-----------------------------+----------+");
            System.out.printf("| %-2s | %-8s | %-27s | %8s |%n", "#", "Layer", "Command", "Size MB");
            System.out.println("+----+----------+-----------------------------+----------+");
            for (int i = 0; i < layers.size(); i++) {
                Layer l = layers.get(i);
                System.out.printf("| %-2d | %-8s | %-27s | %8.1f |%n",
                        i + 1, l.name, l.command, l.sizeMB);
            }
            System.out.println("+----+----------+-----------------------------+----------+");
            System.out.printf("| Total: %43.1f MB |%n", totalSize());
            System.out.println("+--------------------------------------------------+");
        }
    }

    public static void main(String[] args) {
        List<Layer> layers = new ArrayList<>();
        layers.add(new Layer("FROM",   "FROM ubuntu:22.04",             72.8));
        layers.add(new Layer("RUN",    "RUN apt-get install -y openjdk", 312.4));
        layers.add(new Layer("COPY",   "COPY target/app.jar /app.jar",   18.6));
        layers.add(new Layer("RUN",    "RUN chmod +x /app.jar",           0.0));

        DockerImage image = new DockerImage("my-java-app:1.0", layers);
        image.print();

        System.out.println("\nCapas: " + image.layers.size());
        System.out.printf("Tamaño total: %.1f MB%n", image.totalSize());
    }
}
