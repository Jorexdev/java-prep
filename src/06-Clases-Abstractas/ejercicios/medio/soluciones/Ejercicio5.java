public class Ejercicio5 {
    // PROBLEMA: Cuadrado extends Rectangulo viola LSP
    // setAncho en Cuadrado también cambia alto → el caller que espera Rectangulo se sorprende
    static class RectanguloRoto {
        int ancho, alto;
        void setAncho(int a) { this.ancho = a; }
        void setAlto(int a)  { this.alto  = a; }
        int area() { return ancho * alto; }
    }
    static class CuadradoRoto extends RectanguloRoto {
        @Override void setAncho(int a) { this.ancho = this.alto = a; } // viola LSP
        @Override void setAlto(int a)  { this.ancho = this.alto = a; }
    }
    // SOLUCIÓN: jerarquía plana con Figura abstracta
    abstract static class Figura { abstract int area(); }
    static class Rectangulo extends Figura {
        int ancho, alto;
        Rectangulo(int a, int b) { this.ancho = a; this.alto = b; }
        @Override int area() { return ancho * alto; }
    }
    static class Cuadrado extends Figura {
        int lado;
        Cuadrado(int l) { this.lado = l; }
        @Override int area() { return lado * lado; }
    }
    public static void main(String[] args) {
        System.out.println("=== Violación LSP ===");
        RectanguloRoto r = new CuadradoRoto();
        r.setAncho(5); r.setAlto(3);
        System.out.println("Esperado: 15, Real: " + r.area()); // 9 — incorrecto
        System.out.println("=== Solución LSP ===");
        Figura rect = new Rectangulo(5, 3);
        Figura cuad = new Cuadrado(4);
        System.out.println("Rectángulo 5x3: " + rect.area()); // 15
        System.out.println("Cuadrado 4:     " + cuad.area()); // 16
    }
}
