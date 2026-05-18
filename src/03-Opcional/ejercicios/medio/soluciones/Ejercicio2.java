import java.util.Optional;

public class Ejercicio2 {

    static class Direccion {
        private final String calle;

        Direccion(String calle) {
            this.calle = calle;
        }

        // Retorna Optional — la calle puede no existir
        Optional<String> getCalle() {
            return Optional.ofNullable(calle);
        }
    }

    static class Pedido {
        private final Direccion direccion;

        Pedido(Direccion direccion) {
            this.direccion = direccion;
        }

        // Retorna Optional — el pedido puede no tener dirección
        Optional<Direccion> getDireccion() {
            return Optional.ofNullable(direccion);
        }
    }

    public static void main(String[] args) {
        // Caso 1: pedido con dirección y calle
        Pedido pedidoCompleto = new Pedido(new Direccion("Calle Mayor 10"));

        // Sin flatMap: pedidoCompleto.getDireccion().map(Direccion::getCalle)
        //   → Optional<Optional<String>>  (anidado, poco útil)

        // Con flatMap: aplanamos el anidamiento → Optional<String>
        Optional<String> calle = pedidoCompleto.getDireccion()
                .flatMap(Direccion::getCalle);

        System.out.println("Calle (pedido completo): " + calle); // Optional[Calle Mayor 10]

        // Caso 2: pedido con dirección pero sin calle
        Pedido sinCalle = new Pedido(new Direccion(null));
        Optional<String> calleSinCalle = sinCalle.getDireccion().flatMap(Direccion::getCalle);
        System.out.println("Calle (sin calle):       " + calleSinCalle); // Optional.empty

        // Caso 3: pedido sin dirección
        Pedido sinDireccion = new Pedido(null);
        Optional<String> calleSinDir = sinDireccion.getDireccion().flatMap(Direccion::getCalle);
        System.out.println("Calle (sin dirección):   " + calleSinDir); // Optional.empty
    }
}
