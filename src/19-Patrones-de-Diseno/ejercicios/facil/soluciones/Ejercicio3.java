import java.util.ArrayList;
import java.util.List;

public class Ejercicio3 {

    static class Pizza {
        private final String masa;
        private final String salsa;
        private final String queso;
        private final List<String> ingredientes;

        private Pizza(Builder b) {
            this.masa         = b.masa;
            this.salsa        = b.salsa;
            this.queso        = b.queso;
            this.ingredientes = List.copyOf(b.ingredientes);
        }

        @Override public String toString() {
            return "Pizza{masa=" + masa + ", salsa=" + salsa +
                   ", queso=" + queso + ", extras=" + ingredientes + "}";
        }

        static class Builder {
            private final String masa;
            private String salsa = "tomate";
            private String queso = "mozzarella";
            private final List<String> ingredientes = new ArrayList<>();

            Builder(String masa) {
                if (masa == null || masa.isBlank()) throw new IllegalArgumentException("La masa es obligatoria");
                this.masa = masa;
            }

            Builder salsa(String s)         { this.salsa = s; return this; }
            Builder queso(String q)         { this.queso = q; return this; }
            Builder ingrediente(String i)   { this.ingredientes.add(i); return this; }
            Pizza build()                   { return new Pizza(this); }
        }
    }

    public static void main(String[] args) {
        Pizza margarita = new Pizza.Builder("fina")
            .build();

        Pizza especial = new Pizza.Builder("gruesa")
            .salsa("barbacoa")
            .queso("cheddar")
            .ingrediente("pepperoni")
            .ingrediente("jalapeños")
            .build();

        System.out.println(margarita);
        System.out.println(especial);
    }
}
