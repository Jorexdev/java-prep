public class Ejercicio7 {

    static void lanzarNPE() {
        String s = null;
        s.length(); // NullPointerException
    }

    static void lanzarIAE() {
        throw new IllegalArgumentException("argumento invalido");
    }

    public static void main(String[] args) {
        // REGLA: el catch mas especifico debe ir PRIMERO
        // Si pones Exception antes que NullPointerException, el compilador
        // avisa "unreachable catch block" (NullPointerException ya esta cubierta por Exception)

        System.out.println("--- Ejemplo con orden correcto ---");
        try {
            lanzarNPE();
        } catch (NullPointerException e) {
            // Captura solo NPE
            System.out.println("NullPointerException especifica: " + e.getClass().getSimpleName());
        } catch (RuntimeException e) {
            // Captura cualquier otra RuntimeException
            System.out.println("RuntimeException generica: " + e.getClass().getSimpleName());
        } catch (Exception e) {
            // Captura cualquier otra Exception
            System.out.println("Exception generica: " + e.getClass().getSimpleName());
        }

        System.out.println();

        // Con IllegalArgumentException: cae en el catch RuntimeException (no NPE)
        System.out.println("--- IllegalArgumentException -> cae en RuntimeException ---");
        try {
            lanzarIAE();
        } catch (NullPointerException e) {
            System.out.println("NullPointerException: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("RuntimeException capturada: "
                    + e.getClass().getSimpleName() + " -> " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception generica: " + e.getMessage());
        }

        // NOTA: el siguiente bloque NO compilaria — orden incorrecto:
        //
        // try { ... }
        // catch (Exception e) { ... }
        // catch (NullPointerException e) { ... }  // ERROR: unreachable catch block
    }
}
