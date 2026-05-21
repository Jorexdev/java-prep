import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

public class Ejercicio3 {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Secured {
        String[] roles();
    }

    static class Authentication {
        final String username;
        final List<String> roles;

        Authentication(String username, List<String> roles) {
            this.username = username;
            this.roles = List.copyOf(roles);
        }
    }

    static class SecurityContext {
        private static final ThreadLocal<Authentication> CTX = new ThreadLocal<>();
        static void set(Authentication a)   { CTX.set(a); }
        static Authentication get()         { return CTX.get(); }
        static void clear()                 { CTX.remove(); }
    }

    static class AccessDeniedException extends RuntimeException {
        AccessDeniedException(String msg) { super(msg); }
    }

    interface AdminServicePort {
        void crearUsuario(String nombre);
        void borrarUsuario(String nombre);
        void verReporte();
    }

    static class AdminServiceImpl implements AdminServicePort {

        // @PreAuthorize("hasRole('ADMIN')")
        @Secured(roles = {"ADMIN"})
        @Override
        public void crearUsuario(String nombre) {
            System.out.println("[AdminService] Usuario creado: " + nombre);
        }

        // @PreAuthorize("hasRole('ADMIN')")
        @Secured(roles = {"ADMIN"})
        @Override
        public void borrarUsuario(String nombre) {
            System.out.println("[AdminService] Usuario borrado: " + nombre);
        }

        // @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
        @Secured(roles = {"ADMIN", "EDITOR"})
        @Override
        public void verReporte() {
            System.out.println("[AdminService] Mostrando reporte");
        }
    }

    @SuppressWarnings("unchecked")
    static class SecureProxy<T> implements InvocationHandler {
        private final T target;

        SecureProxy(T target) { this.target = target; }

        public static <T> T create(T target, Class<T> iface) {
            return (T) Proxy.newProxyInstance(
                    iface.getClassLoader(),
                    new Class[]{iface},
                    new SecureProxy<>(target));
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Method realMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
            Secured secured = realMethod.getAnnotation(Secured.class);

            if (secured != null) {
                Authentication auth = SecurityContext.get();
                if (auth == null) throw new AccessDeniedException("Sin autenticación");
                boolean allowed = Arrays.stream(secured.roles())
                        .anyMatch(r -> auth.roles.contains(r));
                if (!allowed) {
                    throw new AccessDeniedException(
                            "Usuario '" + auth.username + "' no tiene ninguno de los roles: "
                            + Arrays.toString(secured.roles()));
                }
            }
            return method.invoke(target, args);
        }
    }

    public static void main(String[] args) throws Exception {
        AdminServicePort service = SecureProxy.create(new AdminServiceImpl(), AdminServicePort.class);

        System.out.println("=== Usuario ADMIN ===");
        SecurityContext.set(new Authentication("jorge", List.of("ADMIN")));
        invocar(service, "crearUsuario",  "pedro");
        invocar(service, "borrarUsuario", "pedro");
        invocar(service, "verReporte");
        SecurityContext.clear();

        System.out.println();
        System.out.println("=== Usuario básico (USER) ===");
        SecurityContext.set(new Authentication("ana", List.of("USER")));
        invocar(service, "crearUsuario",  "carlos");
        invocar(service, "borrarUsuario", "carlos");
        invocar(service, "verReporte");
        SecurityContext.clear();
    }

    static void invocar(AdminServicePort svc, String op, String... extra) {
        try {
            switch (op) {
                case "crearUsuario"  -> svc.crearUsuario(extra[0]);
                case "borrarUsuario" -> svc.borrarUsuario(extra[0]);
                case "verReporte"    -> svc.verReporte();
            }
        } catch (AccessDeniedException e) {
            System.out.println("[DENEGADO] " + op + ": " + e.getMessage());
        }
    }
}
