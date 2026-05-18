public class Ejercicio1 {

    // Jerarquia de excepciones
    static class AppException extends RuntimeException {
        AppException(String mensaje) {
            super(mensaje);
        }
    }

    static class ValidationException extends AppException {
        ValidationException(String mensaje) {
            super("Validacion: " + mensaje);
        }
    }

    static class NotFoundException extends AppException {
        NotFoundException(String recurso) {
            super("No encontrado: " + recurso);
        }
    }

    static void validar(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ValidationException("el valor no puede ser nulo o vacio");
        }
    }

    static String buscar(String id) {
        if (id.equals("999")) {
            throw new NotFoundException("usuario con id=" + id);
        }
        return "Usuario-" + id;
    }

    public static void main(String[] args) {
        // Captura generica con AppException
        System.out.println("--- Captura generica con AppException ---");
        for (String id : new String[]{"", "999", "1"}) {
            try {
                validar(id);
                System.out.println("Resultado: " + buscar(id));
            } catch (AppException e) {
                System.out.println("AppException capturada: " + e.getMessage());
            }
        }

        System.out.println();

        // Captura especifica para diferenciar con instanceof
        System.out.println("--- Captura especifica ---");
        try {
            validar("");
        } catch (AppException e) {
            if (e instanceof ValidationException) {
                System.out.println("Es un error de validacion -> " + e.getMessage());
            } else if (e instanceof NotFoundException) {
                System.out.println("Es un not found -> " + e.getMessage());
            }
        }

        try {
            buscar("999");
        } catch (AppException e) {
            if (e instanceof ValidationException) {
                System.out.println("Es un error de validacion -> " + e.getMessage());
            } else if (e instanceof NotFoundException) {
                System.out.println("Es un not found -> " + e.getMessage());
            }
        }
    }
}
