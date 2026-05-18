public class Ejercicio3 {

    static class EdadInvalidaException extends Exception {
        EdadInvalidaException(int edad) {
            super("Edad invalida: " + edad + " (debe estar entre 0 y 150)");
        }
    }

    static void validarEdad(int edad) throws EdadInvalidaException {
        if (edad < 0 || edad > 150) {
            throw new EdadInvalidaException(edad);
        }
        System.out.println("Edad valida: " + edad);
    }

    public static void main(String[] args) {
        // Caso valido
        try {
            validarEdad(25);
        } catch (EdadInvalidaException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // Caso negativo
        try {
            validarEdad(-1);
        } catch (EdadInvalidaException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // Caso mayor que 150
        try {
            validarEdad(200);
        } catch (EdadInvalidaException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
