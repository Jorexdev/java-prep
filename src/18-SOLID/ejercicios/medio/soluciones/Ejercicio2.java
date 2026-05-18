public class Ejercicio2 {

    static abstract class Cuenta {
        protected double saldo;
        Cuenta(double saldoInicial) { this.saldo = saldoInicial; }
        abstract void retirar(double cantidad);
        void depositar(double cantidad) { saldo += cantidad; }
        double getSaldo() { return saldo; }
    }

    static class CuentaCorriente extends Cuenta {
        private final double limiteDescubierto;
        CuentaCorriente(double saldo, double limiteDescubierto) {
            super(saldo);
            this.limiteDescubierto = limiteDescubierto;
        }
        @Override public void retirar(double cantidad) {
            if (cantidad > saldo + limiteDescubierto)
                throw new IllegalArgumentException("Supera el límite de descubierto");
            saldo -= cantidad;
        }
    }

    static class CuentaAhorro extends Cuenta {
        CuentaAhorro(double saldo) { super(saldo); }
        @Override public void retirar(double cantidad) {
            if (cantidad > saldo) throw new IllegalArgumentException("Saldo insuficiente");
            saldo -= cantidad;
        }
    }

    static class CuentaPlazoFijo extends Cuenta {
        private final java.time.LocalDate vencimiento;
        CuentaPlazoFijo(double saldo, java.time.LocalDate vencimiento) {
            super(saldo);
            this.vencimiento = vencimiento;
        }
        @Override public void retirar(double cantidad) {
            if (java.time.LocalDate.now().isBefore(vencimiento))
                throw new IllegalStateException("Plazo fijo no vencido hasta " + vencimiento);
            saldo -= cantidad;
        }
    }

    static void procesarRetiro(Cuenta c, double cantidad) {
        try {
            c.retirar(cantidad);
            System.out.printf("Retiro %.2f OK — saldo: %.2f%n", cantidad, c.getSaldo());
        } catch (Exception e) {
            System.out.println("Retiro rechazado: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        procesarRetiro(new CuentaCorriente(100, 500), 400);
        procesarRetiro(new CuentaAhorro(100), 200);
        procesarRetiro(new CuentaPlazoFijo(1000, java.time.LocalDate.now().plusDays(30)), 500);
    }
}
