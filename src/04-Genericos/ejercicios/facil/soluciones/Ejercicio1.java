public class Ejercicio1 {

    // Clase genérica con un único parámetro de tipo T
    static class Caja<T> {
        private T valor;

        public Caja(T valor) {
            this.valor = valor;
        }

        public T get() {
            return valor;
        }

        public void set(T valor) {
            this.valor = valor;
        }

        @Override
        public String toString() {
            return "Caja[" + valor + "]";
        }
    }

    public static void main(String[] args) {
        Caja<String> cajaTexto = new Caja<>("Hola Genéricos");
        System.out.println(cajaTexto);          // Caja[Hola Genéricos]

        Caja<Integer> cajaNumero = new Caja<>(42);
        System.out.println(cajaNumero);         // Caja[42]

        cajaTexto.set("Actualizado");
        System.out.println(cajaTexto.get());    // Actualizado
    }
}
