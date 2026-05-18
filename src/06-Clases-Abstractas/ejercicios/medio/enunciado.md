# Clases Abstractas — Ejercicios Medio

Ejercicios intermedios: jerarquías múltiples, Template Method con hooks, LSP, estado compartido.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Abstract class vs interface
Modela `Forma` como clase abstracta (con campo `color`) y como interfaz (sin estado). Implementa `Cuadrado` con cada una. Explica en comentarios cuándo usar cada opción.

## Ejercicio 2 — Jerarquía multinivel
`Vehiculo` (abstracta, abstracto `mover()`) → `VehiculoTerrestre` (abstracta, concreto `tipoTerreno()`) → `Coche` (concreta). Polimorfismo: variable tipo `Vehiculo` apunta a `Coche`.

## Ejercicio 3 — Template Method con hook opcional
`OrdenProcesador` abstracta con `procesar()` que llama: `validar()` (abstracto), `aplicarDescuento()` (hook vacío), `confirmar()` (abstracto). `OrdenNormal` no sobreescribe el hook. `OrdenVIP` lo sobreescribe para 20% descuento.

## Ejercicio 4 — Métodos estáticos en abstract class
`Conversor` abstracta con estático `celsiusAFahrenheit(double)` y abstracto `convertir(double)`. `ConversorCF` y `ConversorFC` implementan convertir.

## Ejercicio 5 — Liskov Substitution Principle
Muestra el problema LSP con `Cuadrado extends Rectangulo` (setAncho() también cambia alto). Luego presenta la solución: ambos extienden `Figura` abstracta con solo `area()`.

## Ejercicio 6 — Estado compartido
`Contador` abstracta con `int cuenta`, concreto `incrementar()`, abstracto `mostrar()`. `ContadorSimple` y `ContadorConPrefijo` comparten el estado pero muestran de forma diferente.
