import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

// AOP - Programación Orientada a Aspectos simulada con JDK Dynamic Proxy
// En Spring: @Aspect + @Component sobre una clase con métodos @Before / @Around / etc.
// Aquí: InvocationHandler implementa los 4 advice types sin ninguna dependencia de Spring/AspectJ.
public class ExpAOP {

    // --- Interfaz de negocio (join point target) ---
    interface PagosService {
        double procesar(String concepto, double importe);
        void rechazar(String motivo);
    }

    // Implementación real — equivalente al @Service interceptado en Spring AOP
    static class PagosServiceImpl implements PagosService {
        @Override
        public double procesar(String concepto, double importe) {
            System.out.println("  [SERVICE] Procesando pago: " + concepto + " - " + importe + "€");
            return importe * 1.21; // IVA incluido
        }

        @Override
        public void rechazar(String motivo) {
            System.out.println("  [SERVICE] Pago rechazado: " + motivo);
            throw new IllegalStateException("Pago rechazado: " + motivo);
        }
    }

    // --- Aspect simulado como InvocationHandler ---
    // En Spring: @Aspect @Component public class PagosAspect { ... }
    static class PagosAspect implements InvocationHandler {

        private final Object target;

        PagosAspect(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // @Before — se ejecuta ANTES del método
            // En Spring: @Before("execution(* PagosService.*(..))")
            before(method);

            long inicio = System.currentTimeMillis();
            Object resultado = null;
            Throwable excepcion = null;

            // @Around — rodea la ejecución completa; pjp.proceed() = method.invoke(target, args)
            // En Spring: @Around("execution(* PagosService.*(..))")
            try {
                resultado = method.invoke(target, args);

                // @AfterReturning — solo si el método retornó sin excepción
                // En Spring: @AfterReturning(pointcut = "...", returning = "resultado")
                afterReturning(method, resultado);

            } catch (Throwable t) {
                excepcion = t.getCause() != null ? t.getCause() : t;

                // @AfterThrowing — solo si el método lanzó excepción
                // En Spring: @AfterThrowing(pointcut = "...", throwing = "ex")
                afterThrowing(method, excepcion);

                throw excepcion;
            } finally {
                // Parte "after" del @Around — siempre se ejecuta
                long tiempo = System.currentTimeMillis() - inicio;
                aroundMetrics(method, tiempo);
            }

            return resultado;
        }

        // @Before advice
        private void before(Method method) {
            System.out.println("[BEFORE] Ejecutando: " + method.getName() + "()");
        }

        // @AfterReturning advice
        private void afterReturning(Method method, Object resultado) {
            System.out.println("[AFTER_RETURNING] " + method.getName()
                    + " completado. Resultado: " + resultado);
        }

        // @AfterThrowing advice
        private void afterThrowing(Method method, Throwable ex) {
            System.out.println("[AFTER_THROWING] " + method.getName()
                    + " lanzó excepción: " + ex.getMessage());
        }

        // @Around metrics
        private void aroundMetrics(Method method, long ms) {
            System.out.println("[AROUND/METRICS] " + method.getName() + ": " + ms + " ms");
        }
    }

    // --- Fábrica de proxies (equivale a Spring creando CGLIB/JDK proxies) ---
    @SuppressWarnings("unchecked")
    static <T> T crearProxy(T target, Class<T> interfaz) {
        return (T) Proxy.newProxyInstance(
                interfaz.getClassLoader(),
                new Class<?>[]{ interfaz },
                new PagosAspect(target)
        );
    }

    public static void main(String[] args) {
        PagosService servicio = crearProxy(new PagosServiceImpl(), PagosService.class);

        System.out.println("=== Caso 1: método que retorna valor ===");
        double total = servicio.procesar("Suscripción mensual", 9.99);
        System.out.println("Total con IVA: " + total + "€");

        System.out.println("\n=== Caso 2: método que lanza excepción ===");
        try {
            servicio.rechazar("Saldo insuficiente");
        } catch (IllegalStateException e) {
            System.out.println("[MAIN] Excepción manejada: " + e.getMessage());
        }
    }
}
