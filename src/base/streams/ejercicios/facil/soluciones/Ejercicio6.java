package base.streams.ejemplosercicios.facil.soluciones;

import java.util.Comparator;
import java.util.List;

public class Ejercicio6 {

    public static void main(String[] args) {
        List<Employee> employeeList = List.of(
                new Employee("Luis", 20L),
                new Employee("Jorge", 122L),
                new Employee("Juan", 120L),
                new Employee("Ana", 2000L)
        );


        employeeList
                .stream()
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .limit(5)
                .forEach(System.out::println);
    }

    static class Employee {
        private final String name;
        private final Long salary;

        Employee(String name, Long price) {
            this.name = name;
            this.salary = price;
        }

        public Long getSalary() {
            return salary;
        }

        @Override
        public String toString() {
            return "Product{" +
                    "name='" + name + '\'' +
                    ", salary=" + salary +
                    '}';
        }
    }
}
