import java.lang.reflect.Proxy;

public class Ejercicio4 {

    // Interfaz del advice @AfterReturning
    interface AfterReturningAdvice {
        void afterReturning(Object result);
    }

    interface Calculadora {
        int sumar(int a, int b);
        int restar(int a, int b);
        int multiplicar(int a, int b);
    }

    static class CalculadoraReal implements Calculadora {
        @Override public int sumar(int a, int b)       { return a + b; }
        @Override public int restar(int a, int b)      { return a - b; }
        @Override public int multiplicar(int a, int b) { return a * b; }
    }

    static class ProxyFactory {
        @SuppressWarnings("unchecked")
        static <T> T wrap(T target, AfterReturningAdvice advice) {
            return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                (proxy, method, args) -> {
                    Object result = method.invoke(target, args); // llamada real
                    advice.afterReturning(result);               // @AfterReturning
                    return result;
                }
            );
        }
    }

    public static void main(String[] args) {
        System.out.println("=== @AfterReturning simulado ===\n");

        // Advice de auditoria: imprime resultado original y el doble
        AfterReturningAdvice auditAdvice = result -> {
            int original = (int) result;
            int transformado = original * 2;
            System.out.println("[AUDIT] resultado original=" + original +
                               " -> transformado (x2)=" + transformado);
        };

        Calculadora calc = ProxyFactory.wrap(new CalculadoraReal(), auditAdvice);

        System.out.println("sumar(3, 4):");
        int r1 = calc.sumar(3, 4);
        System.out.println("  valor retornado al cliente: " + r1);
        // Nota: el advice se ejecuta pero no modifica el valor retornado (solo observa)
        System.out.println("  (esperado: 7 original, audit loguea 14 pero devuelve 7)");
        System.out.println();

        System.out.println("restar(20, 5):");
        int r2 = calc.restar(20, 5);
        System.out.println("  valor retornado al cliente: " + r2);
        System.out.println();

        System.out.println("multiplicar(6, 7):");
        int r3 = calc.multiplicar(6, 7);
        System.out.println("  valor retornado al cliente: " + r3);

        System.out.println();
        System.out.println("=== Nota sobre @AfterReturning en Spring ===");
        System.out.println("@AfterReturning recibe el resultado pero NO puede modificarlo.");
        System.out.println("Para modificar el resultado de retorno se necesita @Around con pjp.proceed().");
    }
}
