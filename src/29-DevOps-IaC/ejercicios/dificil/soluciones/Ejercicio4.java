import java.util.*;

public class Ejercicio4 {

    enum TaskStatus { CHANGED, OK, FAILED }

    static class AnsibleTask {
        final String name;
        final boolean willFail;
        final boolean producesChange;

        AnsibleTask(String name, boolean willFail, boolean producesChange) {
            this.name = name;
            this.willFail = willFail;
            this.producesChange = producesChange;
        }

        TaskStatus execute() {
            if (willFail) {
                System.out.println("    FAILED:  " + name);
                return TaskStatus.FAILED;
            }
            TaskStatus s = producesChange ? TaskStatus.CHANGED : TaskStatus.OK;
            System.out.println("    " + s + ":   " + name);
            return s;
        }
    }

    static class BlockRescueAlways {
        final List<AnsibleTask> block;
        final List<AnsibleTask> rescue;
        final List<AnsibleTask> always;

        BlockRescueAlways(List<AnsibleTask> block, List<AnsibleTask> rescue, List<AnsibleTask> always) {
            this.block = block;
            this.rescue = rescue;
            this.always = always;
        }

        void execute() {
            boolean blockFailed = false;

            System.out.println("  [block]");
            for (AnsibleTask task : block) {
                TaskStatus status = task.execute();
                if (status == TaskStatus.FAILED) {
                    blockFailed = true;
                    break;
                }
            }

            if (blockFailed) {
                System.out.println("\n  [rescue] — ejecutando recuperación:");
                for (AnsibleTask task : rescue) {
                    task.execute();
                }
            }

            System.out.println("\n  [always] — siempre se ejecuta:");
            for (AnsibleTask task : always) {
                task.execute();
            }

            System.out.println("\n  Resultado: " + (blockFailed ? "bloque falló, rescue ejecutado" : "bloque exitoso"));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Ansible block/rescue/always ===\n");

        System.out.println("--- Escenario 1: fallo en el bloque ---\n");
        BlockRescueAlways scenario1 = new BlockRescueAlways(
            List.of(
                new AnsibleTask("Detener servicio anterior",  false, true),
                new AnsibleTask("Desplegar nueva versión",    true,  false), // FALLA aquí
                new AnsibleTask("Iniciar nuevo servicio",     false, true)   // no se ejecuta
            ),
            List.of(
                new AnsibleTask("Restaurar versión anterior", false, true),
                new AnsibleTask("Reiniciar servicio estable", false, false),
                new AnsibleTask("Notificar al equipo",        false, true)
            ),
            List.of(
                new AnsibleTask("Limpiar archivos temporales", false, true),
                new AnsibleTask("Registrar resultado en log",  false, true)
            )
        );
        scenario1.execute();

        System.out.println("\n--- Escenario 2: bloque exitoso ---\n");
        BlockRescueAlways scenario2 = new BlockRescueAlways(
            List.of(
                new AnsibleTask("Detener servicio anterior",  false, true),
                new AnsibleTask("Desplegar nueva versión",    false, true),
                new AnsibleTask("Iniciar nuevo servicio",     false, false)
            ),
            List.of(
                new AnsibleTask("Restaurar versión anterior", false, true)  // NO se ejecutará
            ),
            List.of(
                new AnsibleTask("Limpiar archivos temporales", false, true),
                new AnsibleTask("Registrar resultado en log",  false, true)
            )
        );
        scenario2.execute();
    }
}
