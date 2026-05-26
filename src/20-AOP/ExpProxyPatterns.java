import java.lang.reflect.*;
import java.util.Arrays;

public class ExpProxyPatterns {

    // -----------------------------------------------------------------------
    // Target interface (Spring needs an interface for JDK dynamic proxy)
    // -----------------------------------------------------------------------
    interface UserService {
        String findById(int id);
        void save(String name);
    }

    static class UserServiceImpl implements UserService {
        @Override
        public String findById(int id) {
            return "User#" + id;
        }
        @Override
        public void save(String name) {
            System.out.println("  [DB] saved: " + name);
        }
    }

    // -----------------------------------------------------------------------
    // 1. Static Proxy — manual delegation with hand-written boilerplate
    //    Every method must be explicitly forwarded. Rigid but zero overhead.
    // -----------------------------------------------------------------------
    static class StaticLoggingProxy implements UserService {
        private final UserService delegate;

        StaticLoggingProxy(UserService delegate) { this.delegate = delegate; }

        @Override
        public String findById(int id) {
            System.out.println("  [StaticProxy] findById(" + id + ")");
            return delegate.findById(id);
        }

        @Override
        public void save(String name) {
            System.out.println("  [StaticProxy] save(" + name + ")");
            delegate.save(name);
        }
    }

    // -----------------------------------------------------------------------
    // 2. JDK Dynamic Proxy — works with interfaces only (no concrete class needed)
    //    Spring uses this for @Transactional, @Cacheable etc. when target implements interface.
    //    InvocationHandler intercepts every method call at runtime via reflection.
    // -----------------------------------------------------------------------
    static class LoggingInvocationHandler implements InvocationHandler {
        private final Object target;

        LoggingInvocationHandler(Object target) { this.target = target; }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.printf("  [JdkProxy] %s(%s)%n",
                    method.getName(), args != null ? Arrays.toString(args) : "");
            return method.invoke(target, args);
        }
    }

    static UserService createJdkProxy(UserService target) {
        return (UserService) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                new Class<?>[]{ UserService.class },
                new LoggingInvocationHandler(target)
        );
    }

    // -----------------------------------------------------------------------
    // 3. CGLIB-style subclass proxy — simulated with plain inheritance
    //    Spring uses CGLIB when the target has NO interface (or @EnableAspectJAutoProxy(proxyTargetClass=true)).
    //    Real CGLIB generates a subclass at runtime via bytecode manipulation.
    //    Here we simulate it with a manual subclass override.
    // -----------------------------------------------------------------------
    static class ConcreteUserService {
        public String findById(int id) { return "User#" + id; }
        public void save(String name)  { System.out.println("  [DB] saved: " + name); }
    }

    // Subclass overrides every method — CGLIB does this dynamically
    static class CglibStyleProxy extends ConcreteUserService {
        @Override
        public String findById(int id) {
            System.out.println("  [CglibProxy] findById(" + id + ")");
            return super.findById(id);
        }

        @Override
        public void save(String name) {
            System.out.println("  [CglibProxy] save(" + name + ")");
            super.save(name);
        }
    }

    public static void main(String[] args) {
        UserServiceImpl real = new UserServiceImpl();

        System.out.println("=== 1. Static Proxy ===");
        UserService staticProxy = new StaticLoggingProxy(real);
        System.out.println("  result: " + staticProxy.findById(42));
        staticProxy.save("Alice");

        System.out.println("\n=== 2. JDK Dynamic Proxy (interface-based) ===");
        System.out.println("  Spring uses this when target implements an interface.");
        UserService jdkProxy = createJdkProxy(real);
        System.out.println("  result: " + jdkProxy.findById(7));
        jdkProxy.save("Bob");

        System.out.println("\n=== 3. CGLIB-style Subclass Proxy (class-based) ===");
        System.out.println("  Spring falls back to this when there is no interface.");
        ConcreteUserService cglibProxy = new CglibStyleProxy();
        System.out.println("  result: " + cglibProxy.findById(99));
        cglibProxy.save("Carlos");

        System.out.println("\n--- Summary ---");
        System.out.println("  JDK proxy:   requires interface, uses Proxy.newProxyInstance");
        System.out.println("  CGLIB proxy: no interface needed, subclasses the target class");
        System.out.println("  Spring rule: interface present → JDK; no interface → CGLIB");
        System.out.println("  proxyTargetClass=true forces CGLIB even when interface exists");
    }
}
