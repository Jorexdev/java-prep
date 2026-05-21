import java.util.*;

public class Ejercicio4 {

    static void pass(String msg) { System.out.println("PASS: " + msg); }
    static void assertEquals(Object exp, Object act, String msg) {
        if (Objects.equals(exp, act)) pass(msg);
        else System.out.println("FAIL: " + msg + " — esperado=" + exp + " actual=" + act);
    }

    // === CarritoCompras desarrollado con TDD: Red → Green → Refactor ===

    static class CarritoCompras {
        private final List<String> items   = new ArrayList<>();
        private double total = 0;

        void agregar(String producto, double precio) {
            if (producto == null || producto.isBlank()) throw new IllegalArgumentException("Producto inválido");
            if (precio < 0) throw new IllegalArgumentException("Precio negativo");
            items.add(producto); total += precio;
        }

        int    totalItems()                 { return items.size(); }
        double calcularTotal()              { return total; }
        double aplicarDescuento(double pct) {
            if (pct < 0 || pct > 100) throw new IllegalArgumentException("Descuento inválido: " + pct);
            return total * (1 - pct / 100.0);
        }
        void vaciar() { items.clear(); total = 0; }
    }

    public static void main(String[] args) {
        System.out.println("=== Ciclo 1 — totalItems (Red→Green) ===");
        CarritoCompras c = new CarritoCompras();
        assertEquals(0, c.totalItems(), "carrito vacío tiene 0 items");
        c.agregar("Libro", 20.0);
        assertEquals(1, c.totalItems(), "agregar producto incrementa items");
        c.agregar("Pen", 5.0);
        assertEquals(2, c.totalItems(), "agregar segundo producto");

        System.out.println("\n=== Ciclo 2 — calcularTotal (Red→Green) ===");
        CarritoCompras c2 = new CarritoCompras();
        assertEquals(0.0, c2.calcularTotal(), "total vacío es 0");
        c2.agregar("Camisa", 30.0);
        c2.agregar("Pantalón", 50.0);
        assertEquals(80.0, c2.calcularTotal(), "total suma correctamente");

        System.out.println("\n=== Ciclo 3 — aplicarDescuento (Red→Green→Refactor) ===");
        CarritoCompras c3 = new CarritoCompras();
        c3.agregar("Laptop", 1000.0);
        assertEquals(900.0, c3.aplicarDescuento(10), "10% descuento sobre 1000");
        assertEquals(750.0, c3.aplicarDescuento(25), "25% descuento sobre 1000");
        try {
            c3.aplicarDescuento(-5);
            System.out.println("FAIL: debía lanzar excepción para descuento negativo");
        } catch (IllegalArgumentException e) {
            pass("descuento negativo lanza excepción");
        }
        try {
            c3.aplicarDescuento(101);
            System.out.println("FAIL: debía lanzar excepción para descuento > 100");
        } catch (IllegalArgumentException e) {
            pass("descuento > 100 lanza excepción");
        }

        System.out.println("\n=== Refactor — vaciar carrito ===");
        c3.vaciar();
        assertEquals(0,   c3.totalItems(),   "items tras vaciar");
        assertEquals(0.0, c3.calcularTotal(), "total tras vaciar");
    }
}
