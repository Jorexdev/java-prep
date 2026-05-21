public class ExpStrategy {

    // La interfaz define el contrato del algoritmo.
    // Al ser funcional (@FunctionalInterface implícita), se puede usar con lambdas.
    interface PaymentStrategy {
        String pay(int amount);
    }

    static class CardPayment implements PaymentStrategy {
        public String pay(int amount) { return "Pago con tarjeta: " + amount + "€"; }
    }

    static class PaypalPayment implements PaymentStrategy {
        public String pay(int amount) { return "Pago con PayPal: " + amount + "€"; }
    }

    // El contexto (Checkout) no sabe qué estrategia usa.
    // Solo delega a la interfaz. Cambiar la estrategia no requiere tocar esta clase.
    static class Checkout {
        private PaymentStrategy strategy;

        public void setStrategy(PaymentStrategy s) { this.strategy = s; }

        public void process(int amount) {
            System.out.println(strategy.pay(amount));
        }
    }

    public static void main(String[] args) {
        Checkout co = new Checkout();

        co.setStrategy(new CardPayment());
        co.process(50);

        // Cambiamos la estrategia en tiempo de ejecución sin modificar Checkout
        co.setStrategy(new PaypalPayment());
        co.process(75);

        // También se puede usar con lambda directamente
        co.setStrategy(amount -> "Bizum: " + amount + "€");
        co.process(20);
    }
}
