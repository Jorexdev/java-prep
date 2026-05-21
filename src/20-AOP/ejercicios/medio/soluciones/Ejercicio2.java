import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

public class Ejercicio2 {

    interface AroundAdvice {
        Object invoke(String method, Object[] args, Callable<Object> proceed) throws Throwable;
    }

    interface Calculadora {
        int sumar(int a, int b);
        int multiplicar(int a, int b);
    }

    static class CalculadoraReal implements Calculadora {
        @Override public int sumar(int a, int b)       { return a + b; }
        @Override public int multiplicar(int a, int b) { return a * b; }
    }

    // Aplica un advice y retorna un nuevo proxy que encadena con el advice anterior
    @SuppressWarnings("unchecked")
    static <T> T wrap(T target, AroundAdvice advice) {
        return (T) Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            target.getClass().getInterfaces(),
            (proxy, method, args) -> {
                Callable<Object> proceed = () -> {
                    try {
                        return method.invoke(target, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                };
                return advice.invoke(method.getName(), args, proceed);
            }
        );
    }

    // LoggingAspect: @Before + @AfterReturning
    static AroundAdvice loggingAspect() {
        return (method, args, proceed) -> {
            System.out.println("[LOG] before: " + method);
            Object result = proceed.call();
            System.out.println("[LOG] after: " + method + " -> " + result);
            return result;
        };
    }

    // TimingAspect: mide tiempo
    static AroundAdvice timingAspect() {
        return (method, args, proceed) -> {
            System.out.println("[TIMING] before: " + method);
            long start = System.currentTimeMillis();
            Object result = proceed.call();
            System.out.println("[TIMING] after: " + method + " -> " +
                               (System.currentTimeMillis() - start) + "ms");
            return result;
        };
    }

    public static void main(String[] args) throws Throwable {
        System.out.println("=== Aspect Chaining: Logging -> Timing ===");
        System.out.println("Orden esperado: LOG-before, TIMING-before, metodo, TIMING-after, LOG-after\n");

        CalculadoraReal real = new CalculadoraReal();

        // Encadenar: primero aplicar timing al real, luego logging al resultado
        // Esto crea: LoggingProxy -> TimingProxy -> Real
        // Llamada: LOG-before -> TIMING-before -> Real -> TIMING-after -> LOG-after
        Calculadora conTiming = wrap(real, timingAspect());
        Calculadora conLoggingYTiming = wrap(conTiming, loggingAspect());

        System.out.println("Llamando sumar(5, 3):");
        int r = conLoggingYTiming.sumar(5, 3);
        System.out.println("Resultado final: " + r);

        System.out.println();
        System.out.println("Llamando multiplicar(4, 6):");
        int r2 = conLoggingYTiming.multiplicar(4, 6);
        System.out.println("Resultado final: " + r2);

        System.out.println();
        System.out.println("=== Orden inverso: Timing -> Logging ===");
        Calculadora conLoggging2 = wrap(real, loggingAspect());
        Calculadora conTimingYLogging = wrap(conLoggging2, timingAspect());

        System.out.println("Llamando sumar(2, 8) con orden invertido:");
        conTimingYLogging.sumar(2, 8);

        System.out.println();
        System.out.println("En Spring AOP, el orden se controla con @Order(n) en el @Aspect.");
        System.out.println("Menor numero = mayor prioridad = aspecto mas externo (primer before, ultimo after).");
    }
}
