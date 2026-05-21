import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Ejercicio 3 (Medio) — @ConditionalOnWebApplication
// Registra beans distintos según app.type: web / servlet / reactive
public class Ejercicio3 {

    // Tipos de aplicación
    enum AppType {
        WEB, SERVLET, REACTIVE
    }

    // Beans para modo WEB
    static class DispatcherServlet {
        @Override public String toString() { return "DispatcherServlet"; }
    }
    static class HandlerMapping {
        @Override public String toString() { return "HandlerMapping"; }
    }
    static class ViewResolver {
        @Override public String toString() { return "ViewResolver"; }
    }

    // Beans para modo SERVLET
    static class ServletContext {
        @Override public String toString() { return "ServletContext"; }
    }
    static class FilterChain {
        @Override public String toString() { return "FilterChain"; }
    }

    // Beans para modo REACTIVE
    static class WebFluxHandler {
        @Override public String toString() { return "WebFluxHandler"; }
    }
    static class ReactorNetty {
        @Override public String toString() { return "ReactorNetty"; }
    }
    static class RouterFunction {
        @Override public String toString() { return "RouterFunction"; }
    }

    static class BeanContainer {
        private final List<Object> beans = new ArrayList<>();
        private final Map<String, Object> namedBeans = new LinkedHashMap<>();

        public void register(String name, Object bean) {
            beans.add(bean);
            namedBeans.put(name, bean);
            System.out.println("  [Register] " + name + " → " + bean);
        }

        public void printAll() {
            if (namedBeans.isEmpty()) {
                System.out.println("  (contenedor vacío)");
                return;
            }
            namedBeans.forEach((name, bean) ->
                System.out.println("  " + name + " = " + bean));
        }
    }

    static class WebApplicationAutoConfig {

        /**
         * Detecta el tipo de aplicación y registra los beans correspondientes.
         * Simula @ConditionalOnWebApplication y sus variantes.
         */
        public void configure(Map<String, String> config, BeanContainer container) {
            String type = config.getOrDefault("app.type", "reactive");

            AppType appType = switch (type.toLowerCase()) {
                case "web"      -> AppType.WEB;
                case "servlet"  -> AppType.SERVLET;
                default         -> AppType.REACTIVE;
            };

            System.out.println("[AutoConfig] app.type='" + type + "' → modo " + appType);

            switch (appType) {
                case WEB -> {
                    System.out.println("[AutoConfig] Registrando beans para aplicación WEB (MVC):");
                    container.register("dispatcherServlet", new DispatcherServlet());
                    container.register("handlerMapping", new HandlerMapping());
                    container.register("viewResolver", new ViewResolver());
                }
                case SERVLET -> {
                    System.out.println("[AutoConfig] Registrando beans para aplicación SERVLET:");
                    container.register("servletContext", new ServletContext());
                    container.register("filterChain", new FilterChain());
                }
                case REACTIVE -> {
                    System.out.println("[AutoConfig] Registrando beans para aplicación REACTIVE:");
                    container.register("webFluxHandler", new WebFluxHandler());
                    container.register("reactorNetty", new ReactorNetty());
                    container.register("routerFunction", new RouterFunction());
                }
            }
        }
    }

    static void demo(String label, Map<String, String> config) {
        System.out.println("=== " + label + " ===");
        BeanContainer container = new BeanContainer();
        new WebApplicationAutoConfig().configure(config, container);
        System.out.println("Beans registrados:");
        container.printAll();
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("=== @ConditionalOnWebApplication ===");
        System.out.println();

        // Caso 1: app.type = web
        demo("app.type=web", Map.of("app.type", "web"));

        // Caso 2: app.type = servlet
        demo("app.type=servlet", Map.of("app.type", "servlet"));

        // Caso 3: app.type = reactive (o cualquier otro valor)
        demo("app.type=reactive", Map.of("app.type", "reactive"));

        // Caso 4: sin app.type → reactive por defecto
        demo("app.type ausente → reactive por defecto", Map.of());
    }
}
