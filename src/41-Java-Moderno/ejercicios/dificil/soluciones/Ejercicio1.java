import java.util.*;

public class Ejercicio1 {

    // AST sealed con todas las variantes de expresion
    sealed interface Expr permits Num, Var, Add, Sub, Mul, Div, Pow, Let, Sqrt {}
    record Num(double value) implements Expr {}
    record Var(String name) implements Expr {}
    record Add(Expr left, Expr right) implements Expr {}
    record Sub(Expr left, Expr right) implements Expr {}
    record Mul(Expr left, Expr right) implements Expr {}
    record Div(Expr left, Expr right) implements Expr {}
    record Pow(Expr base, Expr exp) implements Expr {}
    record Let(String name, Expr value, Expr body) implements Expr {}
    record Sqrt(Expr expr) implements Expr {}

    // eval: evalúa la expresión en el entorno dado
    static double eval(Expr e, Map<String, Double> env) {
        return switch (e) {
            case Num(var v) -> v;
            case Var(var n) -> {
                if (!env.containsKey(n))
                    throw new IllegalStateException("Variable no definida: '" + n + "'");
                yield env.get(n);
            }
            case Add(var l, var r) -> eval(l, env) + eval(r, env);
            case Sub(var l, var r) -> eval(l, env) - eval(r, env);
            case Mul(var l, var r) -> eval(l, env) * eval(r, env);
            case Div(var l, var r) -> {
                double divisor = eval(r, env);
                if (divisor == 0) throw new ArithmeticException("Division por cero");
                yield eval(l, env) / divisor;
            }
            case Pow(var b, var exp) -> Math.pow(eval(b, env), eval(exp, env));
            case Sqrt(var inner)  -> {
                double v = eval(inner, env);
                if (v < 0) throw new ArithmeticException("Raiz de negativo: " + v);
                yield Math.sqrt(v);
            }
            case Let(var name, var value, var body) -> {
                // Extiende el entorno con la nueva variable (sin mutar el original)
                Map<String, Double> extendido = new HashMap<>(env);
                extendido.put(name, eval(value, env));
                yield eval(body, extendido);
            }
        };
    }

    // prettyPrint: representación legible
    static String prettyPrint(Expr e) {
        return switch (e) {
            case Num(var v) -> v == Math.floor(v) && !Double.isInfinite(v)
                ? String.valueOf((long) v) : String.valueOf(v);
            case Var(var n) -> n;
            case Add(var l, var r) -> "(" + prettyPrint(l) + " + " + prettyPrint(r) + ")";
            case Sub(var l, var r) -> "(" + prettyPrint(l) + " - " + prettyPrint(r) + ")";
            case Mul(var l, var r) -> "(" + prettyPrint(l) + " * " + prettyPrint(r) + ")";
            case Div(var l, var r) -> "(" + prettyPrint(l) + " / " + prettyPrint(r) + ")";
            case Pow(var b, var exp) -> prettyPrint(b) + "^" + prettyPrint(exp);
            case Sqrt(var inner) -> "sqrt(" + prettyPrint(inner) + ")";
            case Let(var n, var v, var b) ->
                "let " + n + " = " + prettyPrint(v) + " in " + prettyPrint(b);
        };
    }

    // vars: variables libres (no ligadas por Let)
    static Set<String> vars(Expr e) {
        return switch (e) {
            case Num n                  -> Set.of();
            case Var(var n)             -> Set.of(n);
            case Add(var l, var r)      -> union(vars(l), vars(r));
            case Sub(var l, var r)      -> union(vars(l), vars(r));
            case Mul(var l, var r)      -> union(vars(l), vars(r));
            case Div(var l, var r)      -> union(vars(l), vars(r));
            case Pow(var b, var exp)    -> union(vars(b), vars(exp));
            case Sqrt(var inner)        -> vars(inner);
            case Let(var name, var v, var b) -> {
                // La variable 'name' está ligada en el body -> se elimina
                Set<String> bodyVars = new HashSet<>(vars(b));
                bodyVars.remove(name);
                yield union(vars(v), bodyVars);
            }
        };
    }

    private static Set<String> union(Set<String> a, Set<String> b) {
        Set<String> result = new HashSet<>(a);
        result.addAll(b);
        return Collections.unmodifiableSet(result);
    }

    public static void main(String[] args) {
        System.out.println("=== Interprete de Expresiones ===\n");

        Map<String, Double> envVacio = Map.of();

        // 1. let x = 3 in let y = 4 in sqrt(x^2 + y^2)
        Expr pitagoras = new Let("x", new Num(3),
            new Let("y", new Num(4),
                new Sqrt(new Add(
                    new Pow(new Var("x"), new Num(2)),
                    new Pow(new Var("y"), new Num(2))
                ))
            )
        );
        System.out.println("Expresion: " + prettyPrint(pitagoras));
        System.out.printf("Resultado: %.4f%n", eval(pitagoras, envVacio));
        System.out.println("Variables libres: " + vars(pitagoras));

        // 2. Variable libre: x + y con env={x:10, y:5}
        System.out.println();
        Expr sumaVars = new Add(new Var("x"), new Var("y"));
        Map<String, Double> env = Map.of("x", 10.0, "y", 5.0);
        System.out.println("Expresion: " + prettyPrint(sumaVars));
        System.out.println("Variables libres: " + vars(sumaVars));
        System.out.println("eval con {x=10, y=5}: " + eval(sumaVars, env));

        // 3. Variable no definida -> excepcion
        System.out.println();
        try {
            eval(new Var("z"), envVacio);
        } catch (IllegalStateException ex) {
            System.out.println("Variable no definida correctamente: " + ex.getMessage());
        }

        // 4. Division por cero
        System.out.println();
        try {
            eval(new Div(new Num(5), new Num(0)), envVacio);
        } catch (ArithmeticException ex) {
            System.out.println("Division por cero capturada: " + ex.getMessage());
        }

        // 5. Expresion compleja: (let x = 2 in x^3) * (3 + 4)
        System.out.println();
        Expr compleja = new Mul(
            new Let("x", new Num(2), new Pow(new Var("x"), new Num(3))),
            new Add(new Num(3), new Num(4))
        );
        System.out.println("Expresion: " + prettyPrint(compleja));
        System.out.printf("Resultado: %.1f%n", eval(compleja, envVacio)); // 8 * 7 = 56

        // 6. Let anidado verificando scope: x en valor de y no se ve afectado por el x interior
        System.out.println();
        Expr scope = new Let("x", new Num(10),
            new Let("x", new Num(20),
                new Var("x")  // debe ver x=20 (el más cercano)
            )
        );
        System.out.println("Shadowing: " + prettyPrint(scope));
        System.out.println("Resultado (esperado 20): " + eval(scope, envVacio));
    }
}
