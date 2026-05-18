public class Ejercicio1 {

    interface Cafe {
        String getDescripcion();
        double getCosto();
    }

    static class CafeSimple implements Cafe {
        @Override public String getDescripcion() { return "Café"; }
        @Override public double getCosto()       { return 1.00; }
    }

    static abstract class DecoradorCafe implements Cafe {
        protected final Cafe cafe;
        DecoradorCafe(Cafe cafe) { this.cafe = cafe; }
    }

    static class Leche extends DecoradorCafe {
        Leche(Cafe cafe) { super(cafe); }
        @Override public String getDescripcion() { return cafe.getDescripcion() + ", Leche"; }
        @Override public double getCosto()       { return cafe.getCosto() + 0.25; }
    }

    static class Azucar extends DecoradorCafe {
        Azucar(Cafe cafe) { super(cafe); }
        @Override public String getDescripcion() { return cafe.getDescripcion() + ", Azúcar"; }
        @Override public double getCosto()       { return cafe.getCosto() + 0.10; }
    }

    static class Canela extends DecoradorCafe {
        Canela(Cafe cafe) { super(cafe); }
        @Override public String getDescripcion() { return cafe.getDescripcion() + ", Canela"; }
        @Override public double getCosto()       { return cafe.getCosto() + 0.15; }
    }

    static void mostrar(Cafe c) {
        System.out.printf("%-40s → %.2f€%n", c.getDescripcion(), c.getCosto());
    }

    public static void main(String[] args) {
        mostrar(new CafeSimple());
        mostrar(new Leche(new CafeSimple()));
        mostrar(new Canela(new Azucar(new Leche(new CafeSimple()))));
    }
}
