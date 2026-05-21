import java.util.LinkedHashMap;
import java.util.Map;

// Ejercicio 3 (Fácil) — @ConditionalOnClass
// Usa Class.forName() para detectar si una clase está en el classpath
public class Ejercicio3 {

    // Beans simulados que se registrarían si la clase correspondiente existe
    static class OptionalSupportBean {
        @Override public String toString() { return "OptionalSupportBean (usa java.util.Optional)"; }
    }

    static class LegacyOptionalBean {
        @Override public String toString() { return "LegacyOptionalBean (usa com.example.Optional)"; }
    }

    static class BeanContainer {
        private final Map<String, Object> beans = new LinkedHashMap<>();

        public void register(String name, Object bean) {
            beans.put(name, bean);
        }

        public boolean hasBean(String name) { return beans.containsKey(name); }
        public Object getBean(String name) { return beans.get(name); }
    }

    /**
     * Simula @ConditionalOnClass: intenta cargar la clase indicada.
     * Si está disponible, registra el bean en el contenedor.
     * Si no, imprime un aviso y no registra nada.
     */
    static void registerIfClassPresent(
            BeanContainer container,
            String className,
            String beanName,
            Object bean) {
        try {
            Class<?> found = Class.forName(className);
            System.out.println("[Condition] Clase '" + className + "' encontrada: "
                    + found.getName() + " → registrando " + beanName);
            container.register(beanName, bean);
        } catch (ClassNotFoundException e) {
            System.out.println("[Condition] Clase '" + className
                    + "' NO encontrada en el classpath → OMITIENDO " + beanName);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== @ConditionalOnClass ===");
        System.out.println();

        BeanContainer container = new BeanContainer();

        // Caso 1: com.example.Optional NO existe → bean omitido
        System.out.println("--- Caso 1: clase inexistente ---");
        registerIfClassPresent(
            container,
            "com.example.Optional",
            "legacyOptionalBean",
            new LegacyOptionalBean()
        );

        System.out.println();

        // Caso 2: java.util.Optional SÍ existe → bean registrado
        System.out.println("--- Caso 2: clase del JDK ---");
        registerIfClassPresent(
            container,
            "java.util.Optional",
            "optionalSupportBean",
            new OptionalSupportBean()
        );

        System.out.println();

        // Caso 3: java.util.ArrayList SÍ existe → bean registrado
        System.out.println("--- Caso 3: java.util.ArrayList ---");
        registerIfClassPresent(
            container,
            "java.util.ArrayList",
            "listSupportBean",
            "ListSupportBean active"
        );

        System.out.println();

        // Caso 4: com.mysql.jdbc.Driver NO existe (sin driver en classpath)
        System.out.println("--- Caso 4: driver MySQL (no en classpath) ---");
        registerIfClassPresent(
            container,
            "com.mysql.jdbc.Driver",
            "mysqlDataSource",
            "MySQLDataSource"
        );

        System.out.println();
        System.out.println("--- Estado del contenedor ---");
        System.out.println("legacyOptionalBean:  " + container.hasBean("legacyOptionalBean") + " (esperado: false)");
        System.out.println("optionalSupportBean: " + container.hasBean("optionalSupportBean") + " (esperado: true)");
        System.out.println("listSupportBean:     " + container.hasBean("listSupportBean") + " (esperado: true)");
        System.out.println("mysqlDataSource:     " + container.hasBean("mysqlDataSource") + " (esperado: false)");
        System.out.println();
        System.out.println("optionalSupportBean → " + container.getBean("optionalSupportBean"));
    }
}
