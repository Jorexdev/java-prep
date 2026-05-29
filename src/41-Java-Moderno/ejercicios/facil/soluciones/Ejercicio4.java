import java.util.List;

public class Ejercicio4 {

    sealed interface Figura permits Circulo, Rectangulo, Triangulo {}
    record Circulo(double radio) implements Figura {}
    record Rectangulo(double ancho, double alto) implements Figura {}
    record Triangulo(double base, double altura) implements Figura {}

    static double area(Figura f) {
        // Switch expression exhaustivo: no necesita default
        // porque el compilador sabe que Figura solo tiene 3 subtipos (sealed)
        return switch (f) {
            case Circulo c    -> Math.PI * c.radio() * c.radio();
            case Rectangulo r -> r.ancho() * r.alto();
            case Triangulo t  -> 0.5 * t.base() * t.altura();
        };
    }

    public static void main(String[] args) {
        List<Figura> figuras = List.of(
            new Circulo(5),
            new Rectangulo(4, 6),
            new Triangulo(3, 8),
            new Circulo(1),
            new Rectangulo(10, 10)
        );

        System.out.println("=== Areas con switch exhaustivo ===");
        figuras.forEach(f ->
            System.out.printf("  %-30s -> area = %.2f%n", f.toString(), area(f))
        );
    }
}
