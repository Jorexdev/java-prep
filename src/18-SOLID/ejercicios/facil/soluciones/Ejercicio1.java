public class Ejercicio1 {

    static class Factura {
        private final String cliente;
        private final double importe;

        Factura(String cliente, double importe) {
            this.cliente = cliente;
            this.importe = importe;
        }

        double calcularTotal(double iva) {
            return importe * (1 + iva);
        }

        String getCliente() { return cliente; }
        double getImporte() { return importe; }
    }

    static class ImpresoraDeFactura {
        void imprimir(Factura f) {
            System.out.println("Factura para: " + f.getCliente());
            System.out.println("Total (IVA 21%): " + f.calcularTotal(0.21));
        }
    }

    static class RepositorioFactura {
        void guardar(Factura f) {
            System.out.println("Guardando factura de " + f.getCliente() + " en BD");
        }
    }

    public static void main(String[] args) {
        Factura factura = new Factura("Acme Corp", 1000.0);
        new ImpresoraDeFactura().imprimir(factura);
        new RepositorioFactura().guardar(factura);
    }
}
