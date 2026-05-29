public class Ejercicio1 {

    record Punto(double x, double y) {
        static final Punto ORIGEN = new Punto(0, 0);

        Punto {
            if (Double.isNaN(x) || Double.isNaN(y))
                throw new IllegalArgumentException("Las coordenadas no pueden ser NaN");
        }

        double distanciaAlOrigen() {
            return Math.sqrt(x * x + y * y);
        }
    }

    public static void main(String[] args) {
        Punto p1 = new Punto(3, 4);
        Punto p2 = new Punto(3, 4);
        Punto p3 = new Punto(0, 5);

        // toString generado: Punto[x=3.0, y=4.0]
        System.out.println("p1 = " + p1);
        System.out.println("p2 = " + p2);

        // equals estructural: compara valores, no identidad
        System.out.println("p1.equals(p2): " + p1.equals(p2));   // true
        System.out.println("p1 == p2: " + (p1 == p2));           // false

        // Accessors sin prefijo 'get'
        System.out.println("p1.x() = " + p1.x());
        System.out.println("p1.y() = " + p1.y());

        // Método custom
        System.out.println("distancia al origen: " + p1.distanciaAlOrigen()); // 5.0
        System.out.println("distancia p3 al origen: " + p3.distanciaAlOrigen()); // 5.0

        // Campo estático
        System.out.println("ORIGEN = " + Punto.ORIGEN);

        // Validación en compact constructor
        try {
            new Punto(Double.NaN, 1);
        } catch (IllegalArgumentException e) {
            System.out.println("Validacion OK: " + e.getMessage());
        }
    }
}
