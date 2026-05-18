public class Ejercicio3 {

    static class DatabaseException extends RuntimeException {
        DatabaseException(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }

    static void cargarDatos() {
        // Simula un fallo de base de datos
        throw new RuntimeException("Error BD: timeout de conexion");
    }

    static void procesarDatos() {
        try {
            cargarDatos();
        } catch (RuntimeException e) {
            // Encadenar: la causa original queda preservada
            throw new DatabaseException("Error cargando datos", e);
        }
    }

    public static void main(String[] args) {
        try {
            procesarDatos();
        } catch (DatabaseException e) {
            System.out.println("DatabaseException: " + e.getMessage());
            System.out.println("Causa original: " + e.getCause().getMessage());
            System.out.println("Tipo de causa: " + e.getCause().getClass().getSimpleName());
        }
    }
}
