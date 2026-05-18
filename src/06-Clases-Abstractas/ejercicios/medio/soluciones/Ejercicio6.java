public class Ejercicio6 {
    abstract static class Contador {
        private int cuenta = 0;
        void incrementar() { cuenta++; }
        int getCuenta()    { return cuenta; }
        abstract void mostrar();
    }
    static class ContadorSimple extends Contador {
        @Override void mostrar() { System.out.println("Cuenta: " + getCuenta()); }
    }
    static class ContadorConPrefijo extends Contador {
        private final String prefijo;
        ContadorConPrefijo(String prefijo) { this.prefijo = prefijo; }
        @Override void mostrar() { System.out.println(prefijo + ": " + getCuenta()); }
    }
    public static void main(String[] args) {
        ContadorSimple     cs = new ContadorSimple();
        ContadorConPrefijo cp = new ContadorConPrefijo("Visitas al portal");
        for (int i = 0; i < 5; i++) { cs.incrementar(); cp.incrementar(); }
        cs.mostrar();
        cp.mostrar();
    }
}
