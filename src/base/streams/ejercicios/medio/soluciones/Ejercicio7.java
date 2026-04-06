package base.streams.ejemplosercicios.medio.soluciones;

import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio7 {
    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Ana", "TI"),
                new Employee("Luis", "Recursos Humanos"),
                new Employee("Clara", "Ventas"),
                new Employee("Pedro", "TI"),
                new Employee("Marta", "Marketing"),
                new Employee("Jorge", "Ventas"),
                new Employee("Lucía", "Marketing")
        );

        employees
                .stream()
                .map(Employee::getDepartment)
                .distinct()
                .reduce((s, s2) -> s+","+s2)
                .ifPresent(System.out::println);

        String resultado =
                employees
                .stream()
                .map(Employee::getDepartment)
                .distinct()
                .collect(Collectors.joining(","));

        System.out.println(resultado);


    }

    static  class Employee {
        private final String name;
        private final String department;

        public Employee(String name, String department) {
            this.name = name;
            this.department = department;
        }

        public String getName() {
            return name;
        }

        public String getDepartment() {
            return department;
        }
    }

}
