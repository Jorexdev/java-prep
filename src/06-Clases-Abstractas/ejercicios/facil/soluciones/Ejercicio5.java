public class Ejercicio5 {
    abstract static class Vehiculo {
        private final String marca;
        private final String modelo;
        Vehiculo(String marca, String modelo) { this.marca = marca; this.modelo = modelo; }
        void info() { System.out.println(marca + " " + modelo); }
    }
    static class Coche extends Vehiculo {
        private final int numPuertas;
        Coche(String marca, String modelo, int numPuertas) {
            super(marca, modelo);
            this.numPuertas = numPuertas;
        }
        @Override void info() { super.info(); System.out.println("  Puertas: " + numPuertas); }
    }
    static class Moto extends Vehiculo {
        private final int cc;
        Moto(String marca, String modelo, int cc) { super(marca, modelo); this.cc = cc; }
        @Override void info() { super.info(); System.out.println("  Cilindrada: " + cc + "cc"); }
    }
    public static void main(String[] args) {
        new Coche("Toyota", "Corolla", 5).info();
        new Moto("Honda", "CB500", 471).info();
    }
}
