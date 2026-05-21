import java.util.List;
import java.util.Set;

public class Ejercicio5 {

    static class GitFlowViolationException extends RuntimeException {
        GitFlowViolationException(String message) {
            super(message);
        }
    }

    static void validateMerge(String from, String to) {
        String fromType = getBranchType(from);

        Set<String> allowed = switch (fromType) {
            case "hotfix"  -> Set.of("main", "develop");
            case "feature" -> Set.of("develop");
            case "release" -> Set.of("main", "develop");
            default        -> Set.of(); // other branches: no rule enforced
        };

        String toBase = to.contains("/") ? to.split("/")[0] : to;

        if (!allowed.isEmpty() && !allowed.contains(toBase) && !allowed.contains(to)) {
            throw new GitFlowViolationException(
                String.format("GitFlow violation: '%s' (%s) no puede mergear a '%s'. Permitidos: %s",
                        from, fromType, to, allowed));
        }

        System.out.printf("[OK] merge '%s' -> '%s' es válido%n", from, to);
    }

    static String getBranchType(String branch) {
        if (branch.startsWith("hotfix/"))  return "hotfix";
        if (branch.startsWith("feature/")) return "feature";
        if (branch.startsWith("release/")) return "release";
        return "other";
    }

    record MergeAttempt(String from, String to, boolean shouldFail) {}

    public static void main(String[] args) {
        List<MergeAttempt> attempts = List.of(
            // Válidos
            new MergeAttempt("hotfix/critical-bug",    "main",    false),
            new MergeAttempt("hotfix/critical-bug",    "develop", false),
            new MergeAttempt("feature/user-profile",   "develop", false),
            new MergeAttempt("release/2.0.0",          "main",    false),
            new MergeAttempt("release/2.0.0",          "develop", false),
            // Inválidos
            new MergeAttempt("feature/user-profile",   "main",    true),
            new MergeAttempt("feature/payment",        "release/1.0", true),
            new MergeAttempt("hotfix/security-patch",  "feature/login", true),
            new MergeAttempt("release/3.0.0",          "feature/admin", true)
        );

        System.out.println("=== GitFlow Validation ===");
        System.out.println();

        for (MergeAttempt attempt : attempts) {
            try {
                validateMerge(attempt.from(), attempt.to());
            } catch (GitFlowViolationException e) {
                System.out.printf("[BLOQUEADO] %s%n", e.getMessage());
            }
        }

        System.out.println();
        System.out.println("=== Resumen de reglas GitFlow ===");
        System.out.println("  hotfix/*  -> main, develop");
        System.out.println("  feature/* -> develop");
        System.out.println("  release/* -> main, develop");
    }
}
