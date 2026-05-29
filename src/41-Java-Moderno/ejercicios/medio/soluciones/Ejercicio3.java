public class Ejercicio3 {

    sealed interface Expr permits Literal, Suma, Multiplicacion, Negacion {}
    record Literal(double valor) implements Expr {}
    record Suma(Expr izq, Expr der) implements Expr {}
    record Multiplicacion(Expr izq, Expr der) implements Expr {}
    record Negacion(Expr expr) implements Expr {}

    // eval: sin Visitor, sin double dispatch — switch + pattern matching
    static double eval(Expr e) {
        return switch (e) {
            case Literal(var v)                 -> v;
            case Negacion(var hijo)             -> -eval(hijo);
            case Suma(var i, var d)             -> eval(i) + eval(d);
            case Multiplicacion(var i, var d)   -> eval(i) * eval(d);
        };
    }

    // prettyPrint: representación legible con paréntesis
    static String prettyPrint(Expr e) {
        return switch (e) {
            case Literal(var v) -> {
                // Mostrar entero si no tiene parte decimal
                yield v == Math.floor(v) ? String.valueOf((int) v) : String.valueOf(v);
            }
            case Negacion(var hijo)           -> "-(" + prettyPrint(hijo) + ")";
            case Suma(var i, var d)           -> "(" + prettyPrint(i) + " + " + prettyPrint(d) + ")";
            case Multiplicacion(var i, var d) -> "(" + prettyPrint(i) + " * " + prettyPrint(d) + ")";
        };
    }

    // contar: número de nodos en el árbol
    static int contar(Expr e) {
        return switch (e) {
            case Literal l                      -> 1;
            case Negacion(var hijo)             -> 1 + contar(hijo);
            case Suma(var i, var d)             -> 1 + contar(i) + contar(d);
            case Multiplicacion(var i, var d)   -> 1 + contar(i) + contar(d);
        };
    }

    public static void main(String[] args) {
        System.out.println("=== Visitor sin double dispatch ===\n");

        // (3 + 4) * -(2)
        Expr expr = new Multiplicacion(
            new Suma(new Literal(3), new Literal(4)),
            new Negacion(new Literal(2))
        );

        System.out.println("Expresion: " + prettyPrint(expr));
        System.out.println("Resultado: " + eval(expr));         // -14.0
        System.out.println("Nodos: " + contar(expr));           // 5

        System.out.println();

        // (10 + -5) * (2 + 3)
        Expr expr2 = new Multiplicacion(
            new Suma(new Literal(10), new Negacion(new Literal(5))),
            new Suma(new Literal(2), new Literal(3))
        );
        System.out.println("Expresion: " + prettyPrint(expr2));
        System.out.println("Resultado: " + eval(expr2));        // 25.0
        System.out.println("Nodos: " + contar(expr2));          // 7

        System.out.println();

        // Literal simple
        Expr expr3 = new Literal(42);
        System.out.println("Expresion: " + prettyPrint(expr3));
        System.out.println("Resultado: " + eval(expr3));
        System.out.println("Nodos: " + contar(expr3));
    }
}
