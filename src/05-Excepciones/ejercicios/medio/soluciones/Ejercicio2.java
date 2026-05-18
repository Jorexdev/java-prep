public class Ejercicio2 {

    static class ValidationException extends RuntimeException {
        ValidationException(String mensaje) {
            super(mensaje);
        }
    }

    static int parseEdad(String s) {
        if (s == null) {
            throw new IllegalArgumentException("El argumento no puede ser null");
        }
        int edad;
        try {
            edad = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new ValidationException("No es un numero: " + s);
        }
        if (edad < 0) {
            throw new ValidationException("Edad negativa");
        }
        return edad;
    }

    public static void main(String[] args) {
        // Caso 1: valido
        try {
            System.out.println("Edad parseada: " + parseEdad("25"));
        } catch (IllegalArgumentException | ValidationException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // Caso 2: null -> IllegalArgumentException
        try {
            System.out.println("Edad parseada: " + parseEdad(null));
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
        } catch (ValidationException e) {
            System.out.println("ValidationException: " + e.getMessage());
        }

        // Caso 3: no es numero -> ValidationException
        try {
            System.out.println("Edad parseada: " + parseEdad("abc"));
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
        } catch (ValidationException e) {
            System.out.println("ValidationException: " + e.getMessage());
        }

        // Caso 4: edad negativa -> ValidationException
        try {
            System.out.println("Edad parseada: " + parseEdad("-5"));
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
        } catch (ValidationException e) {
            System.out.println("ValidationException: " + e.getMessage());
        }
    }
}
