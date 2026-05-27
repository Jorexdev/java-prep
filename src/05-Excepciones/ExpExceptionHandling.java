import java.sql.SQLException;

// ===== Domain exceptions =====

class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

class ServiceException extends RuntimeException {
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    // Constructor que pierde el stack trace original (anti-patrón)
    public ServiceException(String message) {
        super(message);
    }
}

// ===== Repository layer: traduce SQLException → DataAccessException =====

class UserRepository {
    public String findById(int id) throws DataAccessException {
        try {
            simulateSqlQuery(id);
            return "User#" + id;
        } catch (SQLException e) {
            // Exception translation: low-level → domain exception
            throw new DataAccessException("Error al buscar usuario id=" + id, e);
        }
    }

    private void simulateSqlQuery(int id) throws SQLException {
        if (id < 0) throw new SQLException("ID inválido: " + id, "42000", 1064);
    }
}

public class ExpExceptionHandling {

    // ─── 1. Catch-and-rethrow: CON cause vs SIN cause ─────────────────────────
    static void catchAndRethrow() {
        System.out.println("\n── 1. Catch-and-rethrow con vs sin cause ──");

        // BIEN: preserva el stack trace original
        try {
            try {
                throw new SQLException("connection timeout");
            } catch (SQLException e) {
                throw new ServiceException("Fallo en servicio", e); // e como cause → stack trace preservado
            }
        } catch (ServiceException e) {
            System.out.println("CON cause → getCause(): " + e.getCause().getMessage());
        }

        // MAL: pierde el stack trace original — solo queda el mensaje como string
        try {
            try {
                throw new SQLException("connection timeout");
            } catch (SQLException e) {
                throw new ServiceException(e.getMessage()); // solo el mensaje, causa perdida
            }
        } catch (ServiceException e) {
            System.out.println("SIN cause → getCause(): " + e.getCause()); // null
        }
    }

    // ─── 2. Multi-catch ───────────────────────────────────────────────────────
    static void multiCatch() {
        System.out.println("\n── 2. Multi-catch ──");
        for (int scenario = 0; scenario < 3; scenario++) {
            try {
                switch (scenario) {
                    case 0 -> throw new java.io.IOException("Fallo de IO");
                    case 1 -> throw new SQLException("Fallo SQL");
                    case 2 -> System.out.println("Éxito sin excepción");
                }
            } catch (java.io.IOException | SQLException e) {
                // Mismo bloque para ambos tipos — e es efectivamente final
                System.out.println("Multi-catch: " + e.getClass().getSimpleName() + " → " + e.getMessage());
            }
        }
    }

    // ─── 3. Exception translation: SQL → dominio ─────────────────────────────
    static void exceptionTranslation() {
        System.out.println("\n── 3. Exception translation (SQL → DataAccessException) ──");
        UserRepository repo = new UserRepository();

        // ID válido
        try {
            System.out.println("Encontrado: " + repo.findById(42));
        } catch (DataAccessException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // ID inválido → SQLException traducida a DataAccessException
        try {
            repo.findById(-1);
        } catch (DataAccessException e) {
            System.out.println("DataAccessException: " + e.getMessage());
            System.out.println("  causa original: " + e.getCause().getClass().getSimpleName()
                    + " → " + e.getCause().getMessage());
        }
    }

    // ─── 4. finally garantizado — se ejecuta incluso después de return ────────
    static String finallyAfterReturn() {
        System.out.println("\n── 4. finally se ejecuta tras return ──");
        try {
            System.out.println("En try, antes del return");
            return "valor-del-try";
        } finally {
            // Se ejecuta ANTES de que el return entregue el valor al caller
            System.out.println("En finally (se ejecuta siempre)");
        }
    }

    // ─── 5. No capturar Error ni Throwable ────────────────────────────────────
    static void dontCatchError() {
        System.out.println("\n── 5. No capturar Error/Throwable ──");
        /*
         * NUNCA hacer:
         *   catch (Error e) { ... }       // OutOfMemoryError, StackOverflowError...
         *   catch (Throwable t) { ... }   // engloba Error + Exception
         *
         * Razón: los Error indican problemas irrecuperables de la JVM.
         * Capturarlos puede dejar la aplicación en un estado inconsistente.
         * La JVM debe poder terminar limpiamente.
         *
         * EXCEPCIÓN aceptable:
         *   En frameworks/contenedores (Spring, servidores) a veces se captura
         *   Throwable en el nivel más alto solo para loguear antes de propagar.
         */
        System.out.println("No capturamos Error ni Throwable (ver comentario en código)");
    }

    // ─── 6. Checked vs Unchecked: cuándo usar cada uno ────────────────────────
    static void checkedVsUnchecked() {
        System.out.println("\n── 6. Checked vs Unchecked ──");
        /*
         * CHECKED (extends Exception):
         *   - El caller PUEDE recuperarse del problema.
         *   - Ejemplos: IOException, SQLException, FileNotFoundException.
         *   - Úsala cuando forzar al caller a pensar en el error tiene sentido.
         *
         * UNCHECKED (extends RuntimeException):
         *   - Error de programación o estado inesperado — el caller NO puede recuperarse.
         *   - Ejemplos: NullPointerException, IllegalArgumentException, DataAccessException.
         *   - Úsala en capas internas: evita polucionar la API con throws checked.
         *
         * Regla práctica moderna (Spring, JPA):
         *   Las capas de infraestructura usan unchecked para no forzar
         *   try-catch en toda la cadena de llamadas.
         */
        System.out.println("Checked  → el caller puede y debe recuperarse");
        System.out.println("Unchecked → error de programación o fallo irrecuperable");
    }

    public static void main(String[] args) {
        catchAndRethrow();
        multiCatch();
        exceptionTranslation();
        String result = finallyAfterReturn();
        System.out.println("  return recibido: " + result);
        dontCatchError();
        checkedVsUnchecked();
    }
}
