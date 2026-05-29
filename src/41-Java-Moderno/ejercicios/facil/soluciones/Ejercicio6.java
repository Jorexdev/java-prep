public class Ejercicio6 {

    record Rango(int min, int max) {
        Rango {
            if (min > max)
                throw new IllegalArgumentException(
                    "min (" + min + ") no puede ser mayor que max (" + max + ")"
                );
        }

        boolean contiene(int valor) {
            return valor >= min && valor <= max;
        }

        int longitud() {
            return max - min;
        }

        boolean solapaCon(Rango otro) {
            // Se solapan si el inicio de uno no está después del fin del otro
            return this.min <= otro.max && otro.min <= this.max;
        }
    }

    public static void main(String[] args) {
        Rango r1 = new Rango(1, 10);
        Rango r2 = new Rango(5, 15);
        Rango r3 = new Rango(11, 20);

        System.out.println("=== Record Rango ===");
        System.out.println("r1 = " + r1);
        System.out.println("r2 = " + r2);
        System.out.println("r3 = " + r3);

        System.out.println("\n--- contiene ---");
        System.out.println("r1.contiene(5): " + r1.contiene(5));   // true
        System.out.println("r1.contiene(11): " + r1.contiene(11)); // false
        System.out.println("r2.contiene(7): " + r2.contiene(7));   // true

        System.out.println("\n--- longitud ---");
        System.out.println("r1.longitud(): " + r1.longitud()); // 9
        System.out.println("r2.longitud(): " + r2.longitud()); // 10

        System.out.println("\n--- solapaCon ---");
        System.out.println("r1 solapa r2: " + r1.solapaCon(r2)); // true: [1-10] y [5-15]
        System.out.println("r1 solapa r3: " + r1.solapaCon(r3)); // false: [1-10] y [11-20]
        System.out.println("r2 solapa r3: " + r2.solapaCon(r3)); // true: [5-15] y [11-20]

        System.out.println("\n--- Validacion en compact constructor ---");
        try {
            new Rango(10, 5);
        } catch (IllegalArgumentException e) {
            System.out.println("Error capturado: " + e.getMessage());
        }

        // Rango de un solo punto: min == max es valido
        Rango punto = new Rango(7, 7);
        System.out.println("Rango punto: " + punto + ", longitud: " + punto.longitud());
    }
}
