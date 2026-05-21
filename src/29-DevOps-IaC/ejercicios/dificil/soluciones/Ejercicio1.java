import java.util.*;
import java.util.concurrent.*;

public class Ejercicio1 {

    record Resource(String id, Map<String, String> config) {}

    static class RemoteStateStore {
        private static final RemoteStateStore INSTANCE = new RemoteStateStore();
        private final Map<String, Map<String, Resource>> workspaceStates = new ConcurrentHashMap<>();
        private final Map<String, String> locks = new ConcurrentHashMap<>();

        private RemoteStateStore() {}
        static RemoteStateStore getInstance() { return INSTANCE; }

        synchronized boolean lock(String workspace, String owner) {
            if (locks.containsKey(workspace)) {
                System.out.println("  [RemoteState] LOCK DENIED  workspace=" + workspace + " holder=" + locks.get(workspace));
                return false;
            }
            locks.put(workspace, owner);
            System.out.println("  [RemoteState] LOCK ACQUIRED workspace=" + workspace + " by=" + owner);
            return true;
        }

        synchronized void unlock(String workspace, String owner) {
            if (owner.equals(locks.get(workspace))) {
                locks.remove(workspace);
                System.out.println("  [RemoteState] LOCK RELEASED workspace=" + workspace + " by=" + owner);
            }
        }

        void saveState(String workspace, Map<String, Resource> state) {
            workspaceStates.put(workspace, new HashMap<>(state));
            System.out.println("  [RemoteState] state guardado para workspace=" + workspace
                + " (" + state.size() + " recursos)");
        }

        Map<String, Resource> loadState(String workspace) {
            return workspaceStates.getOrDefault(workspace, new HashMap<>());
        }
    }

    static class TerraformWorkspace {
        final String name;
        private final RemoteStateStore store = RemoteStateStore.getInstance();

        TerraformWorkspace(String name) { this.name = name; }

        void apply(List<Resource> desired) {
            if (!store.lock(name, name)) {
                System.out.println("  [" + name + "] Apply abortado: workspace bloqueado por otro proceso.");
                return;
            }
            try {
                System.out.println("  [" + name + "] Aplicando " + desired.size() + " recursos...");
                Map<String, Resource> state = new HashMap<>();
                for (Resource r : desired) state.put(r.id(), r);
                Thread.sleep(100); // simula tiempo de apply
                store.saveState(name, state);
                System.out.println("  [" + name + "] Apply completado.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                store.unlock(name, name);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        RemoteStateStore.getInstance(); // inicializar singleton

        TerraformWorkspace ws1 = new TerraformWorkspace("production");
        TerraformWorkspace ws2 = new TerraformWorkspace("production"); // mismo workspace

        List<Resource> resources1 = List.of(new Resource("aws_vpc.main", Map.of("cidr", "10.0.0.0/16")));
        List<Resource> resources2 = List.of(new Resource("aws_instance.app", Map.of("type", "t3.medium")));

        System.out.println("=== Dos workspaces intentando apply simultáneo ===\n");

        CountDownLatch start = new CountDownLatch(1);
        Thread t1 = new Thread(() -> { try { start.await(); ws1.apply(resources1); } catch (InterruptedException e) {} });
        Thread t2 = new Thread(() -> { try { start.await(); ws2.apply(resources2); } catch (InterruptedException e) {} });

        t1.start(); t2.start();
        start.countDown();
        t1.join(); t2.join();

        System.out.println("\nEstado final: " + RemoteStateStore.getInstance().loadState("production").keySet());
    }
}
