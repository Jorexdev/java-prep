public class Ejercicio2 {

    enum TipoCliente { REGULAR, PREMIUM, VIP, EMPLEADO, MAYORISTA }

    interface EstrategiaDescuento {
        double calcular(double precio);
        String descripcion();
    }

    static class SinDescuento implements EstrategiaDescuento {
        @Override public double calcular(double p) { return p; }
        @Override public String descripcion()      { return "Sin descuento (0%)"; }
    }

    static class DescuentoPorcentaje implements EstrategiaDescuento {
        private final double pct;
        private final String desc;
        DescuentoPorcentaje(double pct, String desc) { this.pct = pct; this.desc = desc; }
        @Override public double calcular(double p)  { return p * (1 - pct); }
        @Override public String descripcion()       { return desc; }
    }

    static class FabricaDescuento {
        static EstrategiaDescuento para(TipoCliente tipo) {
            return switch (tipo) {
                case REGULAR   -> new SinDescuento();
                case PREMIUM   -> new DescuentoPorcentaje(0.10, "Premium 10%");
                case VIP       -> new DescuentoPorcentaje(0.20, "VIP 20%");
                case EMPLEADO  -> new DescuentoPorcentaje(0.30, "Empleado 30%");
                case MAYORISTA -> new DescuentoPorcentaje(0.15, "Mayorista 15%");
            };
        }
    }

    static class CalculadorDescuento {
        double calcular(double precio, TipoCliente tipo) {
            return FabricaDescuento.para(tipo).calcular(precio);
        }
    }

    public static void main(String[] args) {
        CalculadorDescuento calc = new CalculadorDescuento();
        double precio = 100.0;

        for (TipoCliente tipo : TipoCliente.values()) {
            EstrategiaDescuento estrategia = FabricaDescuento.para(tipo);
            System.out.printf("%-10s → %s → %.2f€%n",
                tipo, estrategia.descripcion(), calc.calcular(precio, tipo));
        }
    }
}
