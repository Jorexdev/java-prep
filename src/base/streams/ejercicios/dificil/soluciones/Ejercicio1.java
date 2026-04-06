package base.streams.ejercicios.dificil.soluciones;

import java.util.Comparator;
import java.util.List;

public class Ejercicio1 {

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Ana", 30, 50000),
                new Employee("Luis", 45, 75000),
                new Employee("Clara", 28, 50000),
                new Employee("Pedro", 50, 75000),
                new Employee("Marta", 35, 60000),
                new Employee("Jorge", 40, 60000),
                new Employee("Lucía", 32, 75000),
                new Employee("Raúl", 29, 50000),
                new Employee("Sofía", 45, 60000),
                new Employee("Diego", 38, 75000),
                new Employee("Elena", 33, 50000)


        );

        employees
                .stream()
                .sorted(Comparator.comparing(Employee::getSalary).reversed().thenComparing(Employee::getAge))
                .limit(10)
                .forEach(System.out::println);


    }

    static  class Employee {
        private final String name;
        private final int age;
        private final double salary;

        public Employee(String name, int age, double salary) {
            this.name = name;
            this.age = age;
            this.salary = salary;
        }

        public String getName() {
            return name;
        }
        public int getAge() {
            return age;
        }
        public double getSalary() {
            return salary;
        }

        @Override
        public String toString() {
            return name + " (edad=" + age + ", salario=" + salary + ")";
        }
    }


}
