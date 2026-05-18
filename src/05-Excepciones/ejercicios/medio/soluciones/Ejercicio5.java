public class Ejercicio5 {

    static class ErrorConCodigo extends RuntimeException {
        private final int codigo;

        ErrorConCodigo(int codigo, String mensaje) {
            super(mensaje);
            this.codigo = codigo;
        }

        int getCodigo() {
            return codigo;
        }
    }

    static String buscar(String id) {
        if (id == null || id.isBlank()) {
            throw new ErrorConCodigo(404, "No encontrado: " + id);
        }
        if (id.equals("admin")) {
            throw new ErrorConCodigo(403, "Acceso denegado para: " + id);
        }
        return "Recurso-" + id;
    }

    public static void main(String[] args) {
        // Caso 404
        try {
            buscar("");
        } catch (ErrorConCodigo e) {
            System.out.println("Codigo HTTP: " + e.getCodigo());
            System.out.println("Mensaje: " + e.getMessage());
        }

        System.out.println();

        // Caso 403
        try {
            buscar("admin");
        } catch (ErrorConCodigo e) {
            System.out.println("Codigo HTTP: " + e.getCodigo());
            System.out.println("Mensaje: " + e.getMessage());
        }

        System.out.println();

        // Caso exitoso
        try {
            System.out.println("Encontrado: " + buscar("user1"));
        } catch (ErrorConCodigo e) {
            System.out.println("Error " + e.getCodigo() + ": " + e.getMessage());
        }
    }
}
