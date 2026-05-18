import java.util.Optional;

public class Ejercicio1 {

    static class Pais {
        private final String codigoISO;

        Pais(String codigoISO) { this.codigoISO = codigoISO; }

        String getCodigoISO() { return codigoISO; }
    }

    static class Direccion {
        private final Pais pais;

        Direccion(Pais pais) { this.pais = pais; }

        Optional<Pais> getPais() { return Optional.ofNullable(pais); }
    }

    static class Cliente {
        private final Direccion direccion;

        Cliente(Direccion direccion) { this.direccion = direccion; }

        Optional<Direccion> getDireccion() { return Optional.ofNullable(direccion); }
    }

    static class Pedido {
        private final Cliente cliente;

        Pedido(Cliente cliente) { this.cliente = cliente; }

        Optional<Cliente> getCliente() { return Optional.ofNullable(cliente); }
    }

    // Versión original con null-checks manuales (para comparar)
    static String isoNullChecks(Pedido pedido) {
        String iso = null;
        if (pedido != null && pedido.getCliente().isPresent()
                && pedido.getCliente().get().getDireccion().isPresent()
                && pedido.getCliente().get().getDireccion().get().getPais().isPresent()) {
            iso = pedido.getCliente().get().getDireccion().get().getPais().get().getCodigoISO();
        }
        return iso != null ? iso : "ISO desconocido";
    }

    // Versión con Optional y flatMap encadenados
    static String isoOptional(Pedido pedido) {
        return Optional.ofNullable(pedido)
                .flatMap(Pedido::getCliente)
                .flatMap(Cliente::getDireccion)
                .flatMap(Direccion::getPais)
                .map(Pais::getCodigoISO)
                .orElse("ISO desconocido");
    }

    public static void main(String[] args) {
        // Caso completo — todos los niveles presentes
        Pedido completo = new Pedido(new Cliente(new Direccion(new Pais("ES"))));
        System.out.println("Completo (null-checks): " + isoNullChecks(completo));
        System.out.println("Completo (Optional):    " + isoOptional(completo));

        // Caso con null en el medio — dirección existe pero sin país
        Pedido sinPais = new Pedido(new Cliente(new Direccion(null)));
        System.out.println("\nSin país (null-checks): " + isoNullChecks(sinPais));
        System.out.println("Sin país (Optional):    " + isoOptional(sinPais));

        // Caso con cliente nulo
        Pedido sinCliente = new Pedido(null);
        System.out.println("\nSin cliente (Optional): " + isoOptional(sinCliente));

        // Pedido nulo
        System.out.println("Pedido null (Optional): " + isoOptional(null));
    }
}
