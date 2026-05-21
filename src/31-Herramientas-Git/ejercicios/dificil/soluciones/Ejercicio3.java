import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Ejercicio3 {

    static class CommitData {
        final String message;
        final String code; // contenido del código a commitear

        CommitData(String message, String code) {
            this.message = message;
            this.code = code;
        }
    }

    static class HookResult {
        final String hookName;
        final boolean passed;
        final String errorMessage;

        HookResult(String hookName, boolean passed, String errorMessage) {
            this.hookName = hookName;
            this.passed = passed;
            this.errorMessage = errorMessage;
        }
    }

    static class Hook {
        final String name;
        final Predicate<CommitData> check;

        Hook(String name, Predicate<CommitData> check) {
            this.name = name;
            this.check = check;
        }

        HookResult run(CommitData data) {
            boolean passed = check.test(data);
            String error = passed ? null : "Hook '" + name + "' falló";
            return new HookResult(name, passed, error);
        }
    }

    static class CommitBlockedException extends RuntimeException {
        CommitBlockedException(String message) { super(message); }
    }

    static class HookRunner {
        private final List<Hook> hooks = new ArrayList<>();

        void addHook(Hook hook) {
            hooks.add(hook);
        }

        // Lanza CommitBlockedException si algún hook falla
        void runAll(CommitData data) {
            System.out.println("Ejecutando " + hooks.size() + " pre-commit hooks...");
            List<String> failures = new ArrayList<>();

            for (Hook hook : hooks) {
                HookResult result = hook.run(data);
                System.out.printf("  [%s] %s%n", result.passed ? "PASS" : "FAIL", hook.name);
                if (!result.passed) {
                    failures.add(result.errorMessage);
                }
            }

            if (!failures.isEmpty()) {
                String errorMsg = "Commit bloqueado por " + failures.size() + " hook(s):\n"
                        + String.join("\n", failures);
                throw new CommitBlockedException(errorMsg);
            }
            System.out.println("Todos los hooks pasaron — commit permitido.");
        }
    }

    static void tryCommit(HookRunner runner, CommitData data) {
        System.out.println("--- Intentando commit: '" + data.message + "' ---");
        try {
            runner.runAll(data);
            System.out.println("Commit creado exitosamente.");
        } catch (CommitBlockedException e) {
            System.out.println(e.getMessage());
        }
        System.out.println();
    }

    public static void main(String[] args) {
        HookRunner runner = new HookRunner();

        // Hook 1: no permitir la palabra TODO en el código
        runner.addHook(new Hook(
            "no-todo-in-code",
            data -> !data.code.contains("TODO")
        ));

        // Hook 2: mensaje de commit mínimo de 10 caracteres
        runner.addHook(new Hook(
            "min-commit-message-length",
            data -> data.message.length() >= 10
        ));

        // Hook 3: mensaje no puede ser solo espacios
        runner.addHook(new Hook(
            "non-empty-message",
            data -> !data.message.isBlank()
        ));

        System.out.println("=== Pre-commit hooks configurados ===");
        System.out.println();

        // Caso 1: commit válido
        tryCommit(runner, new CommitData(
            "feat: add user login endpoint",
            "public class LoginController { public void login() { } }"
        ));

        // Caso 2: código con TODO
        tryCommit(runner, new CommitData(
            "feat: add payment service",
            "public class PaymentService { // TODO: implement this }"
        ));

        // Caso 3: mensaje muy corto
        tryCommit(runner, new CommitData(
            "fix",
            "public class Fix { }"
        ));

        // Caso 4: ambos problemas
        tryCommit(runner, new CommitData(
            "wip",
            "// TODO: everything"
        ));

        // Caso 5: código limpio tras corregir
        tryCommit(runner, new CommitData(
            "fix: handle null pointer in cache",
            "public class CacheService { Object get(String key) { return cache.getOrDefault(key, null); } }"
        ));
    }
}
