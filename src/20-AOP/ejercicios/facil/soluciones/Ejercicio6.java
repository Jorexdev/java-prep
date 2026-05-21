import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Set;

public class Ejercicio6 {

    // Advice de logging
    interface LoggingAdvice {
        void log(String method, Object[] args, Object result);
    }

    // Proxy con pointcut por nombre de metodo
    static class NamedPointcutProxy {
        @SuppressWarnings("unchecked")
        static <T> T wrap(T target, Set<String> interceptados, LoggingAdvice advice) {
            return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                (proxy, method, args) -> {
                    Object result;
                    try {
                        result = method.invoke(target, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }

                    // Pointcut: solo aplicar advice si el metodo esta en la lista
                    if (interceptados.contains(method.getName())) {
                        advice.log(method.getName(), args, result);
                    }
                    return result;
                }
            );
        }
    }

    interface Calculadora {
        int sumar(int a, int b);
        int restar(int a, int b);
        int multiplicar(int a, int b); // NO interceptado
    }

    static class CalculadoraReal implements Calculadora {
        @Override public int sumar(int a, int b)       { return a + b; }
        @Override public int restar(int a, int b)      { return a - b; }
        @Override public int multiplicar(int a, int b) { return a * b; }
    }

    public static void main(String[] args) {
        System.out.println("=== Pointcut por nombre de metodo ===");
        System.out.println("Interceptados: sumar, restar  |  No interceptado: multiplicar\n");

        Set<String> pointcut = Set.of("sumar", "restar");

        LoggingAdvice advice = (method, methodArgs, result) ->
            System.out.println("  [INTERCEPTADO] " + method + "(" +
                               methodArgs[0] + ", " + methodArgs[1] + ") = " + result);

        Calculadora calc = NamedPointcutProxy.wrap(new CalculadoraReal(), pointcut, advice);

        System.out.println("sumar(5, 3):");
        calc.sumar(5, 3);
        System.out.println();

        System.out.println("restar(10, 4):");
        calc.restar(10, 4);
        System.out.println();

        System.out.println("multiplicar(3, 7):  <- NO deberia loguear");
        int r = calc.multiplicar(3, 7);
        System.out.println("  resultado (sin log): " + r);

        System.out.println();
        System.out.println("=== Equivalencia en Spring AOP ===");
        System.out.println("El pointcut es como:");
        System.out.println("  @Before(\"execution(* com.example.Calculadora.sumar(..))");
        System.out.println("       || execution(* com.example.Calculadora.restar(..))\")");
        System.out.println("En Spring, los pointcuts pueden usar expresiones AspectJ mucho mas ricas:");
        System.out.println("  execution, within, @annotation, args, target, etc.");
    }
}
