import java.util.Comparator;
import java.util.List;

public class Ejercicio8 {

    public static void main(String[] args) {

        // Ejercicio: encontrar al empleado con el salario más alto
        List<Employee> employees = List.of(
                new Employee("Luis",  20L),
                new Employee("Jorge", 122L),
                new Employee("Juan",  120L),
                new Employee("Ana",   2000L)
        );

        employees
                .stream()
                .max(Comparator.comparing(Employee::getSalary)) // devuelve Optional<Employee>
                .ifPresent(System.out::println);                // imprime solo si existe
    }

    static class Employee {

        private final String name;
        private final Long salary;

        Employee(String name, Long salary) {
            this.name = name;
            this.salary = salary;
        }

        public Long getSalary() {
            return salary;
        }

        @Override
        public String toString() {
            return "Employee{name='" + name + "', salary=" + salary + '}';
        }
    }
}
