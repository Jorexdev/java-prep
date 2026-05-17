import java.util.Comparator;
import java.util.List;

public class Ejercicio6 {

    public static void main(String[] args) {

        // Ejercicio: mostrar los 5 empleados con mayor salario, en orden descendente
        List<Employee> employees = List.of(
                new Employee("Luis",  20L),
                new Employee("Jorge", 122L),
                new Employee("Juan",  120L),
                new Employee("Ana",   2000L)
        );

        employees
                .stream()
                .sorted(Comparator.comparing(Employee::getSalary).reversed()) // mayor salario primero
                .limit(5)                                                      // primeros 5 (hay 4 en el ejemplo)
                .forEach(System.out::println);
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
