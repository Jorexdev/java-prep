import java.util.*;

public class Ejercicio3 {

    @FunctionalInterface
    interface Specification<T> {
        boolean isSatisfiedBy(T entity);
        default Specification<T> and(Specification<T> other) { return e -> isSatisfiedBy(e) && other.isSatisfiedBy(e); }
        default Specification<T> or(Specification<T> other)  { return e -> isSatisfiedBy(e) || other.isSatisfiedBy(e); }
        default Specification<T> not()                        { return e -> !isSatisfiedBy(e); }
    }

    record Empleado(int id, String nombre, String depto, double salario, boolean activo) {}

    record PorDepto(String depto) implements Specification<Empleado> {
        @Override public boolean isSatisfiedBy(Empleado e) { return depto.equals(e.depto()); }
    }

    record SalarioMayorQue(double min) implements Specification<Empleado> {
        @Override public boolean isSatisfiedBy(Empleado e) { return e.salario() > min; }
    }

    static class Activo implements Specification<Empleado> {
        @Override public boolean isSatisfiedBy(Empleado e) { return e.activo(); }
    }

    static class EmpleadoRepository {
        private final List<Empleado> datos;
        EmpleadoRepository(List<Empleado> datos) { this.datos = datos; }
        List<Empleado> findAll(Specification<Empleado> spec) {
            return datos.stream().filter(spec::isSatisfiedBy).toList();
        }
    }

    public static void main(String[] args) {
        var repo = new EmpleadoRepository(List.of(
            new Empleado(1, "Ana",   "IT",      60000, true),
            new Empleado(2, "Pedro", "HR",      35000, true),
            new Empleado(3, "Laura", "IT",      80000, false),
            new Empleado(4, "Juan",  "IT",      45000, true),
            new Empleado(5, "Maria", "Finance", 70000, true)
        ));

        Specification<Empleado> enIT        = new PorDepto("IT");
        Specification<Empleado> salarioAlto = new SalarioMayorQue(50000);
        Specification<Empleado> activo      = new Activo();

        System.out.println("IT + activo + salario > 50k:");
        repo.findAll(enIT.and(activo).and(salarioAlto))
            .forEach(e -> System.out.println("  " + e.nombre() + " - " + e.salario()));

        System.out.println("\nNo IT o salario alto:");
        repo.findAll(enIT.not().or(salarioAlto))
            .forEach(e -> System.out.println("  " + e.nombre() + " (" + e.depto() + ")"));

        System.out.println("\nInactivos:");
        repo.findAll(activo.not())
            .forEach(e -> System.out.println("  " + e.nombre()));

        System.out.println("\nHR activos:");
        repo.findAll(new PorDepto("HR").and(activo))
            .forEach(e -> System.out.println("  " + e.nombre()));
    }
}
