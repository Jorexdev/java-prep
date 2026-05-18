public class Ejercicio2 {
    abstract static class Vehiculo { abstract void mover(); }
    abstract static class VehiculoTerrestre extends Vehiculo {
        String tipoTerreno() { return "asfalto"; }
    }
    static class Coche extends VehiculoTerrestre {
        private final String modelo;
        Coche(String modelo) { this.modelo = modelo; }
        @Override void mover() { System.out.println(modelo + " circula por " + tipoTerreno()); }
    }
    public static void main(String[] args) {
        Vehiculo v = new Coche("Toyota Corolla");
        v.mover();
        System.out.println("Tipo: " + ((VehiculoTerrestre) v).tipoTerreno());
    }
}
