import java.util.HashMap;
import java.util.Map;

public class Ejercicio1 {

    // @Component — bean con estado mutable para demostrar prototype vs singleton
    static class Servicio {
        private static int instanceCount = 0;
        private final int id;

        Servicio() {
            this.id = ++instanceCount;
            System.out.println("  [Servicio] instancia #" + id + " creada");
        }

        int getId() { return id; }
    }

    static class BeanDefinition {
        final Class<?> type;
        final String scope;
        final boolean lazy;

        BeanDefinition(Class<?> type, String scope, boolean lazy) {
            this.type = type;
            this.scope = scope;
            this.lazy = lazy;
        }
    }

    static class BeanContainer {
        private final Map<String, BeanDefinition> definitions = new HashMap<>();
        private final Map<String, Object> singletons = new HashMap<>();

        void register(String name, BeanDefinition def) {
            definitions.put(name, def);
            if (!def.lazy && "singleton".equals(def.scope)) {
                singletons.put(name, createInstance(def));
            }
        }

        Object getBean(String name) {
            BeanDefinition def = definitions.get(name);
            if (def == null) throw new RuntimeException("Bean no registrado: " + name);

            if ("singleton".equals(def.scope)) {
                return singletons.computeIfAbsent(name, k -> createInstance(def));
            } else { // prototype
                return createInstance(def);
            }
        }

        private Object createInstance(BeanDefinition def) {
            try {
                return def.type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("No se pudo instanciar " + def.type.getSimpleName(), e);
            }
        }
    }

    public static void main(String[] args) {
        BeanContainer container = new BeanContainer();

        System.out.println("Registrando beans...");
        container.register("singletonServicio",
            new BeanDefinition(Servicio.class, "singleton", false));
        container.register("prototypeServicio",
            new BeanDefinition(Servicio.class, "prototype", true));

        System.out.println("\n--- Singleton: siempre la misma instancia ---");
        Servicio s1 = (Servicio) container.getBean("singletonServicio");
        Servicio s2 = (Servicio) container.getBean("singletonServicio");
        System.out.println("s1.id=" + s1.getId() + ", s2.id=" + s2.getId() + ", misma instancia: " + (s1 == s2));

        System.out.println("\n--- Prototype: nueva instancia cada vez ---");
        Servicio p1 = (Servicio) container.getBean("prototypeServicio");
        Servicio p2 = (Servicio) container.getBean("prototypeServicio");
        System.out.println("p1.id=" + p1.getId() + ", p2.id=" + p2.getId() + ", misma instancia: " + (p1 == p2));
    }
}
