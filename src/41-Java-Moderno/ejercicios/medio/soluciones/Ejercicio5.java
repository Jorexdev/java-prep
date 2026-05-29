public class Ejercicio5 {

    sealed interface Expr permits Literal, Suma, Multiplicacion, Negacion {}
    record Literal(double valor) implements Expr {}
    record Suma(Expr izq, Expr der) implements Expr {}
    record Multiplicacion(Expr izq, Expr der) implements Expr {}
    record Negacion(Expr expr) implements Expr {}

    static String print(Expr e) {
        return switch (e) {
            case Literal(var v)             -> v == Math.floor(v) ? String.valueOf((int)v) : String.valueOf(v);
            case Negacion(var h)            -> "-(" + print(h) + ")";
            case Suma(var i, var d)         -> "(" + print(i) + " + " + print(d) + ")";
            case Multiplicacion(var i, var d) -> "(" + print(i) + " * " + print(d) + ")";
        };
    }

    // simplificar: aplica reglas algebraicas con deconstruction patterns anidados
    static Expr simplificar(Expr e) {
        return switch (e) {
            // 0 + x → x
            case Suma(Literal(0), var x)            -> simplificar(x);
            // x + 0 → x
            case Suma(var x, Literal(0))            -> simplificar(x);
            // 1 * x → x
            case Multiplicacion(Literal(1), var x)  -> simplificar(x);
            // x * 1 → x
            case Multiplicacion(var x, Literal(1))  -> simplificar(x);
            // 0 * x → 0
            case Multiplicacion(Literal(0), var x)  -> new Literal(0);
            // x * 0 → 0
            case Multiplicacion(var x, Literal(0))  -> new Literal(0);
            // -(-x) → x
            case Negacion(Negacion(var x))           -> simplificar(x);
            // Nodos compuestos: simplificar recursivamente
            case Suma(var i, var d)                 -> new Suma(simplificar(i), simplificar(d));
            case Multiplicacion(var i, var d)       -> new Multiplicacion(simplificar(i), simplificar(d));
            case Negacion(var h)                    -> new Negacion(simplificar(h));
            // Literal: ya es simple
            case Literal l                          -> l;
        };
    }

    static void demostrar(String desc, Expr expr) {
        Expr simplificado = simplificar(expr);
        System.out.printf("  %-30s -> %s%n", print(expr), print(simplificado));
    }

    public static void main(String[] args) {
        System.out.println("=== Simplificacion con deconstruction patterns anidados ===\n");

        Literal cero = new Literal(0);
        Literal uno  = new Literal(1);
        Literal tres = new Literal(3);
        Literal cinco = new Literal(5);

        demostrar("0 + x",     new Suma(cero, tres));
        demostrar("x + 0",     new Suma(tres, cero));
        demostrar("1 * x",     new Multiplicacion(uno, cinco));
        demostrar("x * 1",     new Multiplicacion(cinco, uno));
        demostrar("0 * x",     new Multiplicacion(cero, cinco));
        demostrar("x * 0",     new Multiplicacion(tres, cero));
        demostrar("-(-x)",     new Negacion(new Negacion(tres)));
        demostrar("0 + (1*x)", new Suma(cero, new Multiplicacion(uno, cinco)));
        demostrar("-(-(-x))",  new Negacion(new Negacion(new Negacion(tres))));
        demostrar("(3+0)*(1*5)", new Multiplicacion(
            new Suma(tres, cero),
            new Multiplicacion(uno, cinco)
        ));

        // Sin simplificacion aplicable
        demostrar("(3 + 5)",   new Suma(tres, cinco));
        demostrar("(3 * 5)",   new Multiplicacion(tres, cinco));
    }
}
