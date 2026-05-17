import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio7 {

    public static void main(String[] args) {

        // Ejercicio: concatenar los nombres de departamentos únicos separados por coma
        List<Employee> employees = List.of(
                new Employee("Ana",   "TI"),
                new Employee("Luis",  "Recursos Humanos"),
                new Employee("Clara", "Ventas"),
                new Employee("Pedro", "TI"),
                new Employee("Marta", "Marketing"),
                new Employee("Jorge", "Ventas"),
                new Employee("Lucía", "Marketing")
        );

        String resultado = employees
                .stream()
                .map(Employee::getDepartment)  // extrae solo el nombre del departamento
                .distinct()                    // elimina duplicados
                .collect(Collectors.joining(",")); // une en un String separado por coma

        System.out.println(resultado);
    }

    static class Employee {

        private final String name;
        private final String department;

        Employee(String name, String department) {
            this.name = name;
            this.department = department;
        }

        public String getDepartment() {
            return department;
        }
    }
}
