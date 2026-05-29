import java.util.List;

// Sealed Classes (Java 17 estable) — jerarquías cerradas donde el compilador conoce todas las subclases.
// Las subclases deben ser: final (no se puede extender más), sealed (con sus propias permits),
// o non-sealed (reabre la jerarquía).

// Sealed interface: solo estas implementaciones están permitidas
sealed interface Forma permits Circulo, Rectangulo, Triangulo, Poligono {}

// Subclase final: no se puede extender
record Circulo(double radio) implements Forma {
    Circulo {
        if (radio <= 0) throw new IllegalArgumentException("radio debe ser positivo");
    }
    double area() { return Math.PI * radio * radio; }
    double perimetro() { return 2 * Math.PI * radio; }
}

record Rectangulo(double ancho, double alto) implements Forma {
    double area() { return ancho * alto; }
    double perimetro() { return 2 * (ancho + alto); }
}

final class Triangulo implements Forma {
    private final double base;
    private final double altura;
    private final double ladoA, ladoB, ladoC;

    Triangulo(double base, double altura, double ladoA, double ladoB, double ladoC) {
        this.base = base;
        this.altura = altura;
        this.ladoA = ladoA;
        this.ladoB = ladoB;
        this.ladoC = ladoC;
    }

    double area() { return 0.5 * base * altura; }
    double perimetro() { return ladoA + ladoB + ladoC; }

    @Override public String toString() {
        return "Triangulo[base=" + base + ", altura=" + altura + "]";
    }
}

// Subclase sealed: introduce su propia jerarquía cerrada
sealed interface Poligono extends Forma permits Pentagono, Hexagono {}

record Pentagono(double lado) implements Poligono {
    double area() { return (lado * lado * Math.sqrt(25 + 10 * Math.sqrt(5))) / 4; }
}

record Hexagono(double lado) implements Poligono {
    double area() { return (3 * Math.sqrt(3) / 2) * lado * lado; }
}

// Jerarquía sealed para modelar resultados (patrón común)
sealed interface Resultado<T> permits Resultado.Exito, Resultado.Error {
    record Exito<T>(T valor) implements Resultado<T> {}
    record Error<T>(String mensaje, Exception causa) implements Resultado<T> {
        Error(String mensaje) { this(mensaje, null); }
    }

    default boolean esExito() { return this instanceof Exito<T>; }

    default T obtenerODefecto(T defecto) {
        return switch (this) {
            case Exito<T> e -> e.valor();
            case Error<T> err -> defecto;
        };
    }
}

public class ExpSealedClasses {

    static double calcularArea(Forma f) {
        // Switch exhaustivo: el compilador verifica que todos los casos están cubiertos
        return switch (f) {
            case Circulo c      -> Math.PI * c.radio() * c.radio();
            case Rectangulo r   -> r.ancho() * r.alto();
            case Triangulo t    -> t.area();
            case Pentagono p    -> p.area();
            case Hexagono h     -> h.area();
            // No hace falta default: el compilador sabe que no hay más subtipos
        };
    }

    static String describir(Forma f) {
        return switch (f) {
            case Circulo c when c.radio() > 10 -> "circulo grande (r=" + c.radio() + ")";
            case Circulo c                     -> "circulo pequenyo (r=" + c.radio() + ")";
            case Rectangulo r when r.ancho() == r.alto() -> "cuadrado (" + r.ancho() + ")";
            case Rectangulo r                  -> "rectangulo " + r.ancho() + "x" + r.alto();
            case Triangulo t                   -> "triangulo";
            case Pentagono p                   -> "pentagono (lado=" + p.lado() + ")";
            case Hexagono h                    -> "hexagono (lado=" + h.lado() + ")";
        };
    }

    // Método que usa instanceof para routing (antes de switch patterns)
    static String tipoForma(Forma f) {
        if (f instanceof Circulo c) {
            return "Es un circulo con radio " + c.radio();
        } else if (f instanceof Rectangulo r) {
            return "Es un rectangulo " + r.ancho() + "x" + r.alto();
        } else if (f instanceof Poligono p) {
            return "Es un poligono regular";
        }
        return "Forma desconocida";
    }

    static <T> void procesarResultado(Resultado<T> r) {
        switch (r) {
            case Resultado.Exito<T> e  -> System.out.println("  Exito: " + e.valor());
            case Resultado.Error<T> err -> System.out.println("  Error: " + err.mensaje());
        }
    }

    public static void main(String[] args) {

        System.out.println("=== SEALED CLASSES ===\n");

        List<Forma> formas = List.of(
            new Circulo(5),
            new Circulo(15),
            new Rectangulo(4, 4),
            new Rectangulo(3, 7),
            new Triangulo(6, 4, 5, 5, 6),
            new Pentagono(3),
            new Hexagono(2)
        );

        // 1. Switch exhaustivo calculando areas
        System.out.println("--- Areas (switch exhaustivo) ---");
        for (Forma f : formas) {
            System.out.printf("%-35s -> area = %.2f%n", f.toString(), calcularArea(f));
        }

        // 2. Guarded patterns con when
        System.out.println("\n--- Descripcion con guarded patterns ---");
        for (Forma f : formas) {
            System.out.println("  " + describir(f));
        }

        // 3. instanceof con sealed (antes del switch pattern)
        System.out.println("\n--- instanceof con sealed ---");
        System.out.println(tipoForma(new Circulo(3)));
        System.out.println(tipoForma(new Pentagono(4)));

        // 4. Sellado anidado: Poligono es sealed dentro de Forma
        System.out.println("\n--- Subjerarquia sealed (Poligono) ---");
        Poligono pol = new Hexagono(5);
        String desc = switch (pol) {
            case Pentagono p -> "Pentagono de lado " + p.lado();
            case Hexagono h  -> "Hexagono de lado " + h.lado();
            // exhaustivo dentro de Poligono
        };
        System.out.println(desc);

        // 5. Sealed para resultados
        System.out.println("\n--- Sealed Result type ---");
        List<Resultado<Integer>> resultados = List.of(
            new Resultado.Exito<>(42),
            new Resultado.Error<>("division por cero"),
            new Resultado.Exito<>(100)
        );

        resultados.forEach(ExpSealedClasses::procesarResultado);

        Resultado<String> r = new Resultado.Exito<>("ok");
        System.out.println("esExito: " + r.esExito());
        System.out.println("obtenerODefecto: " + r.obtenerODefecto("fallback"));

        Resultado<String> err = new Resultado.Error<>("fallo de red");
        System.out.println("defecto en error: " + err.obtenerODefecto("fallback"));
    }
}
