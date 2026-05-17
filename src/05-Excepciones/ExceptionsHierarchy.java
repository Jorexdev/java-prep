public class ExceptionsHierarchy {

    public static void main(String[] args) {

        // Todas las excepciones y errores heredan de Throwable
        // pero normalmente trabajamos con Exception y Error.

        try {
            throw new Throwable("Esto es un Throwable");
        } catch (Throwable t) {
            System.out.println("Capturado Throwable: " + t.getMessage());
        }


        // Representa problemas graves que no deberían manejarse
        try {
            throw new OutOfMemoryError("Sin memoria");
        } catch (Error e) {
            System.out.println("Error capturado (no recomendado): " + e);
        }



        // Obligatorio manejarla con try/catch o declarar throws
        try {
            lanzarChecked();
        } catch (Exception e) {
            System.out.println("Checked exception capturada: " + e);
        }


        try {
            int[] arr = new int[2];
            System.out.println(arr[5]); // IndexOutOfBoundsException
        } catch (RuntimeException e) {
            System.out.println("Unchecked exception capturada: " + e);
        }
    }

    // Métod0 que lanza una checked exception
    public static void lanzarChecked() throws Exception {
        throw new Exception("Soy una checked exception");
    }
}
