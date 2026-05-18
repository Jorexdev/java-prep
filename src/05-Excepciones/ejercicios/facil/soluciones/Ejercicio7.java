public class Ejercicio7 {

    static class ProductoNoEncontradoException extends RuntimeException {
        private final int id;

        ProductoNoEncontradoException(int id) {
            super("Producto no encontrado con id: " + id);
            this.id = id;
        }

        int getId() {
            return id;
        }
    }

    static String buscarProducto(int id) {
        if (id <= 0) {
            throw new ProductoNoEncontradoException(id);
        }
        if (id == 1) return "Laptop";
        if (id == 2) return "Monitor";
        throw new ProductoNoEncontradoException(id);
    }

    public static void main(String[] args) {
        // Caso exitoso
        try {
            String producto = buscarProducto(1);
            System.out.println("Producto encontrado: " + producto);
        } catch (ProductoNoEncontradoException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // Caso id invalido
        try {
            String producto = buscarProducto(-1);
            System.out.println("Producto encontrado: " + producto);
        } catch (ProductoNoEncontradoException e) {
            System.out.println("Error (id=" + e.getId() + "): " + e.getMessage());
        }
    }
}
