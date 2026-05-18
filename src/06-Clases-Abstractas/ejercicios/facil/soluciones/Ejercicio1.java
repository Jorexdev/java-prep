import java.util.List;
public class Ejercicio1 {
    abstract static class Figura {
        abstract double area();
        void describir() { System.out.printf("Área: %.2f%n", area()); }
    }
    static class Circulo extends Figura {
        private final double radio;
        Circulo(double radio) { this.radio = radio; }
        @Override double area() { return Math.PI * radio * radio; }
    }
    static class Rectangulo extends Figura {
        private final double ancho, alto;
        Rectangulo(double ancho, double alto) { this.ancho = ancho; this.alto = alto; }
        @Override double area() { return ancho * alto; }
    }
    public static void main(String[] args) {
        List<Figura> figuras = List.of(new Circulo(5), new Rectangulo(4, 6));
        figuras.forEach(f -> { System.out.print(f.getClass().getSimpleName() + " — "); f.describir(); });
    }
}
