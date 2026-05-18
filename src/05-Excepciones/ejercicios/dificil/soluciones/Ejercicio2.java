import java.util.function.Supplier;
public class Ejercicio2 {
    static <T> T ejecutarConReintentos(Supplier<T> op, int maxIntentos) {
        Exception ultimo = null;
        for (int i = 1; i <= maxIntentos; i++) {
            try {
                System.out.println("  Intento " + i + "...");
                return op.get();
            } catch (Exception e) {
                System.out.println("  Fallo: " + e.getMessage());
                ultimo = e;
            }
        }
        throw new RuntimeException("Todos los intentos fallaron", ultimo);
    }
    public static void main(String[] args) {
        int[] contador = {0};
        String resultado = ejecutarConReintentos(() -> {
            contador[0]++;
            if (contador[0] < 3) throw new RuntimeException("Servicio no disponible");
            return "Éxito en intento " + contador[0];
        }, 5);
        System.out.println(resultado);
    }
}
