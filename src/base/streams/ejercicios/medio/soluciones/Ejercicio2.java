package base.streams.ejercicios.medio.soluciones;

import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio2 {

    public static void main(String[] args) {






























        List<Employee> employees = List.of(
                new Employee("Alice", "TI", "Desarrollador"),
                new Employee("Bob", "TI", "QA"),
                new Employee("Clara", "Marketing", "Analista"),
                new Employee("Dan", "Marketing", "Jefe de Área"),
                new Employee("Eve", "Ventas", "Ejecutivo")
        );

        employees
                .stream()
                .collect(
                        Collectors.groupingBy(
                                Employee::getDepartment,
                                Collectors.groupingBy(Employee::getPosition)))

                .entrySet()
                .forEach(System.out::println);

        }













    static class Employee {
        private final String name;
        private final String department;
        private final String position;

        public Employee(String name, String department, String position) {
            this.name = name;
            this.department = department;
            this.position = position;
        }

        public String getName() {
            return name;
        }

        public String getDepartment() {
            return department;
        }

        public String getPosition() {
            return position;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
