import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio1 {

    static class Phase {
        final String name;
        final Runnable action;

        Phase(String name, Runnable action) {
            this.name = name;
            this.action = action;
        }
    }

    static class MavenLifecycle {
        // Orden fijo del lifecycle por defecto de Maven
        private final List<String> phaseOrder = List.of(
            "validate", "compile", "test", "package", "verify", "install", "deploy"
        );

        private final Map<String, Runnable> phaseActions = new LinkedHashMap<>();

        void bind(String phase, Runnable action) {
            if (!phaseOrder.contains(phase)) {
                throw new IllegalArgumentException("Fase desconocida: " + phase);
            }
            phaseActions.put(phase, action);
        }

        // Ejecuta todas las fases desde la primera hasta `targetPhase` inclusive
        void run(String targetPhase) {
            if (!phaseOrder.contains(targetPhase)) {
                throw new IllegalArgumentException("Fase objetivo desconocida: " + targetPhase);
            }
            int targetIndex = phaseOrder.indexOf(targetPhase);
            System.out.println("[INFO] --- Maven build ---");
            System.out.println("[INFO] Objetivo: " + targetPhase);
            System.out.println("[INFO]");

            for (int i = 0; i <= targetIndex; i++) {
                String phase = phaseOrder.get(i);
                System.out.println("[INFO] --- " + phase + " ---");
                Runnable action = phaseActions.get(phase);
                if (action != null) {
                    action.run();
                } else {
                    System.out.println("[INFO] (no hay acciones vinculadas a esta fase)");
                }
                System.out.println("[INFO]");
            }

            System.out.println("[INFO] BUILD SUCCESS");
            System.out.println("[INFO] Fases ejecutadas: " + (targetIndex + 1) + "/" + phaseOrder.size());
        }
    }

    public static void main(String[] args) {
        MavenLifecycle lifecycle = new MavenLifecycle();

        lifecycle.bind("validate", () ->
            System.out.println("[INFO] Validando estructura del proyecto..."));

        lifecycle.bind("compile", () -> {
            System.out.println("[INFO] Compilando fuentes en src/main/java...");
            System.out.println("[INFO] Compiladas 12 clases");
        });

        lifecycle.bind("test", () -> {
            System.out.println("[INFO] Ejecutando tests con Surefire...");
            System.out.println("[INFO] Tests run: 24, Failures: 0, Errors: 0, Skipped: 0");
        });

        lifecycle.bind("package", () -> {
            System.out.println("[INFO] Empaquetando en target/app-1.0.jar...");
            System.out.println("[INFO] Building jar: app-1.0.jar (45KB)");
        });

        lifecycle.bind("install", () ->
            System.out.println("[INFO] Instalando en repositorio local ~/.m2/..."));

        System.out.println("=== mvn package ===");
        System.out.println();
        lifecycle.run("package");

        System.out.println();
        System.out.println("=== mvn compile (solo hasta compile) ===");
        System.out.println();
        lifecycle.run("compile");
    }
}
