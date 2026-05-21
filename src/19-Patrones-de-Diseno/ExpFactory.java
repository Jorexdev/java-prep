public class ExpFactory {

    // Interfaz común para todos los productos.
    // El cliente solo conoce esta abstracción, nunca las clases concretas.
    interface Bot {
        String reply();
    }

    static class SupportBot implements Bot {
        public String reply() { return "Soporte: ¿en qué puedo ayudarte?"; }
    }

    static class SalesBot implements Bot {
        public String reply() { return "Ventas: ¡tengo una oferta para ti!"; }
    }

    // La factory centraliza toda la lógica de creación.
    // Si añades un nuevo tipo de Bot, solo tocas aquí, no el código del cliente.
    static class BotFactory {
        public static Bot create(String type) {
            return switch (type) {
                case "support" -> new SupportBot();
                case "sales"   -> new SalesBot();
                default -> throw new IllegalArgumentException("Tipo no soportado: " + type);
            };
        }
    }

    public static void main(String[] args) {
        // El cliente no usa "new SupportBot()" ni sabe qué clase se crea
        Bot b1 = BotFactory.create("support");
        Bot b2 = BotFactory.create("sales");
        System.out.println(b1.reply());
        System.out.println(b2.reply());
    }
}
