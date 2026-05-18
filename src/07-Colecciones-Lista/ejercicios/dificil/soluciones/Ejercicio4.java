import java.util.List;

public class Ejercicio4 {

    static class Empleado {
        private String nombre;
        private int salario;

        public Empleado(String nombre, int salario) {
            this.nombre = nombre;
            this.salario = salario;
        }

        public String getNombre() { return nombre; }
        public int getSalario() { return salario; }
    }

    public static int binarySearch(List<Empleado> lista, int salarioBuscado) {
        int izq = 0;
        int der = lista.size() - 1;

        while (izq <= der) {
            int mid = (izq + der) / 2;
            int salarioMid = lista.get(mid).getSalario();

            if (salarioMid == salarioBuscado) {
                return mid;
            } else if (salarioMid < salarioBuscado) {
                izq = mid + 1;
            } else {
                der = mid - 1;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        // Lista ya ordenada por salario
        List<Empleado> empleados = List.of(
            new Empleado("Ana",   30000),
            new Empleado("Luis",  45000),
            new Empleado("Marta", 55000),
            new Empleado("Jorge", 70000),
            new Empleado("Eva",   85000)
        );

        int idx1 = binarySearch(empleados, 55000);
        System.out.println("Buscar salario 55000 -> índice: " + idx1 +
            " (" + (idx1 >= 0 ? empleados.get(idx1).getNombre() : "no encontrado") + ")");

        int idx2 = binarySearch(empleados, 30000);
        System.out.println("Buscar salario 30000 -> índice: " + idx2 +
            " (" + (idx2 >= 0 ? empleados.get(idx2).getNombre() : "no encontrado") + ")");

        int idx3 = binarySearch(empleados, 99999);
        System.out.println("Buscar salario 99999 -> índice: " + idx3);
    }
}
