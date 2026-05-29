import java.util.List;

public class Ejercicio4 {

    // =================== JERARQUIA DE FORMAS (3 niveles, 8 tipos) ===================
    sealed interface Forma permits FormaBasica, FormaCompuesta {}

    // Nivel 2: FormaBasica (simple) y FormaCompuesta (agregacion)
    sealed interface FormaBasica extends Forma
        permits Circulo, Cuadrado, Rectangulo, TrianguloEq, TrianguloRect {}
    sealed interface FormaCompuesta extends Forma
        permits Anillo, SemiCirculo, TrapecioRect {}

    record Circulo(double radio) implements FormaBasica {}
    record Cuadrado(double lado) implements FormaBasica {}
    record Rectangulo(double ancho, double alto) implements FormaBasica {}
    record TrianguloEq(double lado) implements FormaBasica {}       // equilátero
    record TrianguloRect(double catA, double catB) implements FormaBasica {} // rectángulo

    record Anillo(double radioExt, double radioInt) implements FormaCompuesta {}
    record SemiCirculo(double radio) implements FormaCompuesta {}
    record TrapecioRect(double baseGrande, double basePeq, double altura) implements FormaCompuesta {}

    // =================== VISITOR TRADICIONAL ===================
    interface FormaVisitor<R> {
        R visitCirculo(Circulo c);
        R visitCuadrado(Cuadrado c);
        R visitRectangulo(Rectangulo r);
        R visitTrianguloEq(TrianguloEq t);
        R visitTrianguloRect(TrianguloRect t);
        R visitAnillo(Anillo a);
        R visitSemiCirculo(SemiCirculo s);
        R visitTrapecioRect(TrapecioRect t);
    }

    // Visitor de area
    static final FormaVisitor<Double> AREA_VISITOR = new FormaVisitor<>() {
        public Double visitCirculo(Circulo c)         { return Math.PI * c.radio() * c.radio(); }
        public Double visitCuadrado(Cuadrado c)       { return c.lado() * c.lado(); }
        public Double visitRectangulo(Rectangulo r)   { return r.ancho() * r.alto(); }
        public Double visitTrianguloEq(TrianguloEq t) { return (Math.sqrt(3)/4) * t.lado() * t.lado(); }
        public Double visitTrianguloRect(TrianguloRect t) { return 0.5 * t.catA() * t.catB(); }
        public Double visitAnillo(Anillo a) {
            return Math.PI * (a.radioExt() * a.radioExt() - a.radioInt() * a.radioInt());
        }
        public Double visitSemiCirculo(SemiCirculo s) { return 0.5 * Math.PI * s.radio() * s.radio(); }
        public Double visitTrapecioRect(TrapecioRect t) {
            return 0.5 * (t.baseGrande() + t.basePeq()) * t.altura();
        }
    };

    // Visitor de descripcion
    static final FormaVisitor<String> DESC_VISITOR = new FormaVisitor<>() {
        public String visitCirculo(Circulo c)         { return "Circulo(r=" + c.radio() + ")"; }
        public String visitCuadrado(Cuadrado c)       { return "Cuadrado(l=" + c.lado() + ")"; }
        public String visitRectangulo(Rectangulo r)   { return "Rect(" + r.ancho() + "x" + r.alto() + ")"; }
        public String visitTrianguloEq(TrianguloEq t) { return "TriEq(l=" + t.lado() + ")"; }
        public String visitTrianguloRect(TrianguloRect t) { return "TriRect(" + t.catA() + "," + t.catB() + ")"; }
        public String visitAnillo(Anillo a)           { return "Anillo(R=" + a.radioExt() + ",r=" + a.radioInt() + ")"; }
        public String visitSemiCirculo(SemiCirculo s) { return "SemiCirculo(r=" + s.radio() + ")"; }
        public String visitTrapecioRect(TrapecioRect t) { return "Trapecio(" + t.baseGrande() + "," + t.basePeq() + ",h=" + t.altura() + ")"; }
    };

    // Double dispatch
    static <R> R accept(Forma f, FormaVisitor<R> visitor) {
        return switch (f) {
            case Circulo c        -> visitor.visitCirculo(c);
            case Cuadrado c       -> visitor.visitCuadrado(c);
            case Rectangulo r     -> visitor.visitRectangulo(r);
            case TrianguloEq t    -> visitor.visitTrianguloEq(t);
            case TrianguloRect t  -> visitor.visitTrianguloRect(t);
            case Anillo a         -> visitor.visitAnillo(a);
            case SemiCirculo s    -> visitor.visitSemiCirculo(s);
            case TrapecioRect t   -> visitor.visitTrapecioRect(t);
        };
    }

    // =================== PATTERN MATCHING (sin Visitor) ===================
    static double areaPM(Forma f) {
        return switch (f) {
            case Circulo(var r)              -> Math.PI * r * r;
            case Cuadrado(var l)             -> l * l;
            case Rectangulo(var a, var b)    -> a * b;
            case TrianguloEq(var l)          -> (Math.sqrt(3)/4) * l * l;
            case TrianguloRect(var a, var b) -> 0.5 * a * b;
            case Anillo(var re, var ri)      -> Math.PI * (re*re - ri*ri);
            case SemiCirculo(var r)          -> 0.5 * Math.PI * r * r;
            case TrapecioRect(var bg, var bp, var h) -> 0.5 * (bg + bp) * h;
        };
    }

    static double perimetroPM(Forma f) {
        return switch (f) {
            case Circulo(var r)              -> 2 * Math.PI * r;
            case Cuadrado(var l)             -> 4 * l;
            case Rectangulo(var a, var b)    -> 2 * (a + b);
            case TrianguloEq(var l)          -> 3 * l;
            case TrianguloRect(var a, var b) -> a + b + Math.sqrt(a*a + b*b);
            case Anillo(var re, var ri)      -> 2 * Math.PI * (re + ri);
            case SemiCirculo(var r)          -> Math.PI * r + 2 * r;
            case TrapecioRect(var bg, var bp, var h) -> bg + bp + h + Math.sqrt(h*h + Math.pow(bg-bp,2));
        };
    }

    static String describirPM(Forma f) {
        return switch (f) {
            case Circulo(var r)              -> "Circulo(r=" + r + ")";
            case Cuadrado(var l)             -> "Cuadrado(l=" + l + ")";
            case Rectangulo(var a, var b)    -> "Rect(" + a + "x" + b + ")";
            case TrianguloEq(var l)          -> "TriEq(l=" + l + ")";
            case TrianguloRect(var a, var b) -> "TriRect(" + a + "," + b + ")";
            case Anillo(var re, var ri)      -> "Anillo(R=" + re + ",r=" + ri + ")";
            case SemiCirculo(var r)          -> "SemiCirculo(r=" + r + ")";
            case TrapecioRect(var bg, var bp, var h) -> "Trapecio(" + bg + "," + bp + ",h=" + h + ")";
        };
    }

    public static void main(String[] args) {
        List<Forma> formas = List.of(
            new Circulo(5), new Cuadrado(4), new Rectangulo(3, 7),
            new TrianguloEq(6), new TrianguloRect(3, 4),
            new Anillo(10, 6), new SemiCirculo(8), new TrapecioRect(10, 6, 4)
        );

        // Verificacion: ambas implementaciones producen el mismo resultado
        System.out.println("=== Visitor vs Pattern Matching ===\n");
        System.out.printf("%-25s %10s %10s %10s%n", "Forma", "VisitorArea", "PMArea", "Iguales");
        System.out.println("-".repeat(60));
        for (Forma f : formas) {
            double vArea = accept(f, AREA_VISITOR);
            double pmArea = areaPM(f);
            System.out.printf("%-25s %10.2f %10.2f %10s%n",
                describirPM(f), vArea, pmArea, Math.abs(vArea - pmArea) < 1e-9 ? "SI" : "NO");
        }

        // Benchmark JMH-style
        System.out.println("\n=== Benchmark 1.000.000 operaciones ===\n");
        int ITER = 1_000_000;
        double sink = 0; // evitar dead code elimination

        // Warm up
        for (int i = 0; i < 50_000; i++) {
            for (Forma f : formas) {
                sink += accept(f, AREA_VISITOR) + areaPM(f);
            }
        }

        // Visitor
        long t1 = System.nanoTime();
        for (int i = 0; i < ITER; i++) {
            for (Forma f : formas) sink += accept(f, AREA_VISITOR);
        }
        long visitorNs = System.nanoTime() - t1;

        // Pattern Matching
        long t2 = System.nanoTime();
        for (int i = 0; i < ITER; i++) {
            for (Forma f : formas) sink += areaPM(f);
        }
        long pmNs = System.nanoTime() - t2;

        System.out.printf("Visitor:         %,d ns (%,.1f ms)%n", visitorNs, visitorNs/1e6);
        System.out.printf("Pattern Matching:%,d ns (%,.1f ms)%n", pmNs, pmNs/1e6);
        System.out.printf("Speedup PM vs Visitor: %.2fx%n", (double) visitorNs / pmNs);
        System.out.println("(sink=" + (long)sink + " - evita optimizacion del compilador)");

        System.out.println("""

                Analisis:
                - Rendimiento: Pattern Matching suele ser similar o ligeramente mas rapido
                  porque el compilador puede generar tableswitch optimizados para tipos sellados.
                  El Visitor tiene overhead de virtual dispatch doble.
                - Extensibilidad (nuevo tipo): Visitor rompe en compilacion (hay que implementar
                  el nuevo metodo en todos los visitors). Pattern Matching idem si sealed.
                - Extensibilidad (nueva op): Visitor: nueva clase visitor, sin tocar jerarquia.
                  Pattern Matching: nuevo metodo estatico, sin tocar jerarquia.
                - Seguridad: sealed + PM obliga exhaustiveness en compilacion.
                  Visitor con default lanza excepcion en runtime si falta caso.
                """);
    }
}
