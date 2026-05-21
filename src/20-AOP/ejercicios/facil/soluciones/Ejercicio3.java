import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class Ejercicio3 {

    // Interfaz del advice @Before
    interface BeforeAdvice {
        void before(String method, Object[] args);
    }

    // Interfaz de negocio
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

    // ProxyFactory: crea un proxy dinamico que aplica BeforeAdvice
    // Usa java.lang.reflect.Proxy (la base de Spring AOP para interfaces)
    static class ProxyFactory {
        @SuppressWarnings("unchecked")
        static <T> T wrap(T target, BeforeAdvice advice) {
            return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                (proxy, method, args) -> {
                    advice.before(method.getName(), args);  // @Before
                    return method.invoke(target, args);     // llamada real
                }
            );
        }
    }

    public static void main(String[] args) {
        System.out.println("=== @Before simulado con java.lang.reflect.Proxy ===\n");

        BeforeAdvice loggingAdvice = (method, methodArgs) ->
            System.out.println("[BEFORE] metodo=" + method + " args=" + Arrays.toString(methodArgs));

        Calculadora calc = ProxyFactory.wrap(new CalculadoraReal(), loggingAdvice);

        System.out.println("sumar(5, 3) = " + calc.sumar(5, 3));
        System.out.println();
        System.out.println("restar(10, 7) = " + calc.restar(10, 7));
        System.out.println();
        System.out.println("multiplicar(4, 6) = " + calc.multiplicar(4, 6));

        System.out.println();
        System.out.println("=== Mecanismo interno ===");
        System.out.println("Proxy.newProxyInstance() genera una clase en memoria que implementa");
        System.out.println("la interfaz y delega al InvocationHandler en cada llamada.");
        System.out.println("Spring AOP hace exactamente esto para beans con interfaz.");
        System.out.println("Para clases sin interfaz, Spring usa CGLIB (subclase dinamica).");
    }
}
