package patronesdiseno.estrategia;

/*
    PATRÓN STRATEGY - Comportamiento

    ¿Qué es?
    Define una familia de algoritmos, los encapsula en clases separadas
    y los hace intercambiables en tiempo de ejecución.

    ¿Para qué sirve?
    Para eliminar if/else o switch gigantes donde el "cómo" varía pero el "qué" no.
    En vez de preguntar "¿qué método de pago es?" en cada operación, delegas
    directamente a la estrategia activa.

    ¿Cuándo usarlo?
    - Cuando tienes varias variantes de un mismo algoritmo o comportamiento.
    - Cuando el cliente debe poder cambiar el comportamiento en tiempo de ejecución.
    - Cuando quieres cumplir Open/Closed: añadir nuevas estrategias sin tocar el contexto.

    ¿Cuándo NO usarlo?
    - Si solo tienes dos variantes fijas y nunca cambian, un simple if es más directo.

    Preguntas típicas de entrevista:
    - ¿En qué se diferencia Strategy de un simple if/else?
    - ¿Cómo se relaciona Strategy con el principio Open/Closed?
    - ¿Se puede implementar Strategy con lambdas en Java? (sí, si la interfaz es funcional)
*/
public class StrategyDemo {

    /*
        La interfaz define el contrato del algoritmo.
        Al ser funcional (@FunctionalInterface implícita), se puede usar con lambdas.
    */
    interface PaymentStrategy {
        String pay(int amount);
    }

    static class CardPayment implements PaymentStrategy {
        public String pay(int amount) { return "Pago con tarjeta: " + amount + "€"; }
    }

    static class PaypalPayment implements PaymentStrategy {
        public String pay(int amount) { return "Pago con PayPal: " + amount + "€"; }
    }

    /*
        El contexto (Checkout) no sabe qué estrategia usa.
        Solo delega a la interfaz. Cambiar la estrategia no requiere tocar esta clase.
    */
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
