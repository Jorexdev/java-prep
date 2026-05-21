import java.util.Arrays;

public class Ejercicio1 {

    // Interfaz del servicio
    interface Calculadora {
        int sumar(int a, int b);
        int restar(int a, int b);
        int multiplicar(int a, int b);
    }

    // Implementacion real
    static class CalculadoraReal implements Calculadora {
        @Override public int sumar(int a, int b)      { return a + b; }
        @Override public int restar(int a, int b)     { return a - b; }
        @Override public int multiplicar(int a, int b){ return a * b; }
    }

    // Proxy de logging: patrón Decorator / Proxy manual
    // Equivale a un @Before + @AfterReturning en Spring AOP
    static class LoggingProxy implements Calculadora {
        private final Calculadora real;

        LoggingProxy(Calculadora real) { this.real = real; }

        private void logBefore(String method, Object... args) {
            System.out.println("[LOG] -> " + method + "(" + Arrays.toString(args) + ")");
        }
        private void logAfter(String method, Object result) {
            System.out.println("[LOG] <- " + method + " = " + result);
        }

        @Override
        public int sumar(int a, int b) {
            logBefore("sumar", a, b);
            int result = real.sumar(a, b);
            logAfter("sumar", result);
            return result;
        }

        @Override
        public int restar(int a, int b) {
            logBefore("restar", a, b);
            int result = real.restar(a, b);
            logAfter("restar", result);
            return result;
        }

        @Override
        public int multiplicar(int a, int b) {
            logBefore("multiplicar", a, b);
            int result = real.multiplicar(a, b);
            logAfter("multiplicar", result);
            return result;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Proxy de Logging (patrón Proxy / Decorator) ===\n");

        Calculadora calc = new LoggingProxy(new CalculadoraReal());

        int r1 = calc.sumar(5, 3);
        System.out.println("  resultado final: " + r1);
        System.out.println();

        int r2 = calc.restar(10, 4);
        System.out.println("  resultado final: " + r2);
        System.out.println();

        int r3 = calc.multiplicar(3, 7);
        System.out.println("  resultado final: " + r3);

        System.out.println();
        System.out.println("=== Equivalencia AOP ===");
        System.out.println("logBefore()   ~ @Before advice");
        System.out.println("logAfter()    ~ @AfterReturning advice");
        System.out.println("LoggingProxy  ~ Spring @Aspect con @Around");
        System.out.println("El proxy es transparente: el cliente no sabe que hay logging.");
    }
}
