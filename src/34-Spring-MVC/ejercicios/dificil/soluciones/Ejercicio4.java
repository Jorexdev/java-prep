public class Ejercicio4 {

    static class ProblemDetail {
        String type;
        String title;
        int status;
        String detail;
        String instance;

        ProblemDetail(String type, String title, int status, String detail, String instance) {
            this.type = type;
            this.title = title;
            this.status = status;
            this.detail = detail;
            this.instance = instance;
        }

        String toJson() {
            return "{"
                + "\"type\":\"" + type + "\","
                + "\"title\":\"" + title + "\","
                + "\"status\":" + status + ","
                + "\"detail\":\"" + detail + "\","
                + "\"instance\":\"" + instance + "\""
                + "}";
        }
    }

    static class ApiException extends RuntimeException {
        private final ProblemDetail problem;

        ApiException(ProblemDetail problem) {
            super(problem.detail);
            this.problem = problem;
        }

        ProblemDetail getProblem() {
            return problem;
        }
    }

    static class ProblemDetailFactory {

        static ProblemDetail notFound(String resource, int id) {
            return new ProblemDetail(
                "https://api.ejemplo.com/errores/not-found",
                "Recurso no encontrado",
                404,
                resource + " con id " + id + " no existe",
                "/" + resource.toLowerCase() + "/" + id
            );
        }

        static ProblemDetail badRequest(String field, String reason) {
            return new ProblemDetail(
                "https://api.ejemplo.com/errores/bad-request",
                "Petición inválida",
                400,
                "El campo '" + field + "' " + reason,
                "/validation"
            );
        }

        static ProblemDetail conflict(String message) {
            return new ProblemDetail(
                "https://api.ejemplo.com/errores/conflict",
                "Conflicto de datos",
                409,
                message,
                "/conflict"
            );
        }
    }

    static class ErrorHandler {
        String handle(Runnable action) {
            try {
                action.run();
                return null;
            } catch (ApiException e) {
                return e.getProblem().toJson();
            } catch (Exception e) {
                ProblemDetail pd = new ProblemDetail(
                    "https://api.ejemplo.com/errores/internal",
                    "Error interno del servidor",
                    500,
                    e.getMessage(),
                    "/error"
                );
                return pd.toJson();
            }
        }
    }

    public static void main(String[] args) {
        ErrorHandler errorHandler = new ErrorHandler();

        System.out.println("-- Not Found --");
        System.out.println(errorHandler.handle(() -> {
            throw new ApiException(ProblemDetailFactory.notFound("Producto", 42));
        }));

        System.out.println("\n-- Bad Request --");
        System.out.println(errorHandler.handle(() -> {
            throw new ApiException(ProblemDetailFactory.badRequest("precio", "debe ser mayor que 0"));
        }));

        System.out.println("\n-- Conflict --");
        System.out.println(errorHandler.handle(() -> {
            throw new ApiException(ProblemDetailFactory.conflict("Ya existe un producto con ese nombre"));
        }));
    }
}
