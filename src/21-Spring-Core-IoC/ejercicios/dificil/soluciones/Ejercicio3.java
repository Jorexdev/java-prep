import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio3 {

    interface NotificadorService {
        void notificar(String mensaje);
    }

    // En Spring: // @Component("emailNotificador")
    static class EmailNotificador implements NotificadorService {
        @Override
        public void notificar(String mensaje) {
            System.out.println("[EMAIL] Enviando: " + mensaje);
        }
    }

    // En Spring: // @Component("smsNotificador")
    static class SmsNotificador implements NotificadorService {
        @Override
        public void notificar(String mensaje) {
            System.out.println("[SMS] Enviando: " + mensaje);
        }
    }

    // Excepción equivalente a NoUniqueBeanDefinitionException de Spring
    static class AmbiguousBeansException extends RuntimeException {
        AmbiguousBeansException(Class<?> tipo, List<String> candidatos) {
            super("Ambiguidad al resolver bean de tipo '" + tipo.getSimpleName()
                + "'. Candidatos: " + candidatos
                + ". Usa @Qualifier o @Primary para desambiguar.");
        }
    }

    static class AmbiguityContainer {
        // Mapa nombre -> instancia
        private final Map<String, Object> beans = new LinkedHashMap<>();

        void register(String nombre, Object bean) {
            beans.put(nombre, bean);
            System.out.println("Bean registrado: " + nombre + " (" + bean.getClass().getSimpleName() + ")");
        }

        // Sin qualifier: lanza excepción si hay más de uno del mismo tipo
        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> tipo) {
            List<String> candidatos = new ArrayList<>();
            for (Map.Entry<String, Object> entry : beans.entrySet()) {
                if (tipo.isInstance(entry.getValue())) {
                    candidatos.add(entry.getKey());
                }
            }
            if (candidatos.size() > 1) {
                throw new AmbiguousBeansException(tipo, candidatos);
            }
            if (candidatos.isEmpty()) {
                throw new IllegalStateException("No hay bean de tipo: " + tipo.getSimpleName());
            }
            return (T) beans.get(candidatos.get(0));
        }

        // Con qualifier: busca por nombre exacto y verifica que sea del tipo correcto
        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> tipo, String qualifier) {
            Object bean = beans.get(qualifier);
            if (bean == null) {
                throw new IllegalArgumentException("No existe bean con nombre: " + qualifier);
            }
            if (!tipo.isInstance(bean)) {
                throw new IllegalArgumentException("Bean '" + qualifier + "' no es de tipo " + tipo.getSimpleName());
            }
            return (T) bean;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== @Autowired con ambigüedad y @Qualifier ===\n");

        AmbiguityContainer container = new AmbiguityContainer();
        container.register("emailNotificador", new EmailNotificador());
        container.register("smsNotificador", new SmsNotificador());

        System.out.println();

        // Caso 1: sin qualifier -> AmbiguousBeansException
        System.out.println("--- Sin @Qualifier (ambigüedad) ---");
        try {
            NotificadorService n = container.getBean(NotificadorService.class);
            n.notificar("mensaje");
        } catch (AmbiguousBeansException e) {
            System.out.println("Excepción: " + e.getMessage());
        }

        System.out.println();

        // Caso 2: con qualifier -> resuelve correctamente
        System.out.println("--- Con @Qualifier(\"emailNotificador\") ---");
        NotificadorService email = container.getBean(NotificadorService.class, "emailNotificador");
        email.notificar("Pedido confirmado");

        System.out.println("--- Con @Qualifier(\"smsNotificador\") ---");
        NotificadorService sms = container.getBean(NotificadorService.class, "smsNotificador");
        sms.notificar("Tu código es 1234");
    }
}
