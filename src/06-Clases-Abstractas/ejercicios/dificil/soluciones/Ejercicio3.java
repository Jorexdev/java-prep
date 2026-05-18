import java.util.List;
public class Ejercicio3 {
    abstract static class Empleado {
        protected final String nombre;
        protected final double salario;
        Empleado(String nombre, double salario) { this.nombre = nombre; this.salario = salario; }
        void info() { System.out.printf("%-15s %.0f€%n", nombre, salario); }
    }
    interface Evaluable { double evaluarRendimiento(); }
    static class EmpleadoTecnico extends Empleado implements Evaluable {
        private final int lineasCodigo;
        EmpleadoTecnico(String nombre, double salario, int lineasCodigo) {
            super(nombre, salario); this.lineasCodigo = lineasCodigo;
        }
        @Override public double evaluarRendimiento() { return lineasCodigo / 100.0; }
    }
    public static void main(String[] args) {
        List<EmpleadoTecnico> equipo = List.of(
            new EmpleadoTecnico("Ana",   55000, 1200),
            new EmpleadoTecnico("Luis",  62000, 850),
            new EmpleadoTecnico("Marta", 58000, 1500)
        );
        System.out.println("--- Info (via Empleado) ---");
        List<Empleado> empleados = List.copyOf(equipo); // upcast
        empleados.forEach(Empleado::info);
        System.out.println("--- Evaluación (via Evaluable) ---");
        List<Evaluable> evaluables = List.copyOf(equipo); // upcast
        evaluables.forEach(e -> System.out.println(((EmpleadoTecnico) e).nombre + " → puntuación: " + e.evaluarRendimiento()));
    }
}
