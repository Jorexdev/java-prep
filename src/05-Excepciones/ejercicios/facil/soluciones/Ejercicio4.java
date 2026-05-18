public class Ejercicio4 {

    static void operacion(String[] datos, String indice) {
        // Puede lanzar NumberFormatException o ArrayIndexOutOfBoundsException
        int idx = Integer.parseInt(indice); // NumberFormatException si no es numero
        System.out.println("Elemento: " + datos[idx]); // ArrayIndexOutOfBoundsException si fuera de rango
    }

    public static void main(String[] args) {
        String[] datos = {"alfa", "beta", "gamma"};

        // Caso 1: NumberFormatException
        System.out.println("--- Caso NumberFormatException ---");
        try {
            operacion(datos, "abc");
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Capturada con multi-catch: " + e.getClass().getSimpleName()
                    + " -> " + e.getMessage());
        }

        // Caso 2: ArrayIndexOutOfBoundsException
        System.out.println("--- Caso ArrayIndexOutOfBoundsException ---");
        try {
            operacion(datos, "10");
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Capturada con multi-catch: " + e.getClass().getSimpleName()
                    + " -> " + e.getMessage());
        }

        // Caso 3: sin excepcion
        System.out.println("--- Caso exitoso ---");
        try {
            operacion(datos, "1");
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
