import java.util.*;

public class Ejercicio2 {

    enum ContainerState {
        CREATED, RUNNING, PAUSED, STOPPED, REMOVED
    }

    static class Container {
        String image;
        String name;
        ContainerState state;

        Container(String image, String name) {
            this.image = image;
            this.name  = name;
            this.state = ContainerState.CREATED;
            log("Creado");
        }

        void start() {
            if (state == ContainerState.CREATED || state == ContainerState.STOPPED) {
                state = ContainerState.RUNNING;
                log("Iniciado");
            } else if (state == ContainerState.RUNNING) {
                log("ERROR: ya está RUNNING");
            } else {
                log("ERROR: no se puede iniciar desde " + state);
            }
        }

        void pause() {
            if (state == ContainerState.RUNNING) {
                state = ContainerState.PAUSED;
                log("Pausado");
            } else {
                log("ERROR: solo se puede pausar desde RUNNING (estado actual: " + state + ")");
            }
        }

        void unpause() {
            if (state == ContainerState.PAUSED) {
                state = ContainerState.RUNNING;
                log("Reanudado");
            } else {
                log("ERROR: no está pausado (estado actual: " + state + ")");
            }
        }

        void stop() {
            if (state == ContainerState.RUNNING || state == ContainerState.PAUSED) {
                state = ContainerState.STOPPED;
                log("Detenido");
            } else {
                log("ERROR: no se puede detener desde " + state);
            }
        }

        void remove() {
            if (state == ContainerState.STOPPED || state == ContainerState.CREATED) {
                state = ContainerState.REMOVED;
                log("Eliminado");
            } else {
                log("ERROR: debe estar STOPPED antes de eliminar (estado actual: " + state + ")");
            }
        }

        void log(String event) {
            System.out.printf("[%-12s] %-20s → %s%n", name, event, state);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Docker Container Lifecycle Demo ===\n");
        Container c = new Container("nginx:latest", "web-server");

        System.out.println("\n-- Ciclo de vida normal --");
        c.start();
        c.pause();
        c.unpause();
        c.stop();
        c.remove();

        System.out.println("\n-- Transiciones inválidas --");
        Container c2 = new Container("redis:7", "cache");
        c2.pause();          // no se puede pausar desde CREATED
        c2.start();
        c2.start();          // ya está RUNNING
        c2.remove();         // no se puede eliminar desde RUNNING
        c2.stop();
        c2.remove();
    }
}
