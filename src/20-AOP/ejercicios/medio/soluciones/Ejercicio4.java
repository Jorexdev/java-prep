import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Ejercicio4 {

    // Anotacion custom @Auditar
    // En Spring: @Aspect detectaria @annotation(auditar) en el pointcut
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Auditar {
        String value() default ""; // descripcion opcional
    }

    interface BancoServicio {
        // @Auditar
        void transferir(String origen, String destino, double monto);

        // @Auditar
        double consultarSaldo(String cuenta);

        // sin @Auditar
        String obtenerNombre(String cuenta);

        // sin @Auditar
        String version();
    }

    static class BancoServicioReal implements BancoServicio {
        @Override
        @Auditar("transferencia bancaria")
        public void transferir(String origen, String destino, double monto) {
            System.out.println("  [Real] transferir: " + origen + " -> " + destino + " $" + monto);
        }

        @Override
        @Auditar("consulta de saldo")
        public double consultarSaldo(String cuenta) {
            System.out.println("  [Real] consultarSaldo: " + cuenta);
            return 1234.56;
        }

        @Override
        public String obtenerNombre(String cuenta) {
            return "Titular-" + cuenta;
        }

        @Override
        public String version() {
            return "BancoServicio v1.0";
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T wrapConAnotacion(T target) {
        Class<?> realClass = target.getClass();
        return (T) Proxy.newProxyInstance(
            realClass.getClassLoader(),
            realClass.getInterfaces(),
            (proxy, method, args) -> {
                // Buscar el metodo en la clase real (las interfaces no tienen la anotacion)
                Method realMethod;
                try {
                    realMethod = realClass.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException e) {
                    realMethod = method;
                }

                Auditar auditar = realMethod.getAnnotation(Auditar.class);
                if (auditar != null) {
                    System.out.println("[AUDIT] interceptado: " + method.getName() +
                                       " | descripcion='" + auditar.value() + "'");
                }

                try {
                    Object result = method.invoke(target, args);
                    if (auditar != null) {
                        System.out.println("[AUDIT] resultado: " + result);
                    }
                    return result;
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        );
    }

    public static void main(String[] args) {
        System.out.println("=== @Auditar: pointcut basado en anotacion ===");
        System.out.println("transferir y consultarSaldo tienen @Auditar.");
        System.out.println("obtenerNombre y version NO tienen @Auditar.\n");

        BancoServicio banco = wrapConAnotacion(new BancoServicioReal());

        System.out.println("--- transferir ---");
        banco.transferir("cuenta-A", "cuenta-B", 500.0);
        System.out.println();

        System.out.println("--- consultarSaldo ---");
        double saldo = banco.consultarSaldo("cuenta-A");
        System.out.println("  saldo para cliente: " + saldo);
        System.out.println();

        System.out.println("--- obtenerNombre (sin @Auditar) ---");
        String nombre = banco.obtenerNombre("cuenta-A");
        System.out.println("  nombre: " + nombre + " (sin log de auditoria)");
        System.out.println();

        System.out.println("--- version (sin @Auditar) ---");
        System.out.println("  " + banco.version() + " (sin log de auditoria)");

        System.out.println();
        System.out.println("=== En Spring AOP ===");
        System.out.println("@Around(\"@annotation(com.example.Auditar)\")");
        System.out.println("public Object auditar(ProceedingJoinPoint pjp, Auditar auditar) { ... }");
    }
}
