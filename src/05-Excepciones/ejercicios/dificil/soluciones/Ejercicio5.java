import java.util.ArrayList;
import java.util.List;
public class Ejercicio5 {
    static class Pedido {
        String nombre; int cantidad; double precio; String email;
        Pedido(String nombre, int cantidad, double precio, String email) {
            this.nombre = nombre; this.cantidad = cantidad; this.precio = precio; this.email = email;
        }
    }
    static List<String> validar(Pedido p) {
        List<String> errores = new ArrayList<>();
        if (p.nombre == null || p.nombre.isBlank()) errores.add("Nombre no puede estar vacío");
        if (p.cantidad <= 0)  errores.add("Cantidad debe ser mayor que 0");
        if (p.precio   <= 0)  errores.add("Precio debe ser mayor que 0");
        if (p.email == null || !p.email.contains("@")) errores.add("Email inválido (falta @)");
        return errores;
    }
    public static void main(String[] args) {
        Pedido valido   = new Pedido("Laptop", 2, 999.0, "user@email.com");
        Pedido invalido = new Pedido("", -1, 999.0, "email-sin-arroba");
        System.out.println("Pedido válido:   " + validar(valido));
        System.out.println("Pedido inválido: " + validar(invalido));
    }
}
