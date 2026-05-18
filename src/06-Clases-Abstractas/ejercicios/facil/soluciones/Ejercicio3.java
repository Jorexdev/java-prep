public class Ejercicio3 {
    abstract static class Figura { abstract double area(); }
    static class Circulo extends Figura {
        private final double r;
        Circulo(double r) { this.r = r; }
        @Override double area() { return Math.PI * r * r; }
    }
    public static void main(String[] args) {
        // new Figura() → NO compila: 'Figura is abstract; cannot be instantiated'
        // Correcto: usar referencia del tipo abstracto, instancia del concreto
        Figura f = new Circulo(3.0);
        System.out.println("Área: " + f.area());
        System.out.println("Tipo real: " + f.getClass().getSimpleName());
    }
}
