import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio3 {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Cacheable {}

    interface CalculadorCostoso {
        int calcular(int a, int b);
        String procesar(String input);
        // sin @Cacheable
        int suma(int a, int b);
    }

    static class CalculadorReal implements CalculadorCostoso {
        private final AtomicInteger llamadasReales = new AtomicInteger(0);

        @Override
        @Cacheable
        public int calcular(int a, int b) {
            llamadasReales.incrementAndGet();
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return a * b + a;
        }

        @Override
        @Cacheable
        public String procesar(String input) {
            llamadasReales.incrementAndGet();
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return input.toUpperCase() + "_procesado";
        }

        @Override
        public int suma(int a, int b) {
            llamadasReales.incrementAndGet();
            return a + b;
        }

        int getLlamadasReales() { return llamadasReales.get(); }
    }

    @SuppressWarnings("unchecked")
    static <T> T wrapConCache(T target) {
        Class<?> realClass = target.getClass();
        ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
        AtomicInteger cacheHits = new AtomicInteger(0);

        T proxy = (T) Proxy.newProxyInstance(
            realClass.getClassLoader(),
            realClass.getInterfaces(),
            (p, method, args) -> {
                // Buscar la anotacion en la clase real
                Method realMethod;
                try {
                    realMethod = realClass.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException e) {
                    realMethod = method;
                }

                if (realMethod.isAnnotationPresent(Cacheable.class)) {
                    String key = method.getName() + Arrays.toString(args);
                    Object cached = cache.get(key);
                    if (cached != null) {
                        cacheHits.incrementAndGet();
                        System.out.println("  [CACHE HIT] " + key + " -> " + cached);
                        return cached;
                    }
                    System.out.println("  [CACHE MISS] " + key + " - ejecutando metodo real...");
                    Object result;
                    try {
                        result = method.invoke(target, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                    cache.put(key, result);
                    return result;
                }

                // Sin @Cacheable: ejecucion directa
                try {
                    return method.invoke(target, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        );

        // Acceso al contador de hits via closure (guardado en el objeto real)
        return proxy;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== @Cacheable aspect ===\n");

        CalculadorReal real = new CalculadorReal();
        CalculadorCostoso calc = wrapConCache(real);

        long start = System.currentTimeMillis();

        System.out.println("--- calcular(5, 3) x3 veces ---");
        for (int i = 0; i < 3; i++) {
            int r = calc.calcular(5, 3);
            System.out.println("  resultado: " + r);
        }

        System.out.println();
        System.out.println("--- calcular(10, 2) x2 veces (args distintos) ---");
        for (int i = 0; i < 2; i++) {
            int r = calc.calcular(10, 2);
            System.out.println("  resultado: " + r);
        }

        System.out.println();
        System.out.println("--- procesar('java') x2 veces ---");
        for (int i = 0; i < 2; i++) {
            String r = calc.procesar("java");
            System.out.println("  resultado: " + r);
        }

        System.out.println();
        System.out.println("--- suma(1, 2) x2 veces (sin @Cacheable) ---");
        for (int i = 0; i < 2; i++) {
            int r = calc.suma(1, 2);
            System.out.println("  resultado: " + r + " (siempre ejecuta el metodo real)");
        }

        long elapsed = System.currentTimeMillis() - start;

        System.out.println();
        System.out.println("=== Estadisticas ===");
        System.out.println("Llamadas reales al metodo: " + real.getLlamadasReales());
        System.out.println("Tiempo total             : " + elapsed + "ms");
        System.out.println("(Sin cache esperaria ~" +
                           (3 * 100 + 2 * 100 + 2 * 50 + 2 * 0) + "ms, con cache mucho menos)");
        System.out.println();
        System.out.println("En Spring: @Cacheable(\"miCache\") integra con CaffeineCacheManager,");
        System.out.println("RedisCacheManager, etc. La clave por defecto son los argumentos del metodo.");
    }
}
