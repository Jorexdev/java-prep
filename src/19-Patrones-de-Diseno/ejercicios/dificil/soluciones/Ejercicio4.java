public class Ejercicio4 {

    interface Visitor {
        double visitar(Numero n);
        double visitar(Suma s);
        double visitar(Producto p);
        double visitar(Negacion n);
    }

    interface Nodo {
        double aceptar(Visitor v);
    }

    record Numero(double valor) implements Nodo {
        @Override public double aceptar(Visitor v) { return v.visitar(this); }
    }
    record Suma(Nodo izq, Nodo der) implements Nodo {
        @Override public double aceptar(Visitor v) { return v.visitar(this); }
    }
    record Producto(Nodo izq, Nodo der) implements Nodo {
        @Override public double aceptar(Visitor v) { return v.visitar(this); }
    }
    record Negacion(Nodo nodo) implements Nodo {
        @Override public double aceptar(Visitor v) { return v.visitar(this); }
    }

    static class EvaluadorVisitor implements Visitor {
        @Override public double visitar(Numero n)   { return n.valor(); }
        @Override public double visitar(Suma s)     { return s.izq().aceptar(this) + s.der().aceptar(this); }
        @Override public double visitar(Producto p) { return p.izq().aceptar(this) * p.der().aceptar(this); }
        @Override public double visitar(Negacion n) { return -n.nodo().aceptar(this); }
    }

    static class ImpresorVisitor implements Visitor {
        @Override public double visitar(Numero n)   { System.out.print(n.valor()); return 0; }
        @Override public double visitar(Suma s)     { System.out.print("("); s.izq().aceptar(this); System.out.print(" + "); s.der().aceptar(this); System.out.print(")"); return 0; }
        @Override public double visitar(Producto p) { System.out.print("("); p.izq().aceptar(this); System.out.print(" * "); p.der().aceptar(this); System.out.print(")"); return 0; }
        @Override public double visitar(Negacion n) { System.out.print("(-"); n.nodo().aceptar(this); System.out.print(")"); return 0; }
    }

    static class ContadorNodosVisitor implements Visitor {
        private int count = 0;
        int getCount() { return count; }
        @Override public double visitar(Numero n)   { count++; return 0; }
        @Override public double visitar(Suma s)     { count++; s.izq().aceptar(this); s.der().aceptar(this); return 0; }
        @Override public double visitar(Producto p) { count++; p.izq().aceptar(this); p.der().aceptar(this); return 0; }
        @Override public double visitar(Negacion n) { count++; n.nodo().aceptar(this); return 0; }
    }

    public static void main(String[] args) {
        // (3 + 4) * -(2)  = -14
        Nodo ast = new Producto(
            new Suma(new Numero(3), new Numero(4)),
            new Negacion(new Numero(2))
        );

        System.out.println("Resultado: " + ast.aceptar(new EvaluadorVisitor()));

        System.out.print("Expresion: ");
        ast.aceptar(new ImpresorVisitor());
        System.out.println();

        ContadorNodosVisitor contador = new ContadorNodosVisitor();
        ast.aceptar(contador);
        System.out.println("Nodos: " + contador.getCount());
    }
}
