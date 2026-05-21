import java.util.*;
import java.util.function.Supplier;

public class Ejercicio5 {

    enum TaskResult { CHANGED, OK, FAILED }

    static class AnsibleTask {
        final String name;
        final String module;
        final Map<String, String> params;
        final Supplier<Boolean> when; // null means always run

        AnsibleTask(String name, String module, Map<String, String> params,
                    Supplier<Boolean> when) {
            this.name   = name;
            this.module = module;
            this.params = params;
            this.when   = when;
        }

        TaskResult execute() {
            // Check 'when' condition
            if (when != null && !when.get()) {
                System.out.printf("  SKIP    [%s] (condition false)%n", name);
                return TaskResult.OK;
            }

            // Simulate execution based on module
            System.out.printf("  RUN     [%s] %s %s%n", name, module, params);
            return simulateModule(module, params);
        }

        private TaskResult simulateModule(String mod, Map<String, String> p) {
            return switch (mod) {
                case "apt"    -> { System.out.println("           -> package installed"); yield TaskResult.CHANGED; }
                case "copy"   -> { System.out.println("           -> file copied"); yield TaskResult.CHANGED; }
                case "service"-> {
                    String state = p.getOrDefault("state", "started");
                    System.out.println("           -> service " + state);
                    yield TaskResult.OK;
                }
                case "fail"   -> { System.out.println("           -> task deliberately failed"); yield TaskResult.FAILED; }
                default       -> { System.out.println("           -> executed ok"); yield TaskResult.OK; }
            };
        }
    }

    public static void main(String[] args) {
        boolean isUbuntu = true;   // simulate host fact
        boolean isDebian = false;

        List<AnsibleTask> tasks = List.of(
            new AnsibleTask("Install nginx", "apt",
                    Map.of("name", "nginx", "state", "present"),
                    null),                                      // always run

            new AnsibleTask("Install curl (Ubuntu only)", "apt",
                    Map.of("name", "curl", "state", "present"),
                    () -> isUbuntu),                            // only if Ubuntu

            new AnsibleTask("Install curl (Debian only)", "apt",
                    Map.of("name", "curl", "state", "present"),
                    () -> isDebian),                            // condition false -> skip

            new AnsibleTask("Copy config", "copy",
                    Map.of("src", "nginx.conf", "dest", "/etc/nginx/nginx.conf"),
                    null),

            new AnsibleTask("Start nginx", "service",
                    Map.of("name", "nginx", "state", "started"),
                    null)
        );

        System.out.println("TASK EXECUTION");
        System.out.println("==============");
        for (AnsibleTask task : tasks) {
            TaskResult result = task.execute();
            System.out.printf("  Result: %s%n%n", result);
        }
    }
}
