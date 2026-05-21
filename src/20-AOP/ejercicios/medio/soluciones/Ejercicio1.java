import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

public class Ejercicio1 {

    // @Around advice: controla completamente la ejecucion
    interface AroundAdvice {
        Object invoke(String method, Object[] args, Callable<Object> proceed) throws Throwable;
    }

    interface Calculadora {
        int sumar(int a, int b);
    }

    static class CalculadoraReal implements Calculadora {
        @Override
        public int sumar(int a, int b) {
            System.out.println("  [Real] ejecutando sumar(" + a + ", " + b + ")");
            return a + b;
        }
    }

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

    public static void main(String[] args) throws Throwable {
        System.out.println("=== @Around advice: 3 casos ===\n");

        // Caso 1: ejecucion normal (delegar a proceed)
        System.out.println("--- Caso 1: ejecucion normal ---");
        AroundAdvice normalAdvice = (method, mArgs, proceed) -> {
            System.out.println("[AROUND] before: " + method);
            Object result = proceed.call();
            System.out.println("[AROUND] after: resultado=" + result);
            return result;
        };
        Calculadora c1 = wrap(new CalculadoraReal(), normalAdvice);
        int r1 = c1.sumar(5, 3);
        System.out.println("Resultado para cliente: " + r1 + "\n");

        // Caso 2: bloquear si el primer argumento es negativo
        System.out.println("--- Caso 2: bloquear si args[0] < 0 ---");
        AroundAdvice blockingAdvice = (method, mArgs, proceed) -> {
            int primerArg = (int) mArgs[0];
            if (primerArg < 0) {
                System.out.println("[AROUND] BLOQUEADO: argumento negativo " + primerArg);
                return -1; // valor de retorno de fallback
            }
            return proceed.call();
        };
        Calculadora c2 = wrap(new CalculadoraReal(), blockingAdvice);
        System.out.println("sumar(-3, 5) -> " + c2.sumar(-3, 5));
        System.out.println("sumar(2, 5)  -> " + c2.sumar(2, 5) + "\n");

        // Caso 3: modificar resultado (multiplicar por 10)
        System.out.println("--- Caso 3: modificar resultado (x10) ---");
        AroundAdvice modifyingAdvice = (method, mArgs, proceed) -> {
            Object original = proceed.call();
            Object modificado = (int) original * 10;
            System.out.println("[AROUND] resultado original=" + original + " -> modificado=" + modificado);
            return modificado;
        };
        Calculadora c3 = wrap(new CalculadoraReal(), modifyingAdvice);
        System.out.println("sumar(4, 6) x10 -> " + c3.sumar(4, 6));

        System.out.println();
        System.out.println("@Around es el advice mas poderoso: puede ejecutar o no el metodo,");
        System.out.println("modificar argumentos y resultado, manejar excepciones.");
    }
}
