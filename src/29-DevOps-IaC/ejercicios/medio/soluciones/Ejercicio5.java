import java.util.*;

public class Ejercicio5 {

    enum TaskStatus { CHANGED, OK, FAILED, SKIPPED }

    static class IdempotentTask {
        final String name;
        final boolean producesChange;
        private boolean alreadyApplied;

        IdempotentTask(String name, boolean producesChange, boolean alreadyApplied) {
            this.name = name;
            this.producesChange = producesChange;
            this.alreadyApplied = alreadyApplied;
        }

        TaskStatus execute() {
            if (alreadyApplied) {
                System.out.println("  SKIPPED  " + name + " (ya aplicado)");
                return TaskStatus.SKIPPED;
            }
            TaskStatus status = producesChange ? TaskStatus.CHANGED : TaskStatus.OK;
            System.out.println("  " + status + "   " + name);
            alreadyApplied = true;
            return status;
        }
    }

    public static void main(String[] args) {
        List<IdempotentTask> tasks = List.of(
            new IdempotentTask("Instalar curl",          true,  true),   // ya aplicado
            new IdempotentTask("Instalar git",           true,  false),  // pendiente
            new IdempotentTask("Crear usuario deploy",   false, true),   // ya aplicado
            new IdempotentTask("Configurar SSH keys",    true,  false),  // pendiente
            new IdempotentTask("Instalar java 21",       true,  true),   // ya aplicado
            new IdempotentTask("Crear directorio /app",  false, true),   // ya aplicado
            new IdempotentTask("Copiar app.jar",         true,  false),  // pendiente
            new IdempotentTask("Configurar systemd",     true,  false),  // pendiente
            new IdempotentTask("Habilitar servicio",     false, true),   // ya aplicado
            new IdempotentTask("Reiniciar nginx",        true,  false)   // pendiente
        );

        System.out.println("=== Primera ejecución del playbook ===");
        Map<TaskStatus, Integer> counters = new EnumMap<>(TaskStatus.class);
        for (TaskStatus s : TaskStatus.values()) counters.put(s, 0);

        for (IdempotentTask task : tasks) {
            TaskStatus s = task.execute();
            counters.merge(s, 1, Integer::sum);
        }
        System.out.println("\nResumen: " + counters);

        System.out.println("\n=== Segunda ejecución (todo idempotente ahora) ===");
        Map<TaskStatus, Integer> counters2 = new EnumMap<>(TaskStatus.class);
        for (TaskStatus s : TaskStatus.values()) counters2.put(s, 0);

        for (IdempotentTask task : tasks) {
            TaskStatus s = task.execute();
            counters2.merge(s, 1, Integer::sum);
        }
        System.out.println("\nResumen: " + counters2);
        System.out.println("\nTodas las tareas devuelven SKIPPED en la segunda ejecución.");
    }
}
