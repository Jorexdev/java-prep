import java.util.HashMap;
import java.util.Map;

// Simula @Primary y @Qualifier para resolver ambigüedad cuando hay varios beans del mismo tipo.
// Spring usa estos metadatos al inyectar; aquí los llevamos con anotaciones en comentarios.
public class ExpQualifier {

    interface Notificador {
        void enviar(String mensaje);
    }

    // @Component("email")
    static class NotificadorEmail implements Notificador {
        @Override
        public void enviar(String mensaje) {
            System.out.println("[EMAIL] " + mensaje);
        }
    }

    // @Component("sms")
    // @Primary  ← gana cuando no hay @Qualifier explícito
    static class NotificadorSMS implements Notificador {
        @Override
        public void enviar(String mensaje) {
            System.out.println("[SMS] " + mensaje);
        }
    }

    // Registro de beans con soporte para @Primary y @Qualifier
    static class BeanRegistry {
        private final Map<String, Notificador> beans = new HashMap<>();
        private String primaryName;

        void registrar(String nombre, Notificador bean) {
            beans.put(nombre, bean);
        }

        // Marca cuál bean gana cuando se inyecta por tipo sin @Qualifier
        void setPrimary(String nombre) {
            this.primaryName = nombre;
        }

        // Inyección por tipo sin calificador → usa @Primary
        Notificador getByType() {
            if (primaryName == null) {
                if (beans.size() == 1) return beans.values().iterator().next();
                // Sin @Primary y más de un candidato → ambigüedad (Spring lanza NoUniqueBeanDefinitionException)
                throw new IllegalStateException(
                    "Ambigüedad: " + beans.size() + " beans de tipo Notificador. Usa @Qualifier o @Primary.");
            }
            return beans.get(primaryName);
        }

        // Inyección con @Qualifier("nombre") → selección explícita
        Notificador getByQualifier(String nombre) {
            Notificador bean = beans.get(nombre);
            if (bean == null) throw new IllegalStateException("No hay bean con qualifier: " + nombre);
            return bean;
        }
    }

    public static void main(String[] args) {
        BeanRegistry registry = new BeanRegistry();
        registry.registrar("email", new NotificadorEmail());
        registry.registrar("sms",   new NotificadorSMS());

        System.out.println("=== Escenario 1: sin @Qualifier ni @Primary → ambigüedad ===");
        try {
            Notificador n = registry.getByType();
            n.enviar("Hola");
        } catch (IllegalStateException e) {
            System.out.println("Error esperado → " + e.getMessage());
        }

        System.out.println("\n=== Escenario 2: con @Primary en SMS → SMS gana por defecto ===");
        registry.setPrimary("sms");
        Notificador porPrimary = registry.getByType();
        porPrimary.enviar("Notificación por defecto");

        System.out.println("\n=== Escenario 3: @Qualifier(\"email\") → email seleccionado explícitamente ===");
        // @Qualifier tiene prioridad sobre @Primary
        Notificador porQualifier = registry.getByQualifier("email");
        porQualifier.enviar("Notificación explícita por email");

        System.out.println("\n=== Resumen ===");
        System.out.println("Sin @Primary, sin @Qualifier → NoUniqueBeanDefinitionException");
        System.out.println("@Primary sms                 → SMS gana cuando no hay @Qualifier");
        System.out.println("@Qualifier(\"email\")          → Email seleccionado, ignora @Primary");
    }
}
