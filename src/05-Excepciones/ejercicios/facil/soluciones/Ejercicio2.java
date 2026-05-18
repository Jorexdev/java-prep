public class Ejercicio2 {

    static class Conexion {
        void abrir() {
            System.out.println("Conexion abierta");
        }

        void operar() {
            System.out.println("Operando...");
            throw new RuntimeException("Error en operacion");
        }

        void cerrar() {
            System.out.println("Conexion cerrada");
        }
    }

    public static void main(String[] args) {
        Conexion con = new Conexion();
        try {
            con.abrir();
            con.operar(); // lanza excepcion
        } finally {
            // cerrar() se llama siempre, haya excepcion o no
            con.cerrar();
        }
    }
}
