/**
 * Organización de paquetes en Java: layer-by-layer vs package-by-feature.
 * Convenciones de nombres, acceso entre paquetes y cohesión.
 *
 * Las "clases" de cada estructura se simulan como clases anidadas con el nombre
 * de paquete completo en su Javadoc para visualizar la jerarquía.
 */
public class ExpPackageOrganization {

    // ═══════════════════════════════════════════════════════════════
    // ESTRUCTURA 1: LAYER-BY-LAYER (por capas técnicas)
    // ═══════════════════════════════════════════════════════════════
    //
    // com.empresa.proyecto
    // ├── controller
    // │   ├── OrderController.java
    // │   └── UserController.java
    // ├── service
    // │   ├── OrderService.java
    // │   └── UserService.java
    // ├── repository
    // │   ├── OrderRepository.java
    // │   └── UserRepository.java
    // └── model
    //     ├── Order.java
    //     └── User.java
    //
    // PROBLEMA: para añadir una nueva feature (ej. "Invoice") hay que tocar 4 paquetes.
    // Todos los tipos son public para cruzar entre capas → exposición innecesaria.

    static class LayerByLayerExample {
        // controller/OrderController.java
        static class OrderController {
            private final OrderService service = new OrderService();
            String getOrder(String id) { return service.findById(id); }
        }

        // service/OrderService.java
        static class OrderService {
            private final OrderRepository repo = new OrderRepository();
            String findById(String id) { return repo.load(id); }
        }

        // repository/OrderRepository.java
        static class OrderRepository {
            String load(String id) { return "ORDER[" + id + "]"; }
        }

        // model/Order.java
        static class Order {
            String id;
            double total;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ESTRUCTURA 2: PACKAGE-BY-FEATURE (por funcionalidad de negocio)
    // ═══════════════════════════════════════════════════════════════
    //
    // com.empresa.proyecto
    // ├── order                          ← todo lo de Pedidos en un lugar
    // │   ├── OrderController.java       (public — punto de entrada HTTP)
    // │   ├── OrderService.java          (package-private — detalle de impl.)
    // │   ├── OrderRepository.java       (package-private — detalle de impl.)
    // │   └── Order.java                 (public — modelo compartible)
    // ├── user
    // │   ├── UserController.java
    // │   ├── UserService.java           (package-private)
    // │   ├── UserRepository.java        (package-private)
    // │   └── User.java
    // └── shared                         ← solo lo verdaderamente transversal
    //     ├── ApiResponse.java
    //     └── PageRequest.java
    //
    // VENTAJAS:
    // - Alta cohesión: todo lo de un feature está junto
    // - Bajo acoplamiento: paquetes se comunican solo por la API pública
    // - Encapsulamiento real: internos son package-private, no necesitan ser public
    // - Nuevas features = un nuevo paquete, sin tocar los existentes
    // - Más fácil de extraer a microservicio: el corte ya está hecho

    static class PackageByFeatureExample {
        // order/Order.java — public (modelo compartido entre features)
        static class Order {
            final String id;
            final double total;
            Order(String id, double total) { this.id = id; this.total = total; }
            public String toString() { return "Order{id=" + id + ", total=" + total + "}"; }
        }

        // order/OrderRepository.java — sería package-private en un proyecto real
        static class OrderRepository {
            Order findById(String id) { return new Order(id, 99.99); }
        }

        // order/OrderService.java — sería package-private en un proyecto real
        static class OrderService {
            private final OrderRepository repo = new OrderRepository();
            Order getOrder(String id) { return repo.findById(id); }
        }

        // order/OrderController.java — public, punto de entrada del feature
        static class OrderController {
            private final OrderService service = new OrderService();
            public String handleGetOrder(String id) {
                Order order = service.getOrder(id);
                return "HTTP 200: " + order;
            }
        }

        // shared/ApiResponse.java — transversal, usado por múltiples features
        static class ApiResponse<T> {
            final int status;
            final T body;
            ApiResponse(int status, T body) { this.status = status; this.body = body; }
            public String toString() { return "ApiResponse{status=" + status + ", body=" + body + "}"; }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CONVENCIÓN DE NOMBRES DE PAQUETES
    // ═══════════════════════════════════════════════════════════════

    static void showNamingConventions() {
        System.out.println("Convención: com.<empresa>.<proyecto>.<feature>[.<subfeature>]");
        System.out.println();

        String[] ejemplos = {
            "com.acme.ecommerce.order",
            "com.acme.ecommerce.order.payment",
            "com.acme.ecommerce.user.authentication",
            "com.acme.ecommerce.shared.pagination",
            "com.acme.ecommerce.notification.email",
        };

        for (String pkg : ejemplos) {
            System.out.println("  " + pkg);
        }

        System.out.println();
        System.out.println("Reglas:");
        System.out.println("  - Todo en minúsculas, sin guiones ni underscores");
        System.out.println("  - Singular para paquetes de feature: 'order', no 'orders'");
        System.out.println("  - 'shared' o 'common' solo para código verdaderamente transversal");
        System.out.println("  - Evitar paquetes genéricos: 'util', 'helpers', 'misc'");
    }

    // ═══════════════════════════════════════════════════════════════
    // ACCESO ENTRE PAQUETES: public vs package-private
    // ═══════════════════════════════════════════════════════════════

    static void showAccessControl() {
        System.out.println("Visibilidad recomendada en package-by-feature:");
        System.out.println();
        System.out.printf("  %-35s %s%n", "Clase/método", "Visibilidad");
        System.out.printf("  %-35s %s%n", "-".repeat(34), "-".repeat(20));
        System.out.printf("  %-35s %s%n", "OrderController (entrada HTTP)",    "public");
        System.out.printf("  %-35s %s%n", "Order (modelo de dominio)",          "public");
        System.out.printf("  %-35s %s%n", "OrderService (lógica de negocio)",   "package-private (default)");
        System.out.printf("  %-35s %s%n", "OrderRepository (acceso a datos)",   "package-private (default)");
        System.out.printf("  %-35s %s%n", "OrderMapper (detalle interno)",      "package-private (default)");
        System.out.println();
        System.out.println("  Regla: si no necesitas que otro paquete lo vea, no lo hagas public.");
        System.out.println("  El compilador te avisará si rompes el encapsulamiento.");
    }

    // ═══════════════════════════════════════════════════════════════
    // MAIN
    // ═══════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        System.out.println("=== Organización de Paquetes en Java ===\n");

        System.out.println("--- Layer-by-layer ---");
        LayerByLayerExample.OrderController layerCtrl = new LayerByLayerExample.OrderController();
        System.out.println("Resultado: " + layerCtrl.getOrder("42"));
        System.out.println("Problema: añadir 'Invoice' requiere crear clases en 4 paquetes distintos\n");

        System.out.println("--- Package-by-feature ---");
        PackageByFeatureExample.OrderController featureCtrl = new PackageByFeatureExample.OrderController();
        System.out.println("Resultado: " + featureCtrl.handleGetOrder("42"));
        System.out.println("Ventaja: añadir 'Invoice' = crear paquete 'invoice' sin tocar 'order'\n");

        PackageByFeatureExample.ApiResponse<String> response =
            new PackageByFeatureExample.ApiResponse<>(200, "OK");
        System.out.println("Shared ApiResponse: " + response + "\n");

        System.out.println("--- Convención de nombres ---");
        showNamingConventions();

        System.out.println("--- Control de acceso ---");
        showAccessControl();

        System.out.println("=== Conclusión ===");
        System.out.println("Package-by-feature > Layer-by-layer para proyectos que crecen.");
        System.out.println("Layer-by-layer puede ser suficiente para proyectos muy pequeños o CRUDs simples.");
    }
}
