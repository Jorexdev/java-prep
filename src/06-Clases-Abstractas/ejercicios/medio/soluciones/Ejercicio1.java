public class Ejercicio1 {
    // OPCIÓN A: abstract class — tiene estado (campo color)
    // Usar cuando los subtipos comparten estado o lógica concreta
    abstract static class FormaClase {
        protected final String color;
        FormaClase(String color) { this.color = color; }
        String getColor() { return color; }
        abstract double area();
    }
    static class CuadradoA extends FormaClase {
        private final double lado;
        CuadradoA(String color, double lado) { super(color); this.lado = lado; }
        @Override double area() { return lado * lado; }
    }
    // OPCIÓN B: interface — sin estado, solo contrato
    // Usar cuando múltiples clases no relacionadas deben cumplir el mismo contrato
    interface FormaInterfaz {
        double area();
        default String tipo() { return "forma"; }
    }
    static class CuadradoB implements FormaInterfaz {
        private final double lado;
        CuadradoB(double lado) { this.lado = lado; }
        @Override public double area() { return lado * lado; }
    }
    public static void main(String[] args) {
        CuadradoA a = new CuadradoA("rojo", 4.0);
        System.out.println("Abstract class — área: " + a.area() + ", color: " + a.getColor());
        CuadradoB b = new CuadradoB(4.0);
        System.out.println("Interface      — área: " + b.area() + ", tipo: " + b.tipo());
    }
}
