import java.util.*;

// Refactorizacion de un sistema heredado con violaciones de todos los principios SOLID
// El codigo "original" (comentado) viola SRP, OCP, LSP, ISP y DIP
// La version refactorizada aplica los 5 principios

public class Ejercicio6 {

    // ========== CODIGO ORIGINAL (viola SOLID) ==========
    // class GestorLegado {
    //   void procesarPedido(String tipo, double precio, String cliente) {
    //     // SRP: mezcla logica de negocio, persistencia, notificacion y descuentos
    //     if (tipo.equals("premium")) precio *= 0.8;
    //     else if (tipo.equals("vip")) precio *= 0.7;
    //     // OCP: hay que modificar esta clase para añadir nuevos tipos
    //     System.out.println("Guardando en BD: " + cliente + " -> " + precio);
    //     System.out.println("Enviando email a " + cliente);
    //     // ISP: todo en un metodo gigante
    //   }
    //   double calcularEnvio(String tipo) {
    //     // DIP: depende directamente de implementacion concreta
    //     if (tipo.equals("express")) return 15.0;
    //     return 5.0;
    //   }
    // }

    // ========== VERSION REFACTORIZADA (aplica SOLID) ==========

    // --- SRP: cada clase tiene una sola responsabilidad ---

    // Entidad de dominio (solo datos)
    static class Pedido {
        final String cliente;
        final double precioOriginal;
        final String tipo;

        Pedido(String cliente, double precioOriginal, String tipo) {
            this.cliente = cliente;
            this.precioOriginal = precioOriginal;
            this.tipo = tipo;
        }
    }

    // --- OCP + DIP: estrategia de descuento via interfaz ---
    interface EstrategiaDescuento {
        double aplicar(double precio);
        String nombre();
    }

    static class SinDescuento implements EstrategiaDescuento {
        public double aplicar(double precio) { return precio; }
        public String nombre() { return "sin descuento"; }
    }

    static class DescuentoPremium implements EstrategiaDescuento {
        public double aplicar(double precio) { return precio * 0.80; }
        public String nombre() { return "20% premium"; }
    }

    static class DescuentoVip implements EstrategiaDescuento {
        public double aplicar(double precio) { return precio * 0.70; }
        public String nombre() { return "30% VIP"; }
    }

    // OCP: añadir Mayorista no toca ninguna clase existente
    static class DescuentoMayorista implements EstrategiaDescuento {
        public double aplicar(double precio) { return precio * 0.85; }
        public String nombre() { return "15% mayorista"; }
    }

    // Factory de descuentos (OCP: extiende el map, no modifica clases)
    static class DescuentoFactory {
        private static final Map<String, EstrategiaDescuento> ESTRATEGIAS = new HashMap<>();
        static {
            ESTRATEGIAS.put("regular",    new SinDescuento());
            ESTRATEGIAS.put("premium",    new DescuentoPremium());
            ESTRATEGIAS.put("vip",        new DescuentoVip());
            ESTRATEGIAS.put("mayorista",  new DescuentoMayorista());
        }
        static EstrategiaDescuento resolver(String tipo) {
            return ESTRATEGIAS.getOrDefault(tipo.toLowerCase(), new SinDescuento());
        }
    }

    // --- ISP: interfaces segregadas por responsabilidad ---
    interface RepositorioPedidos {
        void guardar(Pedido pedido, double precioFinal);
    }

    interface ServicioNotificacion {
        void notificar(Pedido pedido, double precioFinal);
    }

    interface CalculadorEnvio {
        double calcular(String modalidad);
    }

    // Implementaciones concretas (DIP: el servicio principal depende de abstracciones)
    static class RepositorioMemoria implements RepositorioPedidos {
        private final List<String> registros = new ArrayList<>();
        public void guardar(Pedido p, double precio) {
            String reg = p.cliente + " | " + p.tipo + " | $" + String.format("%.2f", precio);
            registros.add(reg);
            System.out.println("  [BD] guardado: " + reg);
        }
        List<String> getRegistros() { return registros; }
    }

    static class NotificadorEmail implements ServicioNotificacion {
        public void notificar(Pedido p, double precio) {
            System.out.printf("  [Email] -> %s: pedido confirmado por $%.2f%n",
                    p.cliente, precio);
        }
    }

    static class CalculadorEnvioEstandar implements CalculadorEnvio {
        public double calcular(String modalidad) {
            return switch (modalidad.toLowerCase()) {
                case "express" -> 15.0;
                case "nocturno" -> 8.0;
                default -> 5.0;
            };
        }
    }

    // --- SRP: servicio principal delega a colaboradores; DIP: solo conoce interfaces ---
    static class ServicioPedidos {
        private final RepositorioPedidos repo;
        private final ServicioNotificacion notificacion;
        private final CalculadorEnvio calculadorEnvio;

        ServicioPedidos(RepositorioPedidos repo,
                        ServicioNotificacion notificacion,
                        CalculadorEnvio calculadorEnvio) {
            this.repo = repo;
            this.notificacion = notificacion;
            this.calculadorEnvio = calculadorEnvio;
        }

        void procesar(Pedido pedido, String modalidadEnvio) {
            EstrategiaDescuento descuento = DescuentoFactory.resolver(pedido.tipo);
            double precioConDesc = descuento.aplicar(pedido.precioOriginal);
            double costoEnvio = calculadorEnvio.calcular(modalidadEnvio);
            double total = precioConDesc + costoEnvio;

            System.out.printf("  Pedido: %-10s | precio: $%.2f | %s -> $%.2f | envio %-8s: $%.2f | total: $%.2f%n",
                    pedido.cliente, pedido.precioOriginal, descuento.nombre(),
                    precioConDesc, modalidadEnvio, costoEnvio, total);

            repo.guardar(pedido, total);
            notificacion.notificar(pedido, total);
        }
    }

    // ========== DEMO ==========

    public static void main(String[] args) {
        System.out.println("=== SOLID: Refactorizacion de sistema heredado ===");
        System.out.println();

        RepositorioMemoria repo = new RepositorioMemoria();
        ServicioPedidos servicio = new ServicioPedidos(
                repo,
                new NotificadorEmail(),
                new CalculadorEnvioEstandar()
        );

        List<Pedido> pedidos = List.of(
                new Pedido("Ana",    100.0, "regular"),
                new Pedido("Carlos", 200.0, "premium"),
                new Pedido("Diana",  300.0, "vip"),
                new Pedido("Eduardo",150.0, "mayorista") // nuevo tipo sin tocar ServicioPedidos
        );

        System.out.println("[ Procesando pedidos ]");
        servicio.procesar(pedidos.get(0), "estandar");
        servicio.procesar(pedidos.get(1), "express");
        servicio.procesar(pedidos.get(2), "estandar");
        servicio.procesar(pedidos.get(3), "nocturno");

        System.out.println();
        System.out.println("[ Registros en BD ]");
        repo.getRegistros().forEach(r -> System.out.println("  " + r));

        System.out.println();
        System.out.println("=== Analisis SOLID ===");
        System.out.println("SRP: Pedido (datos), ServicioPedidos (orquesta),");
        System.out.println("     RepositorioMemoria (persistencia), NotificadorEmail (notif).");
        System.out.println("OCP: Añadir DescuentoMayorista no toco ServicioPedidos ni estrategias existentes.");
        System.out.println("LSP: Todas las EstrategiaDescuento son sustituibles entre si.");
        System.out.println("ISP: RepositorioPedidos, ServicioNotificacion y CalculadorEnvio son interfaces pequeñas.");
        System.out.println("DIP: ServicioPedidos depende de interfaces, no de HashMap ni System.out directo.");
    }
}
