public class Ejercicio6 {

    static void operar() throws Exception {
        try {
            throw new Exception("Error original en operacion");
        } catch (Exception e) {
            System.err.println("[LOG] Excepcion capturada: " + e.getMessage());
            throw e; // relanzar la misma excepcion
        }
    }

    public static void main(String[] args) {
        try {
            operar();
        } catch (Exception e) {
            System.out.println("Excepcion relanzada capturada en main: " + e.getMessage());
        }
    }
}
