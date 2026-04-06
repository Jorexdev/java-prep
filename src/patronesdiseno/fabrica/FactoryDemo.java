package patronesdiseno.fabrica;

/*
    PATRÓN FACTORY - Creacional

    ¿Qué es?
    Centraliza la lógica de creación de objetos en un único lugar.
    El cliente pide un objeto sin saber qué clase concreta se instancia.

    ¿Para qué sirve?
    Para eliminar "new" repartidos por todo el código y condicionales del tipo
    "si es X crea esto, si es Y crea aquello". Toda esa lógica vive en la factory.

    ¿Cuándo usarlo?
    - Cuando la creación del objeto implica lógica condicional o compleja.
    - Cuando quieres que el cliente dependa de abstracciones, no de clases concretas.
    - Cuando el tipo de objeto a crear puede variar en tiempo de ejecución.

    ¿Cuándo NO usarlo?
    - Si solo tienes un tipo de objeto y la creación es trivial, no aporta nada.

    Preguntas típicas de entrevista:
    - ¿Qué diferencia hay entre Simple Factory, Factory Method y Abstract Factory?
    - ¿Cómo se relaciona Factory con el principio Open/Closed?
    - ¿Cómo usa Spring el patrón Factory? (BeanFactory, ApplicationContext)
*/
public class FactoryDemo {

    /*
        Interfaz común para todos los productos.
        El cliente solo conoce esta abstracción, nunca las clases concretas.
    */
    interface Bot {
        String reply();
    }

    static class SupportBot implements Bot {
        public String reply() { return "Soporte: ¿en qué puedo ayudarte?"; }
    }

    static class SalesBot implements Bot {
        public String reply() { return "Ventas: ¡tengo una oferta para ti!"; }
    }

    /*
        La factory centraliza toda la lógica de creación.
        Si añades un nuevo tipo de Bot, solo tocas aquí, no el código del cliente.
    */
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
