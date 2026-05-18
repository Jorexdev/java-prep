public class Ejercicio3 {
    static class Recurso implements AutoCloseable {
        Recurso() { System.out.println("Recurso abierto"); }
        public void usar() { throw new RuntimeException("Error en operación"); }
        @Override public void close() { throw new RuntimeException("Error al cerrar"); }
    }
    public static void main(String[] args) {
        try (Recurso r = new Recurso()) {
            r.usar(); // lanza excepción principal
        } catch (RuntimeException e) {
            System.out.println("Excepción principal: " + e.getMessage());
            System.out.println("Excepciones suprimidas:");
            for (Throwable suprimida : e.getSuppressed()) {
                System.out.println("  → " + suprimida.getMessage());
            }
        }
    }
}
