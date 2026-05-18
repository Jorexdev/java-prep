# Clases Abstractas — Ejercicios Fácil

Ejercicios básicos con clases abstractas: métodos abstractos y concretos, herencia, polimorfismo, Template Method.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Figura con area()
Clase abstracta `Figura` con método abstracto `double area()` y método concreto `void describir()` que imprime "Área: [area()]". Implementa `Circulo(double radio)` y `Rectangulo(double ancho, double alto)`.

## Ejercicio 2 — Animal con hablar()
Clase abstracta `Animal(String nombre)` con abstracto `hablar()` y concreto `respirar()` que imprime "[nombre] respira". Implementa `Perro` ("Guau!") y `Gato` ("Miau!"). Demuestra polimorfismo con List<Animal>.

## Ejercicio 3 — No se puede instanciar
Demuestra con código que una clase abstracta no puede instanciarse directamente. Muestra en comentario el error y la forma correcta: referencia abstracta, instancia concreta.

## Ejercicio 4 — Interfaz + clase abstracta
Interfaz `Volador` con `volar()`. Clase abstracta `Ave extends Animal` con hablar() concreto. Clase `Aguila extends Ave implements Volador`. Demuestra que Aguila ejecuta hablar(), respirar() y volar().

## Ejercicio 5 — Constructor en abstract class
Clase abstracta `Vehiculo(String marca, String modelo)` con método concreto `info()`. Subclases `Coche` y `Moto` llaman a `super(marca, modelo)`.

## Ejercicio 6 — Template Method
Clase abstracta `Informe` con método concreto `generar()` que llama en orden a los abstractos `cabecera()`, `cuerpo()`, `pie()`. Implementa `InformeVentas` e `InformeInventario`.
