import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class Ejercicio2 {

    // Interfaz de un aspect generico
    interface Aspect {
        Object invoke(Method method, Object target, Object[] args, AspectChain chain) throws Throwable;
    }

    // Cadena de aspects: ejecuta el siguiente en la cadena o el metodo real
    static class AspectChain {
        private final List<Aspect> aspects;
        private final int index;
        private final Object target;

        AspectChain(List<Aspect> aspects, Object target, int index) {
            this.aspects = aspects;
            this.target = target;
            this.index = index;
        }

        public Object proceed(Method method, Object[] args) throws Throwable {
            if (index >= aspects.size()) {
                // Final de la cadena: invocar el metodo real
                try {
                    return method.invoke(target, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
            Aspect aspect = aspects.get(index);
            AspectChain nextChain = new AspectChain(aspects, target, index + 1);
            return aspect.invoke(method, target, args, nextChain);
        }
    }

    // Registro de aspects con pointcuts
    static class AspectRegistry {
        record Registration(Predicate<Method> pointcut, Aspect aspect) {}
        private final List<Registration> registrations = new ArrayList<>();

        void register(Predicate<Method> pointcut, Aspect aspect) {
            registrations.add(new Registration(pointcut, aspect));
        }

        @SuppressWarnings("unchecked")
        <T> T weave(T target) {
            return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                (proxy, method, args) -> {
                    // Filtrar solo los aspects cuyo pointcut aplica a este metodo
                    List<Aspect> applicable = new ArrayList<>();
                    for (Registration r : registrations) {
                        if (r.pointcut().test(method)) {
                            applicable.add(r.aspect());
                        }
                    }
                    return new AspectChain(applicable, target, 0).proceed(method, args);
                }
            );
        }
    }

    // Aspects concretos
    static Aspect loggingAspect() {
        return (method, target, args, chain) -> {
            System.out.println("  [LOG] before: " + method.getName() + "(" + Arrays.toString(args) + ")");
            Object result = chain.proceed(method, args);
            System.out.println("  [LOG] after: " + method.getName() + " -> " + result);
            return result;
        };
    }

    static Aspect securityAspect() {
        return (method, target, args, chain) -> {
            System.out.println("  [SECURITY] verificando: " + method.getName());
            // Denegar metodos con nombre que contiene "admin"
            if (method.getName().toLowerCase().contains("admin")) {
                throw new SecurityException("Metodo admin denegado: " + method.getName());
            }
            System.out.println("  [SECURITY] acceso permitido");
            return chain.proceed(method, args);
        };
    }

    interface Servicio {
        String leer(String clave);
        String escribir(String clave, String valor);
        String adminLimpiar();
        String adminStats();
    }

    static class ServicioReal implements Servicio {
        @Override public String leer(String clave)              { return "valor-" + clave; }
        @Override public String escribir(String clave, String v){ return "ok:" + clave + "=" + v; }
        @Override public String adminLimpiar()                  { return "limpiado"; }
        @Override public String adminStats()                    { return "stats: 100 ops"; }
    }

    public static void main(String[] args) {
        System.out.println("=== AOP Registry con pointcuts ===\n");
        System.out.println("Logging:  todos los metodos");
        System.out.println("Security: solo metodos con 'admin' en el nombre\n");

        AspectRegistry registry = new AspectRegistry();
        registry.register(m -> true, loggingAspect());  // todos los metodos
        registry.register(m -> m.getName().contains("admin"), securityAspect()); // solo admin

        Servicio srv = registry.weave(new ServicioReal());

        System.out.println("--- leer (solo logging) ---");
        srv.leer("config.db");
        System.out.println();

        System.out.println("--- escribir (solo logging) ---");
        srv.escribir("key1", "value1");
        System.out.println();

        System.out.println("--- adminStats (logging + security -> DENEGADO) ---");
        try {
            srv.adminStats();
        } catch (SecurityException e) {
            System.out.println("  SecurityException: " + e.getMessage());
        }
        System.out.println();

        System.out.println("--- adminLimpiar (logging + security -> DENEGADO) ---");
        try {
            srv.adminLimpiar();
        } catch (SecurityException e) {
            System.out.println("  SecurityException: " + e.getMessage());
        }
    }
}
