import java.util.*;
import java.util.concurrent.*;

public class Ejercicio5 {

    static class LockConflictException extends RuntimeException {
        LockConflictException(String message) { super(message); }
    }

    static class LockInfo {
        String workspaceId;
        String requester;
        long acquiredAt;

        LockInfo(String workspaceId, String requester) {
            this.workspaceId = workspaceId;
            this.requester   = requester;
            this.acquiredAt  = System.currentTimeMillis();
        }

        @Override public String toString() {
            return String.format("LockInfo{workspace='%s', holder='%s', age=%dms}",
                    workspaceId, requester, System.currentTimeMillis() - acquiredAt);
        }
    }

    // Singleton simulado — en producción sería S3/DynamoDB/GCS
    static class RemoteStateBackend {
        private static final RemoteStateBackend INSTANCE = new RemoteStateBackend();

        // workspace → resources
        private final Map<String, Map<String, String>> states = new ConcurrentHashMap<>();
        // lock activo (solo puede haber uno a la vez)
        private volatile LockInfo currentLock = null;
        private final Object lockMutex = new Object();

        private RemoteStateBackend() {}

        static RemoteStateBackend getInstance() { return INSTANCE; }

        // Intenta adquirir el lock; lanza LockConflictException si ya está tomado
        void acquireLock(String workspaceId, String requester) {
            synchronized (lockMutex) {
                if (currentLock != null) {
                    throw new LockConflictException(
                            String.format("Lock ya adquirido — %s. Solicitante '%s' no puede proceder.",
                                    currentLock, requester));
                }
                currentLock = new LockInfo(workspaceId, requester);
                System.out.printf("  [LOCK ACQUIRED] workspace='%s' by '%s'%n", workspaceId, requester);
            }
        }

        void releaseLock(String workspaceId, String requester) {
            synchronized (lockMutex) {
                if (currentLock != null && currentLock.workspaceId.equals(workspaceId)) {
                    System.out.printf("  [LOCK RELEASED] workspace='%s' by '%s'%n", workspaceId, requester);
                    currentLock = null;
                }
            }
        }

        // Aplica cambios al state; requiere haber adquirido el lock primero
        void applyChanges(String workspaceId, String requester, Map<String, String> changes) {
            acquireLock(workspaceId, requester);
            try {
                System.out.printf("  [APPLY] workspace='%s' — aplicando %d cambios...%n",
                        workspaceId, changes.size());
                Map<String, String> state = states.computeIfAbsent(workspaceId,
                        k -> new LinkedHashMap<>());
                state.putAll(changes);
                changes.forEach((k, v) ->
                        System.out.printf("    + %s = %s%n", k, v));
                System.out.printf("  [APPLY DONE] state de '%s' actualizado%n", workspaceId);
            } finally {
                releaseLock(workspaceId, requester);
            }
        }

        Map<String, String> getState(String workspaceId) {
            return Collections.unmodifiableMap(
                    states.getOrDefault(workspaceId, Map.of()));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Terraform Remote State con Locking Distribuido ===\n");

        RemoteStateBackend backend = RemoteStateBackend.getInstance();

        // Demo 1: apply secuencial exitoso
        System.out.println("--- Demo 1: apply exitoso (workspace-A) ---");
        backend.applyChanges("workspace-A", "developer-1",
                Map.of("aws_vpc.main", "vpc-0a1b2c3d",
                       "aws_subnet.public", "subnet-0x1y2z"));

        System.out.println("\nState actual de workspace-A:");
        backend.getState("workspace-A").forEach((k, v) ->
                System.out.printf("  %s = %s%n", k, v));

        // Demo 2: conflicto — dos workspaces intentan aplicar simultáneamente
        System.out.println("\n--- Demo 2: conflicto de lock (workspace-B) ---");

        // Primero: workspace-B adquiere el lock manualmente (simula que está en medio de un apply)
        backend.acquireLock("workspace-B", "developer-2");
        System.out.println("  developer-2 tiene el lock, aplicando cambios grandes...");

        // workspace-B2 intenta adquirir el lock mientras developer-2 lo tiene
        System.out.println("\n  developer-3 intenta hacer apply en workspace-B:");
        try {
            backend.applyChanges("workspace-B", "developer-3",
                    Map.of("aws_db_instance.main", "db-xyz"));
        } catch (LockConflictException e) {
            System.out.println("  [LockConflictException] " + e.getMessage());
            System.out.println("  developer-3 debe esperar o reintentar.");
        }

        // developer-2 termina y libera el lock
        System.out.println("\n  developer-2 termina y libera el lock:");
        backend.releaseLock("workspace-B", "developer-2");

        // Ahora developer-3 puede reintentar
        System.out.println("\n  developer-3 reintenta tras liberar el lock:");
        backend.applyChanges("workspace-B", "developer-3",
                Map.of("aws_db_instance.main", "db-xyz",
                       "aws_security_group.db", "sg-db-001"));

        System.out.println("\nState final de workspace-B:");
        backend.getState("workspace-B").forEach((k, v) ->
                System.out.printf("  %s = %s%n", k, v));
    }
}
