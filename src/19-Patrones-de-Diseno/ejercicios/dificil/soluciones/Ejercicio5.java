public class Ejercicio5 {

    interface Estado {
        void insertarMoneda(Maquina m);
        void seleccionarProducto(Maquina m);
        void recogerProducto(Maquina m);
    }

    static class SinMoneda implements Estado {
        @Override public void insertarMoneda(Maquina m)      { System.out.println("Moneda insertada"); m.setEstado(new ConMoneda()); }
        @Override public void seleccionarProducto(Maquina m) { System.out.println("Inserta una moneda primero"); }
        @Override public void recogerProducto(Maquina m)     { System.out.println("No hay producto"); }
    }

    static class ConMoneda implements Estado {
        @Override public void insertarMoneda(Maquina m)      { System.out.println("Ya hay una moneda"); }
        @Override public void seleccionarProducto(Maquina m) { System.out.println("Dispensando..."); m.setEstado(new Dispensando()); }
        @Override public void recogerProducto(Maquina m)     { System.out.println("Selecciona un producto primero"); }
    }

    static class Dispensando implements Estado {
        @Override public void insertarMoneda(Maquina m)      { System.out.println("Espera, dispensando..."); }
        @Override public void seleccionarProducto(Maquina m) { System.out.println("Ya dispensando"); }
        @Override public void recogerProducto(Maquina m) {
            System.out.println("Producto recogido");
            m.decrementarStock();
            m.setEstado(m.getStock() > 0 ? new SinMoneda() : new Agotado());
        }
    }

    static class Agotado implements Estado {
        @Override public void insertarMoneda(Maquina m)      { System.out.println("Maquina agotada — devolviendo moneda"); }
        @Override public void seleccionarProducto(Maquina m) { System.out.println("Sin stock"); }
        @Override public void recogerProducto(Maquina m)     { System.out.println("Sin stock"); }
    }

    static class Maquina {
        private Estado estado;
        private int stock;

        Maquina(int stock) { this.stock = stock; this.estado = stock > 0 ? new SinMoneda() : new Agotado(); }

        void setEstado(Estado e)  { this.estado = e; }
        int getStock()            { return stock; }
        void decrementarStock()   { stock--; }

        void insertarMoneda()      { estado.insertarMoneda(this); }
        void seleccionarProducto() { estado.seleccionarProducto(this); }
        void recogerProducto()     { estado.recogerProducto(this); }

        String estadoActual()      { return estado.getClass().getSimpleName() + "(stock=" + stock + ")"; }
    }

    public static void main(String[] args) {
        Maquina m = new Maquina(1);
        System.out.println(m.estadoActual());

        m.seleccionarProducto(); // sin moneda
        m.insertarMoneda();
        m.insertarMoneda();      // ya hay moneda
        m.seleccionarProducto();
        m.recogerProducto();

        System.out.println(m.estadoActual()); // Agotado

        m.insertarMoneda();      // devuelve moneda
    }
}
