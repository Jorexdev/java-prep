import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Ejercicio 2 (Medio) — Condition evaluation
// ConditionContext + 3 Conditions combinadas con AND
public class Ejercicio2 {

    // Contenedor simplificado para el contexto de condiciones
    static class BeanContainer {
        private final Map<Class<?>, Object> beans = new LinkedHashMap<>();

        public <T> void register(Class<T> type, T bean) { beans.put(type, bean); }
        public boolean hasBeanOfType(Class<?> type) { return beans.containsKey(type); }
    }

    static class ConditionContext {
        final Map<String, String> props;
        final BeanContainer container;

        ConditionContext(Map<String, String> props, BeanContainer container) {
            this.props = props;
            this.container = container;
        }
    }

    interface Condition {
        String getName();
        boolean matches(ConditionContext context);
    }

    // Condición 1: la propiedad existe en el mapa de config
    static class PropertyPresentCondition implements Condition {
        private final String key;

        PropertyPresentCondition(String key) { this.key = key; }

        @Override public String getName() { return "PropertyPresent(" + key + ")"; }

        @Override
        public boolean matches(ConditionContext ctx) {
            boolean present = ctx.props.containsKey(key);
            System.out.printf("  %-45s → %s%n", getName(), present ? "MATCH" : "NO_MATCH (ausente)");
            return present;
        }
    }

    // Condición 2: la clase existe en el classpath
    static class ClassPresentCondition implements Condition {
        private final String className;

        ClassPresentCondition(String className) { this.className = className; }

        @Override public String getName() { return "ClassPresent(" + className + ")"; }

        @Override
        public boolean matches(ConditionContext ctx) {
            try {
                Class.forName(className);
                System.out.printf("  %-45s → %s%n", getName(), "MATCH");
                return true;
            } catch (ClassNotFoundException e) {
                System.out.printf("  %-45s → %s%n", getName(), "NO_MATCH (no en classpath)");
                return false;
            }
        }
    }

    // Condición 3: NO hay bean del tipo indicado en el contenedor
    static class BeanAbsentCondition implements Condition {
        private final Class<?> beanType;

        BeanAbsentCondition(Class<?> beanType) { this.beanType = beanType; }

        @Override public String getName() { return "BeanAbsent(" + beanType.getSimpleName() + ")"; }

        @Override
        public boolean matches(ConditionContext ctx) {
            boolean absent = !ctx.container.hasBeanOfType(beanType);
            System.out.printf("  %-45s → %s%n", getName(), absent ? "MATCH" : "NO_MATCH (bean ya existe)");
            return absent;
        }
    }

    // Composición AND de todas las condiciones
    static class CompositeCondition implements Condition {
        private final List<Condition> conditions;
        private final String name;

        CompositeCondition(String name, List<Condition> conditions) {
            this.name = name;
            this.conditions = conditions;
        }

        @Override public String getName() { return name; }

        @Override
        public boolean matches(ConditionContext ctx) {
            System.out.println("Evaluando CompositeCondition AND [" + name + "]:");
            boolean result = true;
            for (Condition c : conditions) {
                boolean matched = c.matches(ctx);
                if (!matched) result = false;
                // No hacemos short-circuit para mostrar todas las condiciones
            }
            System.out.println("  RESULTADO: " + (result ? "MATCH (todas cumplidas)" : "NO_MATCH (alguna falló)"));
            return result;
        }
    }

    // Bean de ejemplo para la condición BeanAbsent
    static class DataSource {
        @Override public String toString() { return "DataSource[h2]"; }
    }

    public static void main(String[] args) {
        System.out.println("=== Condition evaluation ===");
        System.out.println();

        BeanContainer container = new BeanContainer();

        // --- Escenario 1: todas las condiciones se cumplen ---
        System.out.println("=== Escenario 1: todas las condiciones MATCH ===");
        Map<String, String> props1 = new HashMap<>();
        props1.put("db.enabled", "true");

        ConditionContext ctx1 = new ConditionContext(props1, container);

        CompositeCondition condition1 = new CompositeCondition("DataSourceCondition", List.of(
            new PropertyPresentCondition("db.enabled"),
            new ClassPresentCondition("java.util.Optional"),  // siempre existe
            new BeanAbsentCondition(DataSource.class)         // no hay DataSource aún
        ));

        boolean result1 = condition1.matches(ctx1);
        System.out.println("¿Se ejecuta la auto-config? " + result1);

        System.out.println();

        // --- Escenario 2: falla PropertyPresentCondition ---
        System.out.println("=== Escenario 2: propiedad ausente → NO_MATCH ===");
        Map<String, String> props2 = new HashMap<>(); // sin db.enabled
        ConditionContext ctx2 = new ConditionContext(props2, container);

        boolean result2 = condition1.matches(ctx2);
        System.out.println("¿Se ejecuta la auto-config? " + result2);

        System.out.println();

        // --- Escenario 3: falla BeanAbsentCondition ---
        System.out.println("=== Escenario 3: bean ya existe → NO_MATCH ===");
        container.register(DataSource.class, new DataSource());

        boolean result3 = condition1.matches(ctx1);
        System.out.println("¿Se ejecuta la auto-config? " + result3);

        System.out.println();

        // --- Escenario 4: falla ClassPresentCondition ---
        System.out.println("=== Escenario 4: clase no en classpath → NO_MATCH ===");
        BeanContainer emptyContainer = new BeanContainer();
        ConditionContext ctx4 = new ConditionContext(props1, emptyContainer);

        CompositeCondition condition4 = new CompositeCondition("MongoCondition", List.of(
            new PropertyPresentCondition("db.enabled"),
            new ClassPresentCondition("com.mongodb.client.MongoClient"), // no existe
            new BeanAbsentCondition(DataSource.class)
        ));

        boolean result4 = condition4.matches(ctx4);
        System.out.println("¿Se ejecuta la auto-config? " + result4);
    }
}
