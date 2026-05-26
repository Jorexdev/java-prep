import java.util.*;

public class ExpContainerLifecycle {

    enum State { CREATED, RUNNING, PAUSED, STOPPED, REMOVED }

    static class Container {
        private final String name;
        private State state;

        // Valid transitions: each state lists reachable next states
        private static final Map<State, Set<State>> TRANSITIONS = new EnumMap<>(State.class);
        static {
            TRANSITIONS.put(State.CREATED,  EnumSet.of(State.RUNNING, State.REMOVED));
            TRANSITIONS.put(State.RUNNING,  EnumSet.of(State.PAUSED, State.STOPPED));
            TRANSITIONS.put(State.PAUSED,   EnumSet.of(State.RUNNING, State.STOPPED));
            TRANSITIONS.put(State.STOPPED,  EnumSet.of(State.RUNNING, State.REMOVED));
            TRANSITIONS.put(State.REMOVED,  EnumSet.noneOf(State.class));
        }

        Container(String name) {
            this.name = name;
            this.state = State.CREATED;
            System.out.printf("[%s] CREADO → estado: %s%n", name, state);
        }

        // Returns false if the transition is invalid (simulates Docker error)
        boolean transition(State next) {
            Set<State> allowed = TRANSITIONS.get(state);
            if (!allowed.contains(next)) {
                System.out.printf("[%s] ERROR: transición %s → %s no permitida%n", name, state, next);
                return false;
            }
            state = next;
            System.out.printf("[%s] → %s%n", name, state);
            return true;
        }

        State getState() { return state; }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(55));
        System.out.println("  DOCKER CONTAINER LIFECYCLE — simulación");
        System.out.println("═".repeat(55));

        // ── Contenedor 1: ciclo de vida normal ───────────────────
        System.out.println("\n[Escenario 1] Ciclo de vida normal");
        System.out.println("─".repeat(55));

        Container web = new Container("web-server");
        web.transition(State.RUNNING);   // docker start
        web.transition(State.PAUSED);    // docker pause
        web.transition(State.RUNNING);   // docker unpause
        web.transition(State.STOPPED);   // docker stop
        web.transition(State.REMOVED);   // docker rm

        // ── Contenedor 2: kill directo + intento de transición inválida ──
        System.out.println("\n[Escenario 2] Kill directo y transición inválida");
        System.out.println("─".repeat(55));

        Container db = new Container("postgres-db");
        db.transition(State.RUNNING);
        // Intentar pausar-desde-stopped no tiene sentido; demostrar el rechazo primero
        db.transition(State.PAUSED);
        // docker kill → RUNNING → STOPPED directamente (sin pasar por PAUSED)
        // Intentar ir de PAUSED → REMOVED (inválido: debe detenerse antes)
        System.out.println("\n  Intentando transición inválida PAUSED → REMOVED:");
        db.transition(State.REMOVED);   // debe fallar
        // Camino correcto
        db.transition(State.STOPPED);   // docker stop
        db.transition(State.REMOVED);   // docker rm

        System.out.println("\n── Estados finales ──");
        System.out.printf("  web-server : %s%n", web.getState());
        System.out.printf("  postgres-db: %s%n", db.getState());
    }
}
