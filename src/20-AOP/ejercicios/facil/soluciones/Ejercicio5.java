import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class Ejercicio5 {

    // Interfaz del advice @AfterThrowing
    interface AfterThrowingAdvice {
        void afterThrowing(String method, Exception e);
    }

    interface Operaciones {
        int dividir(int a, int b);
        int sumar(int a, int b);
    }

    static class OperacionesReal implements Operaciones {
        @Override
        public int dividir(int a, int b) {
            if (b == 0) throw new ArithmeticException("Division por cero: " + a + "/" + b);
            return a / b;
        }
        @Override
        public int sumar(int a, int b) { return a + b; }
    }

    static class ProxyFactory {
        @SuppressWarnings("unchecked")
        static <T> T wrap(T target, AfterThrowingAdvice advice) {
            return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                (proxy, method, args) -> {
                    try {
                        return method.invoke(target, args);
                    } catch (InvocationTargetException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof Exception ex) {
                            advice.afterThrowing(method.getName(), ex); // @AfterThrowing
                        }
                        throw cause; // re-lanza para que el cliente la vea
                    }
                }
            );
        }
    }

    public static void main(String[] args) {
        System.out.println("=== @AfterThrowing simulado ===\n");

        AfterThrowingAdvice advice = (method, e) ->
            System.out.println("[AFTER_THROWING] excepcion en " + method + "(): " +
                               e.getClass().getSimpleName() + " -> " + e.getMessage());

        Operaciones ops = ProxyFactory.wrap(new OperacionesReal(), advice);

        // Caso 1: sin excepcion
        System.out.println("dividir(10, 2):");
        int r1 = ops.dividir(10, 2);
        System.out.println("  resultado: " + r1 + " (no hay excepcion, advice no se ejecuta)\n");

        // Caso 2: sumar sin excepcion
        System.out.println("sumar(5, 3):");
        int r2 = ops.sumar(5, 3);
        System.out.println("  resultado: " + r2 + " (sin excepcion)\n");

        // Caso 3: con excepcion (b=0)
        System.out.println("dividir(10, 0):");
        try {
            ops.dividir(10, 0);
        } catch (ArithmeticException e) {
            System.out.println("  cliente capturo: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== Nota sobre @AfterThrowing en Spring ===");
        System.out.println("@AfterThrowing(throwing=\"ex\") recibe la excepcion pero NO la suprime.");
        System.out.println("Para suprimir o cambiar la excepcion, usar @Around con try/catch.");
        System.out.println("Se usa tipicamente para logging de errores, alertas o metricas.");
    }
}
