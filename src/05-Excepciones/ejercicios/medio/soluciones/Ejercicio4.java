public class Ejercicio4 {

    static class Recurso implements AutoCloseable {
        private final String nombre;

        Recurso(String nombre) {
            this.nombre = nombre;
            System.out.println("Abriendo " + nombre);
        }

        void operar() {
            System.out.println("Operando con " + nombre);
        }

        @Override
        public void close() {
            System.out.println("Cerrando " + nombre);
        }
    }

    public static void main(String[] args) {
        // Los recursos se cierran en orden inverso al de apertura: B primero, luego A
        System.out.println("--- Apertura y cierre de dos recursos ---");
        try (Recurso r1 = new Recurso("A");
             Recurso r2 = new Recurso("B")) {
            r1.operar();
            r2.operar();
        }

        // Output esperado:
        // Abriendo A
        // Abriendo B
        // Operando con A
        // Operando con B
        // Cerrando B   <- orden inverso
        // Cerrando A
    }
}
