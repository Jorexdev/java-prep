import java.util.ArrayList;
import java.util.List;

public class Ejercicio1 {

    static class Procesador {
        void procesar(String mensaje) {
            if (mensaje.contains("ERROR")) {
                throw new RuntimeException("Fallo al procesar: " + mensaje);
            }
            System.out.println("[OK] procesado: " + mensaje);
        }
    }

    static class ReliableConsumer {
        private static final int MAX_REINTENTOS = 3;
        private final Procesador procesador = new Procesador();
        private final List<String> dlt = new ArrayList<>();

        void consume(String mensaje) {
            int intento = 0;
            while (intento < MAX_REINTENTOS) {
                try {
                    intento++;
                    procesador.procesar(mensaje);
                    return;
                } catch (RuntimeException ex) {
                    System.out.println("[RETRY " + intento + "/" + MAX_REINTENTOS + "] fallo en '" + mensaje + "': " + ex.getMessage());
                }
            }
            dlt.add(mensaje);
            System.out.println("[DLT] '" + mensaje + "' enviado a dead-letter topic");
        }

        List<String> getDlt() {
            return dlt;
        }
    }

    public static void main(String[] args) {
        ReliableConsumer consumer = new ReliableConsumer();
        List<String> mensajes = List.of("ok1", "ERROR_A", "ok2", "ERROR_B", "ok3", "ERROR_C");

        System.out.println("[CONSUME] procesando " + mensajes.size() + " mensajes\n");
        for (String msg : mensajes) {
            consumer.consume(msg);
            System.out.println();
        }

        System.out.println("[DLT FINAL] mensajes en dead-letter: " + consumer.getDlt());
    }
}
