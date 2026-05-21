import java.nio.charset.StandardCharsets;

public class Ejercicio6 {

    interface Serializer<T> {
        byte[] serialize(T obj);
    }

    interface Deserializer<T> {
        T deserialize(byte[] data);
    }

    static class Pedido {
        final int id;
        final String producto;
        final double precio;

        Pedido(int id, String producto, double precio) {
            this.id = id;
            this.producto = producto;
            this.precio = precio;
        }

        @Override
        public String toString() {
            return "Pedido{id=" + id + ", producto='" + producto + "', precio=" + precio + "}";
        }
    }

    static class PedidoSerializer implements Serializer<Pedido> {
        @Override
        public byte[] serialize(Pedido obj) {
            String raw = obj.id + "|" + obj.producto + "|" + obj.precio;
            return raw.getBytes(StandardCharsets.UTF_8);
        }
    }

    static class PedidoDeserializer implements Deserializer<Pedido> {
        @Override
        public Pedido deserialize(byte[] data) {
            String raw = new String(data, StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|");
            return new Pedido(Integer.parseInt(parts[0]), parts[1], Double.parseDouble(parts[2]));
        }
    }

    public static void main(String[] args) {
        Pedido original = new Pedido(42, "teclado-mecanico", 89.99);
        System.out.println("[ORIGINAL] " + original);

        PedidoSerializer serializer = new PedidoSerializer();
        byte[] bytes = serializer.serialize(original);

        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x ", b));
        }
        System.out.println("[BYTES HEX] " + hex.toString().trim());

        PedidoDeserializer deserializer = new PedidoDeserializer();
        Pedido restored = deserializer.deserialize(bytes);
        System.out.println("[RESTAURADO] " + restored);

        boolean ok = original.id == restored.id
                && original.producto.equals(restored.producto)
                && Double.compare(original.precio, restored.precio) == 0;
        System.out.println("[VERIFICACIÓN] " + (ok ? "OK — objetos equivalentes" : "FALLO — objetos distintos"));
    }
}
