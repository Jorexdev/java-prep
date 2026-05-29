import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

// @Qualifier manual con reflection para resolver ambiguedad de beans del mismo tipo

public class Ejercicio6 {

    // Anotacion @Qualifier (simula la de Spring)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @interface Qualifier {
        String value();
    }

    // Anotacion @Inject (simula @Autowired de Spring)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Inject {}

    // Contenedor con soporte de qualifier
    static class QualifierContainer {
        // mapa: tipo -> { qualifier -> instancia }
        private final Map<Class<?>, Map<String, Object>> registry = new HashMap<>();
        // mapa: tipo -> nombre del bean primario (cuando no hay qualifier)
        private final Map<Class<?>, String> primaryBeans = new HashMap<>();

        // Registra un bean con nombre (qualifier)
        void register(Class<?> type, String qualifier, Object instance) {
            registry.computeIfAbsent(type, k -> new LinkedHashMap<>())
                    .put(qualifier, instance);
            System.out.printf("  [Container] registrado %s con qualifier='%s'%n",
                    type.getSimpleName(), qualifier);
        }

        // Marca un qualifier como primario para ese tipo
        void setPrimary(Class<?> type, String qualifier) {
            primaryBeans.put(type, qualifier);
            System.out.printf("  [Container] primary para %s: '%s'%n",
                    type.getSimpleName(), qualifier);
        }

        // Resolucion con qualifier explicito
        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> type, String qualifier) {
            Map<String, Object> beans = registry.get(type);
            if (beans == null || !beans.containsKey(qualifier)) {
                throw new NoSuchElementException("No hay bean de tipo " + type.getSimpleName()
                        + " con qualifier='" + qualifier + "'");
            }
            return (T) beans.get(qualifier);
        }

        // Resolucion sin qualifier: usa primary si esta definido, falla si hay ambiguedad
        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> type) {
            Map<String, Object> beans = registry.get(type);
            if (beans == null || beans.isEmpty()) {
                throw new NoSuchElementException("No hay beans de tipo " + type.getSimpleName());
            }
            if (beans.size() == 1) {
                return (T) beans.values().iterator().next();
            }
            String primary = primaryBeans.get(type);
            if (primary != null && beans.containsKey(primary)) {
                System.out.printf("  [Container] ambiguedad resuelta via @Primary: '%s'%n", primary);
                return (T) beans.get(primary);
            }
            throw new IllegalStateException("Ambiguedad: " + beans.size()
                    + " beans de tipo " + type.getSimpleName() + ": " + beans.keySet()
                    + ". Usa @Qualifier para especificar cual.");
        }

        // Inyeccion automatica via reflection: busca campos @Inject y los resuelve
        // Si el campo tiene @Qualifier, usa el qualifier; si no, resolucion automatica
        void inject(Object target) throws IllegalAccessException {
            Class<?> cls = target.getClass();
            System.out.printf("  [Container] inyectando en %s...%n", cls.getSimpleName());
            for (Field field : cls.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Qualifier q = field.getAnnotation(Qualifier.class);
                    Object value;
                    if (q != null) {
                        value = getBean(field.getType(), q.value());
                        System.out.printf("    campo '%s' -> qualifier='%s' -> %s%n",
                                field.getName(), q.value(), value.getClass().getSimpleName());
                    } else {
                        value = getBean(field.getType());
                        System.out.printf("    campo '%s' -> auto -> %s%n",
                                field.getName(), value.getClass().getSimpleName());
                    }
                    field.set(target, value);
                }
            }
        }
    }

    // Interfaz y dos implementaciones (ambiguedad tipica)
    interface NotificacionService {
        void enviar(String mensaje);
    }

    static class EmailService implements NotificacionService {
        public void enviar(String mensaje) {
            System.out.println("  [Email] " + mensaje);
        }
    }

    static class SmsService implements NotificacionService {
        public void enviar(String mensaje) {
            System.out.println("  [SMS] " + mensaje);
        }
    }

    static class PushService implements NotificacionService {
        public void enviar(String mensaje) {
            System.out.println("  [Push] " + mensaje);
        }
    }

    // Clase que necesita inyeccion con qualifier
    static class PedidoService {
        @Inject
        @Qualifier("email")
        NotificacionService confirmacion;

        @Inject
        @Qualifier("sms")
        NotificacionService urgente;

        void procesarPedido(String cliente) {
            System.out.printf("  PedidoService procesando pedido de %s%n", cliente);
            confirmacion.enviar("Pedido confirmado para " + cliente);
            urgente.enviar("Envio en camino para " + cliente);
        }
    }

    // Clase que usa el bean primario sin qualifier
    static class AlertaService {
        @Inject
        NotificacionService notificacion; // resuelto por @Primary

        void enviarAlerta(String msg) {
            notificacion.enviar("[ALERTA] " + msg);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== @Qualifier manual con reflection ===");
        System.out.println();

        QualifierContainer container = new QualifierContainer();

        // Registrar beans con qualifiers
        System.out.println("[ Registro de beans ]");
        container.register(NotificacionService.class, "email", new EmailService());
        container.register(NotificacionService.class, "sms",   new SmsService());
        container.register(NotificacionService.class, "push",  new PushService());
        container.setPrimary(NotificacionService.class, "email");
        System.out.println();

        // --- Demo 1: getBean con qualifier explicito ---
        System.out.println("[ Demo 1 ] getBean con qualifier explicito");
        NotificacionService emailSvc = container.getBean(NotificacionService.class, "email");
        NotificacionService smsSvc   = container.getBean(NotificacionService.class, "sms");
        emailSvc.enviar("test via email");
        smsSvc.enviar("test via sms");
        System.out.println();

        // --- Demo 2: getBean sin qualifier (usa @Primary) ---
        System.out.println("[ Demo 2 ] getBean sin qualifier (debe usar @Primary = email)");
        NotificacionService primary = container.getBean(NotificacionService.class);
        primary.enviar("mensaje por bean primario");
        System.out.println();

        // --- Demo 3: ambiguedad sin @Primary configurado ---
        System.out.println("[ Demo 3 ] Ambiguedad sin @Primary -> debe lanzar exception");
        QualifierContainer sinPrimary = new QualifierContainer();
        sinPrimary.register(NotificacionService.class, "email", new EmailService());
        sinPrimary.register(NotificacionService.class, "sms",   new SmsService());
        try {
            sinPrimary.getBean(NotificacionService.class);
        } catch (IllegalStateException e) {
            System.out.println("  " + e.getMessage());
        }
        System.out.println();

        // --- Demo 4: inyeccion via reflection con @Qualifier en campos ---
        System.out.println("[ Demo 4 ] Inyeccion via reflection: PedidoService con @Qualifier en campos");
        PedidoService pedidoService = new PedidoService();
        container.inject(pedidoService);
        pedidoService.procesarPedido("Ana");
        System.out.println();

        // --- Demo 5: inyeccion con @Primary (sin @Qualifier en campo) ---
        System.out.println("[ Demo 5 ] Inyeccion via reflection: AlertaService resuelto por @Primary");
        AlertaService alertaService = new AlertaService();
        container.inject(alertaService);
        alertaService.enviarAlerta("sistema bajo presion");
        System.out.println();

        System.out.println("=== Conclusion ===");
        System.out.println("@Qualifier + reflection: resolucion de ambiguedad sin acoplamiento directo.");
        System.out.println("En Spring: @Autowired + @Qualifier('nombre') hace exactamente esto internamente.");
    }
}
