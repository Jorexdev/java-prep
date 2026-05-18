public class Ejercicio2 {

    static abstract class Forma {
        abstract double area();
    }

    static class Circulo extends Forma {
        private final double radio;
        Circulo(double radio) { this.radio = radio; }
        @Override double area() { return Math.PI * radio * radio; }
    }

    static class Rectangulo extends Forma {
        private final double ancho, alto;
        Rectangulo(double ancho, double alto) { this.ancho = ancho; this.alto = alto; }
        @Override double area() { return ancho * alto; }
    }

    static class Triangulo extends Forma {
        private final double base, altura;
        Triangulo(double base, double altura) { this.base = base; this.altura = altura; }
        @Override double area() { return base * altura / 2; }
    }

    static class CalculadoraArea {
        double calcular(Forma f) { return f.area(); }
    }

    public static void main(String[] args) {
        CalculadoraArea calc = new CalculadoraArea();
        System.out.printf("Círculo: %.2f%n", calc.calcular(new Circulo(5)));
        System.out.printf("Rectángulo: %.2f%n", calc.calcular(new Rectangulo(4, 6)));
        System.out.printf("Triángulo: %.2f%n", calc.calcular(new Triangulo(3, 8)));
    }
}
