import java.util.List;

public class Ejercicio5 {

    interface MetodoPago {
        boolean procesar(double monto);
        String nombre();
    }

    static class TarjetaCredito implements MetodoPago {
        private final String numero;
        TarjetaCredito(String numero) { this.numero = numero; }
        @Override public boolean procesar(double monto) {
            System.out.printf("Tarjeta %s: cargo de %.2f€%n", numero, monto);
            return true;
        }
        @Override public String nombre() { return "Tarjeta " + numero; }
    }

    static class PayPal implements MetodoPago {
        private final String email;
        PayPal(String email) { this.email = email; }
        @Override public boolean procesar(double monto) {
            System.out.printf("PayPal %s: pago de %.2f€%n", email, monto);
            return true;
        }
        @Override public String nombre() { return "PayPal " + email; }
    }

    static class Cripto implements MetodoPago {
        private final String wallet;
        Cripto(String wallet) { this.wallet = wallet; }
        @Override public boolean procesar(double monto) {
            System.out.printf("Cripto wallet %s: transferencia de %.2f€%n", wallet, monto);
            return true;
        }
        @Override public String nombre() { return "Cripto " + wallet; }
    }

    static class ProcesadorPago {
        private final MetodoPago metodo;
        ProcesadorPago(MetodoPago metodo) { this.metodo = metodo; }

        void pagar(double monto) {
            System.out.println("Procesando pago con " + metodo.nombre());
            boolean ok = metodo.procesar(monto);
            System.out.println("Resultado: " + (ok ? "APROBADO" : "RECHAZADO"));
        }
    }

    public static void main(String[] args) {
        List<MetodoPago> metodos = List.of(
            new TarjetaCredito("4321-xxxx"),
            new PayPal("user@example.com"),
            new Cripto("0xABC123")
        );

        for (MetodoPago m : metodos) {
            new ProcesadorPago(m).pagar(99.99);
            System.out.println();
        }
    }
}
