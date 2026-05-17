import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

// AOP - Ejemplos prácticos en Spring
// Las clases interceptadas deben ser beans de Spring (@Service, @Component...).
// Esta clase también debe ser un bean con @Aspect + @Component.
// Spring Boot activa AOP automáticamente si spring-boot-starter-aop está en el classpath.
@Aspect
@Component
public class Examples {

    // @Before: se ejecuta ANTES del método.
    // Útil para logging de entrada, validaciones previas.
    // El pointcut execution(* solid.*.*(..)) intercepta cualquier método de cualquier clase en solid.
    @Before("execution(* solid.*.*(..))")
    public void logAntes() {
        System.out.println("[LOG] Ejecutando método...");
    }

    // @AfterReturning: se ejecuta después de que el método retorna correctamente.
    // Con el atributo "returning" puedes acceder al valor devuelto.
    // No se ejecuta si el método lanza una excepción.
    @AfterReturning(pointcut = "execution(* solid.*.*(..))", returning = "resultado")
    public void logDespues(Object resultado) {
        System.out.println("[LOG] Método completado. Resultado: " + resultado);
    }

    // @Around: rodea la ejecución completa del método.
    // Tienes control total: puedes modificar argumentos, resultado, o suprimir excepciones.
    // pjp.proceed() es donde se ejecuta el método original.
    @Around("execution(* solid.*.*(..))")
    public Object medirTiempo(ProceedingJoinPoint pjp) throws Throwable {
        long inicio = System.currentTimeMillis();

        Object resultado = pjp.proceed(); // ejecuta el método original

        long tiempo = System.currentTimeMillis() - inicio;
        System.out.println("[METRICS] " + pjp.getSignature().getName() + ": " + tiempo + " ms");

        return resultado;
    }

    // @AfterThrowing: se ejecuta solo si el método lanza una excepción.
    // Con el atributo "throwing" accedes a la excepción lanzada.
    // No suprime la excepción: el caller la sigue recibiendo.
    @AfterThrowing(pointcut = "execution(* solid.*.*(..))", throwing = "ex")
    public void manejarExcepcion(Exception ex) {
        System.out.println("[ERROR] Excepción capturada: " + ex.getMessage());
    }
}
