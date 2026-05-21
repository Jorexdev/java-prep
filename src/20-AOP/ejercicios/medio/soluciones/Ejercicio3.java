import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class Ejercicio3 {

    // JoinPoint: contexto de la ejecucion del metodo
    static class JoinPoint {
        private final String methodName;
        private final Object[] args;
        private final Class<?> targetClass;

        JoinPoint(String methodName, Object[] args, Class<?> targetClass) {
            this.methodName = methodName;
            this.args = args;
            this.targetClass = targetClass;
        }

        public String getMethodName()  { return methodName; }
        public Object[] getArgs()      { return args; }
        public Class<?> getTargetClass() { return targetClass; }

        @Override
        public String toString() {
            return targetClass.getSimpleName() + "." + methodName + "(" + Arrays.toString(args) + ")";
        }
    }

    // Advice que recibe JoinPoint
    interface JoinPointAdvice {
        void before(JoinPoint jp);
        void afterReturning(JoinPoint jp, Object result);
    }

    interface Servicio {
        String saludar(String nombre);
        int calcular(int a, int b, String operacion);
        void notificar(String mensaje, int prioridad);
    }

    static class ServicioReal implements Servicio {
        @Override public String saludar(String nombre)                { return "Hola, " + nombre + "!"; }
        @Override public int calcular(int a, int b, String operacion) { return operacion.equals("+") ? a + b : a * b; }
        @Override public void notificar(String mensaje, int prioridad){ System.out.println("  [Notif] " + mensaje + " (prio=" + prioridad + ")"); }
    }

    @SuppressWarnings("unchecked")
    static <T> T wrap(T target, JoinPointAdvice advice) {
        return (T) Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            target.getClass().getInterfaces(),
            (proxy, method, args) -> {
                JoinPoint jp = new JoinPoint(method.getName(), args, target.getClass());
                advice.before(jp);
                Object result;
                try {
                    result = method.invoke(target, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
                advice.afterReturning(jp, result);
                return result;
            }
        );
    }

    public static void main(String[] args) {
        System.out.println("=== JoinPoint con datos completos ===\n");

        JoinPointAdvice advice = new JoinPointAdvice() {
            @Override
            public void before(JoinPoint jp) {
                System.out.println("[BEFORE] JoinPoint:");
                System.out.println("  clase  : " + jp.getTargetClass().getName());
                System.out.println("  metodo : " + jp.getMethodName());
                System.out.println("  args   : " + Arrays.toString(jp.getArgs()));
                System.out.println("  firma  : " + jp);
            }
            @Override
            public void afterReturning(JoinPoint jp, Object result) {
                System.out.println("[AFTER]  resultado=" + result);
                System.out.println();
            }
        };

        Servicio srv = wrap(new ServicioReal(), advice);

        srv.saludar("Jorex");
        srv.calcular(5, 3, "+");
        srv.calcular(4, 7, "*");
        srv.notificar("sistema iniciado", 1);

        System.out.println("=== En Spring AOP ===");
        System.out.println("ProceedingJoinPoint extiende JoinPoint con pjp.proceed().");
        System.out.println("jp.getSignature()  -> MethodSignature con nombre, tipo retorno, parametros");
        System.out.println("jp.getTarget()     -> el bean real (sin proxy)");
        System.out.println("jp.getThis()       -> el proxy");
        System.out.println("jp.getArgs()       -> argumentos de la llamada actual");
    }
}
