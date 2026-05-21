import java.util.ArrayList;
import java.util.List;

public class Ejercicio6 {

    enum TaskStatus { CHANGED, OK, FAILED }

    static class AnsibleTask {
        final String name;
        final String module;
        final String params;
        final boolean shouldRun;
        // Simula si la tarea realmente hace cambio o ya estaba en el estado correcto
        final boolean producesChange;
        final boolean willFail;

        AnsibleTask(String name, String module, String params,
                    boolean shouldRun, boolean producesChange, boolean willFail) {
            this.name = name;
            this.module = module;
            this.params = params;
            this.shouldRun = shouldRun;
            this.producesChange = producesChange;
            this.willFail = willFail;
        }

        TaskStatus execute() {
            if (!shouldRun) {
                System.out.println("  SKIPPED  " + name + " (when=false)");
                return TaskStatus.OK;
            }
            if (willFail) {
                System.out.println("  FAILED   " + name);
                return TaskStatus.FAILED;
            }
            TaskStatus status = producesChange ? TaskStatus.CHANGED : TaskStatus.OK;
            System.out.println("  " + status + "  " + name + " [" + module + ": " + params + "]");
            return status;
        }
    }

    static class Playbook {
        private final String name;
        private final List<AnsibleTask> tasks;

        Playbook(String name, List<AnsibleTask> tasks) {
            this.name = name;
            this.tasks = tasks;
        }

        void run() {
            System.out.println("PLAY [" + name + "] ***");
            int changed = 0, ok = 0, failed = 0;

            for (AnsibleTask task : tasks) {
                TaskStatus status = task.execute();
                switch (status) {
                    case CHANGED -> changed++;
                    case OK      -> ok++;
                    case FAILED  -> { failed++;
                        System.out.println("  Playbook abortado tras fallo.");
                        printSummary(changed, ok, failed);
                        return;
                    }
                }
            }
            printSummary(changed, ok, failed);
        }

        private void printSummary(int changed, int ok, int failed) {
            System.out.println("\nPLAY RECAP ***");
            System.out.println("  changed=" + changed + "  ok=" + ok + "  failed=" + failed);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Playbook: configurar servidor web ===\n");
        List<AnsibleTask> tasks = List.of(
            new AnsibleTask("Instalar nginx",        "apt",     "name=nginx state=present", true, true,  false),
            new AnsibleTask("Copiar config nginx",   "copy",    "src=nginx.conf dest=/etc/nginx/", true, true, false),
            new AnsibleTask("Habilitar nginx",       "service", "name=nginx enabled=yes",   true, false, false),
            new AnsibleTask("Abrir puerto 80",       "ufw",     "rule=allow port=80",       true, true,  false),
            new AnsibleTask("Abrir puerto 443 prod", "ufw",     "rule=allow port=443",      false, true, false) // when=false
        );
        new Playbook("Configurar servidor", new ArrayList<>(tasks)).run();

        System.out.println("\n=== Playbook con fallo ===\n");
        List<AnsibleTask> failing = List.of(
            new AnsibleTask("Instalar docker",       "apt",     "name=docker.io",         true, true,  false),
            new AnsibleTask("Descargar imagen",      "shell",   "docker pull myapp:1.0",  true, false, true), // FAIL
            new AnsibleTask("Iniciar contenedor",    "shell",   "docker run myapp:1.0",   true, false, false)
        );
        new Playbook("Desplegar Docker", new ArrayList<>(failing)).run();
    }
}
