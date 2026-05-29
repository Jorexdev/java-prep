import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;

// Aspect que intercepta y transforma resultados + manejo de excepciones encadenado
// Demuestra: result transformation, exception translation, y aspect chaining con InvocationHandler

public class Ejercicio5 {

    // ====== Cadena de aspectos via InvocationHandler ======

    // Aspecto 1: transforma resultados numericos (multiplica por factor)
    static class ResultTransformAspect implements InvocationHandler {
        private final Object target;
        private final double factor;

        ResultTransformAspect(Object target, double factor) {
            this.target = target;
            this.factor = factor;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = method.invoke(target, args);
            if (result instanceof Number n) {
                double transformed = n.doubleValue() * factor;
                System.out.printf("  [ResultTransform] %s: %s -> %.2f (factor %.1f)%n",
                        method.getName(), result, transformed, factor);
                return (int) transformed; // retorna int transformado
            }
            return result;
        }
    }

    // Aspecto 2: intercepta excepciones y las encadena con contexto adicional
    static class ExceptionTranslationAspect implements InvocationHandler {
        private final Object target;

        ExceptionTranslationAspect(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                System.out.printf("  [ExceptionTranslation] %s lanzó %s: \"%s\"%n",
                        method.getName(),
                        cause.getClass().getSimpleName(),
                        cause.getMessage());

                // Traduce excepciones de bajo nivel a excepciones de dominio
                if (cause instanceof ArithmeticException) {
                    throw new DomainException("Error de calculo en " + method.getName(), cause);
                }
                if (cause instanceof IllegalArgumentException) {
                    throw new ValidationException("Argumento invalido en " + method.getName(), cause);
                }
                throw new ServiceException("Error inesperado en " + method.getName(), cause);
            }
        }
    }

    // Aspecto 3: logging detallado (entrada + salida + tiempo)
    static class DetailedLoggingAspect implements InvocationHandler {
        private final Object target;

        DetailedLoggingAspect(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String argStr = args != null ? Arrays.toString(args) : "[]";
            System.out.printf("  [Log] ENTER %s%s%n", method.getName(), argStr);
            long t = System.nanoTime();
            try {
                Object result = method.invoke(target, args);
                long ms = (System.nanoTime() - t) / 1_000_000;
                System.out.printf("  [Log] EXIT  %s -> %s (%dms)%n",
                        method.getName(), result, ms);
                return result;
            } catch (InvocationTargetException e) {
                long ms = (System.nanoTime() - t) / 1_000_000;
                System.out.printf("  [Log] THROW %s -> %s (%dms)%n",
                        method.getName(), e.getCause().getClass().getSimpleName(), ms);
                throw e.getCause();
            }
        }
    }

    // Excepciones de dominio
    static class DomainException extends RuntimeException {
        DomainException(String msg, Throwable cause) { super(msg, cause); }
    }
    static class ValidationException extends RuntimeException {
        ValidationException(String msg, Throwable cause) { super(msg, cause); }
    }
    static class ServiceException extends RuntimeException {
        ServiceException(String msg, Throwable cause) { super(msg, cause); }
    }

    // ====== Servicio objetivo ======

    interface Calculadora {
        int dividir(int a, int b);
        int potencia(int base, int exp);
        int raizCuadrada(int n);
    }

    static class CalculadoraImpl implements Calculadora {
        public int dividir(int a, int b) {
            if (b == 0) throw new ArithmeticException("division por cero");
            return a / b;
        }
        public int potencia(int base, int exp) {
            if (exp < 0) throw new IllegalArgumentException("exponente negativo no soportado");
            return (int) Math.pow(base, exp);
        }
        public int raizCuadrada(int n) {
            if (n < 0) throw new ArithmeticException("raiz de numero negativo");
            return (int) Math.sqrt(n);
        }
    }

    // Factory que apila los 3 aspectos en cadena
    // Orden: Log (externo) -> ExceptionTranslation -> ResultTransform -> Target
    @SuppressWarnings("unchecked")
    static <T> T buildAspectChain(T target, Class<T> iface, double transformFactor) {
        // Capa 1 (mas interna): ResultTransform sobre el target real
        T withTransform = (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class[]{iface},
                new ResultTransformAspect(target, transformFactor)
        );

        // Capa 2: ExceptionTranslation sobre la capa anterior
        T withExcTranslation = (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class[]{iface},
                new ExceptionTranslationAspect(withTransform)
        );

        // Capa 3 (mas externa): Logging sobre todo
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class[]{iface},
                new DetailedLoggingAspect(withExcTranslation)
        );
    }

    public static void main(String[] args) {
        System.out.println("=== Aspect Chain: ResultTransform + ExceptionTranslation + Logging ===");
        System.out.println("Cadena: [Log] -> [ExcTranslation] -> [ResultTransform] -> [Target]");
        System.out.println();

        Calculadora proxy = buildAspectChain(new CalculadoraImpl(), Calculadora.class, 1.5);

        // --- Demo 1: llamada exitosa (resultado transformado) ---
        System.out.println("[ Demo 1 ] dividir(10, 2) -> resultado multiplicado x1.5");
        int r1 = proxy.dividir(10, 2);
        System.out.printf("  Valor recibido por el cliente: %d%n", r1);
        System.out.println();

        // --- Demo 2: potencia exitosa ---
        System.out.println("[ Demo 2 ] potencia(3, 3) -> resultado multiplicado x1.5");
        int r2 = proxy.potencia(3, 3);
        System.out.printf("  Valor recibido por el cliente: %d%n", r2);
        System.out.println();

        // --- Demo 3: excepcion traducida (ArithmeticException -> DomainException) ---
        System.out.println("[ Demo 3 ] dividir(5, 0) -> ArithmeticException traducida a DomainException");
        try {
            proxy.dividir(5, 0);
        } catch (DomainException e) {
            System.out.printf("  Cliente recibe: %s: \"%s\"%n",
                    e.getClass().getSimpleName(), e.getMessage());
            System.out.printf("  Causa original: %s: \"%s\"%n",
                    e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
        }
        System.out.println();

        // --- Demo 4: excepcion traducida (IllegalArgumentException -> ValidationException) ---
        System.out.println("[ Demo 4 ] potencia(2, -1) -> IllegalArgumentException traducida a ValidationException");
        try {
            proxy.potencia(2, -1);
        } catch (ValidationException e) {
            System.out.printf("  Cliente recibe: %s: \"%s\"%n",
                    e.getClass().getSimpleName(), e.getMessage());
        }
        System.out.println();

        // --- Demo 5: raiz de negativo ---
        System.out.println("[ Demo 5 ] raizCuadrada(-4) -> DomainException");
        try {
            proxy.raizCuadrada(-4);
        } catch (DomainException e) {
            System.out.printf("  Cliente recibe: %s: \"%s\"%n",
                    e.getClass().getSimpleName(), e.getMessage());
        }
        System.out.println();

        System.out.println("=== Beneficios del Aspect Chaining ===");
        System.out.println("1. ResultTransform: transforma resultados sin tocar la logica de negocio.");
        System.out.println("2. ExceptionTranslation: aisla al cliente de excepciones de infraestructura.");
        System.out.println("3. Logging: corte transversal sin contaminar ningun metodo.");
        System.out.println("Orden de la cadena: el aspecto mas externo se ejecuta primero/ultimo.");
    }
}
