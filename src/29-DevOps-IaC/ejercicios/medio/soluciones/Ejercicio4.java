import java.util.*;

public class Ejercicio4 {

    enum TaskStatus { CHANGED, OK, FAILED, SKIPPED }

    static class AnsibleTask {
        final String name;
        final boolean producesChange;
        final boolean willFail;

        AnsibleTask(String name, boolean producesChange, boolean willFail) {
            this.name = name;
            this.producesChange = producesChange;
            this.willFail = willFail;
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

    static class Role {
        final String name;
        final List<AnsibleTask> tasks;

        Role(String name, List<AnsibleTask> tasks) {
            this.name = name;
            this.tasks = tasks;
        }
    }

    static class Host {
        final String name;
        final List<String> groups;

        Host(String name, String... groups) {
            this.name = name;
            this.groups = Arrays.asList(groups);
        }

        boolean inGroup(String group) { return groups.contains(group); }
    }

    record RoleAssignment(String group, Role role) {}

    static class Playbook {
        final List<RoleAssignment> assignments;
        final List<Host> hosts;

        Playbook(List<RoleAssignment> assignments, List<Host> hosts) {
            this.assignments = assignments;
            this.hosts = hosts;
        }

        void run() {
            for (Host host : hosts) {
                System.out.println("\n  HOST: " + host.name + " (grupos: " + host.groups + ")");
                for (RoleAssignment ra : assignments) {
                    if (host.inGroup(ra.group())) {
                        System.out.println("    ROLE: " + ra.role().name);
                        for (AnsibleTask task : ra.role().tasks) {
                            task.execute();
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Role commonRole = new Role("common", List.of(
            new AnsibleTask("Actualizar apt cache", true, false),
            new AnsibleTask("Instalar htop",        false, false)
        ));

        Role webRole = new Role("web", List.of(
            new AnsibleTask("Instalar nginx",        true, false),
            new AnsibleTask("Copiar config nginx",   true, false),
            new AnsibleTask("Iniciar nginx",         false, false)
        ));

        Role dbRole = new Role("database", List.of(
            new AnsibleTask("Instalar PostgreSQL",   true, false),
            new AnsibleTask("Crear base de datos",   false, false)
        ));

        List<RoleAssignment> assignments = List.of(
            new RoleAssignment("all",      commonRole),
            new RoleAssignment("webservers", webRole),
            new RoleAssignment("databases",  dbRole)
        );

        List<Host> hosts = List.of(
            new Host("web-01",  "all", "webservers"),
            new Host("web-02",  "all", "webservers"),
            new Host("db-01",   "all", "databases")
        );

        System.out.println("=== Playbook: configurar infraestructura ===");
        new Playbook(assignments, hosts).run();
    }
}
