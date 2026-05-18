public class Ejercicio5 {

    static class Recurso implements AutoCloseable {
        private final String nombre;

        Recurso(String nombre) {
            this.nombre = nombre;
            System.out.println("Recurso [" + nombre + "] creado");
        }

        void usar() {
            System.out.println("Usando recurso [" + nombre + "]");
        }

        @Override
        public void close() {
            System.out.println("Recurso cerrado [" + nombre + "]");
        }
    }

    static class RecursoFallido implements AutoCloseable {
        RecursoFallido() {
            System.out.println("RecursoFallido creado");
        }

        void usar() {
            System.out.println("Usando RecursoFallido...");
            throw new RuntimeException("Fallo al usar el recurso");
        }

        @Override
        public void close() {
            System.out.println("RecursoFallido cerrado (aunque uso() haya fallado)");
        }
    }

    public static void main(String[] args) {
        // Caso normal: close() se llama automaticamente al salir del bloque
        System.out.println("--- Caso normal ---");
        try (Recurso r = new Recurso("A")) {
            r.usar();
        }

        System.out.println();

        // Caso con excepcion: close() igualmente se llama
        System.out.println("--- Caso con excepcion ---");
        try (RecursoFallido rf = new RecursoFallido()) {
            rf.usar();
        } catch (RuntimeException e) {
            System.out.println("Excepcion capturada: " + e.getMessage());
        }
    }
}
