public class Ejercicio3 {
    abstract static class OrdenProcesador {
        double precio;
        OrdenProcesador(double precio) { this.precio = precio; }
        abstract void validar();
        void aplicarDescuento() {} // hook — vacío por defecto, sobreescribible
        abstract void confirmar();
        final void procesar() { validar(); aplicarDescuento(); confirmar(); }
    }
    static class OrdenNormal extends OrdenProcesador {
        OrdenNormal(double precio) { super(precio); }
        @Override void validar()   { System.out.println("Validando orden normal: " + precio + "€"); }
        @Override void confirmar() { System.out.println("Orden normal confirmada: " + precio + "€"); }
    }
    static class OrdenVIP extends OrdenProcesador {
        OrdenVIP(double precio) { super(precio); }
        @Override void validar()   { System.out.println("Validando orden VIP: " + precio + "€"); }
        @Override void aplicarDescuento() { precio *= 0.8; System.out.println("Descuento VIP 20% → " + precio + "€"); }
        @Override void confirmar() { System.out.println("Orden VIP confirmada: " + precio + "€"); }
    }
    public static void main(String[] args) {
        System.out.println("--- Normal ---");
        new OrdenNormal(100.0).procesar();
        System.out.println("--- VIP ---");
        new OrdenVIP(100.0).procesar();
    }
}
