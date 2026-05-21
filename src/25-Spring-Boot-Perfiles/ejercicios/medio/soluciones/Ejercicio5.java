// Ejercicio 5 — Profile expression
// Parsear expresiones como "prod & !debug": activo si perfiles contienen "prod" Y NO "debug".

import java.util.Arrays;
import java.util.List;

public class Ejercicio5 {

    // Nodo del árbol de expresión
    interface ProfileExpr {
        boolean matches(List<String> activeProfiles);
        String describe();
    }

    // Expresión simple: el perfil está activo
    static class ProfileExprSimple implements ProfileExpr {
        private final String profile;

        ProfileExprSimple(String profile) {
            this.profile = profile.trim();
        }

        @Override
        public boolean matches(List<String> activeProfiles) {
            return activeProfiles.contains(profile);
        }

        @Override
        public String describe() { return profile; }
    }

    // Negación: !expr
    static class ProfileExprNot implements ProfileExpr {
        private final ProfileExpr inner;

        ProfileExprNot(ProfileExpr inner) {
            this.inner = inner;
        }

        @Override
        public boolean matches(List<String> activeProfiles) {
            return !inner.matches(activeProfiles);
        }

        @Override
        public String describe() { return "!" + inner.describe(); }
    }

    // Conjunción: expr & expr
    static class ProfileExprAnd implements ProfileExpr {
        private final ProfileExpr left;
        private final ProfileExpr right;

        ProfileExprAnd(ProfileExpr left, ProfileExpr right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean matches(List<String> activeProfiles) {
            return left.matches(activeProfiles) && right.matches(activeProfiles);
        }

        @Override
        public String describe() { return "(" + left.describe() + " & " + right.describe() + ")"; }
    }

    // Disyunción: expr | expr
    static class ProfileExprOr implements ProfileExpr {
        private final ProfileExpr left;
        private final ProfileExpr right;

        ProfileExprOr(ProfileExpr left, ProfileExpr right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean matches(List<String> activeProfiles) {
            return left.matches(activeProfiles) || right.matches(activeProfiles);
        }

        @Override
        public String describe() { return "(" + left.describe() + " | " + right.describe() + ")"; }
    }

    /**
     * Parser de expresiones de perfil simplificado.
     * Soporta: profile, !profile, a & b, a | b (sin paréntesis anidados).
     */
    static class ProfileExprParser {
        static ProfileExpr parse(String expression) {
            expression = expression.trim();

            // Dividir por | primero (menor precedencia)
            String[] orParts = expression.split("\\|");
            if (orParts.length > 1) {
                ProfileExpr result = parse(orParts[0].trim());
                for (int i = 1; i < orParts.length; i++) {
                    result = new ProfileExprOr(result, parse(orParts[i].trim()));
                }
                return result;
            }

            // Dividir por &
            String[] andParts = expression.split("&");
            if (andParts.length > 1) {
                ProfileExpr result = parse(andParts[0].trim());
                for (int i = 1; i < andParts.length; i++) {
                    result = new ProfileExprAnd(result, parse(andParts[i].trim()));
                }
                return result;
            }

            // Negación
            if (expression.startsWith("!")) {
                return new ProfileExprNot(parse(expression.substring(1).trim()));
            }

            // Perfil simple
            return new ProfileExprSimple(expression);
        }
    }

    // Bean con expresión de perfil
    static class ConditionalBean {
        private final String name;
        private final ProfileExpr condition;

        ConditionalBean(String name, String expression) {
            this.name = name;
            this.condition = ProfileExprParser.parse(expression);
        }

        boolean isActive(List<String> profiles) {
            return condition.matches(profiles);
        }

        @Override
        public String toString() {
            return name + " [@Profile(\"" + condition.describe() + "\")]";
        }
    }

    static void evaluate(List<String> profiles, List<ConditionalBean> beans) {
        System.out.println("Perfiles activos: " + profiles);
        for (ConditionalBean bean : beans) {
            System.out.printf("  %-45s → %s%n",
                bean.toString(),
                bean.isActive(profiles) ? "ACTIVO" : "inactivo");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("=== Ejercicio 5 — Profile expression ===\n");

        List<ConditionalBean> beans = List.of(
            new ConditionalBean("ProdService",        "prod & !debug"),
            new ConditionalBean("DevOnlyService",     "dev & !prod"),
            new ConditionalBean("DebugPanel",         "dev | debug"),
            new ConditionalBean("MonitoringService",  "prod | staging"),
            new ConditionalBean("AlwaysExcludeProd",  "!prod")
        );

        System.out.println("Beans definidos:");
        beans.forEach(b -> System.out.println("  " + b));
        System.out.println();

        evaluate(List.of("prod"),          beans);
        evaluate(List.of("prod", "debug"), beans);
        evaluate(List.of("dev"),           beans);
        evaluate(List.of("dev", "debug"),  beans);
        evaluate(List.of("staging"),       beans);
        evaluate(List.of("prod", "extra"), beans);

        System.out.println("--- Sintaxis Spring 5.1+ ---");
        System.out.println("@Profile(\"prod & !debug\")   → activo si 'prod' activo Y 'debug' no activo");
        System.out.println("@Profile(\"dev | debug\")     → activo si cualquiera de los dos está activo");
        System.out.println("@Profile(\"!prod\")            → activo en todos los perfiles excepto prod");
    }
}
