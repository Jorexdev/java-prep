import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Arrays;

// Simulación de pointcut expressions de AspectJ/Spring AOP usando reflection pura.
// En Spring: @Pointcut("execution(* com.ejemplo.servicio..*(..))") en un @Aspect.
public class ExpPointcutExpressions {

    // ── Anotaciones de ejemplo ────────────────────────────────────────────────
    @Retention(RetentionPolicy.RUNTIME) @interface Auditable { String value() default ""; }
    @Retention(RetentionPolicy.RUNTIME) @interface Transactional {}

    // ── Clases de servicio de ejemplo ─────────────────────────────────────────
    static class UserService {
        @Auditable("user-read")  public String findUser(int id)    { return "User#" + id; }
        @Auditable("user-write") public void   saveUser(String u)  { }
        @Transactional           public void   deleteUser(int id)  { }
        public void              helperMethod() { }
    }

    static class OrderService {
        @Auditable("order")  public String createOrder(String item, double price) { return item; }
        @Transactional       public void   cancelOrder(int id)  { }
        private void         internalCheck() { }
    }

    // ── 1. execution() — patrón más usado ─────────────────────────────────────
    // execution(modifiers? return-type class-pattern.method-pattern(params) throws?)
    //
    //   execution(* *(..))                   → cualquier método
    //   execution(public * UserService.*(..)) → públicos de UserService
    //   execution(* *Service.find*(..))       → métodos find* en clases *Service
    //   execution(* *(String, ..))            → primer arg String, resto cualquiera
    //
    // La simulación aquí usa reflection como sustituto del weaver.
    static void executionPointcut() {
        System.out.println("── 1. execution() pointcut ──");
        Class<?>[] servicios = { UserService.class, OrderService.class };

        for (Class<?> svc : servicios) {
            for (Method m : svc.getDeclaredMethods()) {
                // Simula: execution(public * *Service.*(..))
                boolean esPublico = java.lang.reflect.Modifier.isPublic(m.getModifiers());
                boolean esSvc = svc.getSimpleName().endsWith("Service");
                if (esPublico && esSvc) {
                    System.out.println("  MATCH execution: " + svc.getSimpleName() + "." + m.getName() + "()");
                }
            }
        }
    }

    // ── 2. @annotation() — interceptar métodos con anotación específica ───────
    // En Spring: @Pointcut("@annotation(com.ejemplo.Auditable)")
    // Muy usado para: @Transactional, @Cacheable, @PreAuthorize, anotaciones custom.
    static void annotationPointcut() {
        System.out.println("\n── 2. @annotation() pointcut ──");
        Class<?>[] servicios = { UserService.class, OrderService.class };

        for (Class<?> svc : servicios) {
            for (Method m : svc.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Auditable.class)) {
                    Auditable a = m.getAnnotation(Auditable.class);
                    System.out.println("  MATCH @Auditable(\"" + a.value() + "\"): "
                            + svc.getSimpleName() + "." + m.getName() + "()");
                }
                if (m.isAnnotationPresent(Transactional.class)) {
                    System.out.println("  MATCH @Transactional: "
                            + svc.getSimpleName() + "." + m.getName() + "()");
                }
            }
        }
    }

    // ── 3. within() — todos los métodos de un tipo o paquete ─────────────────
    // within(com.ejemplo.servicio.*) → todo método en clases del paquete
    // within(com.ejemplo..*) → paquete + subpaquetes (doble punto)
    //
    // Diferencia con execution(): within aplica al TIPO, no a la firma del método.
    // within(UserService) ≈ execution(* UserService.*(..)) para métodos propios,
    // pero within también captura métodos heredados llamados en esa clase.
    static void withinPointcut() {
        System.out.println("\n── 3. within() pointcut ──");
        // Simula: within(UserService) → todos los métodos declarados
        Class<?> target = UserService.class;
        System.out.println("  Métodos en within(UserService):");
        for (Method m : target.getDeclaredMethods()) {
            System.out.println("    " + m.getName() + "(" +
                    Arrays.stream(m.getParameterTypes()).map(Class::getSimpleName)
                          .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b) + ")");
        }
    }

    // ── 4. args() — filtrar por tipo de argumento en runtime ─────────────────
    // args(String, ..) → métodos cuyo primer argumento en runtime sea String
    // Útil para capturar el valor del argumento en el advice con binding:
    //   @Before("execution(* save*(..)) && args(entity,..)")
    //   public void before(Object entity) { ... }
    static void argsPointcut() throws Exception {
        System.out.println("\n── 4. args() pointcut ──");
        Class<?>[] servicios = { UserService.class, OrderService.class };

        for (Class<?> svc : servicios) {
            for (Method m : svc.getDeclaredMethods()) {
                Class<?>[] params = m.getParameterTypes();
                // Simula: args(String, ..) → primer param es String
                if (params.length > 0 && params[0] == String.class) {
                    System.out.println("  MATCH args(String,..): "
                            + svc.getSimpleName() + "." + m.getName() + "()");
                }
            }
        }
    }

    // ── 5. Combinación con &&, ||, ! ──────────────────────────────────────────
    // @Pointcut("@annotation(Auditable) && execution(public * *(..))")
    // @Pointcut("within(UserService) || within(OrderService)")
    // @Pointcut("execution(* *(..)) && !execution(* toString())")
    //
    // En Spring se pueden componer pointcuts nombrados:
    //   @Pointcut("publicMethods() && auditableAnnotation()")
    static void combinedPointcut() {
        System.out.println("\n── 5. Pointcuts combinados (&&, ||, !) ──");
        Class<?>[] servicios = { UserService.class, OrderService.class };

        for (Class<?> svc : servicios) {
            for (Method m : svc.getDeclaredMethods()) {
                boolean esPublico    = java.lang.reflect.Modifier.isPublic(m.getModifiers());
                boolean tieneAudit   = m.isAnnotationPresent(Auditable.class);
                boolean esFindMethod = m.getName().startsWith("find");

                // Simula: @annotation(Auditable) && execution(public * find*(..))
                if (tieneAudit && esPublico && esFindMethod) {
                    System.out.println("  MATCH @Auditable && public && find*: "
                            + svc.getSimpleName() + "." + m.getName() + "()");
                }
            }
        }
        System.out.println();
        System.out.println("  Resumen de designators:");
        System.out.println("  execution()    → firma del método (lo más usado)");
        System.out.println("  within()       → tipo/paquete del bean");
        System.out.println("  @annotation()  → anotación en el método");
        System.out.println("  @within()      → anotación en la clase");
        System.out.println("  args()         → tipo de argumentos en runtime");
        System.out.println("  bean()         → nombre del bean Spring (solo Spring AOP)");
        System.out.println("  target()       → tipo del objeto real (sin proxy)");
        System.out.println("  this()         → tipo del proxy");
    }

    public static void main(String[] args) throws Exception {
        executionPointcut();
        annotationPointcut();
        withinPointcut();
        argsPointcut();
        combinedPointcut();
    }
}
