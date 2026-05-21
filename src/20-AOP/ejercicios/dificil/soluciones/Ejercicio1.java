import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Ejercicio1 {

    // ProxyFactory generico
    static class ProxyFactory {
        @SuppressWarnings("unchecked")
        static <T> T createProxy(Class<T> iface, T target, InvocationHandler handler) {
            return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{ iface },
                handler
            );
        }
    }

    // Handler encadenado: security + logging + timing
    static class ChainedHandler implements InvocationHandler {
        private final Object target;

        ChainedHandler(Object target) { this.target = target; }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 1. Security check
            if (method.getName().equals("admin")) {
                System.out.println("[SECURITY] Acceso denegado al metodo: " + method.getName());
                throw new SecurityException("Acceso denegado al metodo admin");
            }

            // 2. Logging - before
            System.out.println("[LOG] -> " + method.getName() + "(" + formatArgs(args) + ")");

            // 3. Timing + ejecucion
            long start = System.currentTimeMillis();
            Object result;
            try {
                result = method.invoke(target, args);
            } catch (InvocationTargetException e) {
                long elapsed = System.currentTimeMillis() - start;
                System.out.println("[TIMING] " + method.getName() + " -> " + elapsed + "ms (EXCEPCION)");
                throw e.getCause();
            }
            long elapsed = System.currentTimeMillis() - start;

            // Logging - after + timing
            System.out.println("[LOG] <- " + method.getName() + " = " + result);
            System.out.println("[TIMING] " + method.getName() + " -> " + elapsed + "ms");
            return result;
        }

        private String formatArgs(Object[] args) {
            if (args == null) return "";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(args[i]);
            }
            return sb.toString();
        }
    }

    interface MiServicio {
        String procesar(String input);
        String admin(String comando); // sera bloqueado
        int calcularRapido(int n);
    }

    static class MiServicioReal implements MiServicio {
        @Override
        public String procesar(String input) {
            try { Thread.sleep(30); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "procesado: " + input.toUpperCase();
        }

        @Override
        public String admin(String comando) {
            return "admin: " + comando;
        }

        @Override
        public int calcularRapido(int n) {
            return n * n;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== java.lang.reflect.Proxy: security + logging + timing ===\n");

        MiServicio proxy = ProxyFactory.createProxy(
            MiServicio.class,
            new MiServicioReal(),
            new ChainedHandler(new MiServicioReal())
        );

        System.out.println("--- procesar (normal) ---");
        String r1 = proxy.procesar("hola mundo");
        System.out.println("Resultado para cliente: " + r1);
        System.out.println();

        System.out.println("--- calcularRapido (rapido) ---");
        int r2 = proxy.calcularRapido(7);
        System.out.println("Resultado para cliente: " + r2);
        System.out.println();

        System.out.println("--- admin (bloqueado por security) ---");
        try {
            proxy.admin("DELETE_ALL");
            System.out.println("ERROR: deberia haber lanzado SecurityException");
        } catch (SecurityException e) {
            System.out.println("SecurityException capturada: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== El InvocationHandler ===");
        System.out.println("Un solo handler gestiona todos los concerns transversales.");
        System.out.println("El orden de comprobacion importa: security primero (fail-fast).");
        System.out.println("En Spring AOP cada @Aspect es un InvocationHandler especializado.");
    }
}
