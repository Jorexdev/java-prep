import java.util.List;

public class Ejercicio2 {

    sealed interface Figura permits Circulo, Rectangulo, Triangulo {}

    record Circulo(double radio) implements Figura {}
    record Rectangulo(double ancho, double alto) implements Figura {}
    record Triangulo(double base, double altura) implements Figura {}

    public static void main(String[] args) {
        List<Figura> figuras = List.of(
            new Circulo(5),
            new Rectangulo(4, 6),
            new Triangulo(3, 8)
        );

        System.out.println("=== Figuras ===");
        figuras.forEach(System.out::println);

        // Los records como subclases sealed son final implícitamente
        // El compilador conoce todas las variantes: Circulo, Rectangulo, Triangulo
        System.out.println("\nTipos:");
        for (Figura f : figuras) {
            System.out.println("  " + f.getClass().getSimpleName());
        }
    }
}
