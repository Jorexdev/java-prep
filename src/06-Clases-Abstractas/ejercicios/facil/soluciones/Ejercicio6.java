public class Ejercicio6 {
    abstract static class Informe {
        abstract void cabecera();
        abstract void cuerpo();
        abstract void pie();
        final void generar() { cabecera(); cuerpo(); pie(); }
    }
    static class InformeVentas extends Informe {
        @Override void cabecera() { System.out.println("=== INFORME DE VENTAS ==="); }
        @Override void cuerpo()   { System.out.println("Total ventas: 150.000€ | Unidades: 320"); }
        @Override void pie()      { System.out.println("Generado: " + java.time.LocalDate.now()); }
    }
    static class InformeInventario extends Informe {
        @Override void cabecera() { System.out.println("=== INFORME DE INVENTARIO ==="); }
        @Override void cuerpo()   { System.out.println("Productos: 842 | Sin stock: 12 | Alertas: 5"); }
        @Override void pie()      { System.out.println("Próxima revisión: en 30 días"); }
    }
    public static void main(String[] args) {
        new InformeVentas().generar();
        System.out.println();
        new InformeInventario().generar();
    }
}
